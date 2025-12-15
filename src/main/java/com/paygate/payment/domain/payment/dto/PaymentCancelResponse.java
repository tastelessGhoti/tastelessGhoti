package com.paygate.payment.domain.payment.dto;

import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentCancelHistory;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentCancelResponse {

    private String transactionId;
    private String cancelTransactionId;
    private BigDecimal canceledAmount;
    private BigDecimal totalCanceledAmount;
    private BigDecimal remainingAmount;
    private PaymentStatus status;
    private LocalDateTime canceledAt;

    public static PaymentCancelResponse of(Payment payment, PaymentCancelHistory cancelHistory) {
        return PaymentCancelResponse.builder()
                .transactionId(payment.getTransactionId())
                .cancelTransactionId(cancelHistory.getCancelTransactionId())
                .canceledAmount(cancelHistory.getCancelAmount())
                .totalCanceledAmount(payment.getCanceledAmount())
                .remainingAmount(payment.getCancelableAmount())
                .status(payment.getStatus())
                .canceledAt(cancelHistory.getCreatedAt())
                .build();
    }
}
