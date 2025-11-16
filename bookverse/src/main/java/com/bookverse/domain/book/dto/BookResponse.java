package com.bookverse.domain.book.dto;

import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 도서 응답 DTO
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@Builder
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publishedDate;
    private Integer price;
    private Integer stockQuantity;
    private String description;
    private String coverImage;
    private String categoryName;
    private BookStatus status;
    private Integer totalSales;

    /**
     * Entity를 DTO로 변환
     */
    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishedDate(book.getPublishedDate())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .description(book.getDescription())
                .coverImage(book.getCoverImage())
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .status(book.getStatus())
                .totalSales(book.getTotalSales())
                .build();
    }
}
