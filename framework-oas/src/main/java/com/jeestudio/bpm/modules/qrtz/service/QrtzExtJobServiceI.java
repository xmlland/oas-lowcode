package com.jeestudio.bpm.modules.qrtz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobEntity;


/**
 * @Description: 定时任务服务接口
 */
public interface QrtzExtJobServiceI extends IService<QrtzExtJobEntity> {

    /**
     * 添加定时任务
     *
     * @param qrtzExtJob 定时任务
     */
    void addScheduler(QrtzExtJobEntity qrtzExtJob);

    /**
     * 删除任务
     *
     * @param qrtzExtJob 定时任务
     */
    void deleteScheduler(QrtzExtJobEntity qrtzExtJob);


    /**
     * 暂停定时任务
     *
     * @param qrtzExtJob 定时任务
     */
    void pause(QrtzExtJobEntity qrtzExtJob);


    /**
     * 恢复定时任务
     *
     * @param qrtzExtJob 定时任务
     */
    void resume(QrtzExtJobEntity qrtzExtJob);

    /**
     * 执行定时任务
     *
     * @param qrtzExtJob 定时任务
     */
    void execute(QrtzExtJobEntity qrtzExtJob);
}
