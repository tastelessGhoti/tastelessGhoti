package com.kakaopay.account.domain.member.controller;

import com.kakaopay.account.common.response.ApiResponse;
import com.kakaopay.account.domain.member.dto.MemberResponse;
import com.kakaopay.account.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 내부 서비스 간 통신용 API
 * 게이트웨이에서 외부 접근 차단 필요
 */
@Tag(name = "내부 API - 회원", description = "서비스 간 통신용 회원 API")
@RestController
@RequestMapping("/internal/api/v1/members")
@RequiredArgsConstructor
public class InternalMemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 정보 조회 (마스킹 없음)")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMemberForInternal(@PathVariable Long memberId) {
        return ApiResponse.success(memberService.getMemberForInternal(memberId));
    }

    @Operation(summary = "CI로 회원 존재 여부 확인")
    @GetMapping("/exists")
    public ApiResponse<Boolean> existsByCi(@RequestParam String ci) {
        return ApiResponse.success(memberService.existsByCi(ci));
    }
}
