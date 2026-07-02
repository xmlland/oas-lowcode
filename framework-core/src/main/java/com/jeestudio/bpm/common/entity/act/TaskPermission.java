package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 工作流任务权限
 */
public class TaskPermission extends DataEntity<TaskPermission> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 权限名称。 */
    private String name;
    /** 权限类型。 */
    private String type;
    /** 权限分类编码。 */
    private String category;
    /** 是否启用。 */
    private String isInuse;
    /** 权限作用位置，例如表单、按钮、字段或列表。 */
    private String position;
    /** 权限说明。 */
    private String describe;
    /** 主要操作配置。 */
    private String operation;
    /** 表格或子表操作配置。 */
    private String tableOperation;
    /** 扩展操作配置。 */
    private String extendOperation;
    /** 规则参数 JSON 或表达式。 */
    private String ruleArgs;
    /** 权限分类显示名称。 */
    private String categoryLabel;

    public TaskPermission() {
        super();
    }

    public TaskPermission(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIsInuse() {
        return isInuse;
    }

    public void setIsInuse(String isInuse) {
        this.isInuse = isInuse;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = Encodes.unescapeHtml(operation);
    }

    public String getRuleArgs() {
        if (StringUtils.isNoneBlank(ruleArgs)) {
            ruleArgs = Encodes.unescapeHtml(ruleArgs);
        }
        return ruleArgs;
    }

    public void setRuleArgs(String ruleArgs) {
        this.ruleArgs = ruleArgs;
    }

    public String getTableOperation() {
        return tableOperation;
    }

    public void setTableOperation(String tableOperation) {
        this.tableOperation = tableOperation;
    }

    public String getExtendOperation() {
        return extendOperation;
    }

    public void setExtendOperation(String extendOperation) {
        this.extendOperation = extendOperation;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }
}
