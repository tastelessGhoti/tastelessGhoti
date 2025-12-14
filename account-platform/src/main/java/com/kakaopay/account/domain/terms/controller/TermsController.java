package com.kakaopay.account.domain.terms.controller;

import com.kakaopay.account.common.response.ApiResponse;
import com.kakaopay.account.domain.terms.dto.TermsAgreementRequest;
import com.kakaopay.account.domain.terms.dto.TermsResponse;
import com.kakaopay.account.domain.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "약관", description = "약관 조회 및 동의 관리 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "전체 약관 목록 조회")
    @GetMapping
    public ApiResponse<List<TermsResponse>> getAllTerms() {
        return ApiResponse.success(termsService.getAllActiveTerms());
    }

    @Operation(summary = "약관 상세 조회")
    @GetMapping("/{termsId}")
    public ApiResponse<TermsResponse> getTerms(@PathVariable Long termsId) {
        return ApiResponse.success(termsService.getTerms(termsId));
    }

    @Operation(summary = "회원별 약관 동의 현황 조회")
    @GetMapping("/members/{memberId}")
    public ApiResponse<List<TermsResponse>> getMemberTerms(@PathVariable Long memberId) {
        return ApiResponse.success(termsService.getMemberTermsWithAgreementStatus(memberId));
    }

    @Operation(summary = "약관 동의")
    @PostMapping("/members/{memberId}/agree")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> agreeToTerms(
            @PathVariable Long memberId,
            @Valid @RequestBody TermsAgreementRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        termsService.agreeToTerms(memberId, request, ipAddress);
        return ApiResponse.success();
    }

    @Operation(summary = "약관 동의 철회 (선택 약관만 가능)")
    @DeleteMapping("/members/{memberId}/terms/{termsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawAgreement(
            @PathVariable Long memberId,
            @PathVariable Long termsId) {
        termsService.withdrawTermsAgreement(memberId, termsId);
    }

    @Operation(summary = "필수 약관 동의 여부 확인")
    @GetMapping("/members/{memberId}/check-required")
    public ApiResponse<Boolean> checkRequiredTerms(@PathVariable Long memberId) {
        return ApiResponse.success(termsService.hasAgreedToAllRequiredTerms(memberId));
    }
}
