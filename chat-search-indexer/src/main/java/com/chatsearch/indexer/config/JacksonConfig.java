package com.chatsearch.indexer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper 설정
 *
 * <p>Kafka 메시지 직렬화/역직렬화에 사용
 * LocalDateTime 등의 Java 8 시간 타입 지원
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 Time API 지원
        mapper.registerModule(new JavaTimeModule());

        // Timestamp 대신 ISO-8601 형식 사용
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
