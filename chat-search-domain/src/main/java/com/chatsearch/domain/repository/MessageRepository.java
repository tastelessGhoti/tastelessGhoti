package com.chatsearch.domain.repository;

import com.chatsearch.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 리포지토리
 *
 * <p>주의: 실제 프로덕션 환경에서는 샤딩된 테이블에 직접 접근하는
 * 커스텀 리포지토리 구현이 필요합니다.
 * 예: MessageShardRepositoryImpl
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 채팅방별 메시지 조회 (페이징)
     */
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * 특정 기간의 메시지 조회 (배치 처리용)
     */
    @Query("SELECT m FROM Message m WHERE m.createdAt >= :startDate AND m.createdAt < :endDate")
    List<Message> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 발신자별 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findBySenderId(@Param("senderId") Long senderId, Pageable pageable);

    /**
     * 특정 채팅방의 최근 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findRecentMessagesByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * 메시지 통계 - 채팅방별 메시지 수
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId AND m.isDeleted = false")
    long countByRoomId(@Param("roomId") Long roomId);
}
