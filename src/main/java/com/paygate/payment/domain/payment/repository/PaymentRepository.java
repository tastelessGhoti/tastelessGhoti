package com.paygate.payment.domain.payment.repository;

import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    Optional<Payment> findByTransactionId(String transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdWithLock(@Param("transactionId") String transactionId);

    boolean existsByMerchantIdAndOrderId(String merchantId, String orderId);

    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId AND p.orderId = :orderId")
    Optional<Payment> findByMerchantIdAndOrderId(
            @Param("merchantId") String merchantId,
            @Param("orderId") String orderId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
           "AND p.approvedAt >= :startDate AND p.approvedAt < :endDate " +
           "AND p.merchantId = :merchantId")
    List<Payment> findForSettlement(
            @Param("merchantId") String merchantId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
