package com.chatsearch.api.service;

import com.chatsearch.api.dto.SearchResponse;
import com.chatsearch.domain.document.MessageDocument;
import com.chatsearch.domain.repository.MessageSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 메시지 검색 서비스 테스트
 *
 * <p>검색 기능의 정확성 검증:
 * <ul>
 *   <li>전체 검색</li>
 *   <li>채팅방별 검색</li>
 *   <li>발신자별 검색</li>
 *   <li>기간별 검색</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("메시지 검색 서비스 테스트")
class MessageSearchServiceTest {

    @Mock
    private MessageSearchRepository messageSearchRepository;

    @InjectMocks
    private MessageSearchService messageSearchService;

    private List<MessageDocument> testMessages;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        testMessages = Arrays.asList(
            createTestMessage(1L, 1L, 1L, "안녕하세요 프로젝트 미팅"),
            createTestMessage(2L, 1L, 2L, "프로젝트 일정 확인"),
            createTestMessage(3L, 2L, 1L, "회의록 공유")
        );
    }

    @Test
    @DisplayName("전체 메시지 검색 - 성공")
    void testSearchMessages_Success() {
        // Given
        String keyword = "프로젝트";
        Page<MessageDocument> mockPage = new PageImpl<>(testMessages);

        when(messageSearchRepository.findByContentAndIsDeletedFalse(eq(keyword), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        SearchResponse response = messageSearchService.searchMessages(keyword, 0, 20);

        // Then
        assertNotNull(response);
        assertEquals(3, response.getTotalElements());
        assertEquals(testMessages.size(), response.getMessages().size());

        verify(messageSearchRepository, times(1))
            .findByContentAndIsDeletedFalse(eq(keyword), any(Pageable.class));
    }

    @Test
    @DisplayName("채팅방별 메시지 검색 - 성공")
    void testSearchMessagesByRoom_Success() {
        // Given
        Long roomId = 1L;
        String keyword = "프로젝트";
        List<MessageDocument> roomMessages = testMessages.subList(0, 2);
        Page<MessageDocument> mockPage = new PageImpl<>(roomMessages);

        when(messageSearchRepository.findByRoomIdAndContentAndIsDeletedFalse(
            eq(roomId), eq(keyword), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        SearchResponse response = messageSearchService.searchMessagesByRoom(roomId, keyword, 0, 20);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertTrue(response.getMessages().stream()
            .allMatch(msg -> msg.getRoomId().equals(roomId)));

        verify(messageSearchRepository, times(1))
            .findByRoomIdAndContentAndIsDeletedFalse(eq(roomId), eq(keyword), any(Pageable.class));
    }

    @Test
    @DisplayName("발신자별 메시지 검색 - 성공")
    void testSearchMessagesBySender_Success() {
        // Given
        Long senderId = 1L;
        String keyword = "프로젝트";
        List<MessageDocument> senderMessages = Arrays.asList(testMessages.get(0), testMessages.get(2));
        Page<MessageDocument> mockPage = new PageImpl<>(senderMessages);

        when(messageSearchRepository.findBySenderIdAndContentAndIsDeletedFalse(
            eq(senderId), eq(keyword), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        SearchResponse response = messageSearchService.searchMessagesBySender(senderId, keyword, 0, 20);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertTrue(response.getMessages().stream()
            .allMatch(msg -> msg.getSenderId().equals(senderId)));

        verify(messageSearchRepository, times(1))
            .findBySenderIdAndContentAndIsDeletedFalse(eq(senderId), eq(keyword), any(Pageable.class));
    }

    @Test
    @DisplayName("기간별 메시지 검색 - 성공")
    void testSearchMessagesByDateRange_Success() {
        // Given
        String keyword = "프로젝트";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Page<MessageDocument> mockPage = new PageImpl<>(testMessages);

        when(messageSearchRepository.findByContentAndCreatedAtBetween(
            eq(keyword), eq(startDate), eq(endDate), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        SearchResponse response = messageSearchService.searchMessagesByDateRange(
            keyword, startDate, endDate, 0, 20);

        // Then
        assertNotNull(response);
        assertEquals(3, response.getTotalElements());

        verify(messageSearchRepository, times(1))
            .findByContentAndCreatedAtBetween(eq(keyword), eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    @DisplayName("검색 결과 없음")
    void testSearchMessages_NoResults() {
        // Given
        String keyword = "존재하지않는키워드";
        Page<MessageDocument> emptyPage = Page.empty();

        when(messageSearchRepository.findByContentAndIsDeletedFalse(eq(keyword), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        SearchResponse response = messageSearchService.searchMessages(keyword, 0, 20);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
        assertTrue(response.getMessages().isEmpty());
    }

    @Test
    @DisplayName("페이징 처리 검증")
    void testSearchMessages_Pagination() {
        // Given
        String keyword = "프로젝트";
        int page = 1;
        int size = 10;
        Page<MessageDocument> mockPage = new PageImpl<>(
            testMessages,
            org.springframework.data.domain.PageRequest.of(page, size),
            100
        );

        when(messageSearchRepository.findByContentAndIsDeletedFalse(eq(keyword), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        SearchResponse response = messageSearchService.searchMessages(keyword, page, size);

        // Then
        assertNotNull(response);
        assertEquals(100, response.getTotalElements());
        assertEquals(10, response.getTotalPages());
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
    }

    /**
     * 테스트용 MessageDocument 생성
     */
    private MessageDocument createTestMessage(Long messageId, Long roomId, Long senderId, String content) {
        return MessageDocument.builder()
            .id(messageId.toString())
            .messageId(messageId)
            .roomId(roomId)
            .roomName("테스트 채팅방")
            .senderId(senderId)
            .senderUsername("user" + senderId)
            .senderDisplayName("사용자" + senderId)
            .content(content)
            .messageType("TEXT")
            .isDeleted(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
