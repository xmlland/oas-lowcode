package com.jeestudio.bpm.service.act;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.act.TaskSetting;
import com.jeestudio.bpm.mapper.base.act.TaskPermissionDao;
import com.jeestudio.bpm.mapper.base.act.TaskSettingDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * @Description: 工作流节点配置服务
 */
@Service
public class TaskSettingService extends CrudService<TaskSettingDao, TaskSetting> {

    @Autowired
    private TaskPermissionDao taskPermissionDao;

    public TaskSetting get(String id) {
        TaskSetting taskSetting = super.get(id);
        taskSetting.setTaskPermission(taskPermissionDao.get(taskSetting.getTaskPermission().getId()));
        return taskSetting;
    }

    @Transactional(readOnly = false)
    public void save(TaskSetting taskSetting) {
        TaskPermission taskPermission = taskSetting.getTaskPermission();
        if (taskPermission != null) {
            if (taskPermission.getIsNewRecord()) {
                taskPermission.preInsert();
                taskPermissionDao.insert(taskPermission);
            } else {
                taskPermission.preUpdate();
                taskPermissionDao.update(taskPermission);
            }
        }
        super.save(taskSetting);
    }

    /**
     * Get task setting by process and task
     *
     * @param taskSetting
     * @return TaskSetting
     */
    public TaskSetting getByProcAndTask(TaskSetting taskSetting) {
        TaskSetting theTaskSetting = dao.getByProcAndTask(taskSetting);
        if (theTaskSetting != null && theTaskSetting.getTaskPermission() != null) {
            theTaskSetting.setTaskPermission(taskPermissionDao.get(theTaskSetting.getTaskPermission().getId()));
        }
        return theTaskSetting;
    }

    /**
     * Get task setting list by process definition key
     *
     * @param procDefKey
     * @return TaskSetting list
     */
    public List<TaskSetting> findListByProcDefKey(String procDefKey) {
        return dao.findListByProcDefKey(procDefKey);
    }

    /**
     * Update user task id
     *
     * @param oldId
     * @param newId
     */
    @Transactional(readOnly = false)
    public void updateUserTaskId(String oldId, String newId) {
        dao.updateUserTaskId(oldId, newId);
    }

    @Transactional(readOnly = false)
    public void updateTaskName(ObjectNode jsonNodes){
        String process_id = jsonNodes.get("properties").get("process_id").asText();
        ArrayNode nodes = jsonNodes.withArray("childShapes");
        HashMap<String, String> maps = new HashMap<>(10);
        nodes.forEach(x->{
            if(StringUtils.compare(x.get("stencil").get("id").asText(), "UserTask") == 0){
                maps.put(x.get("properties").get("overrideid").asText(),x.get("properties").get("name").asText());
            }
        });
        if(maps.size() > 0){
            List<TaskSetting> taskSettings = this.findListByProcDefKey(process_id);
            taskSettings.forEach(x->{
                x.setUserTaskName(maps.get(x.getUserTaskId()));
                super.save(x);
            });
        }
    }
}
