package com.eximbay.payoutbatch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PayoutJob {

    /// Fields
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    /// Constructor

    /// Method
    @Bean
    public Job payoutJobMain() {
        /**
         STEP 1. 설정한 시간대에 기동한다.
         STEP 1-1. 스케줄링 필요
         STEP 1-2. 담당자 확인 이후 처리 필요.
         STEP 1-3. 그래서 Business 업무 시간 기준으로 한다.
         STEP 1-4. 오전 11시?

         STEP 2. 실시간 송금 대상 거래건을 조회한다.
         STEP 2-1. 조회하는 조건 검토.
         STEP 2-2. 취소 부분 고려

         STEP 3. 유효성 검증을 한다.
         STEP 3-1. 유효성 검증이 되지 않은 송금분에 대해서 콜백

         STEP 4. 펌뱅킹을 통해 실시간 송금을 진행한다.
         STEP 4-1. 가맹점에 콜백
         STEP 4-1-1. 가맹점 콜백 전송 횟수 제한?
         STEP 4-1-2. 재전송 기능 필요?
         STEP 4-2. 실패건 재처리는 어떻게?
         STEP 4-2-1. 담당자 통해서 실패 사유 확인
         STEP 4-2-2. 가맹점 계좌 정보 확인 등 확인완료 이후
         STEP 4-2-3. 기술팀에 재전송 요청.
         STEP 4-2-4. 다음 배치 처리시 재전송 될 수 있도록 해야함
         STEP 4-2-4-1. 당일 처리 리스트에 추가?
         STEP 4-2-4-2. 그날 처리해야 하는 정산분 확정 기능 필요?

         STEP 5. 완료된 정보로 지급대행 원장 테이블에 상태값 업데이트.

         STEP 6. 프로그램 종료.
         */
        return jobBuilderFactory.get("payoutJob")
                .start(getRemitListStep())
                .build();
    }

    @Bean
    @JobScope
    public Step getRemitListStep(){
        System.out.println(" IN PAYOUT JOB");

        return stepBuilderFactory.get("temp")
                .tasklet((stepContribution, chunkContext) -> {

                    log.info(">>> getRemitListStep 실행 ");

                    System.out.println("test");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
