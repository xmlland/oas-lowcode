package com.jeestudio.bpm.mapper.base.act;

import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.act.TaskSettingVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 流程任务权限数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface TaskPermissionDao extends CrudDao<TaskPermission> {

    List<TaskPermission> findListByIdList(@Param("taskSettingVersionList") List<TaskSettingVersion> taskSettingVersionList);

    List<TaskPermission> findListByPermission(@Param("category") String category, @Param("types") String types);

    TaskPermission findByTaskPermissionId(@Param("permission") String permission, @Param("types") String types);

    String getRuleArgsByUserTaskId(@Param("userTaskId") String userTaskId);
}
