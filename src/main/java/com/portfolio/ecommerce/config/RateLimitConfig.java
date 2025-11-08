package com.portfolio.ecommerce.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting 설정 (Bucket4j 사용)
 * API 호출 횟수 제한을 통한 DDoS 공격 방지
 */
@Configuration
public class RateLimitConfig {

    /**
     * IP 주소별 Rate Limit 버킷 저장소
     * 실제 운영 환경에서는 Redis 기반 분산 캐시 사용 권장
     */
    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * 기본 Rate Limit 버킷 생성
     * 분당 100회, 초당 20회 제한
     */
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        Bandwidth burstLimit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofSeconds(1)));
        return Bucket.builder()
            .addLimit(limit)
            .addLimit(burstLimit)
            .build();
    }

    /**
     * 로그인 API Rate Limit 버킷
     * 보안을 위해 더 엄격한 제한 (분당 5회)
     */
    public Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
