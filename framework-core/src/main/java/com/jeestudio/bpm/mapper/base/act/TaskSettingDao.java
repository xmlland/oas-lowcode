package com.jeestudio.bpm.mapper.base.act;

import org.apache.ibatis.annotations.Param;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.act.TaskSetting;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 流程节点设置数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface TaskSettingDao extends CrudDao<TaskSetting> {

    TaskSetting getByProcAndTask(@Param("taskSetting")TaskSetting taskSetting);

    List<TaskSetting> findListByProcDefKey(@Param("procDefKey")String procDefKey);

    void updateUserTaskId(@Param("oldId")String oldId, @Param("newId")String newId);
}
