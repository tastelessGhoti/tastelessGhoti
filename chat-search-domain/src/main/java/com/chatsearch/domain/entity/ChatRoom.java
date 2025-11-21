package com.chatsearch.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 엔티티
 * 1:1 채팅, 그룹 채팅 등을 관리
 */
@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_room_owner", columnList = "owner_id"),
    @Index(name = "idx_room_type", columnList = "room_type"),
    @Index(name = "idx_room_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(name = "last_message_at")
    private java.time.LocalDateTime lastMessageAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    @Builder
    public ChatRoom(String name, RoomType roomType, Long ownerId) {
        this.name = name;
        this.roomType = roomType;
        this.ownerId = ownerId;
        this.memberCount = 1; // 생성자 자신
    }

    /**
     * 멤버 추가
     */
    public void addMember() {
        this.memberCount++;
    }

    /**
     * 멤버 제거
     */
    public void removeMember() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }

    /**
     * 마지막 메시지 시간 업데이트
     */
    public void updateLastMessageAt(java.time.LocalDateTime messageTime) {
        this.lastMessageAt = messageTime;
    }

    /**
     * 채팅방 유형
     */
    public enum RoomType {
        DIRECT,     // 1:1 채팅
        GROUP,      // 그룹 채팅
        CHANNEL     // 채널 (공개 채팅방)
    }

    /**
     * 채팅방 상태
     */
    public enum RoomStatus {
        ACTIVE,     // 활성
        ARCHIVED,   // 보관됨
        DELETED     // 삭제됨
    }
}
