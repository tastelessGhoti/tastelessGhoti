package com.portfolio.ecommerce.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 결제 리포지토리
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID로 결제 정보 조회
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * 거래 ID로 결제 정보 조회
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * 거래 ID 중복 확인
     */
    boolean existsByTransactionId(String transactionId);
}
