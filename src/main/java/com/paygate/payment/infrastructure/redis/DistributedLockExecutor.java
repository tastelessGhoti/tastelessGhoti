package com.paygate.payment.infrastructure.redis;

import com.paygate.payment.common.exception.ErrorCode;
import com.paygate.payment.common.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 분산 락 실행기.
 * Redisson을 활용한 분산 환경에서의 동시성 제어.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "payment:lock:";
    private static final long DEFAULT_WAIT_TIME = 5L;
    private static final long DEFAULT_LEASE_TIME = 10L;

    /**
     * 분산 락을 획득한 후 작업을 수행.
     * 락 획득 실패 시 PaymentException 발생.
     */
    public <T> T executeWithLock(String key, Supplier<T> task) {
        return executeWithLock(key, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, task);
    }

    public <T> T executeWithLock(String key, long waitTime, long leaseTime, Supplier<T> task) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("분산 락 획득 실패 - key: {}", key);
                throw new PaymentException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.debug("분산 락 획득 성공 - key: {}", key);
            return task.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException(ErrorCode.LOCK_ACQUISITION_FAILED, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("분산 락 해제 - key: {}", key);
            }
        }
    }

    public void executeWithLock(String key, Runnable task) {
        executeWithLock(key, () -> {
            task.run();
            return null;
        });
    }
}
