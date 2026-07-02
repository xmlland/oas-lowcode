package com.jeestudio.bpm.modules.qrtz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.qrtz.dao.QrtzExtJobLogMapper;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobLogEntity;
import com.jeestudio.bpm.modules.qrtz.service.QrtzExtJobLogServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 定时任务日志服务实现
 */
@Service
@Transactional
public class QrtzExtJobLogServiceImpl extends ServiceImpl<QrtzExtJobLogMapper, QrtzExtJobLogEntity> implements QrtzExtJobLogServiceI {


    @Autowired
    ZformService zformService;

}
