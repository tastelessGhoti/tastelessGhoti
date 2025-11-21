package com.chatsearch.api.controller;

import com.chatsearch.api.dto.SearchRequest;
import com.chatsearch.api.dto.SearchResponse;
import com.chatsearch.api.service.MessageSearchService;
import com.chatsearch.domain.document.MessageDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 메시지 검색 컨트롤러 통합 테스트
 *
 * <p>REST API 엔드포인트 검증:
 * <ul>
 *   <li>HTTP 요청/응답 처리</li>
 *   <li>파라미터 검증</li>
 *   <li>응답 포맷</li>
 * </ul>
 */
@WebMvcTest(MessageSearchController.class)
@DisplayName("메시지 검색 컨트롤러 테스트")
class MessageSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageSearchService messageSearchService;

    private List<MessageDocument> testMessages;
    private SearchResponse testResponse;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        testMessages = Arrays.asList(
            createTestMessage(1L, "안녕하세요"),
            createTestMessage(2L, "프로젝트 일정")
        );

        testResponse = SearchResponse.builder()
            .messages(testMessages)
            .totalElements(2L)
            .totalPages(1)
            .currentPage(0)
            .pageSize(20)
            .searchTimeMs(45L)
            .build();
    }

    @Test
    @DisplayName("GET /api/v1/messages/search - 전체 검색 성공")
    void testSearchMessages_Success() throws Exception {
        // Given
        when(messageSearchService.searchMessages(anyString(), anyInt(), anyInt()))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/search")
                .param("keyword", "프로젝트")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.messages").isArray())
            .andExpect(jsonPath("$.data.messages.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/messages/search/room/{roomId} - 채팅방별 검색 성공")
    void testSearchMessagesByRoom_Success() throws Exception {
        // Given
        Long roomId = 1L;
        when(messageSearchService.searchMessagesByRoom(eq(roomId), anyString(), anyInt(), anyInt()))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/search/room/{roomId}", roomId)
                .param("keyword", "회의")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/messages/search/sender/{senderId} - 발신자별 검색 성공")
    void testSearchMessagesBySender_Success() throws Exception {
        // Given
        Long senderId = 1L;
        when(messageSearchService.searchMessagesBySender(eq(senderId), anyString(), anyInt(), anyInt()))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/search/sender/{senderId}", senderId)
                .param("keyword", "프로젝트")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/messages/search/advanced - 복합 조건 검색 성공")
    void testSearchWithFilters_Success() throws Exception {
        // Given
        SearchRequest request = SearchRequest.builder()
            .keyword("프로젝트")
            .roomId(1L)
            .page(0)
            .size(20)
            .build();

        when(messageSearchService.searchWithFilters(any(SearchRequest.class)))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/messages/search/advanced")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/messages/search - 키워드 없이 요청 시 실패")
    void testSearchMessages_MissingKeyword() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/messages/search")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/messages/search - 기본 페이징 파라미터 적용")
    void testSearchMessages_DefaultPagination() throws Exception {
        // Given
        when(messageSearchService.searchMessages(anyString(), eq(0), eq(20)))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/search")
                .param("keyword", "테스트"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/messages/search/date-range - 기간별 검색")
    void testSearchMessagesByDateRange_Success() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

        when(messageSearchService.searchMessagesByDateRange(
            anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyInt(), anyInt()))
            .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/search/date-range")
                .param("keyword", "미팅")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 테스트용 MessageDocument 생성
     */
    private MessageDocument createTestMessage(Long messageId, String content) {
        return MessageDocument.builder()
            .id(messageId.toString())
            .messageId(messageId)
            .roomId(1L)
            .senderId(1L)
            .content(content)
            .messageType("TEXT")
            .isDeleted(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
