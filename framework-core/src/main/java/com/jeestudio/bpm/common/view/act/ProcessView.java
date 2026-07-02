package com.jeestudio.bpm.common.view.act;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;

import java.util.Date;

/**
 * @Description: 流程视图
 */
public class ProcessView extends DataEntity {

    private static final long serialVersionUID = 1L;

    /** 流程定义或流程模型 ID。 */
    private String processId;
    /** 流程分类。 */
    private String category;
    /** 流程定义 Key。 */
    private String key;
    /** 流程名称。 */
    private String name;
    /** 流程版本号。 */
    private Integer version;
    /** 是否挂起。 */
    private boolean suspended;
    /** 部署 ID。 */
    private String deploymentId;
    /** 部署时间。 */
    private Date deploymentTime;
    /** 流程定义名称。 */
    private String processDefinitionName;
    /** 流程实例 ID。 */
    private String processInstanceId;
    /** 流程定义 ID。 */
    private String processDefinitionId;
    /** 当前活动节点 ID。 */
    private String activityId;

    /** 当前活动节点名称。 */
    private String activityName;
    /** 流程实例 ID，兼容业务侧命名。 */
    private String procInsId;
    /** 流程定义 Key，兼容业务侧命名。 */
    private String procDefKey;

    public String getProcInsId() {
        return procInsId;
    }

    public void setProcInsId(String procInsId) {
        this.procInsId = procInsId;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getDeploymentTime() {
        return deploymentTime;
    }

    public void setDeploymentTime(Date deploymentTime) {
        this.deploymentTime = deploymentTime;
    }
}
