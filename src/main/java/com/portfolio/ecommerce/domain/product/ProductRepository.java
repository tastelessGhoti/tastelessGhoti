package com.portfolio.ecommerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 상품 리포지토리
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 카테고리별 상품 조회 (페이징)
     */
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    /**
     * 상태별 상품 조회 (페이징)
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * 상품명으로 검색 (페이징)
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.status = 'AVAILABLE'")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 판매 가능한 상품 조회 (페이징)
     */
    Page<Product> findByStatusAndStockQuantityGreaterThan(
        ProductStatus status, Integer stockQuantity, Pageable pageable);

    /**
     * 카테고리와 상태로 상품 조회
     */
    List<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status);
}
