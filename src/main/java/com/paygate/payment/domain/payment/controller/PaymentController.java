package com.paygate.payment.domain.payment.controller;

import com.paygate.payment.common.response.ApiResponse;
import com.paygate.payment.common.response.PageResponse;
import com.paygate.payment.domain.payment.dto.*;
import com.paygate.payment.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인", description = "카드 결제를 승인합니다")
    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PaymentApprovalResponse> approve(
            @Parameter(description = "가맹점 ID", required = true)
            @RequestHeader("X-Merchant-Id") String merchantId,
            @Valid @RequestBody PaymentApprovalRequest request) {

        PaymentApprovalResponse response = paymentService.approve(merchantId, request);
        return ApiResponse.success("결제가 승인되었습니다", response);
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다. 부분 취소를 지원합니다")
    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PaymentCancelResponse> cancel(
            @Parameter(description = "가맹점 ID", required = true)
            @RequestHeader("X-Merchant-Id") String merchantId,
            @Valid @RequestBody PaymentCancelRequest request) {

        PaymentCancelResponse response = paymentService.cancel(merchantId, request);
        return ApiResponse.success("결제가 취소되었습니다", response);
    }

    @Operation(summary = "결제 상세 조회", description = "결제 상세 정보를 조회합니다")
    @GetMapping("/{transactionId}")
    public ApiResponse<PaymentDetailResponse> getPaymentDetail(
            @Parameter(description = "가맹점 ID", required = true)
            @RequestHeader("X-Merchant-Id") String merchantId,
            @Parameter(description = "트랜잭션 ID", required = true)
            @PathVariable String transactionId) {

        PaymentDetailResponse response = paymentService.getPaymentDetail(merchantId, transactionId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "결제 목록 조회", description = "조건에 맞는 결제 목록을 조회합니다")
    @GetMapping
    public ApiResponse<PageResponse<PaymentDetailResponse>> searchPayments(
            @Parameter(description = "가맹점 ID", required = true)
            @RequestHeader("X-Merchant-Id") String merchantId,
            @ParameterObject PaymentSearchCondition condition,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        var page = paymentService.searchPayments(merchantId, condition, pageable);
        return ApiResponse.success(PageResponse.from(page));
    }
}
