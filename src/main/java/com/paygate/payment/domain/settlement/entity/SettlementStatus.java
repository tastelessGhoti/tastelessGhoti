package com.paygate.payment.domain.settlement.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {

    PENDING("집계대기"),
    CONFIRMED("확정"),
    COMPLETED("정산완료");

    private final String description;
}
