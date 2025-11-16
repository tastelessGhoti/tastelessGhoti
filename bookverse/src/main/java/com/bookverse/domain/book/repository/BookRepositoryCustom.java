package com.bookverse.domain.book.repository;

import com.bookverse.domain.book.dto.BookSearchCondition;
import com.bookverse.domain.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 도서 Repository Custom 인터페이스
 * QueryDSL을 사용한 복잡한 쿼리 처리
 *
 * @author Ghoti
 * @since 2025-11-17
 */
public interface BookRepositoryCustom {

    /**
     * 도서 검색 (제목, 저자, ISBN, 출판사)
     */
    Page<Book> searchBooks(BookSearchCondition condition, Pageable pageable);
}
