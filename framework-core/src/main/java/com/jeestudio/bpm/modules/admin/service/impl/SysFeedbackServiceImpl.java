package com.jeestudio.bpm.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.admin.dao.SysFeedbackMapper;
import com.jeestudio.bpm.modules.admin.entity.SysFeedbackEntity;
import com.jeestudio.bpm.modules.admin.service.SysFeedbackServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 用户反馈服务实现
 */
@Service
@Transactional
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedbackEntity> implements SysFeedbackServiceI {


    @Autowired
    ZformService zformService;

}