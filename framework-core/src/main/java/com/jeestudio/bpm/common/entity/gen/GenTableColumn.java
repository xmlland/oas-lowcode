package com.jeestudio.bpm.common.entity.gen;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Description: 表单配置，数据表字段元数据定义
 */
@Slf4j
public class GenTableColumn extends DataEntity<GenTableColumn> implements FormItemConfigGetter {

    private static final long serialVersionUID = 1L;

    /** 所属表单配置。 */
    private GenTable genTable;
    /** 数据库字段名。 */
    private String name;
    /** 字段中文说明，通常作为表单标签和列表标题。 */
    private String comments;

    //多语言切换时，转换成前端方便处理的属性名称
    @JsonProperty(value = "comments_EN")
    @JSONField(name = "comments_EN")
    /** 字段英文说明，用于多语言或英文界面展示。 */
    private String commentsEn;

    /** 数据库物理类型，例如 varchar(255)、numeric、timestamp。 */
    private String jdbcType;
    /** Java 类型，例如 String、Date、User、Office。 */
    private String javaType;
    /** Java 字段表达式，关联对象字段可能包含属性路径。 */
    private String javaField;
    /** 是否主键字段。 */
    private String isPk;
    /** 新增时是否写入该字段。 */
    private String isInsert;
    /** 编辑时是否允许修改该字段。 */
    private String isEdit;
    /** 是否显示在表单内容区域。 */
    private String isForm;
    /** 是否显示在列表页。 */
    private String isList;
    /** 是否作为查询条件。 */
    private String isQuery;
    /** 查询匹配方式，例如等于、模糊、范围。 */
    private String queryType;
    /** 表单控件类型，例如 input、select、date、userSelect。 */
    private String showType;
    /** 系统字典类型编码。 */
    private String dictType;

    /** 字段统计、分组等扩展设置 JSON。 */
    private String settings;

    /** 字段基础排序。 */
    private Integer sort;
    /** 自定义选择或关联查询的目标表名。 */
    private String tableName;
    /** 自定义选择显示列标题集合。 */
    private String fieldLabels;
    /** 自定义选择显示列字段集合。 */
    private String fieldKeys;
    /** 自定义选择查询区标签集合。 */
    private String searchLabel;
    /** 自定义选择查询区字段集合。 */
    private String searchKey;
    /** 是否允许为空。 */
    private String isNull;
    /** 校验类型，例如 number、email、url。 */
    private String validateType;
    /** 字符串最小长度。 */
    private String minLength;
    /** 字符串最大长度。 */
    private String maxLength;
    /** 数值或日期最小值。 */
    private String minValue;
    /** 数值或日期最大值。 */
    private String maxValue;
    /** 是否在表单中独占一行。 */
    private String isOneLine;
    /** 魔法逻辑或动态规则表达式。 */
    private String magicLogic;
    /** 列表列对齐方式。 */
    private String align;
    /** 字段物理名称类型，用于设计器字段命名策略。 */
    private String jdbcNameType;
    /** 表单区域排序。 */
    private Integer formSort;
    /** 查询区域排序。 */
    private Integer searchSort;
    /** 列表区域排序。 */
    private Integer listSort;
    /** 字段可见性配置。 */
    private String visible;
    /** 可选数据库类型集合。 */
    private String jdbcTypes;
    /** 是否只读。 */
    private String isReadonly;
    /** 默认值。 */
    private String defaultValue = "";
    /** 日期格式类型，例如 yyyy-MM-dd、yyyy-MM。 */
    private String dateType;
    /** 是否参与导入。 */
    private String isImport;
    /** 是否参与导出。 */
    private String isExport;
    /** 扩展参数 1，保留给字段级自定义能力。 */
    private String blockChainParam1 = "0";
    /** 扩展参数 2，保留给字段级自定义能力。 */
    private String blockChainParam2 = "0";
    /** 扩展参数 3，保留给字段级自定义能力。 */
    private String blockChainParam3 = "0";
    /** 扩展参数 4，保留给字段级自定义能力。 */
    private String blockChainParam4;
    /** 扩展参数 5，保留给字段级自定义能力。 */
    private String blockChainParam5;
    /** 扩展参数 6，保留给字段级自定义能力。 */
    private String blockChainParam6;

    /** 自定义选择是否用下拉 */
    private String selectSimple = "0";
    /** valueField字段中存的值 */
    private String selectValuefield;
    /** 过滤条件 */
    private String selectDsf;
    /** 排序子句 */
    private String selectOrder;


    /** 列表配置 JSON，控制列表列宽、dataIndex、排序等行为。 */
    private String listConfig;
    /** 表单项配置 JSON，控制控件属性、弹窗选择、校验等行为。 */
    private String formItemConfig;
    /** 字段级扩展 JavaScript。 */
    private String extendJs;

    public GenTableColumn() {
    }

    public GenTableColumn(String id) {
        super(id);
    }

    public GenTableColumn(GenTable genTable) {
        this.genTable = genTable;
    }

    public GenTable getGenTable() {
        return this.genTable;
    }

    public void setGenTable(GenTable genTable) {
        this.genTable = genTable;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCommentsEn() {
        return commentsEn;
    }

    public void setCommentsEn(String commentsEn) {
        this.commentsEn = commentsEn;
    }

    public String getJdbcType() {
        return StringUtil.lowerCase(this.jdbcType);
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getJavaType() {
        return this.javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaField() {
        return this.javaField;
    }

    public void setJavaField(String javaField) {
        this.javaField = javaField;
    }

    public String getIsPk() {
        return this.isPk;
    }

    public void setIsPk(String isPk) {
        this.isPk = isPk;
    }

    public String getIsNull() {
        return this.isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public String getIsInsert() {
        return this.isInsert;
    }

    public void setIsInsert(String isInsert) {
        this.isInsert = isInsert;
    }

    public String getIsEdit() {
        return this.isEdit;
    }

    public void setIsEdit(String isEdit) {
        this.isEdit = isEdit;
    }

    public void setIsForm(String isForm) {
        this.isForm = isForm;
    }

    public String getIsForm() {
        return this.isForm;
    }

    public String getIsList() {
        return this.isList;
    }

    public void setIsList(String isList) {
        this.isList = isList;
    }

    public String getIsQuery() {
        return this.isQuery;
    }

    public void setIsQuery(String isQuery) {
        this.isQuery = isQuery;
    }

    public String getQueryType() {
        return this.queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getShowType() {
        return this.showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getDictType() {
        return this.dictType == null ? "" : this.dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Integer getSort() {
        return this.sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getNameAndComments() {
        return getName() + (this.comments == null ? "" : new StringBuilder("  :  ").append(this.comments).toString());
    }

    public String getDataLength() {
        String[] ss = StringUtil.split(StringUtil.substringBetween(getJdbcType(), "(", ")"), ",");
        if ((ss != null) && (ss.length == 1)) {
            return ss[0];
        }
        return "0";
    }

    public String getSimpleJavaType() {
        if ("This".equals(getJavaType())) {
            return StringUtil.capitalize(this.genTable.getClassName());
        }
        return StringUtil.indexOf(getJavaType(), ".") != -1 ?
                StringUtil.substringAfterLast(getJavaType(), ".") :
                getJavaType();
    }

    public String getSimpleJavaField() {
        return StringUtil.substringBefore(getJavaField(), ".");
    }

    public String getJavaFieldId() {
        return StringUtil.substringBefore(getJavaField(), "|");
    }

    public String getFriendlyJavaFieldId() {
        String value = StringUtil.substringBefore(getJavaField(), "|");
        if (value.indexOf(".") == -1) {
            value = this.name;
        } else {
            value = this.name + ".id";
        }
        return value;
    }

    public String getJavaFieldName() {
        String[][] ss = getJavaFieldAttrs();
        return ss.length > 0 ? getSimpleJavaField() + "." + ss[0][0] : "";
    }

    public String[][] getJavaFieldAttrs() {
        String[] ss = StringUtil.split(StringUtil.substringAfter(getJavaField(), "|"), "|");
        String[][] sss = new String[ss.length][2];
        if (ss != null) {
            for (int i = 0; i < ss.length; i++) {
                sss[i][0] = ss[i];
                sss[i][1] = StringUtil.toUnderScoreCase(ss[i]);
            }
        }
        return sss;
    }

    public List<String> getAnnotationList() {
        List list = (List) Lists.newArrayList();

        if ("This".equals(getJavaType())) {
            list.add("com.fasterxml.jackson.annotation.JsonBackReference");
        }
        if ("java.util.Date".equals(getJavaType())) {
            if (getComments().indexOf("日期") != -1 || getComments().indexOf("date") != -1) {
                list.add("com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd\",timezone=\"GMT+8\")");
            } else {
                list.add("com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\",timezone=\"GMT+8\")");
            }
        }

        if ((false == "1".equals(getIsNull())) && (false == "String".equals(getJavaType()))) {
            list.add("jakarta.validation.constraints.NotNull(message=\"" + getComments() + " can not be empty\")");
        } else if ((false == "1".equals(getIsNull())) && ("String".equals(getJavaType())) &&
                (this.minLength != null) && (!this.minLength.equals(""))) {
            list.add("org.hibernate.validator.constraints.Length(min=" + this.minLength + ", max=" + this.maxLength +
                    ", message=\"" + getComments() + " length must be between " + this.minLength + " and " + this.maxLength + "\")");
        }

        if ("email".equals(this.validateType)) {
            list.add("org.hibernate.validator.constraints.Email(message=\"" + getComments() + " must be a legal mailbox\")");
        }
        if ("url".equals(this.validateType)) {
            list.add("org.hibernate.validator.constraints.URL(message=\"" + getComments() + " must be a legal website URL\")");
        }
        if ("creditcard".equals(this.validateType)) {
            list.add("org.hibernate.validator.constraints.CreditCardNumber(message=\"" + getComments() + " must be a legal Card Number\")");
        }
        if (("number".equals(this.validateType)) || ("digits".equals(this.validateType))) {
            if ((this.minValue != null) && (!this.minValue.equals(""))) {
                if (this.minValue.contains(".")) {
                    String minv = this.minValue.replace(".", "_digitalPoint_");
                    list.add("jakarta.validation.constraints.Min(value=(long)" + minv + ",message=\"" + getComments() + " minimum value cannot be less than " + minv + "\")");
                } else {
                    list.add("jakarta.validation.constraints.Min(value=" + this.minValue + ",message=\"" + getComments() + " minimum value cannot be less than " + this.minValue + "\")");
                }
            }
            if ((this.maxValue != null) && (false == this.maxValue.equals(""))) {
                if (this.maxValue.contains(".")) {
                    String maxv = this.maxValue.replace(".", "_digitalPoint_");
                    list.add("jakarta.validation.constraints.Max(value=(long)" + maxv + ",message=\"" + getComments() + " maximum value cannot exceed " + maxv + "\")");
                } else {
                    list.add("jakarta.validation.constraints.Max(value=" + this.maxValue + ",message=\"" + getComments() + " maximum value cannot exceed " + this.maxValue + "\")");
                }
            }
        }
        return list;
    }

    public List<String> getSimpleAnnotationList() {
        List list = (List) Lists.newArrayList();
        for (String ann : getAnnotationList()) {
            String anno = StringUtil.substringAfterLast(ann, ".");
            anno = anno.replace("_digitalPoint_", ".");
            list.add(anno);
        }
        return list;
    }

    public Boolean getIsNotBaseField() {
        if (StringUtil.equals(getSimpleJavaField(), "id")
                || StringUtil.equals(getSimpleJavaField(), "remarks")
                || StringUtil.equals(getSimpleJavaField(), "createBy")
                || StringUtil.equals(getSimpleJavaField(), "createDate")
                || StringUtil.equals(getSimpleJavaField(), "updateBy")
                || StringUtil.equals(getSimpleJavaField(), "updateDate")
                || StringUtil.equals(getSimpleJavaField(), "delFlag")) {
            return Boolean.valueOf(false);
        } else {
            return Boolean.valueOf(true);
        }
    }

    public Boolean getIsNotTreeBaseField() {
        if (StringUtil.equals(getSimpleJavaField(), "id")
                || StringUtil.equals(getSimpleJavaField(), "remarks")
                || StringUtil.equals(getSimpleJavaField(), "createBy")
                || StringUtil.equals(getSimpleJavaField(), "createDate")
                || StringUtil.equals(getSimpleJavaField(), "updateBy")
                || StringUtil.equals(getSimpleJavaField(), "updateDate")
                || StringUtil.equals(getSimpleJavaField(), "delFlag")
                || StringUtil.equals(getSimpleJavaField(), "parent")
                || StringUtil.equals(getSimpleJavaField(), "parentIds")
                || StringUtil.equals(getSimpleJavaField(), "name")
                || StringUtil.equals(getSimpleJavaField(), "sort")) {
            return Boolean.valueOf(false);
        } else {
            return Boolean.valueOf(true);
        }
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setFieldLabels(String fieldLabels) {
        this.fieldLabels = fieldLabels;
    }

    public String getFieldLabels() {
        return this.fieldLabels;
    }

    public void setFieldKeys(String fieldKeys) {
        this.fieldKeys = fieldKeys;
    }

    public String getFieldKeys() {
        return this.fieldKeys;
    }

    public void setSearchLabel(String searchLabel) {
        this.searchLabel = searchLabel;
    }

    public String getSearchLabel() {
        return this.searchLabel;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getSearchKey() {
        return this.searchKey;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }

    public String getMinLength() {
        return this.minLength;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    public String getValidateType() {
        return this.validateType;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public String getMaxLength() {
        return this.maxLength;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMinValue() {
        return this.minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getMaxValue() {
        return this.maxValue;
    }

    public void setIsOneLine(String isOneLine) {
        this.isOneLine = isOneLine;
    }

    public String getIsOneLine() {
        return this.isOneLine;
    }

    public String getMagicLogic() {
        return magicLogic;
    }

    public void setMagicLogic(String magicLogic) {
        this.magicLogic = magicLogic;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getJdbcNameType() {
        return jdbcNameType;
    }

    public void setJdbcNameType(String jdbcNameType) {
        this.jdbcNameType = jdbcNameType;
    }

    public Integer getFormSort() {
        return formSort;
    }

    public void setFormSort(Integer formSort) {
        this.formSort = formSort;
    }

    public Integer getSearchSort() {
        return searchSort;
    }

    public void setSearchSort(Integer searchSort) {
        this.searchSort = searchSort;
    }

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getJdbcTypes() {
        return jdbcTypes;
    }

    public void setJdbcTypes(String jdbcTypes) {
        this.jdbcTypes = jdbcTypes;
    }

    public Integer getListSort() {
        return listSort;
    }

    public void setListSort(Integer listSort) {
        this.listSort = listSort;
    }

    public String getIsReadonly() {
        return isReadonly;
    }

    public void setIsReadonly(String isReadonly) {
        this.isReadonly = isReadonly;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getIsImport() {
        return isImport;
    }

    public void setIsImport(String isImport) {
        this.isImport = isImport;
    }

    public String getIsExport() {
        return isExport;
    }

    public void setIsExport(String isExport) {
        this.isExport = isExport;
    }

    public String getBlockChainParam1() {
        return blockChainParam1;
    }

    public void setBlockChainParam1(String blockChainParam1) {
        this.blockChainParam1 = blockChainParam1;
    }

    public String getBlockChainParam2() {
        return blockChainParam2;
    }

    public void setBlockChainParam2(String blockChainParam2) {
        this.blockChainParam2 = blockChainParam2;
    }

    public String getBlockChainParam3() {
        return blockChainParam3;
    }

    public void setBlockChainParam3(String blockChainParam3) {
        this.blockChainParam3 = blockChainParam3;
    }

    public String getBlockChainParam4() {
        return blockChainParam4;
    }

    public void setBlockChainParam4(String blockChainParam4) {
        this.blockChainParam4 = blockChainParam4;
    }

    public String getBlockChainParam5() {
        return blockChainParam5;
    }

    public void setBlockChainParam5(String blockChainParam5) {
        this.blockChainParam5 = blockChainParam5;
    }

    public String getBlockChainParam6() {
        return blockChainParam6;
    }

    public void setBlockChainParam6(String blockChainParam6) {
        this.blockChainParam6 = blockChainParam6;
    }

    public String getNamePlus() {
        return StringUtil.toCamelCase(this.name);
    }

    public String getSelectSimple() {
        return selectSimple;
    }

    public void setSelectSimple(String selectSimple) {
        this.selectSimple = selectSimple;
    }

    public String getSelectValuefield() {
        return selectValuefield;
    }

    public void setSelectValuefield(String selectValuefield) {
        this.selectValuefield = selectValuefield;
    }

    public String getSelectDsf() {
        return selectDsf;
    }

    public void setSelectDsf(String selectDsf) {
        this.selectDsf = selectDsf;
    }

    public String getSelectOrder() {
        return selectOrder;
    }

    public void setSelectOrder(String selectOrder) {
        this.selectOrder = selectOrder;
    }


    public String getListConfig() {
        return listConfig;
    }

    public void setListConfig(String listConfig) {
        this.listConfig = listConfig;
    }

    public String getFormItemConfig() {
        return formItemConfig;
    }

    public void setFormItemConfig(String formItemConfig) {
        this.formItemConfig = formItemConfig;
    }

    public String getExtendJs() {
        return extendJs;
    }

    public void setExtendJs(String extendJs) {
        this.extendJs = extendJs;
    }

    //日期字段别名，表单设计，规则；格式举例： alias:xxx,其他规则表达式，或者alias:xxx
    public String getAlias() {
        String alias = null;
        if (remarks != null && remarks.contains("alias:")) {
            alias = remarks.substring(remarks.indexOf("alias:") + 6);
            int index = alias.indexOf(",");
            if (index == -1) {
                index = alias.indexOf("，");
            }
            if (index != -1) {
                alias = alias.substring(0, index);
            }
        }
        return alias;
    }

    //日期字段附加字段别名，表单设计，规则；格式举例： aliasplus:xxx,其他规则表达式，或者aliasplus:xxx
    public String getAliasplus() {
        String alias = null;
        if (remarks != null && remarks.contains("aliasplus:")) {
            alias = remarks.substring(remarks.indexOf("aliasplus:") + 10);
            int index = alias.indexOf(",");
            if (index == -1) {
                index = alias.indexOf("，");
            }
            if (index != -1) {
                alias = alias.substring(0, index);
            }
        }
        return alias;
    }

    @Override
    public GenTableColumnFormItemConfig getGenTableColumnFormItemConfig() {
        return  formatGenTableColumnFormItemConfig(this.formItemConfig);
    }


    public GenTableColumnSetting getGenTableColumnSetting() {
        if (StringUtil.isEmpty(this.settings)) {
            return new GenTableColumnSetting();
        }
        return JSONObject.parseObject(this.settings, GenTableColumnSetting.class);
    }
}
