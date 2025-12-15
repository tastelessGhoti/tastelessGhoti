package com.paygate.payment.domain.payment.entity;

import com.paygate.payment.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 취소 이력.
 * 부분취소 시 각 취소 건별 이력 관리.
 */
@Entity
@Table(
    name = "payment_cancel_histories",
    indexes = {
        @Index(name = "idx_cancel_payment_id", columnList = "payment_id"),
        @Index(name = "idx_cancel_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "cancel_transaction_id", nullable = false, unique = true, length = 32)
    private String cancelTransactionId;

    @Column(name = "cancel_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal cancelAmount;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "van_cancel_id", length = 64)
    private String vanCancelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CancelStatus status;

    @Builder
    public PaymentCancelHistory(Long paymentId, String cancelTransactionId,
                                 BigDecimal cancelAmount, String cancelReason) {
        this.paymentId = paymentId;
        this.cancelTransactionId = cancelTransactionId;
        this.cancelAmount = cancelAmount;
        this.cancelReason = cancelReason;
        this.status = CancelStatus.PENDING;
    }

    public void complete(String vanCancelId) {
        this.vanCancelId = vanCancelId;
        this.status = CancelStatus.COMPLETED;
    }

    public void fail() {
        this.status = CancelStatus.FAILED;
    }

    public enum CancelStatus {
        PENDING, COMPLETED, FAILED
    }
}
