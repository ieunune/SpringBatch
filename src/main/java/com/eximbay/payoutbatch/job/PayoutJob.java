package com.eximbay.payoutbatch.job;

import com.eximbay.payoutbatch.job.dto.PayoutDto;
import com.eximbay.payoutbatch.job.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PayoutJob {

    /// Fields
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    PayoutRepository payoutRepository;

    /// Constructor

    /// Method
    @Bean
    public Job payoutJobMain() {
        /*
            STEP 1. 실시간 송금 대상 거래건을 조회한다.
            STEP 1-1. 유효성 검증을 한다.
            STEP 1-2. 유효성 검증이 되지 않은 송금분에 대해서 콜백

            STEP 2. 펌뱅킹을 통해 실시간 송금을 진행한다.
            STEP 2-1. 요청 / 응답 결과 처리
            STEP 2-2. 각 응답 값에 맞는 처리 결과 콜백

            Q. 실패건 재처리는 어떻게?
        */
        return jobBuilderFactory.get("payoutJob")
                .start(getRemitDataStep())
                .next(sendFirmBanking())
                .build();
    }

    @Bean
    @JobScope
    public Step getRemitDataStep(){
        System.out.println(" IN PAYOUT JOB");

        return stepBuilderFactory.get("dbToFileStep")
                .<PayoutDto, String>chunk(10) // chunckSize
                .reader(getRemit(null))
                .processor(validationData())
                .build();

    }
    // 2-1. DB에 접근하여 chunkSize만큼 paging하여 data를 read
    @Bean
    @StepScope
    public JdbcCursorItemReader<PayoutDto> getRemit(@Value("#{jobParameters[payoutDate]}") String payoutDate) {

        // replace jpa
        String sql = "select * from card_ledger where REQUEST_DATE = ? ; ";

        return new JdbcCursorItemReaderBuilder<PayoutDto>()
                .fetchSize(10)                                      	// chunk size
                .rowMapper(new BeanPropertyRowMapper<>(PayoutDto.class)) // DTO(DbToFileDTO)에 결과 레코드가 매핑된다.
                .sql(sql)												// sql query
                .queryArguments(payoutDate) 	   						// query parameter
                .name("jdbcCursorItemReader")
                .build();
    }

    // 2-2. read한 data를 원하는 형태에 맞게 가공
    @Bean
    public ItemProcessor<PayoutDto, String> dbToFileStepProcessor() {

        return dbToFileDTO -> {
            // 원하는 형태에 맞게 가공
            return "[ DB 조회 결과 : " + dbToFileDTO.toString() + " ]";
        };
    }

    public ItemProcessor<PayoutDto, String> validationData() {

        return null;
    }

    public Step sendFirmBanking() {

        return null;

    }

}
