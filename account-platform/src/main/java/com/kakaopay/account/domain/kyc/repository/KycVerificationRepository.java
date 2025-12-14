package com.kakaopay.account.domain.kyc.repository;

import com.kakaopay.account.domain.kyc.entity.KycStatus;
import com.kakaopay.account.domain.kyc.entity.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {

    Optional<KycVerification> findByMemberIdAndStatus(Long memberId, KycStatus status);

    List<KycVerification> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT k FROM KycVerification k WHERE k.memberId = :memberId AND k.status = 'VERIFIED' ORDER BY k.verifiedAt DESC LIMIT 1")
    Optional<KycVerification> findLatestVerified(@Param("memberId") Long memberId);

    @Query("SELECT k FROM KycVerification k WHERE k.status = 'VERIFIED' AND k.expiresAt <= :date")
    List<KycVerification> findExpiringVerifications(@Param("date") LocalDate date);

    boolean existsByMemberIdAndStatus(Long memberId, KycStatus status);
}
