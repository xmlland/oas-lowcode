package com.jeestudio.bpm.common.entity.gen;

import lombok.Data;

/**
 * @Description: 多对多关系扩展规则
 */
@Data
public class GenTableExtRuleManyToMany implements FormItemConfigGetter {
    //用于在表单构造一个多选框，对应一个多对多关系表
    /*
    [{
        "name":"dictViewList01",
        "required":"false",
        "label":"组织成员",
        "relTable":"sys_org_user",
        "relColumn":"org_id",
        "relManyColumn":"user_id",
        "formNo":"sys_user",
        "searchKey":"name",
        "searchLabel":"姓名",
        "modalWidth":"1200",
        "dsfPlus":"and a.useable='1' and (a.is_sys is null or a.is_sys != '1')",
        "codeFiled":"s01",
        "codeFiledTitle":"登录名",
        "codeFiledAlign":"left",
        "nameFiled":"s04",
        "nameFiledTitle":"姓名",
        "nameFiledAlign":"left"
    }]
     */
    /** 多对多控件字段名。 */
    private String name;
    /** 是否必填。 */
    private String required = "false";
    /** 表单项显示标签。 */
    private String label;
    /** 多对多关系表名。 */
    private String relTable;
    /** 关系表中指向当前主表的字段。 */
    private String relColumn;
    /** 关系表中指向被选择数据的字段。 */
    private String relManyColumn;
    /** 弹窗选择目标表单编号。 */
    private String formNo;
    /** 弹窗查询字段集合。 */
    private String searchKey;
    /** 弹窗查询标签集合。 */
    private String searchLabel;
    /** 弹窗宽度。 */
    private String modalWidth;
    /** 弹窗数据过滤条件扩展。 */
    private String dsfPlus;
    /** 代码列字段。 */
    private String codeFiled;
    /** 代码列标题。 */
    private String codeFiledTitle;
    /** 代码列对齐方式。 */
    private String codeFiledAlign = "left";
    /** 名称列字段。 */
    private String nameFiled;
    /** 名称列标题。 */
    private String nameFiledTitle;
    /** 名称列对齐方式。 */
    private String nameFiledAlign = "left";


    /** 是否复用 GenTable 生成的查询 SQL。 */
    private String genTableSql = "";
    /** 表单项配置 JSON。 */
    private String formItemConfig;


    @Override
    public GenTableColumnFormItemConfig getGenTableColumnFormItemConfig() {
        return  formatGenTableColumnFormItemConfig(this.formItemConfig);
    }


}
