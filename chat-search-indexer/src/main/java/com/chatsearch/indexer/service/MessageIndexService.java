package com.chatsearch.indexer.service;

import com.chatsearch.common.util.DateTimeUtils;
import com.chatsearch.common.util.ShardingUtils;
import com.chatsearch.domain.document.MessageDocument;
import com.chatsearch.domain.repository.MessageSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 메시지 인덱싱 서비스
 *
 * <p>ElasticSearch에 메시지를 인덱싱하고 관리하는 핵심 서비스
 * <ul>
 *   <li>단건 인덱싱</li>
 *   <li>배치 인덱싱 (대량 데이터)</li>
 *   <li>인덱스 갱신 및 삭제</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageIndexService {

    private final MessageSearchRepository messageSearchRepository;

    /**
     * 단건 메시지 인덱싱
     *
     * @param message 인덱싱할 메시지
     */
    public void indexMessage(MessageDocument message) {
        try {
            // 샤드 키 설정 (사용자 ID + 월별)
            int shardNumber = ShardingUtils.getShardNumber(message.getSenderId());
            String monthKey = DateTimeUtils.getMonthShardKey(message.getCreatedAt());
            message.setShardKey(shardNumber + "_" + monthKey);

            // ElasticSearch에 저장
            messageSearchRepository.save(message);

            log.info("메시지 인덱싱 완료 - ID: {}, RoomID: {}, ShardKey: {}",
                message.getMessageId(), message.getRoomId(), message.getShardKey());

        } catch (Exception e) {
            log.error("메시지 인덱싱 실패 - MessageID: {}, Error: {}",
                message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("메시지 인덱싱 중 오류 발생", e);
        }
    }

    /**
     * 배치 메시지 인덱싱
     * 대량의 메시지를 한 번에 인덱싱 (성능 최적화)
     *
     * @param messages 인덱싱할 메시지 목록
     */
    public void indexMessages(List<MessageDocument> messages) {
        try {
            // 각 메시지에 샤드 키 설정
            messages.forEach(message -> {
                int shardNumber = ShardingUtils.getShardNumber(message.getSenderId());
                String monthKey = DateTimeUtils.getMonthShardKey(message.getCreatedAt());
                message.setShardKey(shardNumber + "_" + monthKey);
            });

            // 배치 저장
            messageSearchRepository.saveAll(messages);

            log.info("배치 메시지 인덱싱 완료 - 건수: {}", messages.size());

        } catch (Exception e) {
            log.error("배치 메시지 인덱싱 실패 - 건수: {}, Error: {}",
                messages.size(), e.getMessage(), e);
            throw new RuntimeException("배치 인덱싱 중 오류 발생", e);
        }
    }

    /**
     * 메시지 삭제 처리
     * 실제 삭제가 아닌 is_deleted 플래그 업데이트
     *
     * @param messageId 메시지 ID
     */
    public void deleteMessage(String messageId) {
        try {
            messageSearchRepository.findById(messageId).ifPresent(message -> {
                message.setIsDeleted(true);
                messageSearchRepository.save(message);
                log.info("메시지 삭제 처리 완료 - ID: {}", messageId);
            });
        } catch (Exception e) {
            log.error("메시지 삭제 처리 실패 - ID: {}, Error: {}",
                messageId, e.getMessage(), e);
        }
    }

    /**
     * 메시지 완전 삭제 (물리 삭제)
     *
     * @param messageId 메시지 ID
     */
    public void hardDeleteMessage(String messageId) {
        try {
            messageSearchRepository.deleteById(messageId);
            log.info("메시지 물리 삭제 완료 - ID: {}", messageId);
        } catch (Exception e) {
            log.error("메시지 물리 삭제 실패 - ID: {}, Error: {}",
                messageId, e.getMessage(), e);
        }
    }
}
