package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.common.entity.act.TaskMessage;
import com.jeestudio.bpm.mapper.base.act.TaskMessageDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.stereotype.Service;

/**
 * @Description: 工作流任务消息服务
 */
@Service
class TaskMessageService extends CrudService<TaskMessageDao, TaskMessage> {
}
