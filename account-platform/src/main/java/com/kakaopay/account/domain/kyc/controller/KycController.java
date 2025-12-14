package com.kakaopay.account.domain.kyc.controller;

import com.kakaopay.account.common.response.ApiResponse;
import com.kakaopay.account.domain.kyc.dto.KycVerificationRequest;
import com.kakaopay.account.domain.kyc.dto.KycVerificationResponse;
import com.kakaopay.account.domain.kyc.entity.KycLevel;
import com.kakaopay.account.domain.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "KYC", description = "KYC 인증 관리 API")
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @Operation(summary = "KYC 인증 요청")
    @PostMapping("/members/{memberId}/verify")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KycVerificationResponse> requestVerification(
            @PathVariable Long memberId,
            @Valid @RequestBody KycVerificationRequest request) {
        return ApiResponse.success(kycService.requestVerification(memberId, request));
    }

    @Operation(summary = "KYC 인증 상태 조회")
    @GetMapping("/members/{memberId}/status")
    public ApiResponse<KycVerificationResponse> getVerificationStatus(@PathVariable Long memberId) {
        return ApiResponse.success(kycService.getVerificationStatus(memberId));
    }

    @Operation(summary = "KYC 인증 이력 조회")
    @GetMapping("/members/{memberId}/history")
    public ApiResponse<List<KycVerificationResponse>> getVerificationHistory(@PathVariable Long memberId) {
        return ApiResponse.success(kycService.getVerificationHistory(memberId));
    }

    @Operation(summary = "KYC 레벨 조회")
    @GetMapping("/members/{memberId}/level")
    public ApiResponse<KycLevel> getMemberKycLevel(@PathVariable Long memberId) {
        return ApiResponse.success(kycService.getMemberKycLevel(memberId));
    }

    @Operation(summary = "KYC 인증 여부 확인")
    @GetMapping("/members/{memberId}/verified")
    public ApiResponse<Boolean> isKycVerified(@PathVariable Long memberId) {
        return ApiResponse.success(kycService.isKycVerified(memberId));
    }
}
