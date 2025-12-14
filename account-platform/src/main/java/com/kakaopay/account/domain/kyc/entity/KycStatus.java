package com.kakaopay.account.domain.kyc.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KycStatus {

    PENDING("대기중"),
    IN_PROGRESS("진행중"),
    VERIFIED("인증완료"),
    REJECTED("거절"),
    EXPIRED("만료");

    private final String description;
}
