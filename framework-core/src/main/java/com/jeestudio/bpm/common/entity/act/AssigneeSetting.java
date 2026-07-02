package com.jeestudio.bpm.common.entity.act;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.common.entity.system.User;

import java.util.Date;

/**
 * @Description: 办理人设置
 */
public class AssigneeSetting extends DataEntity<AssigneeSetting> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 生效开始时间。 */
    private Date beginTime;
    /** 生效结束时间。 */
    private Date endTime;
    /** 指定办理人。 */
    private User assignee;
    /** 适用流程范围。 */
    private String processScope;
    /** 查询条件：结束时间起始值。 */
    private Date beginEndTime;
    /** 查询条件：结束时间截止值。 */
    private Date endEndTime;

    public AssigneeSetting() {
        super();
    }

    public AssigneeSetting(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getProcessScope() {
        return processScope;
    }

    public void setProcessScope(String processScope) {
        this.processScope = processScope;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getBeginEndTime() {
        return beginEndTime;
    }

    public void setBeginEndTime(Date beginEndTime) {
        this.beginEndTime = beginEndTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndEndTime() {
        return endEndTime;
    }

    public void setEndEndTime(Date endEndTime) {
        this.endEndTime = endEndTime;
    }
}
