package com.portfolio.ecommerce.domain.order;

import com.portfolio.ecommerce.common.BaseEntity;
import com.portfolio.ecommerce.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주문 항목 엔티티
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // 주문 당시 가격

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;  // quantity * price

    @Builder
    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(quantity));

        // 재고 감소
        product.decreaseStock(quantity);
    }

    /**
     * Order 설정 (양방향 관계 설정용)
     */
    void setOrder(Order order) {
        this.order = order;
    }

    /**
     * 재고 복구 (주문 취소 시)
     */
    void restoreStock() {
        this.product.increaseStock(this.quantity);
    }
}
