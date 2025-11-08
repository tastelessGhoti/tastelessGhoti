package com.portfolio.ecommerce.domain.product;

import com.portfolio.ecommerce.common.BaseEntity;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(length = 500)
    private String imageUrl;

    @Builder
    public Product(String name, String description, BigDecimal price,
                   Integer stockQuantity, ProductCategory category, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.status = ProductStatus.AVAILABLE;
        this.imageUrl = imageUrl;
    }

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                String.format("재고가 부족합니다. (현재 재고: %d, 요청 수량: %d)", this.stockQuantity, quantity));
        }
        this.stockQuantity -= quantity;

        // 재고가 0이 되면 품절 상태로 변경
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    /**
     * 재고 증가 (반품 등)
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;

        // 재고가 있으면 판매 가능 상태로 변경
        if (this.stockQuantity > 0 && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.AVAILABLE;
        }
    }

    /**
     * 상품 정보 수정
     */
    public void updateInfo(String name, String description, BigDecimal price,
                          ProductCategory category, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    /**
     * 상품 판매 중단
     */
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    /**
     * 상품 판매 재개
     */
    public void resume() {
        this.status = this.stockQuantity > 0 ? ProductStatus.AVAILABLE : ProductStatus.OUT_OF_STOCK;
    }
}
