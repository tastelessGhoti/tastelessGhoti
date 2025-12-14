package com.kakaopay.account.domain.member.repository;

import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Optional<Member> findByCi(String ci);

    boolean existsByCi(String ci);

    Optional<Member> findByPhoneNumberAndStatus(String phoneNumber, MemberStatus status);

    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.lastLoginAt < :threshold")
    List<Member> findDormantCandidates(
            @Param("status") MemberStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    long countByStatus(@Param("status") MemberStatus status);
}
