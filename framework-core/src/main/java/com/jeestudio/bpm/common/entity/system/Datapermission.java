package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 数据权限
 */
public class Datapermission extends DataEntity<Datapermission> {

    private static final long serialVersionUID = 1L;

    /** 数据权限名称。 */
    private String name;
    /** 适用主表名。 */
    private String mainTable;
    /** 数据权限表达式或 SQL 条件片段。 */
    private String expression;
    /** 排序号。 */
    private Integer sort;

    public Datapermission() {
        super();
    }

    public Datapermission(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
