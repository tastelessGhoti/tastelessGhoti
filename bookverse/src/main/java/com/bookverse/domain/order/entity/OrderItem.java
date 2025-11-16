package com.bookverse.domain.order.entity;

import com.bookverse.common.entity.BaseTimeEntity;
import com.bookverse.domain.book.entity.Book;
import jakarta.persistence.*;
import lombok.*;

/**
 * 주문 항목 엔티티
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer orderPrice;     // 주문 당시 가격 (가격이 변동될 수 있으므로 저장)

    @Column(nullable = false)
    private Integer quantity;

    /**
     * 주문 할당
     */
    public void assignOrder(Order order) {
        this.order = order;
    }

    /**
     * 총 가격 계산
     */
    public int getTotalPrice() {
        return orderPrice * quantity;
    }

    /**
     * 주문 취소 - 재고 복구
     */
    public void cancel() {
        book.increaseStock(quantity);
    }

    /**
     * 정적 팩토리 메서드 - 주문 항목 생성
     */
    public static OrderItem createOrderItem(Book book, int quantity) {
        // 재고 확인 및 감소
        book.decreaseStock(quantity);

        // 판매량 증가
        book.increaseSales(quantity);

        return OrderItem.builder()
                .book(book)
                .orderPrice(book.getPrice())
                .quantity(quantity)
                .build();
    }
}
