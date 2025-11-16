package com.bookverse.domain.book.entity;

/**
 * 도서 판매 상태
 *
 * @author Ghoti
 * @since 2025-11-17
 */
public enum BookStatus {
    AVAILABLE,      // 판매 중
    OUT_OF_STOCK,   // 품절
    DISCONTINUED    // 판매 중단
}
