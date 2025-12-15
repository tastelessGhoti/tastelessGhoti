package com.paygate.payment.domain.payment.dto;

import com.paygate.payment.common.util.MaskingUtil;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentMethod;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentApprovalResponse {

    private String transactionId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String cardNumber;
    private String cardCompany;
    private Integer installmentMonths;
    private String approvalNumber;
    private LocalDateTime approvedAt;

    public static PaymentApprovalResponse from(Payment payment) {
        return PaymentApprovalResponse.builder()
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .cardNumber(MaskingUtil.maskCardNumber(payment.getCardNumber()))
                .cardCompany(payment.getCardCompany())
                .installmentMonths(payment.getInstallmentMonths())
                .approvalNumber(payment.getApprovalNumber())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
