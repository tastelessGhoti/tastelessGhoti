package com.portfolio.ecommerce.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 상태
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확인"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;
}
