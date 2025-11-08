package com.portfolio.ecommerce.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    ELECTRONICS("전자제품"),
    CLOTHING("의류"),
    FOOD("식품"),
    BOOKS("도서"),
    HOME("가정용품"),
    SPORTS("스포츠"),
    BEAUTY("뷰티"),
    TOYS("완구"),
    ETC("기타");

    private final String description;
}
