package com.paygate.payment.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CARD("신용카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    BANK_TRANSFER("계좌이체"),
    PHONE("휴대폰결제");

    private final String description;
}
