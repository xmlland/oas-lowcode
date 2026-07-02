package com.jeestudio.bpm.modules.qrtz.aop;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobLogEntity;
import com.jeestudio.bpm.modules.qrtz.service.QrtzExtJobLogServiceI;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 定时任务执行日志切面
 */
@Component
@Aspect
@Slf4j
public class JobAspect {

    @Autowired
    QrtzExtJobLogServiceI qrtzExtJobLogService;

    @Pointcut("execution(void org.quartz.Job.execute(..))")
    public void execute() {
    }

    @Around("execute()")
    public Object processLog(ProceedingJoinPoint joinPoint) {
        Object object = null;
        JobDetail jobDetail = ((JobExecutionContext) joinPoint.getArgs()[0]).getJobDetail();
        JobKey key = jobDetail.getKey();
        String name = key.getName();
        String error = "";
        String success = Global.YES;
        log.info("执行定时任务开始：{}", key);
        long start = System.currentTimeMillis();
        try {
            object = joinPoint.proceed();
        } catch (Throwable throwable) {
            error = ExceptionUtil.stacktraceToString(throwable);
            success = Global.NO;
        } finally {
            long end = System.currentTimeMillis();

            String execUser = "quartzScheduler";
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            if (jobDataMap.containsKey("jobId")) {
                name = ConvertUtil.getString(jobDataMap.get("jobId"));
                execUser = ConvertUtil.getString(jobDataMap.get("execUser"));
            }
            QrtzExtJobLogEntity jobLog = new QrtzExtJobLogEntity();
            jobLog.setLogTime(DateUtil.date());
            jobLog.setUseTimes(String.valueOf(end - start));
            jobLog.setExecUser(execUser);
            jobLog.setJobId(name);
            jobLog.setJobSuccess(success);

            if (Global.YES.equals(success)) {
                log.info("执行定时任务完成：{}", key);
            } else {
                log.error("执行定时任务异常：{}", error);
                jobLog.setExceptionMessage(error);
            }
            qrtzExtJobLogService.save(jobLog);
        }
        return object;
    }
}
