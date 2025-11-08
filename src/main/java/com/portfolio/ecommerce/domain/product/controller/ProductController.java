package com.portfolio.ecommerce.domain.product.controller;

import com.portfolio.ecommerce.common.ApiResponse;
import com.portfolio.ecommerce.common.PageResponse;
import com.portfolio.ecommerce.domain.product.ProductCategory;
import com.portfolio.ecommerce.domain.product.dto.ProductRequest;
import com.portfolio.ecommerce.domain.product.dto.ProductResponse;
import com.portfolio.ecommerce.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 API 컨트롤러
 * 상품 CRUD 및 검색 API 제공
 */
@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록 (관리자)
     */
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다. (관리자 권한 필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품이 등록되었습니다.", response));
    }

    /**
     * 상품 상세 조회
     */
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 목록 조회
     */
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<ProductResponse> response = productService.getProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리별 상품 조회
     */
    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품을 조회합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable ProductCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<ProductResponse> response =
            productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 검색
     */
    @Operation(summary = "상품 검색", description = "상품명으로 상품을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<ProductResponse> response =
            productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 판매 가능한 상품 조회
     */
    @Operation(summary = "판매 가능 상품 조회", description = "현재 판매 가능한 상품만 조회합니다.")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAvailableProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<ProductResponse> response = productService.getAvailableProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 수정 (관리자)
     */
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다. (관리자 권한 필요)")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", response));
    }

    /**
     * 상품 삭제 (관리자)
     */
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. (관리자 권한 필요)")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다.", null));
    }
}
