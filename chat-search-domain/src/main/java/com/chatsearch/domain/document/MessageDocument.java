package com.chatsearch.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * ElasticSearch 메시지 문서
 *
 * <p>인덱스 전략:
 * <ul>
 *   <li>월별 인덱스 생성: message-202401, message-202402 ...</li>
 *   <li>샤드 수: 3 (기본)</li>
 *   <li>레플리카 수: 1 (고가용성)</li>
 * </ul>
 *
 * <p>한글 형태소 분석기 (nori) 활용:
 * <ul>
 *   <li>content 필드: 한글 자연어 검색 지원</li>
 *   <li>자동완성, 초성 검색 등 고급 검색 기능 구현 가능</li>
 * </ul>
 */
@Document(indexName = "message-#{T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMM'))}")
@Setting(settingPath = "elasticsearch/message-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDocument {

    @Id
    private String id; // ElasticSearch document ID (message.id + timestamp)

    @Field(type = FieldType.Long, name = "message_id")
    private Long messageId; // RDB 메시지 ID

    @Field(type = FieldType.Long, name = "room_id")
    private Long roomId;

    @Field(type = FieldType.Keyword, name = "room_name")
    private String roomName;

    @Field(type = FieldType.Long, name = "sender_id")
    private Long senderId;

    @Field(type = FieldType.Keyword, name = "sender_username")
    private String senderUsername;

    @Field(type = FieldType.Keyword, name = "sender_display_name")
    private String senderDisplayName;

    /**
     * 메시지 본문 - 한글 형태소 분석
     * nori_analyzer를 사용하여 "안녕하세요" -> ["안녕", "하", "세요"] 분석
     */
    @Field(type = FieldType.Text, name = "content", analyzer = "nori_analyzer")
    private String content;

    @Field(type = FieldType.Keyword, name = "message_type")
    private String messageType;

    @Field(type = FieldType.Keyword, name = "file_url")
    private String fileUrl;

    @Field(type = FieldType.Keyword, name = "file_name")
    private String fileName;

    @Field(type = FieldType.Long, name = "file_size")
    private Long fileSize;

    @Field(type = FieldType.Boolean, name = "is_deleted")
    @Setter
    private Boolean isDeleted;

    @Field(type = FieldType.Date, name = "created_at", format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, name = "updated_at", format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Long, name = "parent_message_id")
    private Long parentMessageId;

    /**
     * 검색 성능 향상을 위한 추가 필드
     */
    @Field(type = FieldType.Keyword, name = "shard_key")
    @Setter
    private String shardKey; // 샤딩 키 (예: "5_202401")

    /**
     * 메시지 내용 요약 (첫 100자)
     * 검색 결과 프리뷰용
     */
    public String getContentPreview() {
        if (content == null) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
