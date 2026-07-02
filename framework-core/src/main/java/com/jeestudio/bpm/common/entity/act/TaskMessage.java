package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 工作流任务消息
 */
public class TaskMessage extends DataEntity<TaskMessage> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 适用流程范围。 */
    private String processScope;
    /** 触发消息的流程操作。 */
    private String operation;

    public TaskMessage() {
        super();
    }

    public TaskMessage(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getProcessScope() {
        return processScope;
    }

    public void setProcessScope(String processScope) {
        this.processScope = processScope;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
