package com.paygate.payment.domain.payment.entity;

import com.paygate.payment.common.entity.BaseTimeEntity;
import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.PaymentException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 트랜잭션 핵심 엔티티.
 * 결제 승인, 취소, 부분취소 등 모든 결제 상태를 관리.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_merchant_order", columnList = "merchant_id, order_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 32)
    private String transactionId;

    @Column(name = "merchant_id", nullable = false, length = 20)
    private String merchantId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Column(name = "canceled_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal canceledAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "card_company", length = 20)
    private String cardCompany;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "approval_number", length = 20)
    private String approvalNumber;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "van_transaction_id", length = 64)
    private String vanTransactionId;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "buyer_name", length = 50)
    private String buyerName;

    @Column(name = "buyer_email", length = 100)
    private String buyerEmail;

    @Column(name = "buyer_phone", length = 20)
    private String buyerPhone;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Version
    private Long version;

    @Builder
    public Payment(String transactionId, String merchantId, String orderId,
                   BigDecimal amount, PaymentMethod paymentMethod,
                   String cardNumber, String cardCompany, Integer installmentMonths,
                   String productName, String buyerName, String buyerEmail, String buyerPhone) {
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.amount = amount;
        this.canceledAmount = BigDecimal.ZERO;
        this.status = PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.cardCompany = cardCompany;
        this.installmentMonths = installmentMonths;
        this.productName = productName;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
    }

    /**
     * 결제 승인 처리.
     * 상태가 PENDING일 때만 승인 가능.
     */
    public void approve(String approvalNumber, String vanTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentException(ErrorCode.PAYMENT_ALREADY_APPROVED);
        }
        this.status = PaymentStatus.APPROVED;
        this.approvalNumber = approvalNumber;
        this.vanTransactionId = vanTransactionId;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 결제 실패 처리.
     */
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
    }

    /**
     * 부분 취소 처리.
     * 취소 금액 누적 관리 및 전체/부분 취소 상태 구분.
     */
    public void cancel(BigDecimal cancelAmount) {
        validateCancelable(cancelAmount);

        this.canceledAmount = this.canceledAmount.add(cancelAmount);

        if (this.canceledAmount.compareTo(this.amount) == 0) {
            this.status = PaymentStatus.CANCELED;
        } else {
            this.status = PaymentStatus.PARTIAL_CANCELED;
        }
    }

    private void validateCancelable(BigDecimal cancelAmount) {
        if (this.status == PaymentStatus.CANCELED) {
            throw new PaymentException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }
        if (this.status == PaymentStatus.PENDING || this.status == PaymentStatus.FAILED) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_FOUND,
                    "취소 가능한 결제 상태가 아닙니다. 현재 상태: " + this.status);
        }

        BigDecimal remainingAmount = this.amount.subtract(this.canceledAmount);
        if (cancelAmount.compareTo(remainingAmount) > 0) {
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_EXCEEDED,
                    String.format("취소 가능 금액: %s, 요청 금액: %s", remainingAmount, cancelAmount));
        }
    }

    /**
     * 취소 가능 잔액 반환.
     */
    public BigDecimal getCancelableAmount() {
        return this.amount.subtract(this.canceledAmount);
    }

    /**
     * 전체 취소 여부.
     */
    public boolean isFullyCanceled() {
        return this.status == PaymentStatus.CANCELED;
    }
}
