package com.bookverse.domain.order.entity;

import java.util.Arrays;
import java.util.List;

/**
 * 주문 상태
 *
 * @author Ghoti
 * @since 2025-11-17
 */
public enum OrderStatus {
    PENDING,        // 주문 대기 (결제 전)
    PAID,           // 결제 완료
    PREPARING,      // 배송 준비 중
    SHIPPED,        // 배송 중
    DELIVERED,      // 배송 완료
    CANCELLED;      // 취소

    /**
     * 상태 전이 가능 여부 확인
     */
    public boolean canChangeTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return Arrays.asList(PAID, CANCELLED).contains(newStatus);
            case PAID:
                return Arrays.asList(PREPARING, CANCELLED).contains(newStatus);
            case PREPARING:
                return Arrays.asList(SHIPPED, CANCELLED).contains(newStatus);
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}
