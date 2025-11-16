package com.bookverse.domain.book.controller;

import com.bookverse.common.response.ApiResponse;
import com.bookverse.domain.book.dto.BookCreateRequest;
import com.bookverse.domain.book.dto.BookResponse;
import com.bookverse.domain.book.dto.BookSearchCondition;
import com.bookverse.domain.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도서 API 컨트롤러
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 도서 등록 (관리자)
     */
    @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다. (관리자 전용)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createBook(@Valid @RequestBody BookCreateRequest request) {
        Long bookId = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(bookId));
    }

    /**
     * 도서 상세 조회
     */
    @Operation(summary = "도서 상세 조회", description = "도서 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        BookResponse book = bookService.getBook(bookId);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    /**
     * 도서 검색
     */
    @Operation(summary = "도서 검색", description = "다양한 조건으로 도서를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @ModelAttribute BookSearchCondition condition,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookResponse> books = bookService.searchBooks(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * 카테고리별 도서 조회
     */
    @Operation(summary = "카테고리별 도서 조회", description = "특정 카테고리의 도서 목록을 조회합니다.")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooksByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookResponse> books = bookService.getBooksByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * 베스트셀러 조회
     */
    @Operation(summary = "베스트셀러 조회", description = "판매량 기준 베스트셀러를 조회합니다.")
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {
        List<BookResponse> books = bookService.getBestSellers(limit);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * 신간 도서 조회
     */
    @Operation(summary = "신간 도서 조회", description = "출판일 기준 최신 도서를 조회합니다.")
    @GetMapping("/new-releases")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getNewReleases(
            @RequestParam(defaultValue = "10") int limit) {
        List<BookResponse> books = bookService.getNewReleases(limit);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * 도서 재고 증가 (관리자)
     */
    @Operation(summary = "도서 재고 증가", description = "도서 재고를 증가시킵니다. (관리자 전용)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{bookId}/stock/increase")
    public ResponseEntity<ApiResponse<Void>> increaseStock(
            @PathVariable Long bookId,
            @RequestParam int quantity) {
        bookService.increaseStock(bookId, quantity);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 도서 삭제 (관리자)
     */
    @Operation(summary = "도서 삭제", description = "도서를 삭제합니다. (관리자 전용)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
