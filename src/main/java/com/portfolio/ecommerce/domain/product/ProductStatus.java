package com.portfolio.ecommerce.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 상태
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    AVAILABLE("판매중"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("판매중단");

    private final String description;
}
