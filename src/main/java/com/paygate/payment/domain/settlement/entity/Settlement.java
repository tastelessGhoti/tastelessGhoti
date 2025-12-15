package com.paygate.payment.domain.settlement.entity;

import com.paygate.payment.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 정산 정보.
 * 가맹점별 일자별 정산 데이터 집계.
 */
@Entity
@Table(
    name = "settlements",
    indexes = {
        @Index(name = "idx_settlement_merchant_date", columnList = "merchant_id, settlement_date"),
        @Index(name = "idx_settlement_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_merchant_date", columnNames = {"merchant_id", "settlement_date"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false, length = 20)
    private String merchantId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "total_fee", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalFee;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal netAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "cancel_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal cancelAmount;

    @Column(name = "cancel_count", nullable = false)
    private Integer cancelCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status;

    @Builder
    public Settlement(String merchantId, LocalDate settlementDate,
                     BigDecimal totalAmount, BigDecimal totalFee,
                     int transactionCount, BigDecimal cancelAmount, int cancelCount) {
        this.merchantId = merchantId;
        this.settlementDate = settlementDate;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.netAmount = totalAmount.subtract(totalFee).subtract(cancelAmount);
        this.transactionCount = transactionCount;
        this.cancelAmount = cancelAmount;
        this.cancelCount = cancelCount;
        this.status = SettlementStatus.PENDING;
    }

    public void confirm() {
        this.status = SettlementStatus.CONFIRMED;
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
    }
}
