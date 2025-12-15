package com.paygate.payment.infrastructure.van;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VanCancelRequest {

    private String merchantId;
    private String vanTransactionId;
    private String approvalNumber;
    private BigDecimal cancelAmount;
    private String cancelReason;
}
