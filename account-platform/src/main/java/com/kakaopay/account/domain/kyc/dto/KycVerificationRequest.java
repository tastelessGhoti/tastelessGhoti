package com.kakaopay.account.domain.kyc.dto;

import com.kakaopay.account.domain.kyc.entity.KycVerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycVerificationRequest {

    @NotNull(message = "인증 유형은 필수입니다")
    private KycVerificationType verificationType;

    @NotBlank(message = "신분증 종류는 필수입니다")
    private String idCardType;

    @NotBlank(message = "신분증 번호는 필수입니다")
    private String idCardNumber;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "생년월일은 필수입니다")
    @Pattern(regexp = "^[0-9]{8}$", message = "생년월일은 YYYYMMDD 형식이어야 합니다")
    private String birthDate;
}
