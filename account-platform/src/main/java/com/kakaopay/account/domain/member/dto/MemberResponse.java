package com.kakaopay.account.domain.member.dto;

import com.kakaopay.account.common.util.MaskingUtils;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {

    private Long memberId;
    private String name;
    private String phoneNumber;
    private String email;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .name(MaskingUtils.maskName(member.getName()))
                .phoneNumber(MaskingUtils.maskPhoneNumber(member.getPhoneNumber()))
                .email(MaskingUtils.maskEmail(member.getEmail()))
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .lastLoginAt(member.getLastLoginAt())
                .build();
    }

    /**
     * 내부 서비스용 - 마스킹 없이 원본 정보 반환
     */
    public static MemberResponse forInternal(Member member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .lastLoginAt(member.getLastLoginAt())
                .build();
    }
}
