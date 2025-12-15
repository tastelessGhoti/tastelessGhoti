package com.paygate.payment.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * 멱등성 키 저장소.
 * 동일한 요청의 중복 처리를 방지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyKeyStore {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "payment:idempotency:";

    @Value("${payment.idempotency.ttl-hours:24}")
    private int ttlHours;

    /**
     * 멱등성 키 저장.
     * 이미 존재하는 경우 false 반환.
     */
    public boolean saveIfAbsent(String key, String transactionId) {
        String redisKey = KEY_PREFIX + key;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, transactionId, Duration.ofHours(ttlHours));

        if (Boolean.TRUE.equals(success)) {
            log.debug("멱등성 키 저장 성공 - key: {}", key);
            return true;
        }

        log.info("멱등성 키 이미 존재 - key: {}", key);
        return false;
    }

    /**
     * 멱등성 키로 저장된 트랜잭션 ID 조회.
     */
    public Optional<String> get(String key) {
        String redisKey = KEY_PREFIX + key;
        Object value = redisTemplate.opsForValue().get(redisKey);
        return Optional.ofNullable(value).map(Object::toString);
    }

    /**
     * 멱등성 키 삭제.
     * 트랜잭션 실패 시 재시도를 위해 키 삭제.
     */
    public void delete(String key) {
        String redisKey = KEY_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.debug("멱등성 키 삭제 - key: {}", key);
    }
}
