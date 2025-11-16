package com.bookverse.domain.order.dto;

import com.bookverse.domain.order.entity.Order;
import com.bookverse.domain.order.entity.OrderItem;
import com.bookverse.domain.order.entity.OrderStatus;
import com.bookverse.domain.order.entity.PaymentMethod;
import com.bookverse.domain.user.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private List<OrderItemResponse> items;
    private OrderStatus status;
    private Integer totalPrice;
    private Address deliveryAddress;
    private String orderMessage;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .orderMessage(order.getOrderMessage())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long bookId;
        private String bookTitle;
        private Integer orderPrice;
        private Integer quantity;
        private Integer totalPrice;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .bookId(item.getBook().getId())
                    .bookTitle(item.getBook().getTitle())
                    .orderPrice(item.getOrderPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(item.getTotalPrice())
                    .build();
        }
    }
}
