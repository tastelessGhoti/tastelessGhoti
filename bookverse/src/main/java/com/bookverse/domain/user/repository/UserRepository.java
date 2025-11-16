package com.bookverse.domain.user.repository;

import com.bookverse.domain.user.entity.User;
import com.bookverse.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일로 사용자 조회 (상태 포함)
     */
    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
}
