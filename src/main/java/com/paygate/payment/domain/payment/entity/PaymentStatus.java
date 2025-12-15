package com.paygate.payment.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("대기"),
    APPROVED("승인"),
    FAILED("실패"),
    PARTIAL_CANCELED("부분취소"),
    CANCELED("취소");

    private final String description;

    public boolean isCancelable() {
        return this == APPROVED || this == PARTIAL_CANCELED;
    }
}
