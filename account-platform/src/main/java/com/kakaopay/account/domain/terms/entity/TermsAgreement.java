package com.kakaopay.account.domain.terms.entity;

import com.kakaopay.account.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_agreement", indexes = {
        @Index(name = "idx_terms_agreement_member", columnList = "member_id, terms_id"),
        @Index(name = "idx_terms_agreement_terms", columnList = "terms_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "terms_id", nullable = false)
    private Long termsId;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Column(name = "agreed_ip", length = 45)
    private String agreedIp;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private TermsAgreement(Long memberId, Long termsId, String agreedIp) {
        this.memberId = memberId;
        this.termsId = termsId;
        this.agreedAt = LocalDateTime.now();
        this.agreedIp = agreedIp;
    }

    public void withdraw() {
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.withdrawnAt == null;
    }
}
