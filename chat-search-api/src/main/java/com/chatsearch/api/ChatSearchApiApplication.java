package com.chatsearch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 채팅 검색 API 애플리케이션
 *
 * <p>주요 기능:
 * <ul>
 *   <li>메시지 검색 REST API</li>
 *   <li>사용자 인증/인가 (JWT)</li>
 *   <li>실시간 메시지 전송 (Kafka Producer)</li>
 *   <li>검색 결과 캐싱 (Redis)</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.chatsearch")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.chatsearch.domain.repository")
@EnableElasticsearchRepositories(basePackages = "com.chatsearch.domain.repository")
public class ChatSearchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatSearchApiApplication.class, args);
    }
}
