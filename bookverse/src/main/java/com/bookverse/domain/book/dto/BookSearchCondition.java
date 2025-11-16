package com.bookverse.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 도서 검색 조건
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchCondition {

    private String keyword;         // 통합 검색 키워드
    private String title;           // 제목
    private String author;          // 저자
    private String isbn;            // ISBN
    private String publisher;       // 출판사
    private Long categoryId;        // 카테고리 ID
    private Integer minPrice;       // 최소 가격
    private Integer maxPrice;       // 최대 가격
}
