package com.bookverse.domain.book.repository;

import com.bookverse.domain.book.dto.BookSearchCondition;
import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.entity.BookStatus;
import com.bookverse.domain.book.entity.QBook;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 도서 Repository Custom 구현체
 * QueryDSL을 사용한 동적 쿼리 처리
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Book> searchBooks(BookSearchCondition condition, Pageable pageable) {
        QBook book = QBook.book;

        // 도서 목록 조회 쿼리
        List<Book> content = queryFactory
                .selectFrom(book)
                .where(
                        statusEq(BookStatus.AVAILABLE),
                        keywordContains(condition.getKeyword()),
                        titleContains(condition.getTitle()),
                        authorContains(condition.getAuthor()),
                        isbnEq(condition.getIsbn()),
                        publisherContains(condition.getPublisher()),
                        categoryIdEq(condition.getCategoryId()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(book.createdAt.desc())
                .fetch();

        // Count 쿼리 (성능 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(book.count())
                .from(book)
                .where(
                        statusEq(BookStatus.AVAILABLE),
                        keywordContains(condition.getKeyword()),
                        titleContains(condition.getTitle()),
                        authorContains(condition.getAuthor()),
                        isbnEq(condition.getIsbn()),
                        publisherContains(condition.getPublisher()),
                        categoryIdEq(condition.getCategoryId()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 상태 조건
     */
    private BooleanExpression statusEq(BookStatus status) {
        return status != null ? QBook.book.status.eq(status) : null;
    }

    /**
     * 통합 검색 키워드 조건 (제목 또는 저자 또는 출판사)
     */
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        QBook book = QBook.book;
        return book.title.contains(keyword)
                .or(book.author.contains(keyword))
                .or(book.publisher.contains(keyword));
    }

    /**
     * 제목 조건
     */
    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? QBook.book.title.contains(title) : null;
    }

    /**
     * 저자 조건
     */
    private BooleanExpression authorContains(String author) {
        return StringUtils.hasText(author) ? QBook.book.author.contains(author) : null;
    }

    /**
     * ISBN 조건
     */
    private BooleanExpression isbnEq(String isbn) {
        return StringUtils.hasText(isbn) ? QBook.book.isbn.eq(isbn) : null;
    }

    /**
     * 출판사 조건
     */
    private BooleanExpression publisherContains(String publisher) {
        return StringUtils.hasText(publisher) ? QBook.book.publisher.contains(publisher) : null;
    }

    /**
     * 카테고리 조건
     */
    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? QBook.book.category.id.eq(categoryId) : null;
    }

    /**
     * 가격 범위 조건
     */
    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return QBook.book.price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return QBook.book.price.goe(minPrice);
        } else if (maxPrice != null) {
            return QBook.book.price.loe(maxPrice);
        }
        return null;
    }
}
