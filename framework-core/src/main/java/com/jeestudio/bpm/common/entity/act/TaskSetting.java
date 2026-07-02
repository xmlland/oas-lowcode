package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 工作流节点设置
 */
public class TaskSetting extends DataEntity<TaskSetting> {

    private static final long serialVersionUID = 1L;

    /** BPMN 用户任务节点 ID。 */
    private String userTaskId;
    /** BPMN 用户任务节点名称。 */
    private String userTaskName;
    /** 流程定义 Key。 */
    private String procDefKey;
    /** 权限或设置类型。 */
    private String permissionType;
    /** 节点设置值。 */
    private String settingValue;
    /** 节点设置扩展值。 */
    private String settingValue2;
    /** 规则参数 JSON 或表达式。 */
    private String ruleArgs;

    /** 关联的任务权限配置。 */
    private TaskPermission taskPermission;

    public TaskSetting() {
        super();
    }

    public TaskSetting(String id){
        super(id);
    }

    public String getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(String userTaskId) {
        this.userTaskId = userTaskId;
    }

    public String getUserTaskName() {
        return userTaskName;
    }

    public void setUserTaskName(String userTaskName) {
        this.userTaskName = userTaskName;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingValue2() {
        return settingValue2;
    }

    public void setSettingValue2(String settingValue2) {
        this.settingValue2 = settingValue2;
    }

    public String getRuleArgs() {
        return ruleArgs;
    }

    public void setRuleArgs(String ruleArgs) {
        this.ruleArgs = ruleArgs;
    }

    public TaskPermission getTaskPermission() {
        return taskPermission;
    }

    public void setTaskPermission(TaskPermission taskPermission) {
        this.taskPermission = taskPermission;
    }
}
