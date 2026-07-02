package com.jeestudio.bpm.common.entity.act;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.ActEntity;

import java.util.Date;

/**
 * @Description: 流程催办记录实体
 */
public class UrgeProcess extends ActEntity<UrgeProcess> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 流程分类。 */
    private String procCategory;
    /** 流程发起人。 */
    private String procCreateUser;
    /** 流程发起时间文本。 */
    private String procCreateTime;
    /** 当前节点停留时长。 */
    private String duringTime;
    /** 当前节点开始时间。 */
    private Date startTime;
    /** 当前节点名称。 */
    private String cnode;
    /** 当前办理人姓名。 */
    private String cuser;
    /** 当前办理人 ID。 */
    private String cuserId;
    /** 当前任务 ID。 */
    private String taskId;
    /** 最近一次催办人姓名。 */
    private String lastUrgeUser;
    /** 最近一次催办人 ID。 */
    private String lastUrgeUserId;
    /** 最近一次催办时间。 */
    private Date lastUrgeDate;
    /** 催办次数。 */
    private Integer urgeCount;
    /** 业务详情访问路径。 */
    private String requestMapping;
    /** 关联业务实体 ID。 */
    private String entityId;

    public UrgeProcess() {
        super();
    }

    public UrgeProcess(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getProcCategory() {
        return procCategory;
    }

    public void setProcCategory(String procCategory) {
        this.procCategory = procCategory;
    }

    public String getProcCreateUser() {
        return procCreateUser;
    }

    public void setProcCreateUser(String procCreateUser) {
        this.procCreateUser = procCreateUser;
    }

    public String getProcCreateTime() {
        return procCreateTime;
    }

    public void setProcCreateTime(String procCreateTime) {
        this.procCreateTime = procCreateTime;
    }

    public String getDuringTime() {
        return duringTime;
    }

    public void setDuringTime(String duringTime) {
        this.duringTime = duringTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getCnode() {
        return cnode;
    }

    public void setCnode(String cnode) {
        this.cnode = cnode;
    }

    public String getCuser() {
        return cuser;
    }

    public void setCuser(String cuser) {
        this.cuser = cuser;
    }

    public String getCuserId() {
        return cuserId;
    }

    public void setCuserId(String cuserId) {
        this.cuserId = cuserId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLastUrgeUser() {
        return lastUrgeUser;
    }

    public void setLastUrgeUser(String lastUrgeUser) {
        this.lastUrgeUser = lastUrgeUser;
    }

    public String getLastUrgeUserId() {
        return lastUrgeUserId;
    }

    public void setLastUrgeUserId(String lastUrgeUserId) {
        this.lastUrgeUserId = lastUrgeUserId;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getLastUrgeDate() {
        return lastUrgeDate;
    }

    public void setLastUrgeDate(Date lastUrgeDate) {
        this.lastUrgeDate = lastUrgeDate;
    }

    public Integer getUrgeCount() {
        return urgeCount;
    }

    public void setUrgeCount(Integer urgeCount) {
        this.urgeCount = urgeCount;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
