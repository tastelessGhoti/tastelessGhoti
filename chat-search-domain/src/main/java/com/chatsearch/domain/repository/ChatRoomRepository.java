package com.chatsearch.domain.repository;

import com.chatsearch.domain.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅방 리포지토리
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 소유자 ID로 채팅방 목록 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.ownerId = :ownerId AND cr.status = 'ACTIVE' ORDER BY cr.lastMessageAt DESC")
    Page<ChatRoom> findActiveRoomsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * 채팅방 유형별 조회
     */
    List<ChatRoom> findByRoomTypeAndStatus(ChatRoom.RoomType roomType, ChatRoom.RoomStatus status);

    /**
     * 최근 활동이 있는 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.status = 'ACTIVE' ORDER BY cr.lastMessageAt DESC")
    Page<ChatRoom> findRecentActiveRooms(Pageable pageable);
}
