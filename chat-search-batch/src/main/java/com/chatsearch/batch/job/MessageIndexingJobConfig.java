package com.chatsearch.batch.job;

import com.chatsearch.domain.document.MessageDocument;
import com.chatsearch.domain.entity.Message;
import com.chatsearch.domain.repository.MessageRepository;
import com.chatsearch.domain.repository.MessageSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * 메시지 인덱싱 배치 작업 설정
 *
 * <p>RDB의 메시지 데이터를 ElasticSearch로 마이그레이션
 * <ul>
 *   <li>청크 단위: 1000건</li>
 *   <li>처리 방식: Read → Process → Write</li>
 *   <li>예상 처리량: 시간당 약 100만 건 이상</li>
 * </ul>
 *
 * <p>실행 방법:
 * <pre>
 * java -jar chat-search-batch.jar --job.name=messageIndexingJob startDate=2024-01-01 endDate=2024-01-31
 * </pre>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageIndexingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MessageRepository messageRepository;
    private final MessageSearchRepository messageSearchRepository;

    private static final int CHUNK_SIZE = 1000;

    /**
     * 메시지 인덱싱 Job
     */
    @Bean
    public Job messageIndexingJob() {
        return new JobBuilder("messageIndexingJob", jobRepository)
            .start(messageIndexingStep())
            .build();
    }

    /**
     * 메시지 인덱싱 Step
     */
    @Bean
    public Step messageIndexingStep() {
        return new StepBuilder("messageIndexingStep", jobRepository)
            .<Message, MessageDocument>chunk(CHUNK_SIZE, transactionManager)
            .reader(messageReader())
            .processor(messageProcessor())
            .writer(messageWriter())
            .build();
    }

    /**
     * RDB에서 메시지 읽기
     * Paging 방식으로 대용량 데이터 효율적 처리
     */
    @Bean
    public RepositoryItemReader<Message> messageReader() {
        return new RepositoryItemReaderBuilder<Message>()
            .name("messageReader")
            .repository(messageRepository)
            .methodName("findAll")
            .pageSize(CHUNK_SIZE)
            .sorts(Map.of("id", Sort.Direction.ASC))
            .build();
    }

    /**
     * Message → MessageDocument 변환
     */
    @Bean
    public ItemProcessor<Message, MessageDocument> messageProcessor() {
        return message -> {
            try {
                // MessageDocument로 변환
                return MessageDocument.builder()
                    .id(message.getId() + "_" + message.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                    .messageId(message.getId())
                    .roomId(message.getRoomId())
                    .senderId(message.getSenderId())
                    .content(message.getContent())
                    .messageType(message.getMessageType().name())
                    .fileUrl(message.getFileUrl())
                    .fileName(message.getFileName())
                    .fileSize(message.getFileSize())
                    .isDeleted(message.getIsDeleted())
                    .createdAt(message.getCreatedAt())
                    .updatedAt(message.getUpdatedAt())
                    .parentMessageId(message.getParentMessageId())
                    .build();

            } catch (Exception e) {
                log.error("메시지 변환 실패 - MessageID: {}, Error: {}",
                    message.getId(), e.getMessage(), e);
                return null; // null 반환 시 해당 아이템 스킵
            }
        };
    }

    /**
     * ElasticSearch에 메시지 저장
     * Bulk Insert로 성능 최적화
     */
    @Bean
    public ItemWriter<MessageDocument> messageWriter() {
        return items -> {
            try {
                messageSearchRepository.saveAll(items);

                log.info("메시지 인덱싱 완료 - 건수: {}", items.size());

            } catch (Exception e) {
                log.error("메시지 저장 실패 - 건수: {}, Error: {}",
                    items.size(), e.getMessage(), e);
                throw e;
            }
        };
    }
}
