package com.kakaopay.account.domain.terms.repository;

import com.kakaopay.account.domain.terms.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    List<TermsAgreement> findByMemberIdAndWithdrawnAtIsNull(Long memberId);

    @Query("SELECT ta FROM TermsAgreement ta WHERE ta.memberId = :memberId AND ta.termsId = :termsId AND ta.withdrawnAt IS NULL")
    Optional<TermsAgreement> findActiveAgreement(
            @Param("memberId") Long memberId,
            @Param("termsId") Long termsId
    );

    @Query("SELECT ta.termsId FROM TermsAgreement ta WHERE ta.memberId = :memberId AND ta.withdrawnAt IS NULL")
    List<Long> findAgreedTermsIds(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndTermsIdAndWithdrawnAtIsNull(Long memberId, Long termsId);
}
