package com.paygate.payment.config;

import com.paygate.payment.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

/**
 * 배치 설정.
 * 정산 집계 배치 Job 정의.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final SettlementService settlementService;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("settlementStep", jobRepository)
                .tasklet(settlementTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet settlementTasklet() {
        return (contribution, chunkContext) -> {
            // 전일 정산
            LocalDate targetDate = LocalDate.now().minusDays(1);

            log.info("정산 배치 시작 - targetDate: {}", targetDate);
            settlementService.createDailySettlement(targetDate);
            log.info("정산 배치 완료 - targetDate: {}", targetDate);

            return RepeatStatus.FINISHED;
        };
    }
}
