package com.eximbay.payoutbatch.Jobmanager;

import com.eximbay.payoutbatch.job.PayoutJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JobManager {

    /// Fields
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    PayoutJob payoutJob;
    /// Constructor

    /// Method
    // @Scheduled(cron = "0 0 11 * * *")
    @Scheduled(cron = "0 0/5 * * * *")
    public void payout() {

        // 파라미터 정보 설정
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        confMap.put("payoutDate", new JobParameter("2020-06-08"));
        JobParameters jobParameters = new JobParameters(confMap);

        try {
            JobExecution jobExecution = jobLauncher.run(payoutJob.payoutJobMain(), jobParameters);
            BatchStatus batchStatus = jobExecution.getStatus();
            System.out.println(" 처리 결과 : " + batchStatus);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

}
