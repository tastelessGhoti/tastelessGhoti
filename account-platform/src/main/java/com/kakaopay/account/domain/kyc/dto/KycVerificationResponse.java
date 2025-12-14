package com.kakaopay.account.domain.kyc.dto;

import com.kakaopay.account.domain.kyc.entity.KycLevel;
import com.kakaopay.account.domain.kyc.entity.KycStatus;
import com.kakaopay.account.domain.kyc.entity.KycVerification;
import com.kakaopay.account.domain.kyc.entity.KycVerificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class KycVerificationResponse {

    private Long verificationId;
    private KycVerificationType verificationType;
    private KycStatus status;
    private KycLevel level;
    private String verifiedName;
    private LocalDateTime verifiedAt;
    private LocalDate expiresAt;
    private String rejectionReason;
    private boolean canRetry;

    public static KycVerificationResponse from(KycVerification verification) {
        return KycVerificationResponse.builder()
                .verificationId(verification.getId())
                .verificationType(verification.getVerificationType())
                .status(verification.getStatus())
                .level(verification.getLevel())
                .verifiedName(maskName(verification.getVerifiedName()))
                .verifiedAt(verification.getVerifiedAt())
                .expiresAt(verification.getExpiresAt())
                .rejectionReason(verification.getRejectionReason())
                .canRetry(verification.canRetry())
                .build();
    }

    private static String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
