package com.chatsearch.api.controller;

import com.chatsearch.api.dto.SearchRequest;
import com.chatsearch.api.dto.SearchResponse;
import com.chatsearch.api.service.MessageSearchService;
import com.chatsearch.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 메시지 검색 REST API Controller
 *
 * <p>주요 엔드포인트:
 * <ul>
 *   <li>GET /api/v1/messages/search - 전체 메시지 검색</li>
 *   <li>GET /api/v1/messages/search/room/{roomId} - 채팅방별 검색</li>
 *   <li>GET /api/v1/messages/search/sender/{senderId} - 발신자별 검색</li>
 *   <li>POST /api/v1/messages/search/advanced - 복합 조건 검색</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message Search", description = "메시지 검색 API")
public class MessageSearchController {

    private final MessageSearchService messageSearchService;

    /**
     * 전체 메시지 검색
     */
    @Operation(summary = "전체 메시지 검색", description = "키워드로 전체 메시지를 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> searchMessages(
        @Parameter(description = "검색 키워드", example = "안녕하세요")
        @RequestParam String keyword,

        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        long startTime = System.currentTimeMillis();

        SearchResponse response = messageSearchService.searchMessages(keyword, page, size);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.info("전체 메시지 검색 API 호출 - 키워드: {}, 결과: {}건, 소요시간: {}ms",
            keyword, response.getTotalElements(), response.getSearchTimeMs());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 채팅방별 메시지 검색
     */
    @Operation(summary = "채팅방별 메시지 검색", description = "특정 채팅방 내에서 메시지를 검색합니다")
    @GetMapping("/search/room/{roomId}")
    public ResponseEntity<ApiResponse<SearchResponse>> searchMessagesByRoom(
        @Parameter(description = "채팅방 ID", example = "1")
        @PathVariable Long roomId,

        @Parameter(description = "검색 키워드", example = "회의")
        @RequestParam String keyword,

        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        long startTime = System.currentTimeMillis();

        SearchResponse response = messageSearchService.searchMessagesByRoom(roomId, keyword, page, size);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.info("채팅방별 메시지 검색 API 호출 - RoomID: {}, 키워드: {}, 결과: {}건",
            roomId, keyword, response.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 발신자별 메시지 검색
     */
    @Operation(summary = "발신자별 메시지 검색", description = "특정 사용자가 보낸 메시지를 검색합니다")
    @GetMapping("/search/sender/{senderId}")
    public ResponseEntity<ApiResponse<SearchResponse>> searchMessagesBySender(
        @Parameter(description = "발신자 ID", example = "1")
        @PathVariable Long senderId,

        @Parameter(description = "검색 키워드", example = "프로젝트")
        @RequestParam String keyword,

        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        long startTime = System.currentTimeMillis();

        SearchResponse response = messageSearchService.searchMessagesBySender(senderId, keyword, page, size);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.info("발신자별 메시지 검색 API 호출 - SenderID: {}, 키워드: {}, 결과: {}건",
            senderId, keyword, response.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기간별 메시지 검색
     */
    @Operation(summary = "기간별 메시지 검색", description = "특정 기간 내의 메시지를 검색합니다")
    @GetMapping("/search/date-range")
    public ResponseEntity<ApiResponse<SearchResponse>> searchMessagesByDateRange(
        @Parameter(description = "검색 키워드", example = "미팅")
        @RequestParam String keyword,

        @Parameter(description = "시작 날짜", example = "2024-01-01T00:00:00")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

        @Parameter(description = "종료 날짜", example = "2024-12-31T23:59:59")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        long startTime = System.currentTimeMillis();

        SearchResponse response = messageSearchService.searchMessagesByDateRange(
            keyword, startDate, endDate, page, size
        );
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.info("기간별 메시지 검색 API 호출 - 키워드: {}, 기간: {} ~ {}, 결과: {}건",
            keyword, startDate, endDate, response.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 복합 조건 검색 (고급 검색)
     */
    @Operation(summary = "복합 조건 검색", description = "여러 필터를 조합하여 메시지를 검색합니다")
    @PostMapping("/search/advanced")
    public ResponseEntity<ApiResponse<SearchResponse>> searchWithFilters(
        @Valid @RequestBody SearchRequest request
    ) {
        long startTime = System.currentTimeMillis();

        SearchResponse response = messageSearchService.searchWithFilters(request);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        log.info("복합 조건 검색 API 호출 - 조건: {}, 결과: {}건",
            request, response.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
