package com.portfolio.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.portfolio.ecommerce.domain")
public class JpaConfig {
}
