package com.bookverse.domain.book.service;

import com.bookverse.domain.book.dto.BookCreateRequest;
import com.bookverse.domain.book.dto.BookResponse;
import com.bookverse.domain.book.dto.BookSearchCondition;
import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.repository.BookRepository;
import com.bookverse.domain.category.entity.Category;
import com.bookverse.domain.category.repository.CategoryRepository;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 도서 비즈니스 로직 서비스
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 도서 등록 (관리자)
     */
    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Long createBook(BookCreateRequest request) {
        // 카테고리 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 도서 생성
        Book book = request.toEntity(category);
        Book savedBook = bookRepository.save(book);

        log.info("새로운 도서 등록: {}", savedBook.getTitle());
        return savedBook.getId();
    }

    /**
     * 도서 상세 조회
     * Redis 캐싱 적용 - 자주 조회되는 도서는 캐시에서 빠르게 응답
     */
    @Cacheable(value = "books", key = "#bookId")
    public BookResponse getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return BookResponse.from(book);
    }

    /**
     * 도서 검색 (QueryDSL 동적 쿼리)
     */
    public Page<BookResponse> searchBooks(BookSearchCondition condition, Pageable pageable) {
        Page<Book> books = bookRepository.searchBooks(condition, pageable);
        return books.map(BookResponse::from);
    }

    /**
     * 카테고리별 도서 조회
     */
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        Page<Book> books = bookRepository.findByCategoryId(categoryId, pageable);
        return books.map(BookResponse::from);
    }

    /**
     * 베스트셀러 조회
     * Redis 캐싱 적용 - 베스트셀러는 자주 조회되므로 캐싱
     */
    @Cacheable(value = "bestSellers", key = "#limit")
    public List<BookResponse> getBestSellers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findBestSellers(pageable);
        return books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 신간 도서 조회
     */
    @Cacheable(value = "newReleases", key = "#limit")
    public List<BookResponse> getNewReleases(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findNewReleases(pageable);
        return books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 도서 재고 증가 (관리자)
     */
    @Transactional
    @CacheEvict(value = "books", key = "#bookId")
    public void increaseStock(Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        book.increaseStock(quantity);
        log.info("도서 재고 증가: {} (+{})", book.getTitle(), quantity);
    }

    /**
     * 도서 삭제 (관리자)
     */
    @Transactional
    @CacheEvict(value = "books", key = "#bookId")
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        bookRepository.delete(book);
        log.info("도서 삭제: {}", book.getTitle());
    }
}
