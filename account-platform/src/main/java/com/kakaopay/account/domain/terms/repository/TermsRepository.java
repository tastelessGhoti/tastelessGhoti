package com.kakaopay.account.domain.terms.repository;

import com.kakaopay.account.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    @Query("SELECT t FROM Terms t WHERE t.active = true AND t.effectiveDate <= :date ORDER BY t.displayOrder")
    List<Terms> findAllActiveTerms(@Param("date") LocalDate date);

    @Query("SELECT t FROM Terms t WHERE t.active = true AND t.required = true AND t.effectiveDate <= :date")
    List<Terms> findAllRequiredTerms(@Param("date") LocalDate date);

    Optional<Terms> findByTermsCodeAndVersion(String termsCode, Integer version);

    @Query("SELECT t FROM Terms t WHERE t.termsCode = :code AND t.active = true ORDER BY t.version DESC LIMIT 1")
    Optional<Terms> findLatestByTermsCode(@Param("code") String termsCode);

    boolean existsByTermsCodeAndVersion(String termsCode, Integer version);
}
