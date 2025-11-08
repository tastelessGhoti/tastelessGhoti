package com.portfolio.ecommerce.domain.payment;

import com.portfolio.ecommerce.common.BaseEntity;
import com.portfolio.ecommerce.domain.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(unique = true, length = 100)
    private String transactionId;  // 외부 결제 시스템의 거래 ID

    private LocalDateTime paidAt;

    @Column(length = 500)
    private String failureReason;

    @Builder
    public Payment(Order order, BigDecimal amount, PaymentMethod method, String transactionId) {
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.transactionId = transactionId;
    }

    /**
     * 결제 성공 처리
     */
    public void completePayment() {
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    /**
     * 결제 취소
     */
    public void cancelPayment() {
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * 환불 처리
     */
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}
