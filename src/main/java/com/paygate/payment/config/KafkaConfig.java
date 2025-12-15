package com.paygate.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConfig {

    public static final String PAYMENT_APPROVED_TOPIC = "payment.approved";
    public static final String PAYMENT_CANCELED_TOPIC = "payment.canceled";
    public static final String PAYMENT_SETTLEMENT_TOPIC = "payment.settlement";

    @Bean
    public NewTopic paymentApprovedTopic() {
        return TopicBuilder.name(PAYMENT_APPROVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCanceledTopic() {
        return TopicBuilder.name(PAYMENT_CANCELED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentSettlementTopic() {
        return TopicBuilder.name(PAYMENT_SETTLEMENT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public RecordMessageConverter messageConverter() {
        return new JsonMessageConverter();
    }
}
