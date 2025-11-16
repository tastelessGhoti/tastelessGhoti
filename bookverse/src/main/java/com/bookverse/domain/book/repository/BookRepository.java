package com.bookverse.domain.book.repository;

import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 도서 Repository
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    /**
     * ISBN으로 도서 조회
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 상태별 도서 조회 (페이징)
     */
    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    /**
     * 카테고리별 도서 조회 (페이징)
     */
    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId AND b.status = 'AVAILABLE'")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 베스트셀러 조회 (판매량 기준)
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' ORDER BY b.totalSales DESC")
    List<Book> findBestSellers(Pageable pageable);

    /**
     * 신간 도서 조회 (출판일 기준)
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' ORDER BY b.publishedDate DESC")
    List<Book> findNewReleases(Pageable pageable);
}
