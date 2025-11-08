package com.portfolio.ecommerce.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 상태
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지");

    private final String description;
}
