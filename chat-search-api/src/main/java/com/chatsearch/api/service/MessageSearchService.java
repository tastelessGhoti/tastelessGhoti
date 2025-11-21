package com.chatsearch.api.service;

import com.chatsearch.api.dto.SearchRequest;
import com.chatsearch.api.dto.SearchResponse;
import com.chatsearch.domain.document.MessageDocument;
import com.chatsearch.domain.repository.MessageSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 검색 서비스
 *
 * <p>다양한 검색 옵션 제공:
 * <ul>
 *   <li>전체 메시지 검색</li>
 *   <li>채팅방별 메시지 검색</li>
 *   <li>발신자별 메시지 검색</li>
 *   <li>기간별 메시지 검색</li>
 *   <li>복합 조건 검색</li>
 * </ul>
 *
 * <p>검색 성능:
 * <ul>
 *   <li>ElasticSearch의 분산 검색으로 빠른 응답 속도</li>
 *   <li>형태소 분석 기반 정확한 한글 검색</li>
 *   <li>페이징 처리로 대용량 결과 효율적 처리</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSearchService {

    private final MessageSearchRepository messageSearchRepository;

    /**
     * 전체 메시지 검색
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과
     */
    public SearchResponse searchMessages(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MessageDocument> results = messageSearchRepository.findByContentAndIsDeletedFalse(
            keyword, pageable
        );

        log.info("전체 메시지 검색 완료 - 키워드: {}, 결과: {}건", keyword, results.getTotalElements());

        return buildSearchResponse(results);
    }

    /**
     * 채팅방별 메시지 검색
     *
     * @param roomId 채팅방 ID
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과
     */
    public SearchResponse searchMessagesByRoom(Long roomId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MessageDocument> results = messageSearchRepository.findByRoomIdAndContentAndIsDeletedFalse(
            roomId, keyword, pageable
        );

        log.info("채팅방별 메시지 검색 완료 - RoomID: {}, 키워드: {}, 결과: {}건",
            roomId, keyword, results.getTotalElements());

        return buildSearchResponse(results);
    }

    /**
     * 발신자별 메시지 검색
     *
     * @param senderId 발신자 ID
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과
     */
    public SearchResponse searchMessagesBySender(Long senderId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MessageDocument> results = messageSearchRepository.findBySenderIdAndContentAndIsDeletedFalse(
            senderId, keyword, pageable
        );

        log.info("발신자별 메시지 검색 완료 - SenderID: {}, 키워드: {}, 결과: {}건",
            senderId, keyword, results.getTotalElements());

        return buildSearchResponse(results);
    }

    /**
     * 기간별 메시지 검색
     *
     * @param keyword 검색 키워드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과
     */
    public SearchResponse searchMessagesByDateRange(
        String keyword,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MessageDocument> results = messageSearchRepository.findByContentAndCreatedAtBetween(
            keyword, startDate, endDate, pageable
        );

        log.info("기간별 메시지 검색 완료 - 키워드: {}, 기간: {} ~ {}, 결과: {}건",
            keyword, startDate, endDate, results.getTotalElements());

        return buildSearchResponse(results);
    }

    /**
     * 복합 조건 검색
     *
     * @param request 검색 요청
     * @return 검색 결과
     */
    public SearchResponse searchWithFilters(SearchRequest request) {
        Pageable pageable = PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 채팅방 + 기간 필터
        if (request.getRoomId() != null && request.getStartDate() != null) {
            List<MessageDocument> results = messageSearchRepository
                .findByRoomIdAndCreatedAtBetweenAndIsDeletedFalse(
                    request.getRoomId(),
                    request.getStartDate(),
                    request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now()
                );

            // 키워드 필터링
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                results = results.stream()
                    .filter(doc -> doc.getContent().contains(request.getKeyword()))
                    .collect(Collectors.toList());
            }

            log.info("복합 조건 검색 완료 - 조건: RoomID={}, Keyword={}, 결과: {}건",
                request.getRoomId(), request.getKeyword(), results.size());

            return SearchResponse.builder()
                .messages(results)
                .totalElements(results.size())
                .totalPages(1)
                .currentPage(0)
                .pageSize(results.size())
                .build();
        }

        // 기본 검색
        return searchMessages(request.getKeyword(), request.getPage(), request.getSize());
    }

    /**
     * 검색 결과를 SearchResponse로 변환
     */
    private SearchResponse buildSearchResponse(Page<MessageDocument> page) {
        return SearchResponse.builder()
            .messages(page.getContent())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .build();
    }
}
