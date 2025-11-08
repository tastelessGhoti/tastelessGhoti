package com.portfolio.ecommerce.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 캐시 설정
 * 도메인별 캐시 TTL 및 전략 정의
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 캐시 매니저 설정
     * 도메인별로 다른 TTL 적용
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // 기본 TTL 10분
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            )
            .disableCachingNullValues();  // null 값은 캐싱하지 않음

        // 도메인별 캐시 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 상품 캐시: 30분 (상품 정보는 자주 변경되지 않음)
        cacheConfigurations.put("products",
            defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 사용자 캐시: 5분 (사용자 정보는 자주 조회되지만 실시간성 중요)
        cacheConfigurations.put("users",
            defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 주문 캐시: 3분 (주문은 실시간성이 매우 중요)
        cacheConfigurations.put("orders",
            defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // 상품 목록 캐시: 10분
        cacheConfigurations.put("productList",
            defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()  // 트랜잭션 지원
            .build();
    }
}
