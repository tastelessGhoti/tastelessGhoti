package com.bookverse.domain.book.dto;

import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.category.entity.Category;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 도서 등록 요청 DTO
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "저자는 필수입니다.")
    @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
    private String author;

    @NotBlank(message = "ISBN은 필수입니다.")
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.")
    private String isbn;

    @NotBlank(message = "출판사는 필수입니다.")
    @Size(max = 100, message = "출판사는 100자 이하여야 합니다.")
    private String publisher;

    @NotNull(message = "출판일은 필수입니다.")
    private LocalDate publishedDate;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Integer stockQuantity;

    private String description;
    private String coverImage;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    /**
     * DTO를 Entity로 변환
     */
    public Book toEntity(Category category) {
        return Book.builder()
                .title(this.title)
                .author(this.author)
                .isbn(this.isbn)
                .publisher(this.publisher)
                .publishedDate(this.publishedDate)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .description(this.description)
                .coverImage(this.coverImage)
                .category(category)
                .build();
    }
}
