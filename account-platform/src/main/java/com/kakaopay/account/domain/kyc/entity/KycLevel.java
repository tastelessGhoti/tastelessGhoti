package com.kakaopay.account.domain.kyc.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * KYC 인증 레벨
 * - 레벨에 따라 거래 한도 및 서비스 이용 범위가 달라짐
 */
@Getter
@RequiredArgsConstructor
public enum KycLevel {

    NONE("미인증", 0L, 0L),
    LEVEL_1("1단계", 500_000L, 3_000_000L),
    LEVEL_2("2단계", 2_000_000L, 10_000_000L),
    LEVEL_3("3단계", 5_000_000L, 50_000_000L);

    private final String description;
    private final long dailyLimit;
    private final long monthlyLimit;

    public boolean canTransact(long amount, long todayTotal, long monthTotal) {
        return (todayTotal + amount) <= dailyLimit && (monthTotal + amount) <= monthlyLimit;
    }
}
