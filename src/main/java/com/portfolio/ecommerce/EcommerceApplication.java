package com.portfolio.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * E-Commerce API 애플리케이션 메인 클래스
 *
 * @author Ghoti
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing  // JPA Auditing 활성화 (생성일시, 수정일시 자동 관리)
@EnableCaching      // 캐싱 활성화
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
