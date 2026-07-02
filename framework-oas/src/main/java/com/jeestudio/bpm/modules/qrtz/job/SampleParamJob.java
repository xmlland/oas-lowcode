package com.jeestudio.bpm.modules.qrtz.job;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @Description: 带参数示例定时任务
 */
@Slf4j
public class SampleParamJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Object parameter = jobExecutionContext.getJobDetail().getJobDataMap().get("parameter");
        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            log.warn("定时任务线程被中断", e);
            Thread.currentThread().interrupt();
        }
        log.info("执行定时任务，有参数:{},{}", parameter, DateUtil.now());
    }
}
