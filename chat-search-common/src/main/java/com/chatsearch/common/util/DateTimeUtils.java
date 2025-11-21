package com.chatsearch.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 날짜/시간 관련 유틸리티 클래스
 * 대용량 메시지 데이터의 시간 기반 샤딩 및 파티셔닝에 활용
 */
public class DateTimeUtils {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 현재 시간 조회 (KST 기준)
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(KST);
    }

    /**
     * 시간 기반 샤딩 키 생성 (년월 기준)
     * 예: 2024년 1월 -> "202401"
     *
     * @param dateTime 대상 시간
     * @return 샤딩 키 (YYYYMM 형식)
     */
    public static String getMonthShardKey(LocalDateTime dateTime) {
        return dateTime.format(MONTH_FORMATTER);
    }

    /**
     * 현재 시간 기준 샤딩 키 생성
     */
    public static String getCurrentMonthShardKey() {
        return getMonthShardKey(now());
    }

    /**
     * 특정 개월 수 만큼 이전 시간 조회
     *
     * @param months 이전 개월 수
     * @return 이전 시간
     */
    public static LocalDateTime minusMonths(int months) {
        return now().minusMonths(months);
    }

    /**
     * Unix timestamp를 LocalDateTime으로 변환
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            KST
        );
    }

    /**
     * LocalDateTime을 Unix timestamp로 변환
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(KST).toInstant().toEpochMilli();
    }
}
