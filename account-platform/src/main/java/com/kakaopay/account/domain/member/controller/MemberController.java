package com.kakaopay.account.domain.member.controller;

import com.kakaopay.account.common.response.ApiResponse;
import com.kakaopay.account.domain.member.dto.*;
import com.kakaopay.account.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원 가입/조회/수정/탈퇴 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 가입")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ApiResponse.success(memberService.signUp(request));
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {
        return ApiResponse.success(memberService.getMember(memberId));
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping("/{memberId}")
    public ApiResponse<MemberResponse> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberService.updateMember(memberId, request));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long memberId) {
        memberService.withdraw(memberId);
    }

    @Operation(summary = "회원 검색 (관리자용)")
    @GetMapping
    public ApiResponse<Page<MemberResponse>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        MemberSearchCondition condition = MemberSearchCondition.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .status(status)
                .build();

        return ApiResponse.success(memberService.searchMembers(condition, pageable));
    }
}
