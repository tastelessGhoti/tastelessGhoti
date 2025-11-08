package com.portfolio.ecommerce.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 수단
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PAYMENT("모바일 결제"),
    POINT("포인트");

    private final String description;
}
