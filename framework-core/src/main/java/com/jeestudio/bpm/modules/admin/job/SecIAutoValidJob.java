package com.jeestudio.bpm.modules.admin.job;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 安全配置自动校验定时任务
 */
public class SecIAutoValidJob implements Job {

    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    SecLogService secLogService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String parameter = ConvertUtil.getString(context.getJobDetail().getJobDataMap().get("parameter"));
        if (StrUtil.isNotEmpty(parameter)) {
            sysSecIConfigService.autoValid(parameter);
        } else {
            sysSecIConfigService.autoValid();
        }

        secLogService.checkIntegrityProtection();
    }
}
