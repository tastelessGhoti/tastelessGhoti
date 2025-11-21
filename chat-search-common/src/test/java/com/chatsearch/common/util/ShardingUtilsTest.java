package com.chatsearch.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShardingUtils 테스트
 *
 * <p>샤딩 로직의 정확성을 검증:
 * <ul>
 *   <li>동일한 입력에 대해 항상 동일한 샤드 번호 반환</li>
 *   <li>균등 분산 확인</li>
 *   <li>경계값 테스트</li>
 * </ul>
 */
@DisplayName("샤딩 유틸리티 테스트")
class ShardingUtilsTest {

    @Test
    @DisplayName("동일한 사용자 ID는 항상 같은 샤드에 할당")
    void testConsistentSharding() {
        Long userId = 12345L;

        int shard1 = ShardingUtils.getShardNumber(userId);
        int shard2 = ShardingUtils.getShardNumber(userId);
        int shard3 = ShardingUtils.getShardNumber(userId);

        assertEquals(shard1, shard2);
        assertEquals(shard2, shard3);
    }

    @Test
    @DisplayName("샤드 번호는 0부터 shardCount-1 범위 내")
    void testShardNumberRange() {
        int shardCount = 16;

        for (long i = 1; i <= 1000; i++) {
            int shardNumber = ShardingUtils.getShardNumber(i, shardCount);

            assertTrue(shardNumber >= 0, "샤드 번호는 0 이상이어야 함");
            assertTrue(shardNumber < shardCount, "샤드 번호는 shardCount 미만이어야 함");
        }
    }

    @Test
    @DisplayName("문자열 키 기반 샤딩 일관성 테스트")
    void testStringKeySharding() {
        String key = "room_12345";

        int shard1 = ShardingUtils.getShardNumber(key);
        int shard2 = ShardingUtils.getShardNumber(key);

        assertEquals(shard1, shard2);
    }

    @Test
    @DisplayName("샤드 테이블명 생성 테스트")
    void testShardTableName() {
        String tableName = ShardingUtils.getShardTableName("message", 5, "202401");

        assertEquals("message_5_202401", tableName);
    }

    @Test
    @DisplayName("월별 키 없이 샤드 테이블명 생성")
    void testShardTableNameWithoutMonth() {
        String tableName = ShardingUtils.getShardTableName("message", 3, null);

        assertEquals("message_3", tableName);
    }

    @Test
    @DisplayName("ElasticSearch 인덱스명 생성 테스트")
    void testIndexName() {
        String indexName = ShardingUtils.getIndexName("message", "202401");

        assertEquals("message-202401", indexName);
    }

    @Test
    @DisplayName("null userId 예외 처리")
    void testNullUserIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShardingUtils.getShardNumber((Long) null);
        });
    }

    @Test
    @DisplayName("null 문자열 키 예외 처리")
    void testNullStringKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShardingUtils.getShardNumber((String) null);
        });
    }

    @Test
    @DisplayName("빈 문자열 키 예외 처리")
    void testEmptyStringKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShardingUtils.getShardNumber("");
        });
    }

    @Test
    @DisplayName("대량 데이터 균등 분산 검증")
    void testEvenDistribution() {
        int shardCount = 16;
        int totalUsers = 10000;
        int[] shardCounts = new int[shardCount];

        // 10,000명의 사용자를 샤드에 할당
        for (long userId = 1; userId <= totalUsers; userId++) {
            int shard = ShardingUtils.getShardNumber(userId, shardCount);
            shardCounts[shard]++;
        }

        // 각 샤드의 데이터 수 확인
        double average = (double) totalUsers / shardCount;
        double threshold = average * 0.3; // 30% 오차 허용

        for (int i = 0; i < shardCount; i++) {
            assertTrue(Math.abs(shardCounts[i] - average) < threshold,
                String.format("샤드 %d의 데이터 수(%d)가 평균(%f)과 너무 차이남",
                    i, shardCounts[i], average));
        }
    }
}
