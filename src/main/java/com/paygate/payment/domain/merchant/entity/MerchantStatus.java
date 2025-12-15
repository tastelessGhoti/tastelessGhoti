package com.paygate.payment.domain.merchant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MerchantStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지");

    private final String description;
}
