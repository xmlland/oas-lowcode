package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 节点设置版本
 */
public class TaskSettingVersion extends DataEntity<TaskSettingVersion> {

    private static final long serialVersionUID = 1L;

    /** BPMN 用户任务节点 ID。 */
    private String userTaskId;
    /** BPMN 用户任务节点名称。 */
    private String userTaskName;
    /** 流程定义 ID，绑定到具体发布版本。 */
    private String procDefId;
    /** 节点设置值。 */
    private String settingValue;
    /** 节点设置扩展值。 */
    private String settingValue2;
    /** 权限或设置类型。 */
    private String permissionType;
    /** 权限配置内容。 */
    private String permission;
    /** 规则参数 JSON 或表达式。 */
    private String ruleArgs;

    public TaskSettingVersion() {
        super();
    }

    public TaskSettingVersion(String id){
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

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
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

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRuleArgs() {
        return ruleArgs;
    }

    public void setRuleArgs(String ruleArgs) {
        this.ruleArgs = ruleArgs;
    }
}
