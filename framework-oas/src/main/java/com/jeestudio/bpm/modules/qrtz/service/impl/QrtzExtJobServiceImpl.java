package com.jeestudio.bpm.modules.qrtz.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.qrtz.dao.QrtzExtJobMapper;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobEntity;
import com.jeestudio.bpm.modules.qrtz.service.QrtzExtJobServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Description: 定时任务服务实现
 */
@Service
@Transactional
@Slf4j
public class QrtzExtJobServiceImpl extends ServiceImpl<QrtzExtJobMapper, QrtzExtJobEntity> implements QrtzExtJobServiceI {

    @Autowired
    Scheduler scheduler;

    @Autowired
    ZformService zformService;

    private Job getClass(String classname) throws Exception {
        Class<?> class1 = Class.forName(classname);
        return (Job) class1.newInstance();
    }

    @Override
    public void addScheduler(QrtzExtJobEntity qrtzExtJob) {
        try {
            String jobClassName = ConvertUtil.getString(qrtzExtJob.getExecuteClass()).trim();
            String jobId = ConvertUtil.getString(qrtzExtJob.getId()).trim();
            String parameter = ConvertUtil.getString(qrtzExtJob.getExecParam()).trim();
            String cronExpression = ConvertUtil.getString(qrtzExtJob.getCronExpression()).trim();
            // 启动调度器
            scheduler.start();
            JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(jobId).usingJobData("parameter", parameter).build();
            // 表达式调度构建器(即任务执行的时间)
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            // 按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobId).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            log.error("创建定时任务失败:{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("创建定时任务失败");
        }
    }

    @Override
    public void deleteScheduler(QrtzExtJobEntity qrtzExtJob) {
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(qrtzExtJob.getId()));
            scheduler.unscheduleJob(TriggerKey.triggerKey(qrtzExtJob.getId()));
            scheduler.deleteJob(JobKey.jobKey(qrtzExtJob.getId()));
        } catch (SchedulerException e) {
            log.error("删除定时任务失败:{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("删除定时任务失败");
        }
    }

    @Override
    public void pause(QrtzExtJobEntity qrtzExtJob) {
        this.deleteScheduler(qrtzExtJob);
        qrtzExtJob.setJobStatus(Global.NO);
        this.updateById(qrtzExtJob);
    }

    @Override
    public void resume(QrtzExtJobEntity qrtzExtJob) {
        this.deleteScheduler(qrtzExtJob);
        this.addScheduler(qrtzExtJob);
        qrtzExtJob.setJobStatus(Global.YES);
        this.updateById(qrtzExtJob);
    }

    @Override
    public void execute(QrtzExtJobEntity qrtzExtJob) {
        try {
            String jobId = ConvertUtil.getString(qrtzExtJob.getId()).trim();
            String jobClassName = ConvertUtil.getString(qrtzExtJob.getExecuteClass()).trim();
            String parameter = ConvertUtil.getString(qrtzExtJob.getExecParam()).trim();
            Date startDate = DateUtil.date();
            String ymd = DateUtil.now();
            String identity = jobId + ymd;
            //1s 后执行 只执行一次
            startDate.setTime(startDate.getTime() + 1000L);
            // 定义一个Trigger
            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(identity)
                    .startAt(startDate)
                    .build();
            // 构建job信息

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("parameter", parameter);
            jobDataMap.put("execUser", UserUtil.getCurrentUserId());
            jobDataMap.put("jobId", jobId);
            JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(identity).usingJobData(jobDataMap).build();
            // 将trigger和 jobDetail 加入这个调度
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动scheduler
            scheduler.start();
        } catch (Exception e) {
            log.error("执行定时任务失败", e);
            throw new BusinessException("执行定时任务失败");
        }
    }
}
