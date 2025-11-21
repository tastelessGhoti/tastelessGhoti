package com.chatsearch.domain.repository;

import com.chatsearch.domain.document.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ElasticSearch 메시지 검색 리포지토리
 *
 * <p>고급 검색 기능:
 * <ul>
 *   <li>전문 검색 (Full-Text Search)</li>
 *   <li>형태소 분석 기반 한글 검색</li>
 *   <li>복합 조건 검색</li>
 *   <li>하이라이팅</li>
 * </ul>
 */
@Repository
public interface MessageSearchRepository extends ElasticsearchRepository<MessageDocument, String> {

    /**
     * 메시지 내용 검색 (형태소 분석)
     */
    Page<MessageDocument> findByContentAndIsDeletedFalse(String content, Pageable pageable);

    /**
     * 채팅방별 메시지 검색
     */
    Page<MessageDocument> findByRoomIdAndContentAndIsDeletedFalse(
        Long roomId,
        String content,
        Pageable pageable
    );

    /**
     * 발신자별 메시지 검색
     */
    Page<MessageDocument> findBySenderIdAndContentAndIsDeletedFalse(
        Long senderId,
        String content,
        Pageable pageable
    );

    /**
     * 기간별 메시지 검색
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"content\": \"?0\"}}, {\"range\": {\"created_at\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}], \"must_not\": [{\"term\": {\"is_deleted\": true}}]}}")
    Page<MessageDocument> findByContentAndCreatedAtBetween(
        String content,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * 채팅방 내 기간별 메시지 조회
     */
    List<MessageDocument> findByRoomIdAndCreatedAtBetweenAndIsDeletedFalse(
        Long roomId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * 발신자명으로 검색
     */
    Page<MessageDocument> findBySenderDisplayNameAndIsDeletedFalse(
        String senderDisplayName,
        Pageable pageable
    );
}
