package com.bookverse.domain.review.entity;

import com.bookverse.common.entity.BaseTimeEntity;
import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 리뷰 엔티티
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_book", columnList = "book_id"),
        @Index(name = "idx_review_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;         // 1-5점

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 리뷰 수정
     */
    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
