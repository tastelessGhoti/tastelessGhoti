package com.bookverse.domain.book.entity;

import com.bookverse.common.entity.BaseTimeEntity;
import com.bookverse.domain.category.entity.Category;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 도서 엔티티
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author"),
        @Index(name = "idx_book_isbn", columnList = "isbn")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSales = 0;

    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity -= quantity;
    }

    /**
     * 판매량 증가
     */
    public void increaseSales(int quantity) {
        this.totalSales += quantity;
    }

    /**
     * 도서 정보 수정
     */
    public void updateInfo(String title, String author, String publisher,
                          LocalDate publishedDate, Integer price, String description,
                          String coverImage, Category category) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.price = price;
        this.description = description;
        this.coverImage = coverImage;
        this.category = category;
    }

    /**
     * 도서 상태 변경
     */
    public void updateStatus(BookStatus status) {
        this.status = status;
    }

    /**
     * 재고 확인
     */
    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }
}
