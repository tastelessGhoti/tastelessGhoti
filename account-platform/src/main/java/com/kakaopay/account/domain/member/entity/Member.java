package com.kakaopay.account.domain.member.entity;

import com.kakaopay.account.common.entity.BaseTimeEntity;
import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_ci", columnList = "ci"),
        @Index(name = "idx_member_status", columnList = "status"),
        @Index(name = "idx_member_phone", columnList = "phone_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ci", nullable = false, unique = true, length = 88)
    private String ci;

    @Column(name = "di", length = 64)
    private String di;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "birth_date", length = 8)
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Builder
    private Member(String ci, String di, String name, String phoneNumber,
                   String email, String birthDate) {
        this.ci = ci;
        this.di = di;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDate = birthDate;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void withdraw() {
        validateStatusForWithdrawal();
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public void suspend(String reason) {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    public void reactivate() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS,
                    "정지 상태인 회원만 재활성화할 수 있습니다");
        }
        this.status = MemberStatus.ACTIVE;
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    private void validateStatusForWithdrawal() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }
}
