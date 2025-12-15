package com.paygate.payment.domain.settlement.service;

import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 대사(Reconciliation) 서비스.
 * 내부 결제 데이터와 VAN사 데이터의 정합성 검증.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final PaymentRepository paymentRepository;

    /**
     * 일별 대사 수행.
     * VAN사로부터 수신한 데이터와 비교하여 불일치 건 탐지.
     */
    @Transactional(readOnly = true)
    public ReconciliationResult reconcile(LocalDate targetDate, List<VanSettlementData> vanDataList) {
        log.info("대사 시작 - date: {}, vanDataCount: {}", targetDate, vanDataList.size());

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // 내부 데이터 조회
        List<Payment> internalPayments = new ArrayList<>();
        internalPayments.addAll(paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.APPROVED, endOfDay));

        Map<String, VanSettlementData> vanDataMap = vanDataList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        VanSettlementData::getVanTransactionId,
                        v -> v,
                        (v1, v2) -> v1
                ));

        List<DiscrepancyRecord> discrepancies = new ArrayList<>();

        for (Payment payment : internalPayments) {
            if (payment.getVanTransactionId() == null) continue;

            VanSettlementData vanData = vanDataMap.get(payment.getVanTransactionId());

            if (vanData == null) {
                // VAN에 없는 건
                discrepancies.add(DiscrepancyRecord.builder()
                        .transactionId(payment.getTransactionId())
                        .vanTransactionId(payment.getVanTransactionId())
                        .type(DiscrepancyType.MISSING_IN_VAN)
                        .internalAmount(payment.getAmount())
                        .build());
                continue;
            }

            // 금액 불일치 체크
            if (payment.getAmount().compareTo(vanData.getAmount()) != 0) {
                discrepancies.add(DiscrepancyRecord.builder()
                        .transactionId(payment.getTransactionId())
                        .vanTransactionId(payment.getVanTransactionId())
                        .type(DiscrepancyType.AMOUNT_MISMATCH)
                        .internalAmount(payment.getAmount())
                        .vanAmount(vanData.getAmount())
                        .build());
            }

            // 상태 불일치 체크
            if (!isStatusMatched(payment.getStatus(), vanData.getStatus())) {
                discrepancies.add(DiscrepancyRecord.builder()
                        .transactionId(payment.getTransactionId())
                        .vanTransactionId(payment.getVanTransactionId())
                        .type(DiscrepancyType.STATUS_MISMATCH)
                        .internalStatus(payment.getStatus().name())
                        .vanStatus(vanData.getStatus())
                        .build());
            }

            vanDataMap.remove(payment.getVanTransactionId());
        }

        // 내부에 없는 VAN 건
        for (VanSettlementData remainingVan : vanDataMap.values()) {
            discrepancies.add(DiscrepancyRecord.builder()
                    .vanTransactionId(remainingVan.getVanTransactionId())
                    .type(DiscrepancyType.MISSING_IN_INTERNAL)
                    .vanAmount(remainingVan.getAmount())
                    .build());
        }

        ReconciliationResult result = ReconciliationResult.builder()
                .targetDate(targetDate)
                .totalInternalCount(internalPayments.size())
                .totalVanCount(vanDataList.size())
                .discrepancyCount(discrepancies.size())
                .discrepancies(discrepancies)
                .build();

        log.info("대사 완료 - date: {}, 불일치건수: {}", targetDate, discrepancies.size());

        return result;
    }

    private boolean isStatusMatched(PaymentStatus internal, String vanStatus) {
        return switch (internal) {
            case APPROVED -> "APPROVED".equals(vanStatus) || "00".equals(vanStatus);
            case CANCELED -> "CANCELED".equals(vanStatus) || "01".equals(vanStatus);
            case PARTIAL_CANCELED -> "PARTIAL_CANCELED".equals(vanStatus) || "02".equals(vanStatus);
            default -> false;
        };
    }

    /**
     * VAN사 정산 데이터 구조.
     */
    @lombok.Getter
    @lombok.Builder
    public static class VanSettlementData {
        private String vanTransactionId;
        private String approvalNumber;
        private BigDecimal amount;
        private String status;
        private LocalDateTime transactionDate;
    }

    /**
     * 대사 결과.
     */
    @lombok.Getter
    @lombok.Builder
    public static class ReconciliationResult {
        private LocalDate targetDate;
        private int totalInternalCount;
        private int totalVanCount;
        private int discrepancyCount;
        private List<DiscrepancyRecord> discrepancies;

        public boolean hasDiscrepancy() {
            return discrepancyCount > 0;
        }
    }

    /**
     * 불일치 건 기록.
     */
    @lombok.Getter
    @lombok.Builder
    public static class DiscrepancyRecord {
        private String transactionId;
        private String vanTransactionId;
        private DiscrepancyType type;
        private BigDecimal internalAmount;
        private BigDecimal vanAmount;
        private String internalStatus;
        private String vanStatus;
    }

    public enum DiscrepancyType {
        MISSING_IN_VAN,      // 내부에는 있으나 VAN에 없음
        MISSING_IN_INTERNAL, // VAN에는 있으나 내부에 없음
        AMOUNT_MISMATCH,     // 금액 불일치
        STATUS_MISMATCH      // 상태 불일치
    }
}
