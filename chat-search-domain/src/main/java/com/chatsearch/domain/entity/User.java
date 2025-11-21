package com.chatsearch.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 * 채팅 서비스의 기본 사용자 정보 관리
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    @Builder
    public User(String email, String username, String password, String displayName, String profileImageUrl) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = java.time.LocalDateTime.now();
    }

    /**
     * 사용자 상태
     */
    public enum UserStatus {
        ACTIVE,     // 활성
        INACTIVE,   // 비활성
        SUSPENDED,  // 정지
        DELETED     // 삭제
    }
}
