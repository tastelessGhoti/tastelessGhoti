package com.portfolio.ecommerce.domain.product.service;

import com.portfolio.ecommerce.common.PageResponse;
import com.portfolio.ecommerce.domain.product.Product;
import com.portfolio.ecommerce.domain.product.ProductCategory;
import com.portfolio.ecommerce.domain.product.ProductRepository;
import com.portfolio.ecommerce.domain.product.ProductStatus;
import com.portfolio.ecommerce.domain.product.dto.ProductRequest;
import com.portfolio.ecommerce.domain.product.dto.ProductResponse;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 서비스
 * 상품 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 등록
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .category(request.getCategory())
            .imageUrl(request.getImageUrl())
            .build();

        Product savedProduct = productRepository.save(product);
        log.info("새로운 상품 등록: {}", savedProduct.getName());

        return ProductResponse.from(savedProduct);
    }

    /**
     * 상품 상세 조회
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                "상품을 찾을 수 없습니다. ID: " + productId));

        return ProductResponse.from(product);
    }

    /**
     * 상품 목록 조회 (페이징)
     */
    public PageResponse<ProductResponse> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        Page<ProductResponse> responses = products.map(ProductResponse::from);
        return PageResponse.of(responses);
    }

    /**
     * 카테고리별 상품 조회
     */
    public PageResponse<ProductResponse> getProductsByCategory(
            ProductCategory category, Pageable pageable) {
        Page<Product> products = productRepository.findByCategory(category, pageable);
        Page<ProductResponse> responses = products.map(ProductResponse::from);
        return PageResponse.of(responses);
    }

    /**
     * 상품명 검색
     */
    public PageResponse<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchByName(keyword, pageable);
        Page<ProductResponse> responses = products.map(ProductResponse::from);
        return PageResponse.of(responses);
    }

    /**
     * 상품 수정
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.updateInfo(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getCategory(),
            request.getImageUrl()
        );

        log.info("상품 정보 수정: {}", product.getName());

        return ProductResponse.from(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
        log.info("상품 삭제: {}", product.getName());
    }

    /**
     * 판매 가능한 상품 조회
     */
    public PageResponse<ProductResponse> getAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository
            .findByStatusAndStockQuantityGreaterThan(ProductStatus.AVAILABLE, 0, pageable);
        Page<ProductResponse> responses = products.map(ProductResponse::from);
        return PageResponse.of(responses);
    }
}
