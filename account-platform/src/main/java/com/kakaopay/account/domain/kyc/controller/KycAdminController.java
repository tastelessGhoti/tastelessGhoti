package com.kakaopay.account.domain.kyc.controller;

import com.kakaopay.account.common.response.ApiResponse;
import com.kakaopay.account.domain.kyc.dto.KycVerificationResponse;
import com.kakaopay.account.domain.kyc.entity.KycLevel;
import com.kakaopay.account.domain.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * KYC 심사 관리자용 API
 * 실제 운영에서는 별도 인증 필요
 */
@Tag(name = "KYC 관리", description = "KYC 심사 관리자용 API")
@RestController
@RequestMapping("/admin/api/v1/kyc")
@RequiredArgsConstructor
public class KycAdminController {

    private final KycService kycService;

    @Operation(summary = "KYC 인증 승인")
    @PostMapping("/{verificationId}/approve")
    public ApiResponse<KycVerificationResponse> approve(
            @PathVariable Long verificationId,
            @RequestParam KycLevel level) {
        return ApiResponse.success(kycService.approveVerification(verificationId, level));
    }

    @Operation(summary = "KYC 인증 거절")
    @PostMapping("/{verificationId}/reject")
    public ApiResponse<KycVerificationResponse> reject(
            @PathVariable Long verificationId,
            @RequestParam String reason) {
        return ApiResponse.success(kycService.rejectVerification(verificationId, reason));
    }
}
