package com.kakaopay.account.domain.member.repository;

import com.kakaopay.account.domain.member.dto.MemberSearchCondition;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        QMember member = QMember.member;

        List<Member> content = queryFactory
                .selectFrom(member)
                .where(
                        nameContains(condition.getName()),
                        phoneNumberEq(condition.getPhoneNumber()),
                        statusEq(condition.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        nameContains(condition.getName()),
                        phoneNumberEq(condition.getPhoneNumber()),
                        statusEq(condition.getStatus())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? QMember.member.name.contains(name) : null;
    }

    private BooleanExpression phoneNumberEq(String phoneNumber) {
        return StringUtils.hasText(phoneNumber) ? QMember.member.phoneNumber.eq(phoneNumber) : null;
    }

    private BooleanExpression statusEq(String status) {
        return StringUtils.hasText(status) ? QMember.member.status.stringValue().eq(status) : null;
    }
}
