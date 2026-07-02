package com.jeestudio.bpm.modules.admin.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.modules.admin.dao.SysFileQueueMapper;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueEntity;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueExportStatus;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueExportThread;
import com.jeestudio.bpm.modules.admin.service.SysFileQueueServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 系统文件队列服务实现
 */
@Service
@Transactional
public class SysFileQueueServiceImpl extends ServiceImpl<SysFileQueueMapper, SysFileQueueEntity> implements SysFileQueueServiceI {
}
