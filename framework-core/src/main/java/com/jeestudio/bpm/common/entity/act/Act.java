package com.jeestudio.bpm.common.entity.act;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.common.BaseEntity;
import com.jeestudio.bpm.common.utils.Variable;
import com.jeestudio.bpm.utils.TimeUtils;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 工作流活动实体
 */
public class Act extends BaseEntity<Act> {

    private static final long serialVersionUID = 1L;

    /** 流程启动状态码。 */
    public static final String STATUS_START = "00";
    /** 流程办结状态码。 */
    public static final String STATUS_END = "99";

    /** Flowable 任务 ID。 */
    private String taskId;
    /** 当前任务名称。 */
    private String taskName;
    /** BPMN 用户任务定义 Key。 */
    private String taskDefKey;
    /** 流程实例 ID。 */
    private String procInsId;
    /** 流程定义 ID。 */
    private String procDefId;
    /** 流程定义 Key。 */
    private String procDefKey;
    /** 关联业务表名。 */
    private String businessTable;
    /** 关联业务数据 ID。 */
    private String businessId;
    /** 流程标题或业务标题。 */
    private String title;
    /** 流程或任务状态。 */
    private String status;
    /** 审批意见或办理意见。 */
    private String comment = "";
    /** 办理标记，例如同意、驳回等。 */
    private String flag;
    /** 扩展参数，供流程操作入口传递上下文。 */
    private String param;
    /** Flowable 当前任务对象，序列化时忽略。 */
    private Task task;
    /** Flowable 流程定义对象，序列化时忽略。 */
    private ProcessDefinition procDef;
    /** Flowable 运行中流程实例对象，序列化时忽略。 */
    private ProcessInstance procIns;
    /** Flowable 历史流程实例对象。 */
    private HistoricProcessInstance hisProcIns;
    /** Flowable 历史任务对象，序列化时忽略。 */
    private HistoricTaskInstance histTask;
    /** Flowable 历史活动节点对象，序列化时忽略。 */
    private HistoricActivityInstance histIns;
    /** 当前办理人 ID。 */
    private String assignee;
    /** 当前办理人姓名。 */
    private String assigneeName;
    /** 流程变量集合。 */
    private Variable vars;
    /** 查询条件：开始时间。 */
    private Date beginDate;
    /** 查询条件：结束时间。 */
    private Date endDate;
    /** 流程活动列表，常用于历史轨迹展示。 */
    private List<Act> list;
    /** 流程是否挂起。 */
    private Boolean isSuspend;
    /** 流程扩展数据 Map。 */
    Map<String, Object> actMap = Maps.newHashMap();

    public Act() {
        super();
    }

    public Act(Task task) {
        this.taskId = task.getId();
        this.taskName = task.getName();
        this.taskDefKey = task.getTaskDefinitionKey();
        this.procInsId = task.getProcessInstanceId();
        this.procDefId = task.getProcessDefinitionId();
    }

    public Act(HistoricTaskInstance historicTaskInstance) {
        this.taskId = historicTaskInstance.getId();
        this.taskName = historicTaskInstance.getName();
        this.taskDefKey = historicTaskInstance.getTaskDefinitionKey();
        this.procInsId = historicTaskInstance.getProcessInstanceId();
        this.procDefId = historicTaskInstance.getProcessDefinitionId();
    }

    public String getTaskId() {
        if (taskId == null && task != null){
            taskId = task.getId();
        }
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        if (taskName == null && task != null){
            taskName = task.getName();
        }
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDefKey() {
        if (taskDefKey == null && task != null){
            taskDefKey = task.getTaskDefinitionKey();
        }
        return taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getTaskCreateDate() {
        if (task != null){
            return task.getCreateTime();
        }
        return null;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getTaskEndDate() {
        if (histTask != null){
            return histTask.getEndTime();
        }
        return null;
    }

    @JsonIgnore
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @JsonIgnore
    public ProcessDefinition getProcDef() {
        return procDef;
    }

    public void setProcDef(ProcessDefinition procDef) {
        this.procDef = procDef;
    }

    public String getProcDefName() {
        if (procDef == null) {
            return "";
        }
        return procDef.getName();
    }

    @JsonIgnore
    public ProcessInstance getProcIns() {
        return procIns;
    }

    public void setProcIns(ProcessInstance procIns) {
        this.procIns = procIns;
        if (procIns != null && procIns.getBusinessKey() != null && procIns.getBusinessKey().contains(":")) {
            String[] ss = procIns.getBusinessKey().split(":");
            setBusinessTable(ss[0]);
            setBusinessId(ss[1]);
        } else if (procIns != null && procIns.getBusinessKey() != null) {
            setBusinessId(procIns.getBusinessKey());
        }
    }

    public void setFinishedProcIns(HistoricProcessInstance procIns) {
        //this.procIns = procIns;
        if (procIns != null && procIns.getBusinessKey() != null && procIns.getBusinessKey().contains(":")) {
            String[] ss = procIns.getBusinessKey().split(":");
            setBusinessTable(ss[0]);
            setBusinessId(ss[1]);
        }else if (procIns != null && procIns.getBusinessKey() != null){
            setBusinessId(procIns.getBusinessKey());
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonIgnore
    public HistoricTaskInstance getHistTask() {
        return histTask;
    }

    public void setHistTask(HistoricTaskInstance histTask) {
        this.histTask = histTask;
    }

    @JsonIgnore
    public HistoricActivityInstance getHistIns() {
        return histIns;
    }

    public void setHistIns(HistoricActivityInstance histIns) {
        this.histIns = histIns;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getProcDefId() {
        if (procDefId == null && task != null){
            procDefId = task.getProcessDefinitionId();
        }
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcInsId() {
        if (procInsId == null && task != null){
            procInsId = task.getProcessInstanceId();
        }
        return procInsId;
    }

    public void setProcInsId(String procInsId) {
        this.procInsId = procInsId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessTable() {
        return businessTable;
    }

    public void setBusinessTable(String businessTable) {
        this.businessTable = businessTable;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }



    public Boolean getIsSuspend() {
        return isSuspend;
    }

    public void setIsSuspend(Boolean isSuspend) {
        this.isSuspend = isSuspend;
    }

    public String getAssignee() {
        if (assignee == null && task != null){
            assignee = task.getAssignee();
        }
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public List<Act> getList() {
        return list;
    }

    public void setList(List<Act> list) {
        this.list = list;
    }

    public Variable getVars() {
        return vars;
    }

    public void setVars(Variable vars) {
        this.vars = vars;
    }

    public void setVarsMap(Map<String, Object> map) {
        this.vars = new Variable(map);
    }

    public String getProcDefKey() {
        if (StringUtils.isBlank(procDefKey) && StringUtils.isNotBlank(procDefId)){
            procDefKey = StringUtils.split(procDefId, ":")[0];
        }
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getDurationTime(){
        if (histIns!=null && histIns.getDurationInMillis() != null){
            return TimeUtils.toTimeString(histIns.getDurationInMillis());
        }
        return "";
    }

    public String getDurationTime_EN(){
        if (histIns!=null && histIns.getDurationInMillis() != null){
            return TimeUtils.toTimeString_EN(histIns.getDurationInMillis());
        }
        return "";
    }

    public boolean isTodoTask(){
        return "todo".equals(status) || "claim".equals(status);
    }

    public boolean isFinishTask(){
        return "finish".equals(status) || StringUtils.isBlank(taskId);
    }

    @Override
    public void preInsert() {
    }

    @Override
    public void preUpdate() {
    }

    public HistoricProcessInstance getHisProcIns() {
        return hisProcIns;
    }

    public void setHisProcIns(HistoricProcessInstance hisProcIns) {
        this.hisProcIns = hisProcIns;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Map<String, Object> getActMap() {
        return actMap;
    }
}
