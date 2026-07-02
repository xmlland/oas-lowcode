package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.act.TaskSettingVersion;
import com.jeestudio.bpm.mapper.base.act.TaskPermissionDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 工作流任务权限服务
 */
@Service
public class TaskPermissionService extends CrudService<TaskPermissionDao, TaskPermission> {

    private static final Logger logger = LoggerFactory.getLogger(TaskPermissionService.class);

    /**
     * Find task permission list by task setting version id list
     *
     * @param taskSettingVersionList
     * @return TaskPermission list
     */
    public List<TaskPermission> findListByIdList(List<TaskSettingVersion> taskSettingVersionList) {
        return dao.findListByIdList(taskSettingVersionList);
    }

    /**
     * Find task permission list by category and types
     *
     * @param category
     * @param types
     * @return TaskPermission list
     */
    public List<TaskPermission> findListByPermission(String category, String types) {
        return dao.findListByPermission(category, types);
    }

    /**
     * Find task permission by permission id and types
     *
     * @param permission id
     * @param types
     * @return TaskPermission
     */
    public TaskPermission findByTaskPermissionId(String permission, String types) {
        TaskPermission taskPermission = dao.findByTaskPermissionId(permission, types);
        return taskPermission;
    }

    public String getBackFlagByUserTaskId(String userTaskId) {
        String backFlag = null;
        try {
            String rule = dao.getRuleArgsByUserTaskId(userTaskId);
            if (rule.indexOf("form_backFlag") != -1) {
                rule = rule.substring(rule.indexOf("form_backFlag") + 28);
                backFlag = rule.substring(0, rule.indexOf("\""));
            }
        } catch (Exception e) {
            logger.error("获取回退标志失败, userTaskId={}", userTaskId, e);
        }
        return backFlag;
    }
}
