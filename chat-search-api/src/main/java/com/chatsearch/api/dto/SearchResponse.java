package com.chatsearch.api.dto;

import com.chatsearch.domain.document.MessageDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 메시지 검색 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "메시지 검색 응답")
public class SearchResponse {

    @Schema(description = "검색된 메시지 목록")
    private List<MessageDocument> messages;

    @Schema(description = "전체 결과 수", example = "1234")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "62")
    private int totalPages;

    @Schema(description = "현재 페이지 번호", example = "0")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "20")
    private int pageSize;

    @Schema(description = "검색 소요 시간 (ms)", example = "45")
    private Long searchTimeMs;
}
