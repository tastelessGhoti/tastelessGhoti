package com.paygate.payment.config;

import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 스케줄러 설정.
 * 정기적인 배치 작업 및 타임아웃 처리.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final PaymentRepository paymentRepository;

    /**
     * 대기 상태로 방치된 결제 건 타임아웃 처리.
     * 5분 이상 대기 상태인 건은 실패 처리.
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void handlePendingPaymentTimeout() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);

        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, timeoutThreshold);

        if (pendingPayments.isEmpty()) return;

        log.info("타임아웃 처리 대상: {}건", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            payment.fail("결제 처리 시간 초과");
            log.warn("결제 타임아웃 처리 - txId: {}", payment.getTransactionId());
        }
    }
}
