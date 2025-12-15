package com.paygate.payment.domain.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentCancelRequest {

    @NotBlank(message = "트랜잭션 ID는 필수입니다")
    private String transactionId;

    @DecimalMin(value = "100", message = "취소금액은 100원 이상이어야 합니다")
    private BigDecimal cancelAmount;

    @Size(max = 200, message = "취소사유는 200자를 초과할 수 없습니다")
    private String cancelReason;

    private String idempotencyKey;

    /**
     * 전체 취소 여부.
     * cancelAmount가 null이면 전체 취소로 간주.
     */
    public boolean isFullCancel() {
        return this.cancelAmount == null;
    }
}
