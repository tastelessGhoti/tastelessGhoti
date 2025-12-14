package com.kakaopay.account.domain.member.repository;

import com.kakaopay.account.domain.member.dto.MemberSearchCondition;
import com.kakaopay.account.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable);
}
