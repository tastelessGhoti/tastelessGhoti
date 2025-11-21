package com.chatsearch.indexer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer 설정
 *
 * <p>대용량 메시지 처리를 위한 최적화:
 * <ul>
 *   <li>동시성 설정: 메시지 처리 성능 향상</li>
 *   <li>배치 리스너: 여러 메시지 일괄 처리</li>
 *   <li>Auto Offset Commit: 자동 커밋으로 안정성 확보</li>
 * </ul>
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:chat-search-indexer}")
    private String groupId;

    /**
     * Kafka Consumer Factory 생성
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Kafka 브로커 주소
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer Group ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Key/Value Deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Auto Offset Reset: earliest (처음부터), latest (최신만)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Auto Commit 설정
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // 성능 최적화
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // 한 번에 가져올 레코드 수
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024 * 100); // 최소 페치 크기 100KB
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // 최대 대기 시간 500ms

        // JSON 신뢰 패키지 설정
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chatsearch.*");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory
     * 동시성 처리를 위한 설정
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 동시성 레벨: 3개의 스레드로 메시지 처리
        factory.setConcurrency(3);

        // 배치 리스너 활성화
        factory.setBatchListener(false);

        return factory;
    }

    /**
     * 배치 처리용 Listener Container Factory
     * 대량 메시지를 한 번에 처리
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(2);
        factory.setBatchListener(true); // 배치 리스너 활성화

        return factory;
    }
}
