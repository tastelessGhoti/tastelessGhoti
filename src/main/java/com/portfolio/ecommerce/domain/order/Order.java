package com.portfolio.ecommerce.domain.order;

import com.portfolio.ecommerce.common.BaseEntity;
import com.portfolio.ecommerce.domain.user.Address;
import com.portfolio.ecommerce.domain.user.User;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 엔티티
 * 인덱스 최적화: user_id, status, createdAt (주문 조회 성능 향상)
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_user_status", columnList = "user_id, status"),
    @Index(name = "idx_order_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Embedded
    private Address deliveryAddress;

    @Column(length = 500)
    private String orderMessage;

    @Builder
    public Order(User user, Address deliveryAddress, String orderMessage) {
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.orderMessage = orderMessage;
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
    }

    /**
     * 주문 항목 추가
     */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalAmount();
    }

    /**
     * 총 금액 계산
     */
    private void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 주문 확인
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                "대기 중인 주문만 확인할 수 있습니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * 배송 시작
     */
    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                "확인된 주문만 배송할 수 있습니다.");
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * 배송 완료
     */
    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                "배송 중인 주문만 완료할 수 있습니다.");
        }
        this.status = OrderStatus.DELIVERED;
    }

    /**
     * 주문 취소
     */
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED,
                "이미 취소된 주문입니다.");
        }
        if (this.status == OrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                "배송 완료된 주문은 취소할 수 없습니다.");
        }

        this.status = OrderStatus.CANCELLED;

        // 재고 복구
        orderItems.forEach(OrderItem::restoreStock);
    }

    /**
     * 주문 가능 여부 확인
     */
    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }
}
