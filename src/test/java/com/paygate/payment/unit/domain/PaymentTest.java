package com.paygate.payment.unit.domain;

import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.PaymentException;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment 엔티티 테스트")
class PaymentTest {

    @Nested
    @DisplayName("결제 승인")
    class ApproveTest {

        @Test
        @DisplayName("정상적으로 결제가 승인된다")
        void 정상_승인() {
            // given
            Payment payment = PaymentFixture.createPendingPayment();

            // when
            payment.approve("12345678", "VAN123456");

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getApprovalNumber()).isEqualTo("12345678");
            assertThat(payment.getVanTransactionId()).isEqualTo("VAN123456");
            assertThat(payment.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 승인된 결제는 재승인할 수 없다")
        void 이미_승인된_결제_재승인_실패() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment();

            // when & then
            assertThatThrownBy(() -> payment.approve("99999999", "VAN999999"))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_ALREADY_APPROVED);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelTest {

        @Test
        @DisplayName("전체 금액을 취소하면 CANCELED 상태가 된다")
        void 전체_취소() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("30000"));

            // when
            payment.cancel(new BigDecimal("30000"));

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo("30000");
            assertThat(payment.getCancelableAmount()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("부분 취소 시 PARTIAL_CANCELED 상태가 된다")
        void 부분_취소() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("50000"));

            // when
            payment.cancel(new BigDecimal("20000"));

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo("20000");
            assertThat(payment.getCancelableAmount()).isEqualByComparingTo("30000");
        }

        @Test
        @DisplayName("부분 취소 후 나머지를 취소하면 CANCELED 상태가 된다")
        void 부분_취소_후_전체_취소() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("50000"));
            payment.cancel(new BigDecimal("20000"));

            // when
            payment.cancel(new BigDecimal("30000"));

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(payment.getCanceledAmount()).isEqualByComparingTo("50000");
        }

        @Test
        @DisplayName("취소 가능 금액을 초과하면 예외가 발생한다")
        void 취소_금액_초과() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("30000"));

            // when & then
            assertThatThrownBy(() -> payment.cancel(new BigDecimal("50000")))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_EXCEEDED);
        }

        @Test
        @DisplayName("이미 전체 취소된 결제는 취소할 수 없다")
        void 전체_취소된_결제_재취소_실패() {
            // given
            Payment payment = PaymentFixture.createApprovedPayment(new BigDecimal("30000"));
            payment.cancel(new BigDecimal("30000"));

            // when & then
            assertThatThrownBy(() -> payment.cancel(new BigDecimal("10000")))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        @Test
        @DisplayName("대기 상태의 결제는 취소할 수 없다")
        void 대기_상태_취소_실패() {
            // given
            Payment payment = PaymentFixture.createPendingPayment();

            // when & then
            assertThatThrownBy(() -> payment.cancel(new BigDecimal("10000")))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class FailTest {

        @Test
        @DisplayName("결제 실패 처리가 정상적으로 된다")
        void 결제_실패_처리() {
            // given
            Payment payment = PaymentFixture.createPendingPayment();

            // when
            payment.fail("잔액 부족");

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailReason()).isEqualTo("잔액 부족");
        }
    }
}
