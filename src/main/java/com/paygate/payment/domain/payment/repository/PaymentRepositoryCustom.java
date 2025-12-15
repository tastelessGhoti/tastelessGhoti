package com.paygate.payment.domain.payment.repository;

import com.paygate.payment.domain.payment.dto.PaymentSearchCondition;
import com.paygate.payment.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryCustom {

    Page<Payment> searchPayments(PaymentSearchCondition condition, Pageable pageable);
}
