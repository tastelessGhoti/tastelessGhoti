package com.bookverse.domain.order.entity;

import com.bookverse.common.entity.BaseTimeEntity;
import com.bookverse.domain.user.entity.Address;
import com.bookverse.domain.user.entity.User;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 엔티티
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private Integer totalPrice;

    @Embedded
    private Address deliveryAddress;

    private String orderMessage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    private String paymentKey;      // 결제 키 (토스페이먼츠 등)

    /**
     * 주문 항목 추가
     */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    /**
     * 총 금액 계산
     */
    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    /**
     * 주문 상태 변경
     */
    public void updateStatus(OrderStatus status) {
        // 상태 전이 검증
        if (!this.status.canChangeTo(status)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = status;
    }

    /**
     * 주문 취소
     */
    public void cancel() {
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        this.status = OrderStatus.CANCELLED;

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 결제 정보 설정
     */
    public void setPaymentInfo(PaymentMethod paymentMethod, String paymentKey) {
        this.paymentMethod = paymentMethod;
        this.paymentKey = paymentKey;
    }

    /**
     * 정적 팩토리 메서드 - 주문 생성
     */
    public static Order createOrder(User user, Address deliveryAddress, String orderMessage,
                                   List<OrderItem> orderItems) {
        Order order = Order.builder()
                .user(user)
                .deliveryAddress(deliveryAddress)
                .orderMessage(orderMessage)
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.calculateTotalPrice();
        return order;
    }
}
