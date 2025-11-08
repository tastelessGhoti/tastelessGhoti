package com.portfolio.ecommerce.domain.order.service;

import com.portfolio.ecommerce.domain.order.Order;
import com.portfolio.ecommerce.domain.order.OrderRepository;
import com.portfolio.ecommerce.domain.order.OrderStatus;
import com.portfolio.ecommerce.domain.order.dto.OrderItemRequest;
import com.portfolio.ecommerce.domain.order.dto.OrderRequest;
import com.portfolio.ecommerce.domain.order.dto.OrderResponse;
import com.portfolio.ecommerce.domain.product.Product;
import com.portfolio.ecommerce.domain.product.ProductCategory;
import com.portfolio.ecommerce.domain.product.ProductRepository;
import com.portfolio.ecommerce.domain.user.Address;
import com.portfolio.ecommerce.domain.user.User;
import com.portfolio.ecommerce.domain.user.UserRepository;
import com.portfolio.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 주문 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        Long userId = 1L;
        User user = User.builder()
            .email("test@example.com")
            .password("password123!")
            .name("테스트 사용자")
            .phoneNumber("010-1234-5678")
            .address(new Address("12345", "서울시 강남구", "테헤란로 123"))
            .role(UserRole.USER)
            .build();

        Product product = Product.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(BigDecimal.valueOf(10000))
            .stockQuantity(100)
            .category(ProductCategory.ELECTRONICS)
            .imageUrl("http://example.com/image.jpg")
            .build();

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        OrderRequest request = new OrderRequest(
            Arrays.asList(itemRequest),
            "12345",
            "서울시 강남구",
            "테헤란로 123",
            "빠른 배송 부탁드립니다."
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });

        // when
        OrderResponse response = orderService.createOrder(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }
}
