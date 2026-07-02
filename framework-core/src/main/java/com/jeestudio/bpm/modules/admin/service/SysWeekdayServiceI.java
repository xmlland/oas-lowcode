package com.jeestudio.bpm.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jeestudio.bpm.modules.admin.entity.SysWeekdayEntity;


/**
 * @Description: 工作日历服务接口
 */
public interface SysWeekdayServiceI extends IService<SysWeekdayEntity> {

    String calcDate(String date, int days);
}
