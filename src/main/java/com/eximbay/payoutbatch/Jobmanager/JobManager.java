package com.eximbay.payoutbatch.Jobmanager;

import com.eximbay.payoutbatch.job.PayoutJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JobManager {

    /// Fields
    JobLauncher jobLauncher;
    PayoutJob payoutJob;

    /// Constructor
    public JobManager(JobLauncher jobLauncher, PayoutJob payoutJob) {
        this.jobLauncher = jobLauncher;
        this.payoutJob = payoutJob;

        payout();
    }

    /// Method
    // @Scheduled(cron = "0 0 11 * * *")
    // @Scheduled(cron = "0 0/5 * * * *")
    public void payout() {

        // 파라미터 정보 설정
//        Map<String, JobParameter> confMap = new HashMap<>();
//        confMap.put("time", new JobParameter(System.currentTimeMillis()));
//        confMap.put("payoutDate", new JobParameter("20200612"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("time", String.valueOf(System.currentTimeMillis()))
                .addString("payoutDate", "20220612")
                .toJobParameters();

        try {
            BatchStatus batchStatus = jobLauncher.run(payoutJob.payoutJobMain(), jobParameters).getStatus();
            log.debug(" 처리 결과 : " + batchStatus);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

}
