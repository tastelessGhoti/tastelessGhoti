package com.paygate.payment.domain.merchant.entity;

import com.paygate.payment.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 가맹점 정보.
 * 파트너사의 기본 정보 및 계약 조건 관리.
 */
@Entity
@Table(
    name = "merchants",
    indexes = {
        @Index(name = "idx_merchant_api_key", columnList = "api_key"),
        @Index(name = "idx_merchant_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Merchant extends BaseTimeEntity {

    @Id
    @Column(name = "merchant_id", length = 20)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(name = "secret_key", nullable = false, length = 128)
    private String secretKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MerchantStatus status;

    @Column(name = "business_number", length = 12)
    private String businessNumber;

    @Column(name = "representative_name", length = 50)
    private String representativeName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "fee_rate", precision = 5, scale = 4)
    private BigDecimal feeRate;

    @Column(name = "settlement_cycle_days")
    private Integer settlementCycleDays;

    @Builder
    public Merchant(String merchantId, String merchantName, String apiKey,
                   String secretKey, String businessNumber, String representativeName,
                   String email, String phone, BigDecimal feeRate, Integer settlementCycleDays) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.status = MerchantStatus.ACTIVE;
        this.businessNumber = businessNumber;
        this.representativeName = representativeName;
        this.email = email;
        this.phone = phone;
        this.feeRate = feeRate;
        this.settlementCycleDays = settlementCycleDays;
    }

    public boolean isActive() {
        return this.status == MerchantStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = MerchantStatus.INACTIVE;
    }

    public void activate() {
        this.status = MerchantStatus.ACTIVE;
    }
}
