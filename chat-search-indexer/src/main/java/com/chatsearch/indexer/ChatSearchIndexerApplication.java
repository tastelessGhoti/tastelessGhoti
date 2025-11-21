package com.chatsearch.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 채팅 메시지 인덱서 애플리케이션
 *
 * <p>역할:
 * <ul>
 *   <li>Kafka에서 메시지 이벤트 수신</li>
 *   <li>ElasticSearch에 메시지 인덱싱</li>
 *   <li>실시간 검색 인덱스 갱신</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.chatsearch")
@EnableKafka
@EnableElasticsearchRepositories(basePackages = "com.chatsearch.domain.repository")
public class ChatSearchIndexerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatSearchIndexerApplication.class, args);
    }
}
