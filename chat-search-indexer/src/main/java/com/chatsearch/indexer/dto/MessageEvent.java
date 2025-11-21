package com.chatsearch.indexer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka 메시지 이벤트 DTO
 *
 * <p>메시지 생성/수정/삭제 이벤트를 Kafka를 통해 전달
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEvent {

    /**
     * 이벤트 유형
     */
    private EventType eventType;

    /**
     * 메시지 정보
     */
    private Long messageId;
    private Long roomId;
    private String roomName;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String content;
    private String messageType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentMessageId;

    /**
     * 이벤트 유형
     */
    public enum EventType {
        CREATE,  // 메시지 생성
        UPDATE,  // 메시지 수정
        DELETE   // 메시지 삭제
    }
}
