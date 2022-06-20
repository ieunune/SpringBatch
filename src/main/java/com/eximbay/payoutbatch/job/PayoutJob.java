package com.eximbay.payoutbatch.job;

import com.eximbay.payoutbatch.dto.PayoutDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PayoutJob {

    /// Fields
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // DB 처리
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

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
        return jobBuilderFactory.get("payoutJobMain")
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        log.info(" >>> Job 실행 전 수행");

                        // 테스트를 위한 더미 데이터 생성
                        StringBuilder sb = new StringBuilder();

//                        sb.append(" create table payout_ledgar ( ");
//                        sb.append("     PAYOUT_DATE VARCHAR(8),  ");
//                        sb.append("     TRANSACTION_TYPE VARCHAR(8),  ");
//                        sb.append(" 	STATUS       VARCHAR(50) ");
//                        sb.append(" ); ");

                        sb.append("INSERT INTO payout_ledgar VALUES ('20220613', 'PAYOUT', 'REQUESTED');");
                        sb.append("INSERT INTO payout_ledgar VALUES ('20220612', 'PAYOUT', 'COMPLETED');");
                        sb.append("INSERT INTO payout_ledgar VALUES ('20220612', 'PAYOUT', 'REQUESTED');");

                        sb.append("INSERT INTO payout_ledgar VALUES ('20220612', 'REFUND', 'REQUESTED');");
                        sb.append("INSERT INTO payout_ledgar VALUES ('20220612', 'REFUND', 'REQUESTED');");
                        sb.append("INSERT INTO payout_ledgar VALUES ('20220612', 'REFUND', 'REQUESTED');");

                        jdbcTemplate.execute(sb.toString()); // query 실행

                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info(" >>> Job 실행 후 수행");
                    }
                })
                .start(getRemitDataStep())
                    .on("COMPLETED")
                    .to(sendFirmBanking())
                        // .next(sendCallBack())
                .from(getRemitDataStep())
                    .on("*")
                    .end()
                .end()
                .build();
    }

    // STEP 1.
    @Bean
    @JobScope
    public Step getRemitDataStep(){
        return stepBuilderFactory.get("getRemitDataStep")
                .<PayoutDto, String>chunk(10) // chunckSize
                .reader(getRemitData(null))
                .processor(validationData())
                .writer(makeFileStepWriter())
                .build();

    }
    // STEP 1-1.
    @Bean
    @JobScope
    public JdbcCursorItemReader<PayoutDto> getRemitData(@Value("#{jobParameters[payoutDate]}") String payoutDate) {

        String sql = "SELECT * FROM payout_ledgar WHERE PAYOUT_DATE = ? ; ";

        JdbcCursorItemReader<PayoutDto> jdbcCursorItemReader = new JdbcCursorItemReaderBuilder<PayoutDto>()
                .fetchSize(10) // chunk size
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(PayoutDto.class)) // DTO(DbToFileDTO)에 결과 레코드가 매핑된다.
                .sql(sql) // sql query
                .queryArguments(payoutDate) // query parameter
                .name("jdbcCursorItemReader")
                .build();

        return jdbcCursorItemReader;
    }

    // STEP 1-2.
    @Bean
    public ItemProcessor<PayoutDto, String> validationData() {

        log.info(">>> in validationData");

        return payoutDto -> {
            log.info(payoutDto.toString());
            return "[ DB 조회 결과 : " + payoutDto.toString() + " ]";
        };
    }

    // STEP 1-3.
    @Bean
    public FlatFileItemWriter<String> makeFileStepWriter() {

        String filename = "temp.txt";


        return new FlatFileItemWriterBuilder<String>()
                .name("flatFileItemWriter")
                .resource(new FileSystemResource("./file/"+filename))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();

    }

    // STEP 2
    @Bean
    public Step sendFirmBanking() {

        return stepBuilderFactory.get("sendFirmBanking")
                .tasklet((stepContribution, chunkContext) -> {

                    log.info(">>> sendFirmBanking");

                    // 서버 정보 가져오기.

                    // 전문 생성 하기

                    // 전송하기

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

//    @Bean
//    public Step sendCallBack() {
//        return stepBuilderFactory.get("sendCallBack")
//                .<PayoutDto, String>chunk(10) // chunckSize
//                .reader(getCallBackInfo(null))
//                .processor(sendCallBackToMerchant())
//                // .writer(sendResultWriter())
//                .build();
//    }

    @Bean
    @JobScope
    public ItemReader<? extends PayoutDto> getCallBackInfo(@Value("#{jobParameters[payoutDate]}") String payoutDate) {

        // get CallBack Info
        String sql = "select * from PAYOUT_LEDGAR where PAYOUT_DATE = ? ; ";

        return new JdbcCursorItemReaderBuilder<PayoutDto>()
                .fetchSize(10) // chunk size
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(PayoutDto.class)) // DTO(DbToFileDTO)에 결과 레코드가 매핑된다.
                .sql(sql) // sql query
                .queryArguments(payoutDate) // query parameter
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<PayoutDto, String> sendCallBackToMerchant() {

        return payoutDto -> "[ DB 조회 결과 : " + payoutDto + " ]";
    }

//    @Bean
//    public FlatFileItemWriter<PayoutDto> sendResultWriter() {
//
//        String sql = "UPDATE payout_ledger SET `STATUS` = 'COMPLETE' WHERE :payoutDate";
//
//        return new FlatFileItemWriterBuilder<PayoutDto>()
//                .dataSource(dataSource)
//                .sql(sql)
//                .beanMapped()
//                .build();
//    }

}
