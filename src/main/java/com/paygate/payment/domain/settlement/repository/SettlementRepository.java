package com.paygate.payment.domain.settlement.repository;

import com.paygate.payment.domain.settlement.entity.Settlement;
import com.paygate.payment.domain.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByMerchantIdAndSettlementDate(String merchantId, LocalDate settlementDate);

    List<Settlement> findByMerchantIdAndSettlementDateBetween(
            String merchantId, LocalDate startDate, LocalDate endDate);

    List<Settlement> findByStatusAndSettlementDateBefore(SettlementStatus status, LocalDate date);

    boolean existsByMerchantIdAndSettlementDate(String merchantId, LocalDate settlementDate);
}
