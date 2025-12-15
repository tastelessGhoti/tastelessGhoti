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
public class PaymentDetailResponse {

    private String transactionId;
    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private BigDecimal canceledAmount;
    private BigDecimal cancelableAmount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String cardNumber;
    private String cardCompany;
    private Integer installmentMonths;
    private String approvalNumber;
    private LocalDateTime approvedAt;
    private String productName;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private LocalDateTime createdAt;

    public static PaymentDetailResponse from(Payment payment) {
        return PaymentDetailResponse.builder()
                .transactionId(payment.getTransactionId())
                .merchantId(payment.getMerchantId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .canceledAmount(payment.getCanceledAmount())
                .cancelableAmount(payment.getCancelableAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .cardNumber(MaskingUtil.maskCardNumber(payment.getCardNumber()))
                .cardCompany(payment.getCardCompany())
                .installmentMonths(payment.getInstallmentMonths())
                .approvalNumber(payment.getApprovalNumber())
                .approvedAt(payment.getApprovedAt())
                .productName(payment.getProductName())
                .buyerName(payment.getBuyerName())
                .buyerEmail(MaskingUtil.maskEmail(payment.getBuyerEmail()))
                .buyerPhone(MaskingUtil.maskPhoneNumber(payment.getBuyerPhone()))
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
