package com.jeestudio.bpm.modules.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.oa.dao.OaSysAnnouncementReadMapper;
import com.jeestudio.bpm.modules.oa.entity.OaSysAnnouncementReadEntity;
import com.jeestudio.bpm.modules.oa.service.OaSysAnnouncementReadServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 公告阅读服务实现
 */
@Service
@Transactional
public class OaSysAnnouncementReadServiceImpl extends ServiceImpl<OaSysAnnouncementReadMapper, OaSysAnnouncementReadEntity> implements OaSysAnnouncementReadServiceI {


    @Autowired
    ZformService zformService;

}