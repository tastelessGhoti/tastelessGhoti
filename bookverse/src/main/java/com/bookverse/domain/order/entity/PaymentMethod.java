package com.bookverse.domain.order.entity;

/**
 * 결제 수단
 *
 * @author Ghoti
 * @since 2025-11-17
 */
public enum PaymentMethod {
    CARD,           // 신용/체크카드
    VIRTUAL_ACCOUNT,// 가상계좌
    TRANSFER,       // 계좌이체
    MOBILE,         // 휴대폰
    KAKAOPAY,       // 카카오페이
    NAVERPAY        // 네이버페이
}
