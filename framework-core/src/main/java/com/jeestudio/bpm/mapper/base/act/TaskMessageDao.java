package com.jeestudio.bpm.mapper.base.act;

import com.jeestudio.bpm.common.entity.act.TaskMessage;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description: 流程任务消息数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface TaskMessageDao extends CrudDao<TaskMessage> {
}
