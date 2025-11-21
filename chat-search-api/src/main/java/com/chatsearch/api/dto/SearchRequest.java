package com.chatsearch.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 검색 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "메시지 검색 요청")
public class SearchRequest {

    @Schema(description = "검색 키워드", example = "안녕하세요")
    @NotBlank(message = "검색 키워드는 필수입니다")
    private String keyword;

    @Schema(description = "채팅방 ID (옵션)", example = "1")
    private Long roomId;

    @Schema(description = "발신자 ID (옵션)", example = "1")
    private Long senderId;

    @Schema(description = "검색 시작 날짜 (옵션)", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "검색 종료 날짜 (옵션)", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private int size = 20;

    @Schema(description = "메시지 유형 필터 (옵션)", example = "TEXT")
    private String messageType;

    @Schema(description = "하이라이팅 여부", example = "true")
    private boolean enableHighlight = false;
}
