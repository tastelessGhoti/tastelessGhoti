package com.kakaopay.account.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    ACTIVE("활성"),
    DORMANT("휴면"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴");

    private final String description;

    public boolean canLogin() {
        return this == ACTIVE || this == DORMANT;
    }
}
