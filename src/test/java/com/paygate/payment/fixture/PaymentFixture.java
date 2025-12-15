package com.paygate.payment.fixture;

import com.paygate.payment.domain.merchant.entity.Merchant;
import com.paygate.payment.domain.payment.dto.PaymentApprovalRequest;
import com.paygate.payment.domain.payment.dto.PaymentCancelRequest;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentMethod;
import com.paygate.payment.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 테스트 데이터 생성 유틸.
 */
public class PaymentFixture {

    public static final String TEST_MERCHANT_ID = "M20231201001";
    public static final String TEST_API_KEY = "test-api-key-12345678";

    public static Payment createPendingPayment() {
        return Payment.builder()
                .transactionId("TXN" + System.currentTimeMillis())
                .merchantId(TEST_MERCHANT_ID)
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("9410123456789012")
                .installmentMonths(0)
                .productName("테스트 상품")
                .buyerName("홍길동")
                .buyerEmail("test@example.com")
                .buyerPhone("01012345678")
                .build();
    }

    public static Payment createApprovedPayment() {
        Payment payment = createPendingPayment();
        payment.approve("12345678", "VAN123456");
        return payment;
    }

    public static Payment createApprovedPayment(BigDecimal amount) {
        Payment payment = Payment.builder()
                .transactionId("TXN" + System.currentTimeMillis())
                .merchantId(TEST_MERCHANT_ID)
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(amount)
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("9410123456789012")
                .installmentMonths(0)
                .build();
        payment.approve("12345678", "VAN123456");
        return payment;
    }

    public static PaymentApprovalRequest createApprovalRequest() {
        return PaymentApprovalRequest.builder()
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(new BigDecimal("35000"))
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("9410123456789012")
                .expiryDate("1226")
                .installmentMonths(0)
                .productName("스프링 부트 실전 가이드")
                .buyerName("김개발")
                .buyerEmail("dev@company.com")
                .buyerPhone("01098765432")
                .build();
    }

    public static PaymentApprovalRequest createApprovalRequest(BigDecimal amount) {
        return PaymentApprovalRequest.builder()
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(amount)
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("9410123456789012")
                .expiryDate("1226")
                .installmentMonths(0)
                .build();
    }

    public static PaymentCancelRequest createCancelRequest(String transactionId) {
        return PaymentCancelRequest.builder()
                .transactionId(transactionId)
                .cancelReason("고객 변심")
                .build();
    }

    public static PaymentCancelRequest createPartialCancelRequest(String transactionId, BigDecimal amount) {
        return PaymentCancelRequest.builder()
                .transactionId(transactionId)
                .cancelAmount(amount)
                .cancelReason("부분 환불 요청")
                .build();
    }

    public static Merchant createActiveMerchant() {
        return Merchant.builder()
                .merchantId(TEST_MERCHANT_ID)
                .merchantName("테스트 쇼핑몰")
                .apiKey(TEST_API_KEY)
                .secretKey("secret-key-12345678")
                .businessNumber("1234567890")
                .representativeName("대표자")
                .email("merchant@test.com")
                .phone("0212345678")
                .feeRate(new BigDecimal("0.025"))
                .settlementCycleDays(3)
                .build();
    }
}
