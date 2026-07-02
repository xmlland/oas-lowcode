package com.jeestudio.bpm.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.admin.dao.SysSecIdataMapper;
import com.jeestudio.bpm.modules.admin.entity.SysSecIdataEntity;
import com.jeestudio.bpm.modules.admin.service.SysSecIdataServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 安全数据服务实现
 */
@Service
@Transactional
public class SysSecIdataServiceImpl extends ServiceImpl<SysSecIdataMapper, SysSecIdataEntity> implements SysSecIdataServiceI {


    @Autowired
    @Lazy
    ZformService zformService;

}
