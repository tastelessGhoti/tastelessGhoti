package com.portfolio.ecommerce.domain.product.service;

import com.portfolio.ecommerce.domain.product.Product;
import com.portfolio.ecommerce.domain.product.ProductCategory;
import com.portfolio.ecommerce.domain.product.ProductRepository;
import com.portfolio.ecommerce.domain.product.dto.ProductRequest;
import com.portfolio.ecommerce.domain.product.dto.ProductResponse;
import com.portfolio.ecommerce.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 상품 서비스 테스트
 * Mockito를 사용한 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 성공")
    void createProduct_Success() {
        // given
        ProductRequest request = new ProductRequest(
            "테스트 상품",
            "테스트 설명",
            BigDecimal.valueOf(10000),
            100,
            ProductCategory.ELECTRONICS,
            "http://example.com/image.jpg"
        );

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .category(request.getCategory())
            .imageUrl(request.getImageUrl())
            .build();

        given(productRepository.save(any(Product.class))).willReturn(product);

        // when
        ProductResponse response = productService.createProduct(request);

        // then
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(10000));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(BigDecimal.valueOf(10000))
            .stockQuantity(100)
            .category(ProductCategory.ELECTRONICS)
            .imageUrl("http://example.com/image.jpg")
            .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductResponse response = productService.getProduct(productId);

        // then
        assertThat(response.getName()).isEqualTo("테스트 상품");
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - 존재하지 않는 상품")
    void getProduct_NotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(productId))
            .isInstanceOf(BusinessException.class);
    }
}
