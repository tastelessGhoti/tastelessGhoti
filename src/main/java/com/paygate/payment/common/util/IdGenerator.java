package com.paygate.payment.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 결제 관련 고유 ID 생성기.
 * 시간 기반 + 시퀀스 조합으로 정렬 가능하고 충돌 방지.
 */
public final class IdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final long MAX_SEQUENCE = 9999L;

    private IdGenerator() {
    }

    /**
     * 결제 트랜잭션 ID 생성.
     * 형식: TXN + yyyyMMddHHmmss + 4자리 시퀀스
     */
    public static String generateTransactionId() {
        return "TXN" + generateTimestampWithSequence();
    }

    /**
     * 승인번호 생성.
     * 형식: yyyyMMddHHmmss + 4자리 시퀀스
     */
    public static String generateApprovalNumber() {
        return generateTimestampWithSequence();
    }

    private static String generateTimestampWithSequence() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long seq = SEQUENCE.updateAndGet(val -> (val >= MAX_SEQUENCE) ? 0 : val + 1);
        return timestamp + String.format("%04d", seq);
    }
}
