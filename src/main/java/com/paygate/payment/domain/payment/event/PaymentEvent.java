package com.paygate.payment.domain.payment.event;

import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PaymentEvent {

    private String eventType;
    private String transactionId;
    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime occurredAt;

    @Builder
    public PaymentEvent(String eventType, String transactionId, String merchantId,
                        String orderId, BigDecimal amount, PaymentStatus status) {
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.occurredAt = LocalDateTime.now();
    }

    public static PaymentEvent approved(Payment payment) {
        return PaymentEvent.builder()
                .eventType("PAYMENT_APPROVED")
                .transactionId(payment.getTransactionId())
                .merchantId(payment.getMerchantId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }

    public static PaymentEvent canceled(Payment payment, BigDecimal cancelAmount) {
        return PaymentEvent.builder()
                .eventType("PAYMENT_CANCELED")
                .transactionId(payment.getTransactionId())
                .merchantId(payment.getMerchantId())
                .orderId(payment.getOrderId())
                .amount(cancelAmount)
                .status(payment.getStatus())
                .build();
    }
}
