package com.chatsearch.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 엔티티
 *
 * <p>샤딩 전략:
 * <ul>
 *   <li>1차 샤딩: 사용자 ID 기반 (16개 샤드)</li>
 *   <li>2차 파티셔닝: 월별 테이블 분할 (YYYYMM)</li>
 * </ul>
 *
 * <p>예시 테이블명: message_5_202401
 * <ul>
 *   <li>message: 기본 테이블명</li>
 *   <li>5: 샤드 번호 (userId % 16)</li>
 *   <li>202401: 2024년 1월</li>
 * </ul>
 *
 * <p>이 구조를 통해 대용량 메시지 데이터를 효율적으로 분산 저장하고,
 * 검색 성능을 최적화할 수 있습니다.
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_msg_room_created", columnList = "room_id, created_at"),
    @Index(name = "idx_msg_sender_created", columnList = "sender_id, created_at"),
    @Index(name = "idx_msg_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @Column(name = "parent_message_id")
    private Long parentMessageId; // 답장 기능용

    @Builder
    public Message(Long roomId, Long senderId, String content, MessageType messageType,
                   String fileUrl, String fileName, Long fileSize, Long parentMessageId) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType != null ? messageType : MessageType.TEXT;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.parentMessageId = parentMessageId;
    }

    /**
     * 메시지 삭제 (소프트 삭제)
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = java.time.LocalDateTime.now();
        this.content = "삭제된 메시지입니다.";
    }

    /**
     * 메시지 유형
     */
    public enum MessageType {
        TEXT,       // 텍스트
        IMAGE,      // 이미지
        FILE,       // 파일
        VIDEO,      // 비디오
        AUDIO,      // 오디오
        SYSTEM      // 시스템 메시지
    }
}
