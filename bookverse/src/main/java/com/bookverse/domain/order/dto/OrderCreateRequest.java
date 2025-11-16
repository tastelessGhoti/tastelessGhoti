package com.bookverse.domain.order.dto;

import com.bookverse.domain.order.entity.PaymentMethod;
import com.bookverse.domain.user.entity.Address;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotEmpty(message = "주문 항목은 필수입니다.")
    private List<OrderItemRequest> items;

    @NotNull(message = "배송지 정보는 필수입니다.")
    private Address deliveryAddress;

    private String orderMessage;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "도서 ID는 필수입니다.")
        private Long bookId;

        @NotNull(message = "수량은 필수입니다.")
        private Integer quantity;
    }
}
