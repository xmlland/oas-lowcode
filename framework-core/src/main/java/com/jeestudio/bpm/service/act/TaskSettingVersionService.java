package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.common.entity.act.Act;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import com.jeestudio.bpm.common.entity.act.TaskSettingVersion;
import com.jeestudio.bpm.mapper.base.act.TaskSettingVersionDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 节点配置版本服务
 */
@Service
public class TaskSettingVersionService extends CrudService<TaskSettingVersionDao, TaskSettingVersion> {

    /**
     * Batch save task setting version list
     */
    @Transactional()
    public void batchSave(List<TaskSettingVersion> taskSettingVersionList) {
        dao.batchSave(taskSettingVersionList);
    }

    /**
     * Get task setting version by act
     *
     * @return TaskSettingVersion
     */
    public TaskSettingVersion getTaskSettingVersionByAct(Act act) {
        return dao.getTaskSettingVersionByAct(act);
    }

    /**
     * Get task setting version list by permission and process definition id
     *
     * @return TaskSettingVersion list
     */
    public List<TaskSettingVersion> getByPermission(String permission, String procDefId) {
        return dao.getByPermission(permission, procDefId);
    }

    /**
     * Update act
     */
    @Transactional()
    public void updateActByte(byte[] bytes, String deploymentId) {
        dao.updateActByte(bytes, deploymentId);
    }

    /**
     * Update history task
     */
    @Transactional()
    public void updateHistoricTask(TaskEntity newTaskEntity) {
        dao.updateHistoricTask(newTaskEntity);
    }
}
