package com.paygate.payment.domain.payment.repository;

import com.paygate.payment.domain.payment.entity.PaymentCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentCancelHistoryRepository extends JpaRepository<PaymentCancelHistory, Long> {

    List<PaymentCancelHistory> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    Optional<PaymentCancelHistory> findByCancelTransactionId(String cancelTransactionId);
}
