package com.paygate.payment.integration.repository;

import com.paygate.payment.domain.payment.dto.PaymentSearchCondition;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentMethod;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.paygate.payment.config.JpaConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("PaymentRepository 테스트")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private String merchantId = "M_TEST_001";

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        for (int i = 1; i <= 10; i++) {
            Payment payment = Payment.builder()
                    .transactionId("TXN_TEST_" + String.format("%03d", i))
                    .merchantId(merchantId)
                    .orderId("ORD_" + String.format("%03d", i))
                    .amount(new BigDecimal(10000 * i))
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("941012345678901" + i)
                    .installmentMonths(0)
                    .build();

            if (i % 2 == 0) {
                payment.approve("APP" + i, "VAN" + i);
            }

            paymentRepository.save(payment);
        }
    }

    @Test
    @DisplayName("트랜잭션 ID로 결제를 조회한다")
    void 트랜잭션ID로_조회() {
        // when
        var result = paymentRepository.findByTransactionId("TXN_TEST_001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("ORD_001");
    }

    @Test
    @DisplayName("가맹점ID와 주문ID로 결제를 조회한다")
    void 가맹점ID_주문ID로_조회() {
        // when
        var result = paymentRepository.findByMerchantIdAndOrderId(merchantId, "ORD_005");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN_TEST_005");
    }

    @Test
    @DisplayName("중복 주문번호 존재 여부를 확인한다")
    void 중복_주문번호_확인() {
        // when
        boolean exists = paymentRepository.existsByMerchantIdAndOrderId(merchantId, "ORD_001");
        boolean notExists = paymentRepository.existsByMerchantIdAndOrderId(merchantId, "ORD_999");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("상태별로 결제를 조회한다")
    void 상태별_조회() {
        // when
        PaymentSearchCondition condition = PaymentSearchCondition.builder()
                .merchantId(merchantId)
                .status(PaymentStatus.APPROVED)
                .build();

        Page<Payment> result = paymentRepository.searchPayments(condition, PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).allMatch(p -> p.getStatus() == PaymentStatus.APPROVED);
        assertThat(result.getTotalElements()).isEqualTo(5); // 짝수 번호만 승인됨
    }

    @Test
    @DisplayName("기간별로 결제를 조회한다")
    void 기간별_조회() {
        // when
        PaymentSearchCondition condition = PaymentSearchCondition.builder()
                .merchantId(merchantId)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        Page<Payment> result = paymentRepository.searchPayments(condition, PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("주문번호 검색으로 결제를 조회한다")
    void 주문번호_검색() {
        // when
        PaymentSearchCondition condition = PaymentSearchCondition.builder()
                .merchantId(merchantId)
                .orderId("003")
                .build();

        Page<Payment> result = paymentRepository.searchPayments(condition, PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOrderId()).contains("003");
    }

    @Test
    @DisplayName("페이징이 정상 동작한다")
    void 페이징_테스트() {
        // when
        PaymentSearchCondition condition = PaymentSearchCondition.builder()
                .merchantId(merchantId)
                .build();

        Page<Payment> page1 = paymentRepository.searchPayments(condition, PageRequest.of(0, 3));
        Page<Payment> page2 = paymentRepository.searchPayments(condition, PageRequest.of(1, 3));

        // then
        assertThat(page1.getSize()).isEqualTo(3);
        assertThat(page1.getContent()).hasSize(3);
        assertThat(page2.getContent()).hasSize(3);
        assertThat(page1.getTotalPages()).isEqualTo(4);
    }

    @Test
    @DisplayName("특정 시간 이전의 PENDING 상태 결제를 조회한다")
    void 타임아웃_대상_조회() {
        // when
        List<Payment> result = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING,
                LocalDateTime.now().plusMinutes(1)
        );

        // then - PENDING 상태는 홀수 번호 5개
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(p -> p.getStatus() == PaymentStatus.PENDING);
    }
}
