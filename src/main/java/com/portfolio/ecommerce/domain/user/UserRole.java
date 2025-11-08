package com.portfolio.ecommerce.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String description;
}
