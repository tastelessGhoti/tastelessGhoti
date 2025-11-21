package com.chatsearch.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Message 엔티티 테스트
 *
 * <p>도메인 로직 검증:
 * <ul>
 *   <li>메시지 삭제 (소프트 삭제)</li>
 *   <li>엔티티 빌더 패턴</li>
 *   <li>비즈니스 규칙</li>
 * </ul>
 */
@DisplayName("메시지 엔티티 테스트")
class MessageTest {

    @Test
    @DisplayName("메시지 생성 - Builder 패턴")
    void testMessageCreation() {
        // Given & When
        Message message = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content("테스트 메시지")
            .messageType(Message.MessageType.TEXT)
            .build();

        // Then
        assertNotNull(message);
        assertEquals(1L, message.getRoomId());
        assertEquals(1L, message.getSenderId());
        assertEquals("테스트 메시지", message.getContent());
        assertEquals(Message.MessageType.TEXT, message.getMessageType());
        assertFalse(message.getIsDeleted());
    }

    @Test
    @DisplayName("메시지 소프트 삭제")
    void testMessageSoftDelete() {
        // Given
        Message message = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content("삭제할 메시지")
            .messageType(Message.MessageType.TEXT)
            .build();

        assertFalse(message.getIsDeleted());
        assertNull(message.getDeletedAt());

        // When
        message.delete();

        // Then
        assertTrue(message.getIsDeleted());
        assertNotNull(message.getDeletedAt());
        assertEquals("삭제된 메시지입니다.", message.getContent());
    }

    @Test
    @DisplayName("파일 메시지 생성")
    void testFileMessageCreation() {
        // Given & When
        Message message = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content("파일 업로드")
            .messageType(Message.MessageType.FILE)
            .fileUrl("https://example.com/file.pdf")
            .fileName("document.pdf")
            .fileSize(1024000L)
            .build();

        // Then
        assertNotNull(message);
        assertEquals(Message.MessageType.FILE, message.getMessageType());
        assertEquals("https://example.com/file.pdf", message.getFileUrl());
        assertEquals("document.pdf", message.getFileName());
        assertEquals(1024000L, message.getFileSize());
    }

    @Test
    @DisplayName("답장 메시지 생성")
    void testReplyMessageCreation() {
        // Given & When
        Message originalMessage = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content("원본 메시지")
            .messageType(Message.MessageType.TEXT)
            .build();

        // 원본 메시지에 대한 답장
        Message replyMessage = Message.builder()
            .roomId(1L)
            .senderId(2L)
            .content("답장 메시지")
            .messageType(Message.MessageType.TEXT)
            .parentMessageId(1L) // 원본 메시지 ID
            .build();

        // Then
        assertNotNull(replyMessage.getParentMessageId());
        assertEquals(1L, replyMessage.getParentMessageId());
    }

    @Test
    @DisplayName("이미지 메시지 생성")
    void testImageMessageCreation() {
        // Given & When
        Message message = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content("이미지 전송")
            .messageType(Message.MessageType.IMAGE)
            .fileUrl("https://example.com/image.jpg")
            .fileName("photo.jpg")
            .fileSize(2048000L)
            .build();

        // Then
        assertEquals(Message.MessageType.IMAGE, message.getMessageType());
        assertNotNull(message.getFileUrl());
    }

    @Test
    @DisplayName("시스템 메시지 생성")
    void testSystemMessageCreation() {
        // Given & When
        Message message = Message.builder()
            .roomId(1L)
            .senderId(0L) // 시스템 사용자
            .content("사용자가 채팅방에 입장했습니다.")
            .messageType(Message.MessageType.SYSTEM)
            .build();

        // Then
        assertEquals(Message.MessageType.SYSTEM, message.getMessageType());
        assertEquals("사용자가 채팅방에 입장했습니다.", message.getContent());
    }

    @Test
    @DisplayName("삭제된 메시지는 내용이 변경됨")
    void testDeletedMessageContentChange() {
        // Given
        String originalContent = "중요한 메시지";
        Message message = Message.builder()
            .roomId(1L)
            .senderId(1L)
            .content(originalContent)
            .messageType(Message.MessageType.TEXT)
            .build();

        // When
        message.delete();

        // Then
        assertNotEquals(originalContent, message.getContent());
        assertEquals("삭제된 메시지입니다.", message.getContent());
    }
}
