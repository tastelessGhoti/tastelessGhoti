package com.paygate.payment.unit.service;

import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.PaymentException;
import com.paygate.payment.domain.merchant.entity.Merchant;
import com.paygate.payment.domain.merchant.repository.MerchantRepository;
import com.paygate.payment.domain.payment.dto.PaymentApprovalRequest;
import com.paygate.payment.domain.payment.dto.PaymentApprovalResponse;
import com.paygate.payment.domain.payment.dto.PaymentCancelRequest;
import com.paygate.payment.domain.payment.dto.PaymentCancelResponse;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentCancelHistoryRepository;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import com.paygate.payment.domain.payment.service.PaymentService;
import com.paygate.payment.fixture.PaymentFixture;
import com.paygate.payment.infrastructure.kafka.PaymentEventPublisher;
import com.paygate.payment.infrastructure.redis.DistributedLockExecutor;
import com.paygate.payment.infrastructure.redis.IdempotencyKeyStore;
import com.paygate.payment.infrastructure.van.VanApprovalResponse;
import com.paygate.payment.infrastructure.van.VanCancelResponse;
import com.paygate.payment.infrastructure.van.VanClient;
import com.paygate.payment.infrastructure.van.VanClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCancelHistoryRepository cancelHistoryRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private VanClientFactory vanClientFactory;

    @Mock
    private VanClient vanClient;

    @Mock
    private DistributedLockExecutor lockExecutor;

    @Mock
    private IdempotencyKeyStore idempotencyKeyStore;

    @Mock
    private PaymentEventPublisher eventPublisher;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = PaymentFixture.createActiveMerchant();
    }

    @Nested
    @DisplayName("결제 승인")
    class ApproveTest {

        @Test
        @DisplayName("정상적인 결제 승인이 성공한다")
        void 정상_결제_승인_성공() {
            // given
            PaymentApprovalRequest request = PaymentFixture.createApprovalRequest();

            given(merchantRepository.findById(PaymentFixture.TEST_MERCHANT_ID))
                    .willReturn(Optional.of(merchant));
            given(paymentRepository.existsByMerchantIdAndOrderId(any(), any()))
                    .willReturn(false);
            given(vanClientFactory.getDefaultClient()).willReturn(vanClient);
            given(vanClient.approve(any())).willReturn(
                    VanApprovalResponse.success("VAN123", "87654321", "삼성카드"));
            given(paymentRepository.save(any(Payment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // 분산 락 모킹 - 실제 로직 실행
            given(lockExecutor.executeWithLock(any(String.class), any(Supplier.class)))
                    .willAnswer(invocation -> {
                        Supplier<?> supplier = invocation.getArgument(1);
                        return supplier.get();
                    });

            // when
            PaymentApprovalResponse response = paymentService.approve(
                    PaymentFixture.TEST_MERCHANT_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(response.getApprovalNumber()).isEqualTo("87654321");
            verify(eventPublisher).publishApproved(any());
        }

        @Test
        @DisplayName("존재하지 않는 가맹점으로 결제하면 실패한다")
        void 존재하지_않는_가맹점_실패() {
            // given
            PaymentApprovalRequest request = PaymentFixture.createApprovalRequest();
            given(merchantRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    paymentService.approve("INVALID_MERCHANT", request))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MERCHANT_NOT_FOUND);
        }

        @Test
        @DisplayName("중복 주문번호로 결제하면 실패한다")
        void 중복_주문번호_실패() {
            // given
            PaymentApprovalRequest request = PaymentFixture.createApprovalRequest();

            given(merchantRepository.findById(any())).willReturn(Optional.of(merchant));
            given(paymentRepository.existsByMerchantIdAndOrderId(any(), any()))
                    .willReturn(true);
            given(lockExecutor.executeWithLock(any(String.class), any(Supplier.class)))
                    .willAnswer(invocation -> {
                        Supplier<?> supplier = invocation.getArgument(1);
                        return supplier.get();
                    });

            // when & then
            assertThatThrownBy(() ->
                    paymentService.approve(PaymentFixture.TEST_MERCHANT_ID, request))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ORDER_ID);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelTest {

        @Test
        @DisplayName("전체 취소가 정상적으로 처리된다")
        void 전체_취소_성공() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("50000"));
            PaymentCancelRequest request = PaymentFixture.createCancelRequest(payment.getTransactionId());

            given(merchantRepository.findById(any())).willReturn(Optional.of(merchant));
            given(paymentRepository.findByTransactionIdWithLock(any()))
                    .willReturn(Optional.of(payment));
            given(vanClientFactory.getDefaultClient()).willReturn(vanClient);
            given(vanClient.cancel(any())).willReturn(VanCancelResponse.success("CANCEL123"));
            given(cancelHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PaymentCancelResponse response = paymentService.cancel(
                    PaymentFixture.TEST_MERCHANT_ID, request);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(response.getCanceledAmount()).isEqualByComparingTo("50000");
            assertThat(response.getRemainingAmount()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("부분 취소가 정상적으로 처리된다")
        void 부분_취소_성공() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("50000"));
            PaymentCancelRequest request = PaymentFixture.createPartialCancelRequest(
                    payment.getTransactionId(), new BigDecimal("20000"));

            given(merchantRepository.findById(any())).willReturn(Optional.of(merchant));
            given(paymentRepository.findByTransactionIdWithLock(any()))
                    .willReturn(Optional.of(payment));
            given(vanClientFactory.getDefaultClient()).willReturn(vanClient);
            given(vanClient.cancel(any())).willReturn(VanCancelResponse.success("CANCEL456"));
            given(cancelHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PaymentCancelResponse response = paymentService.cancel(
                    PaymentFixture.TEST_MERCHANT_ID, request);

            // then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELED);
            assertThat(response.getCanceledAmount()).isEqualByComparingTo("20000");
            assertThat(response.getRemainingAmount()).isEqualByComparingTo("30000");
        }

        @Test
        @DisplayName("존재하지 않는 결제를 취소하면 실패한다")
        void 존재하지_않는_결제_취소_실패() {
            // given
            PaymentCancelRequest request = PaymentFixture.createCancelRequest("INVALID_TX");

            given(merchantRepository.findById(any())).willReturn(Optional.of(merchant));
            given(paymentRepository.findByTransactionIdWithLock(any()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    paymentService.cancel(PaymentFixture.TEST_MERCHANT_ID, request))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
        }
    }
}
