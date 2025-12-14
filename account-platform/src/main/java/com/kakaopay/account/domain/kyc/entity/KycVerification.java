package com.kakaopay.account.domain.kyc.entity;

import com.kakaopay.account.common.entity.BaseTimeEntity;
import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * KYC(Know Your Customer) 인증 정보
 * 자금세탁방지법에 따른 고객 확인 절차
 */
@Entity
@Table(name = "kyc_verification", indexes = {
        @Index(name = "idx_kyc_member", columnList = "member_id"),
        @Index(name = "idx_kyc_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KycVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false, length = 30)
    private KycVerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private KycStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private KycLevel level;

    @Column(name = "id_card_type", length = 30)
    private String idCardType;

    @Column(name = "id_card_number_hash", length = 64)
    private String idCardNumberHash;

    @Column(name = "verified_name", length = 50)
    private String verifiedName;

    @Column(name = "verified_birth_date", length = 8)
    private String verifiedBirthDate;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "retry_count")
    private int retryCount;

    @Builder
    private KycVerification(Long memberId, KycVerificationType verificationType) {
        this.memberId = memberId;
        this.verificationType = verificationType;
        this.status = KycStatus.PENDING;
        this.level = KycLevel.NONE;
        this.retryCount = 0;
    }

    public void startVerification(String idCardType, String idCardNumberHash,
                                   String verifiedName, String verifiedBirthDate) {
        this.idCardType = idCardType;
        this.idCardNumberHash = idCardNumberHash;
        this.verifiedName = verifiedName;
        this.verifiedBirthDate = verifiedBirthDate;
        this.status = KycStatus.IN_PROGRESS;
    }

    public void approve(KycLevel level, LocalDate expiresAt) {
        if (this.status == KycStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.KYC_ALREADY_VERIFIED);
        }
        this.status = KycStatus.VERIFIED;
        this.level = level;
        this.verifiedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public void reject(String reason) {
        this.status = KycStatus.REJECTED;
        this.rejectionReason = reason;
        this.retryCount++;
    }

    public void expire() {
        this.status = KycStatus.EXPIRED;
    }

    public boolean isVerified() {
        return this.status == KycStatus.VERIFIED && !isExpired();
    }

    public boolean isExpired() {
        return this.expiresAt != null && LocalDate.now().isAfter(this.expiresAt);
    }

    public boolean canRetry() {
        return this.retryCount < 3;
    }
}
