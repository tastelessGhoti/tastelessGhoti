package com.paygate.payment.domain.merchant.repository;

import com.paygate.payment.domain.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, String> {

    Optional<Merchant> findByApiKey(String apiKey);

    boolean existsByApiKey(String apiKey);
}
