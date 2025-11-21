package com.chatsearch.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 채팅 검색 배치 애플리케이션
 *
 * <p>주요 배치 작업:
 * <ul>
 *   <li>RDB → ElasticSearch 데이터 마이그레이션</li>
 *   <li>인덱스 재생성 (월별)</li>
 *   <li>오래된 데이터 아카이빙</li>
 *   <li>검색 인덱스 최적화</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.chatsearch")
@EnableBatchProcessing
@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.chatsearch.domain.repository")
@EnableElasticsearchRepositories(basePackages = "com.chatsearch.domain.repository")
public class ChatSearchBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatSearchBatchApplication.class, args);
    }
}
