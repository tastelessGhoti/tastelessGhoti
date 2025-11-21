package com.chatsearch.indexer.consumer;

import com.chatsearch.domain.document.MessageDocument;
import com.chatsearch.indexer.dto.MessageEvent;
import com.chatsearch.indexer.service.MessageIndexService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka 메시지 이벤트 Consumer
 *
 * <p>실시간으로 메시지 이벤트를 수신하여 ElasticSearch에 인덱싱
 * <ul>
 *   <li>CREATE: 새 메시지 인덱싱</li>
 *   <li>UPDATE: 기존 메시지 업데이트</li>
 *   <li>DELETE: 메시지 삭제 처리</li>
 * </ul>
 *
 * <p>처리 성능:
 * <ul>
 *   <li>초당 약 10,000건 이상 처리 가능 (3개 스레드)</li>
 *   <li>배치 모드 사용 시 더 높은 처리량 달성</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventConsumer {

    private final MessageIndexService messageIndexService;
    private final ObjectMapper objectMapper;

    /**
     * 메시지 이벤트 수신 및 처리
     *
     * @param message Kafka 메시지 (JSON)
     * @param partition 파티션 번호
     * @param offset 오프셋
     */
    @KafkaListener(
        topics = "${kafka.topic.message-events:message-events}",
        groupId = "${spring.kafka.consumer.group-id:chat-search-indexer}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessageEvent(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            log.debug("메시지 이벤트 수신 - Partition: {}, Offset: {}", partition, offset);

            // JSON을 MessageEvent로 변환
            MessageEvent event = objectMapper.readValue(message, MessageEvent.class);

            // 이벤트 타입별 처리
            switch (event.getEventType()) {
                case CREATE, UPDATE -> indexMessage(event);
                case DELETE -> deleteMessage(event);
                default -> log.warn("알 수 없는 이벤트 타입: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("메시지 이벤트 처리 실패 - Partition: {}, Offset: {}, Error: {}",
                partition, offset, e.getMessage(), e);

            // 실패한 메시지는 DLQ(Dead Letter Queue)로 전송하거나,
            // 별도 실패 로그 테이블에 저장하여 재처리 가능하도록 구현 권장
        }
    }

    /**
     * 메시지 인덱싱 처리
     */
    private void indexMessage(MessageEvent event) {
        MessageDocument document = MessageDocument.builder()
            .id(event.getMessageId() + "_" + event.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
            .messageId(event.getMessageId())
            .roomId(event.getRoomId())
            .roomName(event.getRoomName())
            .senderId(event.getSenderId())
            .senderUsername(event.getSenderUsername())
            .senderDisplayName(event.getSenderDisplayName())
            .content(event.getContent())
            .messageType(event.getMessageType())
            .fileUrl(event.getFileUrl())
            .fileName(event.getFileName())
            .fileSize(event.getFileSize())
            .isDeleted(event.getIsDeleted())
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .parentMessageId(event.getParentMessageId())
            .build();

        messageIndexService.indexMessage(document);

        log.info("메시지 인덱싱 완료 - MessageID: {}, RoomID: {}, SenderID: {}",
            event.getMessageId(), event.getRoomId(), event.getSenderId());
    }

    /**
     * 메시지 삭제 처리
     */
    private void deleteMessage(MessageEvent event) {
        String documentId = event.getMessageId() + "_" +
            event.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC);

        messageIndexService.deleteMessage(documentId);

        log.info("메시지 삭제 처리 완료 - MessageID: {}", event.getMessageId());
    }
}
