package com.paygate.payment.domain.payment.repository;

import com.paygate.payment.domain.payment.dto.PaymentSearchCondition;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.paygate.payment.domain.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Payment> searchPayments(PaymentSearchCondition condition, Pageable pageable) {
        List<Payment> content = queryFactory
                .selectFrom(payment)
                .where(
                        merchantIdEq(condition.getMerchantId()),
                        statusEq(condition.getStatus()),
                        orderIdContains(condition.getOrderId()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate())
                )
                .orderBy(payment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(payment.count())
                .from(payment)
                .where(
                        merchantIdEq(condition.getMerchantId()),
                        statusEq(condition.getStatus()),
                        orderIdContains(condition.getOrderId()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression merchantIdEq(String merchantId) {
        return StringUtils.hasText(merchantId) ? payment.merchantId.eq(merchantId) : null;
    }

    private BooleanExpression statusEq(PaymentStatus status) {
        return status != null ? payment.status.eq(status) : null;
    }

    private BooleanExpression orderIdContains(String orderId) {
        return StringUtils.hasText(orderId) ? payment.orderId.contains(orderId) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return null;
        }
        if (start != null && end != null) {
            return payment.createdAt.between(start, end);
        }
        if (start != null) {
            return payment.createdAt.goe(start);
        }
        return payment.createdAt.loe(end);
    }
}
