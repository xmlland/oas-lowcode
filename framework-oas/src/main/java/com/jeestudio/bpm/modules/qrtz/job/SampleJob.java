package com.jeestudio.bpm.modules.qrtz.job;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/**
 * @Description: 示例定时任务
 */
@Slf4j
public class SampleJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("执行定时任务，无参数:{}", DateUtil.now());
    }
}
