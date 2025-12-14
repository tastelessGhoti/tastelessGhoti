package com.kakaopay.account.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSearchCondition {

    private String name;
    private String phoneNumber;
    private String status;
}
