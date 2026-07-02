package com.jeestudio.bpm.modules.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.oa.dao.OaTaskFallbackPermissionsSettingMapper;
import com.jeestudio.bpm.modules.oa.entity.OaTaskFallbackPermissionsSettingEntity;
import com.jeestudio.bpm.modules.oa.service.OaTaskFallbackPermissionsSettingServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 任务回退权限配置服务实现
 */
@Service
@Transactional
public class OaTaskFallbackPermissionsSettingServiceImpl extends ServiceImpl<OaTaskFallbackPermissionsSettingMapper, OaTaskFallbackPermissionsSettingEntity> implements OaTaskFallbackPermissionsSettingServiceI {


    @Autowired
    ZformService zformService;

}
