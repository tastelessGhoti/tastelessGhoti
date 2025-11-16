package com.bookverse.domain.order.controller;

import com.bookverse.common.response.ApiResponse;
import com.bookverse.domain.order.dto.OrderCreateRequest;
import com.bookverse.domain.order.dto.OrderResponse;
import com.bookverse.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 API 컨트롤러
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        Long orderId = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderId));
    }

    /**
     * 주문 상세 조회
     */
    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        OrderResponse order = orderService.getOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * 내 주문 목록 조회
     */
    @Operation(summary = "내 주문 목록 조회", description = "내 주문 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getMyOrders(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * 주문 취소
     */
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
