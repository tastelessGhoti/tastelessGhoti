package com.paygate.payment.domain.payment.service;

import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.PaymentException;
import com.paygate.payment.common.exception.VanException;
import com.paygate.payment.common.util.IdGenerator;
import com.paygate.payment.domain.merchant.entity.Merchant;
import com.paygate.payment.domain.merchant.repository.MerchantRepository;
import com.paygate.payment.domain.payment.dto.*;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentCancelHistory;
import com.paygate.payment.domain.payment.event.PaymentEvent;
import com.paygate.payment.domain.payment.repository.PaymentCancelHistoryRepository;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import com.paygate.payment.infrastructure.kafka.PaymentEventPublisher;
import com.paygate.payment.infrastructure.redis.DistributedLockExecutor;
import com.paygate.payment.infrastructure.redis.IdempotencyKeyStore;
import com.paygate.payment.infrastructure.van.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 결제 승인/취소 핵심 비즈니스 로직.
 * 멱등성 보장, 분산 락, VAN 연동, 이벤트 발행을 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCancelHistoryRepository cancelHistoryRepository;
    private final MerchantRepository merchantRepository;
    private final VanClientFactory vanClientFactory;
    private final DistributedLockExecutor lockExecutor;
    private final IdempotencyKeyStore idempotencyKeyStore;
    private final PaymentEventPublisher eventPublisher;

    /**
     * 결제 승인 처리.
     * 멱등성 키를 통한 중복 요청 방지 및 분산 락으로 동시성 제어.
     */
    @Transactional
    public PaymentApprovalResponse approve(String merchantId, PaymentApprovalRequest request) {
        log.info("결제 승인 요청 - merchantId: {}, orderId: {}", merchantId, request.getOrderId());

        Merchant merchant = validateMerchant(merchantId);

        // 멱등성 체크
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            return handleIdempotency(request.getIdempotencyKey(), merchantId, request);
        }

        return processApproval(merchant, request);
    }

    private PaymentApprovalResponse handleIdempotency(String idempotencyKey,
                                                       String merchantId,
                                                       PaymentApprovalRequest request) {
        var existingTxId = idempotencyKeyStore.get(idempotencyKey);
        if (existingTxId.isPresent()) {
            Payment existingPayment = paymentRepository.findByTransactionId(existingTxId.get())
                    .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));
            log.info("멱등성 키로 기존 결제 반환 - txId: {}", existingTxId.get());
            return PaymentApprovalResponse.from(existingPayment);
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PaymentException(ErrorCode.MERCHANT_NOT_FOUND));

        PaymentApprovalResponse response = processApproval(merchant, request);

        idempotencyKeyStore.saveIfAbsent(idempotencyKey, response.getTransactionId());
        return response;
    }

    private PaymentApprovalResponse processApproval(Merchant merchant, PaymentApprovalRequest request) {
        // 중복 주문 체크
        if (paymentRepository.existsByMerchantIdAndOrderId(merchant.getMerchantId(), request.getOrderId())) {
            throw new PaymentException(ErrorCode.DUPLICATE_ORDER_ID);
        }

        String transactionId = IdGenerator.generateTransactionId();

        // 분산 락 내에서 결제 처리
        return lockExecutor.executeWithLock("payment:" + transactionId, () -> {
            Payment payment = createPayment(transactionId, merchant.getMerchantId(), request);
            payment = paymentRepository.save(payment);

            try {
                VanApprovalResponse vanResponse = requestVanApproval(request);

                if (!vanResponse.isSuccess()) {
                    payment.fail(vanResponse.getResponseMessage());
                    throw new VanException(ErrorCode.PAYMENT_APPROVAL_FAILED,
                            vanResponse.getResponseCode(), vanResponse.getResponseMessage());
                }

                payment.approve(vanResponse.getApprovalNumber(), vanResponse.getVanTransactionId());

                // 이벤트 발행
                eventPublisher.publishApproved(PaymentEvent.approved(payment));

                log.info("결제 승인 완료 - txId: {}, approvalNo: {}",
                        transactionId, vanResponse.getApprovalNumber());

                return PaymentApprovalResponse.from(payment);

            } catch (VanException e) {
                throw e;
            } catch (Exception e) {
                payment.fail(e.getMessage());
                log.error("결제 승인 중 오류 발생 - txId: {}", transactionId, e);
                throw new PaymentException(ErrorCode.PAYMENT_APPROVAL_FAILED, e);
            }
        });
    }

    private Payment createPayment(String transactionId, String merchantId, PaymentApprovalRequest request) {
        return Payment.builder()
                .transactionId(transactionId)
                .merchantId(merchantId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .cardNumber(request.getCardNumber())
                .installmentMonths(request.getInstallmentMonths() != null ? request.getInstallmentMonths() : 0)
                .productName(request.getProductName())
                .buyerName(request.getBuyerName())
                .buyerEmail(request.getBuyerEmail())
                .buyerPhone(request.getBuyerPhone())
                .build();
    }

    private VanApprovalResponse requestVanApproval(PaymentApprovalRequest request) {
        VanClient vanClient = vanClientFactory.getDefaultClient();

        VanApprovalRequest vanRequest = VanApprovalRequest.builder()
                .cardNumber(request.getCardNumber())
                .expiryDate(request.getExpiryDate())
                .amount(request.getAmount())
                .installmentMonths(request.getInstallmentMonths())
                .productName(request.getProductName())
                .build();

        return vanClient.approve(vanRequest);
    }

    /**
     * 결제 취소 처리.
     * 부분 취소 지원, 비관적 락으로 동시 취소 요청 방지.
     */
    @Transactional
    public PaymentCancelResponse cancel(String merchantId, PaymentCancelRequest request) {
        log.info("결제 취소 요청 - txId: {}, amount: {}",
                request.getTransactionId(), request.getCancelAmount());

        validateMerchant(merchantId);

        // 비관적 락으로 결제 정보 조회
        Payment payment = paymentRepository.findByTransactionIdWithLock(request.getTransactionId())
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        // 가맹점 권한 체크
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        var cancelAmount = request.isFullCancel()
                ? payment.getCancelableAmount()
                : request.getCancelAmount();

        String cancelTxId = IdGenerator.generateTransactionId();

        PaymentCancelHistory cancelHistory = PaymentCancelHistory.builder()
                .paymentId(payment.getId())
                .cancelTransactionId(cancelTxId)
                .cancelAmount(cancelAmount)
                .cancelReason(request.getCancelReason())
                .build();

        cancelHistory = cancelHistoryRepository.save(cancelHistory);

        try {
            VanCancelResponse vanResponse = requestVanCancel(payment, cancelAmount, request.getCancelReason());

            if (!vanResponse.isSuccess()) {
                cancelHistory.fail();
                throw new VanException(ErrorCode.PAYMENT_CANCEL_FAILED,
                        vanResponse.getResponseCode(), vanResponse.getResponseMessage());
            }

            // 취소 처리
            payment.cancel(cancelAmount);
            cancelHistory.complete(vanResponse.getVanCancelId());

            eventPublisher.publishCanceled(PaymentEvent.canceled(payment, cancelAmount));

            log.info("결제 취소 완료 - txId: {}, cancelTxId: {}, amount: {}",
                    request.getTransactionId(), cancelTxId, cancelAmount);

            return PaymentCancelResponse.of(payment, cancelHistory);

        } catch (VanException e) {
            throw e;
        } catch (Exception e) {
            cancelHistory.fail();
            log.error("결제 취소 중 오류 - txId: {}", request.getTransactionId(), e);
            throw new PaymentException(ErrorCode.PAYMENT_CANCEL_FAILED, e);
        }
    }

    private VanCancelResponse requestVanCancel(Payment payment, java.math.BigDecimal cancelAmount, String reason) {
        VanClient vanClient = vanClientFactory.getDefaultClient();

        VanCancelRequest vanRequest = VanCancelRequest.builder()
                .vanTransactionId(payment.getVanTransactionId())
                .approvalNumber(payment.getApprovalNumber())
                .cancelAmount(cancelAmount)
                .cancelReason(reason)
                .build();

        return vanClient.cancel(vanRequest);
    }

    /**
     * 결제 상세 조회.
     */
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(String merchantId, String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        return PaymentDetailResponse.from(payment);
    }

    /**
     * 결제 목록 조회.
     */
    @Transactional(readOnly = true)
    public Page<PaymentDetailResponse> searchPayments(String merchantId,
                                                       PaymentSearchCondition condition,
                                                       Pageable pageable) {
        condition.setMerchantId(merchantId);
        return paymentRepository.searchPayments(condition, pageable)
                .map(PaymentDetailResponse::from);
    }

    private Merchant validateMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PaymentException(ErrorCode.MERCHANT_NOT_FOUND));

        if (!merchant.isActive()) {
            throw new PaymentException(ErrorCode.MERCHANT_NOT_ACTIVE);
        }

        return merchant;
    }
}
