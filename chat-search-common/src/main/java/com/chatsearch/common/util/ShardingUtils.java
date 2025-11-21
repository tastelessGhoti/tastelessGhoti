package com.chatsearch.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 데이터 샤딩 유틸리티 클래스
 * 대용량 채팅 메시지를 효율적으로 분산 저장하기 위한 샤딩 로직 제공
 */
public class ShardingUtils {

    private static final int DEFAULT_SHARD_COUNT = 16;

    private ShardingUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 사용자 ID 기반 샤드 번호 계산
     * 특정 사용자의 메시지는 항상 같은 샤드에 저장되도록 보장
     *
     * @param userId 사용자 ID
     * @return 샤드 번호 (0 ~ shardCount-1)
     */
    public static int getShardNumber(Long userId) {
        return getShardNumber(userId, DEFAULT_SHARD_COUNT);
    }

    /**
     * 사용자 ID 기반 샤드 번호 계산 (샤드 개수 지정)
     *
     * @param userId 사용자 ID
     * @param shardCount 전체 샤드 개수
     * @return 샤드 번호
     */
    public static int getShardNumber(Long userId, int shardCount) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        // 일관성 있는 해싱을 위해 절대값 사용
        return Math.abs(userId.hashCode() % shardCount);
    }

    /**
     * 문자열 기반 샤드 번호 계산 (채팅방 ID 등)
     * MD5 해시를 활용한 균등 분산
     *
     * @param key 샤딩 키
     * @return 샤드 번호
     */
    public static int getShardNumber(String key) {
        return getShardNumber(key, DEFAULT_SHARD_COUNT);
    }

    /**
     * 문자열 기반 샤드 번호 계산 (샤드 개수 지정)
     *
     * @param key 샤딩 키
     * @param shardCount 전체 샤드 개수
     * @return 샤드 번호
     */
    public static int getShardNumber(String key, int shardCount) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));

            // 해시 값의 앞 4바이트를 정수로 변환
            int hashInt = ((hash[0] & 0xFF) << 24) |
                         ((hash[1] & 0xFF) << 16) |
                         ((hash[2] & 0xFF) << 8) |
                         (hash[3] & 0xFF);

            return Math.abs(hashInt % shardCount);
        } catch (NoSuchAlgorithmException e) {
            // MD5는 표준 알고리즘이므로 발생하지 않음
            throw new IllegalStateException("MD5 algorithm not found", e);
        }
    }

    /**
     * 샤드 테이블명 생성
     * 예: "message" + 5 + "202401" -> "message_5_202401"
     *
     * @param baseTableName 기본 테이블명
     * @param shardNumber 샤드 번호
     * @param monthKey 월별 키 (옵션)
     * @return 샤드 테이블명
     */
    public static String getShardTableName(String baseTableName, int shardNumber, String monthKey) {
        if (monthKey == null || monthKey.isEmpty()) {
            return String.format("%s_%d", baseTableName, shardNumber);
        }
        return String.format("%s_%d_%s", baseTableName, shardNumber, monthKey);
    }

    /**
     * ElasticSearch 인덱스명 생성
     * 예: "message" + "202401" -> "message-202401"
     *
     * @param baseIndexName 기본 인덱스명
     * @param monthKey 월별 키
     * @return 인덱스명
     */
    public static String getIndexName(String baseIndexName, String monthKey) {
        return String.format("%s-%s", baseIndexName, monthKey);
    }
}
