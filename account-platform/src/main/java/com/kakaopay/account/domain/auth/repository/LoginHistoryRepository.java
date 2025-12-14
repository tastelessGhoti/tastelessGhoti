package com.kakaopay.account.domain.auth.repository;

import com.kakaopay.account.domain.auth.entity.LoginHistory;
import com.kakaopay.account.domain.auth.entity.LoginResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Query("SELECT COUNT(h) FROM LoginHistory h " +
           "WHERE h.memberId = :memberId " +
           "AND h.loginResult = :result " +
           "AND h.createdAt > :since")
    int countRecentLoginsByResult(
            @Param("memberId") Long memberId,
            @Param("result") LoginResult result,
            @Param("since") LocalDateTime since
    );
}
