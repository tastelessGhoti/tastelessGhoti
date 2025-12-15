package com.paygate.payment.infrastructure.van;

import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.VanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NICE VAN사 연동 클라이언트 구현체.
 * 실제 운영환경에서는 NICE API와 HTTP 통신을 수행.
 * 현재는 테스트를 위한 Mock 구현.
 */
@Slf4j
@Component
public class NiceVanClient implements VanClient {

    private static final String VAN_TYPE = "NICE";

    // 테스트용 카드사 매핑 (BIN 번호 기준)
    private static final Map<String, String> CARD_COMPANY_MAP = Map.of(
            "4", "비자",
            "5", "마스터",
            "9", "삼성카드",
            "3", "현대카드"
    );

    @Value("${van.nice.base-url:https://api.nicepay.co.kr}")
    private String baseUrl;

    @Value("${van.nice.timeout-seconds:30}")
    private int timeoutSeconds;

    // 테스트용 실패 카드번호
    private static final Map<String, String> FAIL_CARDS = new ConcurrentHashMap<>(Map.of(
            "4111111111111111", "잔액부족",
            "5500000000000004", "유효기간만료",
            "9000000000000001", "분실카드"
    ));

    @Override
    public VanApprovalResponse approve(VanApprovalRequest request) {
        log.info("NICE VAN 승인 요청 - orderId: {}, amount: {}",
                request.getOrderId(), request.getAmount());

        // 실패 케이스 시뮬레이션
        String failReason = FAIL_CARDS.get(request.getCardNumber());
        if (failReason != null) {
            log.warn("NICE VAN 승인 실패 - reason: {}", failReason);
            return VanApprovalResponse.fail("1001", failReason);
        }

        // 카드사 판별
        String cardCompany = resolveCardCompany(request.getCardNumber());

        String vanTxId = "NICE" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String approvalNo = generateApprovalNumber();

        log.info("NICE VAN 승인 완료 - vanTxId: {}, approvalNo: {}", vanTxId, approvalNo);

        return VanApprovalResponse.success(vanTxId, approvalNo, cardCompany);
    }

    @Override
    public VanCancelResponse cancel(VanCancelRequest request) {
        log.info("NICE VAN 취소 요청 - vanTxId: {}, amount: {}",
                request.getVanTransactionId(), request.getCancelAmount());

        if (request.getVanTransactionId() == null) {
            throw new VanException(ErrorCode.VAN_RESPONSE_ERROR, "9999", "원거래 정보 없음");
        }

        String vanCancelId = "NICEC" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);

        log.info("NICE VAN 취소 완료 - vanCancelId: {}", vanCancelId);

        return VanCancelResponse.success(vanCancelId);
    }

    @Override
    public String getVanType() {
        return VAN_TYPE;
    }

    private String resolveCardCompany(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "기타";
        }
        String firstDigit = cardNumber.substring(0, 1);
        return CARD_COMPANY_MAP.getOrDefault(firstDigit, "기타");
    }

    private String generateApprovalNumber() {
        return String.format("%08d", (int) (Math.random() * 100000000));
    }
}
