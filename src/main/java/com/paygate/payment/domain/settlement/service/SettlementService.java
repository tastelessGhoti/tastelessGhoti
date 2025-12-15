package com.paygate.payment.domain.settlement.service;

import com.paygate.payment.domain.merchant.entity.Merchant;
import com.paygate.payment.domain.merchant.repository.MerchantRepository;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import com.paygate.payment.domain.settlement.entity.Settlement;
import com.paygate.payment.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 정산 서비스.
 * 가맹점별 일자별 결제 데이터를 집계하여 정산 데이터 생성.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;

    /**
     * 특정 일자의 정산 데이터 생성.
     * 전일 결제 데이터를 집계.
     */
    @Transactional
    public void createDailySettlement(LocalDate settlementDate) {
        log.info("일별 정산 처리 시작 - date: {}", settlementDate);

        List<Merchant> merchants = merchantRepository.findAll();

        for (Merchant merchant : merchants) {
            if (!merchant.isActive()) continue;

            try {
                createSettlementForMerchant(merchant, settlementDate);
            } catch (Exception e) {
                log.error("정산 처리 실패 - merchantId: {}, date: {}, error: {}",
                        merchant.getMerchantId(), settlementDate, e.getMessage());
            }
        }

        log.info("일별 정산 처리 완료 - date: {}", settlementDate);
    }

    private void createSettlementForMerchant(Merchant merchant, LocalDate settlementDate) {
        if (settlementRepository.existsByMerchantIdAndSettlementDate(
                merchant.getMerchantId(), settlementDate)) {
            log.info("이미 정산 완료 - merchantId: {}, date: {}",
                    merchant.getMerchantId(), settlementDate);
            return;
        }

        LocalDateTime startOfDay = settlementDate.atStartOfDay();
        LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);

        // 승인 건 집계
        List<Payment> approvedPayments = paymentRepository.findForSettlement(
                merchant.getMerchantId(),
                PaymentStatus.APPROVED,
                startOfDay,
                endOfDay
        );

        // 부분취소 건도 포함
        List<Payment> partialCanceledPayments = paymentRepository.findForSettlement(
                merchant.getMerchantId(),
                PaymentStatus.PARTIAL_CANCELED,
                startOfDay,
                endOfDay
        );

        BigDecimal totalAmount = calculateTotalAmount(approvedPayments, partialCanceledPayments);
        BigDecimal cancelAmount = calculateCancelAmount(partialCanceledPayments);
        int totalCount = approvedPayments.size() + partialCanceledPayments.size();

        if (totalCount == 0) {
            log.debug("정산 대상 없음 - merchantId: {}, date: {}",
                    merchant.getMerchantId(), settlementDate);
            return;
        }

        BigDecimal feeRate = merchant.getFeeRate() != null
                ? merchant.getFeeRate()
                : new BigDecimal("0.025"); // 기본 수수료 2.5%

        BigDecimal totalFee = totalAmount.multiply(feeRate)
                .setScale(0, RoundingMode.DOWN);

        Settlement settlement = Settlement.builder()
                .merchantId(merchant.getMerchantId())
                .settlementDate(settlementDate)
                .totalAmount(totalAmount)
                .totalFee(totalFee)
                .transactionCount(totalCount)
                .cancelAmount(cancelAmount)
                .cancelCount(partialCanceledPayments.size())
                .build();

        settlementRepository.save(settlement);

        log.info("정산 생성 완료 - merchantId: {}, date: {}, amount: {}, fee: {}",
                merchant.getMerchantId(), settlementDate, totalAmount, totalFee);
    }

    private BigDecimal calculateTotalAmount(List<Payment> approved, List<Payment> partialCanceled) {
        BigDecimal approvedSum = approved.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal partialSum = partialCanceled.stream()
                .map(p -> p.getAmount().subtract(p.getCanceledAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return approvedSum.add(partialSum);
    }

    private BigDecimal calculateCancelAmount(List<Payment> partialCanceled) {
        return partialCanceled.stream()
                .map(Payment::getCanceledAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 정산 확정 처리.
     * 대사 완료 후 정산 확정.
     */
    @Transactional
    public void confirmSettlement(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산 정보 없음: " + settlementId));

        settlement.confirm();
        log.info("정산 확정 - settlementId: {}", settlementId);
    }
}
