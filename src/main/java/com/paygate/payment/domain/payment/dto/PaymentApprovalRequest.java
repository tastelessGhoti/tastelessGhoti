package com.paygate.payment.domain.payment.dto;

import com.paygate.payment.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentApprovalRequest {

    @NotBlank(message = "주문번호는 필수입니다")
    @Size(max = 64, message = "주문번호는 64자를 초과할 수 없습니다")
    private String orderId;

    @NotNull(message = "결제금액은 필수입니다")
    @DecimalMin(value = "100", message = "최소 결제금액은 100원입니다")
    @DecimalMax(value = "100000000", message = "최대 결제금액은 1억원입니다")
    private BigDecimal amount;

    @NotNull(message = "결제수단은 필수입니다")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "카드번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{14,16}$", message = "유효하지 않은 카드번호 형식입니다")
    private String cardNumber;

    @NotBlank(message = "유효기간은 필수입니다")
    @Pattern(regexp = "^(0[1-9]|1[0-2])[0-9]{2}$", message = "유효기간은 MMYY 형식입니다")
    private String expiryDate;

    @Pattern(regexp = "^[0-9]{2}$", message = "생년월일 앞 2자리를 입력해주세요")
    private String birthDate;

    @Pattern(regexp = "^[0-9]{2}$", message = "비밀번호 앞 2자리를 입력해주세요")
    private String cardPassword;

    @Min(value = 0, message = "할부개월은 0 이상이어야 합니다")
    @Max(value = 12, message = "할부개월은 12개월을 초과할 수 없습니다")
    private Integer installmentMonths;

    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다")
    private String productName;

    @Size(max = 50, message = "구매자명은 50자를 초과할 수 없습니다")
    private String buyerName;

    @Email(message = "유효하지 않은 이메일 형식입니다")
    private String buyerEmail;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "유효하지 않은 전화번호 형식입니다")
    private String buyerPhone;

    private String idempotencyKey;
}
