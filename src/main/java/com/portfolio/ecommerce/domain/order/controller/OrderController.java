package com.portfolio.ecommerce.domain.order.controller;

import com.portfolio.ecommerce.common.ApiResponse;
import com.portfolio.ecommerce.common.PageResponse;
import com.portfolio.ecommerce.domain.order.dto.OrderRequest;
import com.portfolio.ecommerce.domain.order.dto.OrderResponse;
import com.portfolio.ecommerce.domain.order.service.OrderService;
import com.portfolio.ecommerce.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 API 컨트롤러
 * 주문 생성, 조회, 취소 등 주문 관련 API 제공
 */
@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenProvider tokenProvider;

    /**
     * 주문 생성
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        // 실제로는 UserService를 통해 이메일로 사용자 ID를 조회해야 하지만,
        // 간단한 예제를 위해 토큰에서 추출한 이메일을 사용
        // 여기서는 임시로 1L을 사용 (실제 구현 시에는 수정 필요)
        Long userId = 1L; // TODO: 실제 사용자 ID 조회 로직 추가
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("주문이 완료되었습니다.", response));
    }

    /**
     * 주문 상세 조회
     */
    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 주문 목록 조회
     */
    @Operation(summary = "내 주문 목록 조회", description = "로그인한 사용자의 주문 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {
        Long userId = 1L; // TODO: 실제 사용자 ID 조회 로직 추가
        PageResponse<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 전체 주문 목록 조회 (관리자)
     */
    @Operation(summary = "전체 주문 목록 조회", description = "모든 주문 목록을 조회합니다. (관리자 권한 필요)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 주문 취소
     */
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = 1L; // TODO: 실제 사용자 ID 조회 로직 추가
        OrderResponse response = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다.", response));
    }

    /**
     * 주문 확인 (관리자)
     */
    @Operation(summary = "주문 확인", description = "주문을 확인 처리합니다. (관리자 권한 필요)")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("주문이 확인되었습니다.", response));
    }

    /**
     * 배송 시작 (관리자)
     */
    @Operation(summary = "배송 시작", description = "주문의 배송을 시작합니다. (관리자 권한 필요)")
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.shipOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("배송이 시작되었습니다.", response));
    }

    /**
     * 배송 완료 (관리자)
     */
    @Operation(summary = "배송 완료", description = "주문의 배송을 완료 처리합니다. (관리자 권한 필요)")
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.deliverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("배송이 완료되었습니다.", response));
    }
}
