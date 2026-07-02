package com.jeestudio.bpm.common.entity.common;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.system.SysMsg;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 工作流实体基类
 */
public abstract class ActEntity<T> extends DataEntity<T> {

    private static final long serialVersionUID = 1L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 流程实例 ID。 */
    private String procInsId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前流程任务名称。 */
    private String procTaskName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前任务权限配置。 */
    private TaskPermission procTaskPermission;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 流程定义 Key。 */
    private String procDefKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前用户任务节点 Key。 */
    private String userTaskKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前办理人账号或 ID。 */
    private String currentAssignee;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前流程状态。 */
    private String currentStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 当前办理人查询条件。 */
    private String currentAssigneeQuery;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 自定义排序 SQL 或排序表达式。 */
    private String customSort;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 流程实例 ID 列表，批量查询时使用。 */
    private List<String> procInsIdList;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 临时登录名集合，规则计算或候选人查询时使用。 */
    private String[] tempLoginName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 临时节点 Key，流程规则计算时使用。 */
    private String tempNodeKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 自定义节点名称。 */
    private String customNodeName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 临时规则参数类名。 */
    private String tempRuleArgsClass;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 节点规则参数 Map。 */
    private Map<String, Map<String, String>> ruleArgs = Maps.newHashMap();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：流程创建开始时间。 */
    private Date createTimeBegin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：流程创建结束时间。 */
    private Date createTimeEnd;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 工作流操作上下文。 */
    private Act act;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 公文类型。 */
    private String edocType;

    /** 流程规则变量。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JSONObject actRuleArgs;

    /** 自定义系统消息内容，在 around 的 beforeSaveZform 中构造；为空时使用系统默认消息内容。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sysMsgContent;

    /** 自定义系统消息对象，在 around 的 beforeSaveZform 中构造；为空时根据 sysMsgContent 构造。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SysMsg sysMsg;

    public ActEntity() {
        super();
    }

    public ActEntity(String id) {
        super(id);
    }

    public Act getAct() {
        if (act == null) {
            act = new Act();
        }
        return act;
    }

    public void setAct(Act act) {
        this.act = act;
    }

    public String getProcInsId() {
        return procInsId;
    }

    public void setProcInsId(String procInsId) {
        this.procInsId = procInsId;
    }

    public String getProcTaskName() {
        return procTaskName;
    }

    public void setProcTaskName(String procTaskName) {
        this.procTaskName = procTaskName;
    }

    public TaskPermission getProcTaskPermission() {
        return procTaskPermission;
    }

    public void setProcTaskPermission(TaskPermission procTaskPermission) {
        this.procTaskPermission = procTaskPermission;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getUserTaskKey() {
        return userTaskKey;
    }

    public void setUserTaskKey(String userTaskKey) {
        this.userTaskKey = userTaskKey;
    }

    public String getCurrentAssignee() {
        return currentAssignee;
    }

    public void setCurrentAssignee(String currentAssignee) {
        this.currentAssignee = currentAssignee;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCurrentAssigneeQuery() {
        return currentAssigneeQuery;
    }

    public void setCurrentAssigneeQuery(String currentAssigneeQuery) {
        this.currentAssigneeQuery = currentAssigneeQuery;
    }

    public String getCustomSort() {
        return customSort;
    }

    public void setCustomSort(String customSort) {
        this.customSort = customSort;
    }

    public String[] getTempLoginName() {
        return tempLoginName;
    }

    public void setTempLoginName(String[] tempLoginName) {
        this.tempLoginName = tempLoginName;
    }

    public Map<String, Map<String, String>> getRuleArgs() {
        return ruleArgs;
    }

    public void setRuleArgs(Map<String, Map<String, String>> ruleArgs) {
        this.ruleArgs = ruleArgs;
    }

    public String getRuleArgsJson() {
        if (ruleArgs == null) {
            return new Gson().toJson(Maps.newHashMap());
        } else {
            return new Gson().toJson(ruleArgs);
        }
    }

    public String getTempNodeKey() {
        return tempNodeKey;
    }

    public void setTempNodeKey(String tempNodeKey) {
        this.tempNodeKey = tempNodeKey;
    }

    public String getEdocType() {
        return edocType;
    }

    public void setEdocType(String edocType) {
        this.edocType = edocType;
    }

    public List<String> getProcInsIdList() {
        return procInsIdList;
    }

    public String getProcInsIdListStr() {
        return "'" + StrUtil.join("','", procInsIdList) + "'";
    }

    public void setProcInsIdList(List<String> procInsIdList) {
        this.procInsIdList = procInsIdList;
    }

    public String getCustomNodeName() {
        return customNodeName;
    }

    public void setCustomNodeName(String customNodeName) {
        this.customNodeName = customNodeName;
    }

    public String getTempRuleArgsClass() {
        return tempRuleArgsClass;
    }

    public void setTempRuleArgsClass(String tempRuleArgsClass) {
        this.tempRuleArgsClass = tempRuleArgsClass;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTimeBegin() {
        return createTimeBegin;
    }

    public void setCreateTimeBegin(Date createTimeBegin) {
        this.createTimeBegin = createTimeBegin;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public JSONObject getActRuleArgs() {
        return actRuleArgs;
    }

    public void setActRuleArgs(JSONObject actRuleArgs) {
        this.actRuleArgs = actRuleArgs;
    }

    public String getSysMsgContent() {
        return sysMsgContent;
    }

    public void setSysMsgContent(String sysMsgContent) {
        this.sysMsgContent = sysMsgContent;
    }

    public SysMsg getSysMsg() {
        return sysMsg;
    }

    public void setSysMsg(SysMsg sysMsg) {
        this.sysMsg = sysMsg;
    }
}
