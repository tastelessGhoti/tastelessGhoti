package com.paygate.payment.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paygate.payment.domain.merchant.entity.Merchant;
import com.paygate.payment.domain.merchant.repository.MerchantRepository;
import com.paygate.payment.domain.payment.dto.PaymentApprovalRequest;
import com.paygate.payment.domain.payment.dto.PaymentCancelRequest;
import com.paygate.payment.domain.payment.entity.Payment;
import com.paygate.payment.domain.payment.entity.PaymentMethod;
import com.paygate.payment.domain.payment.entity.PaymentStatus;
import com.paygate.payment.domain.payment.repository.PaymentRepository;
import com.paygate.payment.fixture.PaymentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.paygate.payment.infrastructure.redis.DistributedLockExecutor;
import com.paygate.payment.infrastructure.redis.IdempotencyKeyStore;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("결제 API 통합 테스트")
class PaymentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private DistributedLockExecutor lockExecutor;

    @MockBean
    private IdempotencyKeyStore idempotencyKeyStore;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = merchantRepository.save(PaymentFixture.createActiveMerchant());

        // 분산 락 모킹
        given(lockExecutor.executeWithLock(any(String.class), any(Supplier.class)))
                .willAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
    }

    @Nested
    @DisplayName("POST /v1/payments/approve")
    class ApproveApiTest {

        @Test
        @DisplayName("유효한 요청으로 결제 승인에 성공한다")
        void 결제_승인_성공() throws Exception {
            // given
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId("ORD-TEST-001")
                    .amount(new BigDecimal("25000"))
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .expiryDate("1226")
                    .installmentMonths(0)
                    .productName("테스트 상품")
                    .buyerName("테스터")
                    .buyerEmail("test@test.com")
                    .buyerPhone("01011112222")
                    .build();

            // when
            ResultActions result = mockMvc.perform(post("/v1/payments/approve")
                    .header("X-Merchant-Id", merchant.getMerchantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"))
                    .andExpect(jsonPath("$.data.orderId").value("ORD-TEST-001"))
                    .andExpect(jsonPath("$.data.amount").value(25000))
                    .andExpect(jsonPath("$.data.cardNumber").value(startsWith("941012")));
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 에러를 반환한다")
        void 필수_필드_누락_실패() throws Exception {
            // given
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId("ORD-TEST-002")
                    // amount 누락
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .expiryDate("1226")
                    .build();

            // when
            ResultActions result = mockMvc.perform(post("/v1/payments/approve")
                    .header("X-Merchant-Id", merchant.getMerchantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("최소 금액 미만 시 400 에러를 반환한다")
        void 최소_금액_미만_실패() throws Exception {
            // given
            PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                    .orderId("ORD-TEST-003")
                    .amount(new BigDecimal("50")) // 최소 100원
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .expiryDate("1226")
                    .build();

            // when
            ResultActions result = mockMvc.perform(post("/v1/payments/approve")
                    .header("X-Merchant-Id", merchant.getMerchantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /v1/payments/cancel")
    class CancelApiTest {

        @Test
        @DisplayName("승인된 결제를 취소할 수 있다")
        void 결제_취소_성공() throws Exception {
            // given
            Payment payment = Payment.builder()
                    .transactionId("TXN20231201001")
                    .merchantId(merchant.getMerchantId())
                    .orderId("ORD-CANCEL-001")
                    .amount(new BigDecimal("30000"))
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .installmentMonths(0)
                    .build();
            payment.approve("12345678", "VAN123");
            paymentRepository.save(payment);

            PaymentCancelRequest request = PaymentCancelRequest.builder()
                    .transactionId("TXN20231201001")
                    .cancelReason("테스트 취소")
                    .build();

            // when
            ResultActions result = mockMvc.perform(post("/v1/payments/cancel")
                    .header("X-Merchant-Id", merchant.getMerchantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CANCELED"))
                    .andExpect(jsonPath("$.data.canceledAmount").value(30000));
        }

        @Test
        @DisplayName("부분 취소가 정상적으로 처리된다")
        void 부분_취소_성공() throws Exception {
            // given
            Payment payment = Payment.builder()
                    .transactionId("TXN20231201002")
                    .merchantId(merchant.getMerchantId())
                    .orderId("ORD-PARTIAL-001")
                    .amount(new BigDecimal("50000"))
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .installmentMonths(0)
                    .build();
            payment.approve("87654321", "VAN456");
            paymentRepository.save(payment);

            PaymentCancelRequest request = PaymentCancelRequest.builder()
                    .transactionId("TXN20231201002")
                    .cancelAmount(new BigDecimal("20000"))
                    .cancelReason("부분 환불")
                    .build();

            // when
            ResultActions result = mockMvc.perform(post("/v1/payments/cancel")
                    .header("X-Merchant-Id", merchant.getMerchantId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("PARTIAL_CANCELED"))
                    .andExpect(jsonPath("$.data.canceledAmount").value(20000))
                    .andExpect(jsonPath("$.data.remainingAmount").value(30000));
        }
    }

    @Nested
    @DisplayName("GET /v1/payments/{transactionId}")
    class GetPaymentDetailApiTest {

        @Test
        @DisplayName("결제 상세 정보를 조회할 수 있다")
        void 결제_상세_조회_성공() throws Exception {
            // given
            Payment payment = Payment.builder()
                    .transactionId("TXN20231201003")
                    .merchantId(merchant.getMerchantId())
                    .orderId("ORD-DETAIL-001")
                    .amount(new BigDecimal("15000"))
                    .paymentMethod(PaymentMethod.CARD)
                    .cardNumber("9410123456789012")
                    .installmentMonths(0)
                    .productName("조회 테스트 상품")
                    .build();
            payment.approve("11112222", "VAN789");
            paymentRepository.save(payment);

            // when
            ResultActions result = mockMvc.perform(get("/v1/payments/TXN20231201003")
                    .header("X-Merchant-Id", merchant.getMerchantId()));

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.transactionId").value("TXN20231201003"))
                    .andExpect(jsonPath("$.data.orderId").value("ORD-DETAIL-001"))
                    .andExpect(jsonPath("$.data.amount").value(15000))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 404 에러를 반환한다")
        void 존재하지_않는_결제_조회_실패() throws Exception {
            // when
            ResultActions result = mockMvc.perform(get("/v1/payments/INVALID_TX")
                    .header("X-Merchant-Id", merchant.getMerchantId()));

            // then
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
