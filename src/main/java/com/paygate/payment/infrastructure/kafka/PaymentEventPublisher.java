package com.paygate.payment.infrastructure.kafka;

import com.paygate.payment.config.KafkaConfig;
import com.paygate.payment.domain.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 결제 이벤트 발행기.
 * 결제 상태 변경 시 Kafka로 이벤트 발행.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishApproved(PaymentEvent event) {
        publish(KafkaConfig.PAYMENT_APPROVED_TOPIC, event);
    }

    public void publishCanceled(PaymentEvent event) {
        publish(KafkaConfig.PAYMENT_CANCELED_TOPIC, event);
    }

    public void publishForSettlement(PaymentEvent event) {
        publish(KafkaConfig.PAYMENT_SETTLEMENT_TOPIC, event);
    }

    private void publish(String topic, PaymentEvent event) {
        CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(topic, event.getTransactionId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("이벤트 발행 실패 - topic: {}, txId: {}, error: {}",
                        topic, event.getTransactionId(), ex.getMessage());
            } else {
                log.info("이벤트 발행 성공 - topic: {}, txId: {}, partition: {}",
                        topic, event.getTransactionId(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}
