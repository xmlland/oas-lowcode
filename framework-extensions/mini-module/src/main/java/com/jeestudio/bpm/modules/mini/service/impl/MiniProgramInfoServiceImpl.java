package com.jeestudio.bpm.modules.mini.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.mini.dao.MiniProgramInfoMapper;
import com.jeestudio.bpm.modules.mini.entity.MiniProgramInfoEntity;
import com.jeestudio.bpm.modules.mini.service.MiniProgramInfoServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 小程序信息服务实现
 */
@Service
@Transactional
public class MiniProgramInfoServiceImpl extends ServiceImpl<MiniProgramInfoMapper, MiniProgramInfoEntity> implements MiniProgramInfoServiceI {


    @Autowired
    ZformService zformService;

}
