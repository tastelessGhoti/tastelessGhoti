package com.portfolio.ecommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<OrderItemRequest> orderItems;

    private String zipCode;
    private String address;
    private String detailAddress;

    @Size(max = 500, message = "주문 메시지는 500자 이하여야 합니다.")
    private String orderMessage;
}
