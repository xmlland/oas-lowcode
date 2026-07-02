package com.jeestudio.bpm.mapper.base.act;

import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.act.TaskSettingVersion;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 流程节点设置版本数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface TaskSettingVersionDao extends CrudDao<TaskSettingVersion> {

    void batchSave(@Param("taskSettingVersionList") List<TaskSettingVersion> taskSettingVersionList);

    TaskSettingVersion getTaskSettingVersionByAct(@Param("act") Act act);

    List<TaskSettingVersion> getByPermission(@Param("permission") String permission, @Param("procDefId") String procDefId);

    void updateActByte(@Param("bytes") byte[] bytes, @Param("deploymentId") String deploymentId);

    void updateHistoricTask(@Param("newTaskEntity") TaskEntity newTaskEntity);
}
