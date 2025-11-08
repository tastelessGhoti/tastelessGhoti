package com.portfolio.ecommerce.domain.order.dto;

import com.portfolio.ecommerce.domain.order.Order;
import com.portfolio.ecommerce.domain.order.OrderStatus;
import com.portfolio.ecommerce.domain.user.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private List<OrderItemResponse> orderItems;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Address deliveryAddress;
    private String orderMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getUser().getId(),
            order.getUser().getName(),
            itemResponses,
            order.getTotalAmount(),
            order.getStatus(),
            order.getDeliveryAddress(),
            order.getOrderMessage(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
