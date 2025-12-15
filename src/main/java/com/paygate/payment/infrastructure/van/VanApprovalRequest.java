package com.paygate.payment.infrastructure.van;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VanApprovalRequest {

    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private String cardNumber;
    private String expiryDate;
    private String birthDate;
    private String cardPassword;
    private Integer installmentMonths;
    private String productName;
}
