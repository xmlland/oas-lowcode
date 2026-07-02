package com.jeestudio.bpm.common.entity.gen;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeestudio.bpm.common.entity.system.DictGenView;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 代码生成列视图
 */
@Slf4j
public class GenTableColumnView implements Serializable {

    /** 字段配置 ID。 */
    private String id;
    /** 所属表单配置 ID。 */
    private String genTableId;
    /** 表单控件类型，例如 input、select、date、userSelect。 */
    private String showType;
    /** 是否在表单中独占一行。 */
    private String isOneLine;
    /** 是否允许为空。 */
    private String isNull;
    /** 字段中文说明，通常作为表单标签和列表标题。 */
    private String comments;

    @JsonProperty(value = "comments_EN")
    @JSONField(name = "comments_EN")
    /** 字段英文说明，用于多语言或英文界面展示。 */
    private String commentsEn;

    /** Java 字段表达式，关联对象字段可能包含属性路径。 */
    private String javaField;
    /** 字符串最大长度。 */
    private String maxLength;
    /** 字符串最小长度。 */
    private String minLength;
    /** 数值或日期最小值。 */
    private String minValue;
    /** 数值或日期最大值。 */
    private String maxValue;
    /** 面向前端展示的数据库类型说明。 */
    private String friendlyJdbcType;
    /** Java 类型，例如 String、Date、User、Office。 */
    private String javaType;
    /** 数据库物理类型，例如 varchar(255)、numeric、timestamp。 */
    private String jdbcType;
    /** 查询匹配方式，例如等于、模糊、范围。 */
    private String queryType;
    /** 校验类型，例如 number、email、url。 */
    private String validateType;
    /** 是否显示在表单内容区域。 */
    private String isForm;
    /** 是否作为查询条件。 */
    private String isQuery;

    /** 列表列对齐方式。 */
    private String align;
    /** 是否显示在列表页。 */
    private String isList;
    /** 表单区域排序。 */
    private String formSort;
    /** 查询区域排序。 */
    private String searchSort;
    /** 是否只读。 */
    private String isReadonly;
    /** 默认值。 */
    private String defaultValue;
    /** 数据库字段名。 */
    private String name;
    /** 列表区域排序。 */
    private String listSort;
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
    /** 系统字典类型编码。 */
    private String dictType;
    /** 字段统计、分组等扩展设置 JSON。 */
    private String settings;
    /** 字典项视图列表，供前端渲染系统字典控件。 */
    private List<DictGenView> dictList;
    /** 日期格式类型，例如 yyyy-MM-dd、yyyy-MM。 */
    private String dateType;
    /** 是否参与导入。 */
    private String isImport;
    /** 是否参与导出。 */
    private String isExport;
    /** 扩展参数 1，保留给字段级自定义能力。 */
    private String blockChainParam1;
    /** 扩展参数 2，保留给字段级自定义能力。 */
    private String blockChainParam2;
    /** 扩展参数 3，保留给字段级自定义能力。 */
    private String blockChainParam3;
    /** 扩展参数 4，保留给字段级自定义能力。 */
    private String blockChainParam4;
    /** 扩展参数 5，保留给字段级自定义能力。 */
    private String blockChainParam5;
    /** 扩展参数 6，保留给字段级自定义能力。 */
    private String blockChainParam6;
    /** 字段备注，部分运行时规则会从备注中读取别名配置。 */
    private String remarks;

    /** 自定义选择是否以简单下拉方式展示。 */
    private String selectSimple = "0";
    /** 自定义选择 valueField 字段中存储的值。 */
    private String selectValuefield;
    /** 自定义选择过滤条件。 */
    private String selectDsf;
    /** 自定义选择排序子句。 */
    private String selectOrder;


    /** 列表配置 JSON，控制列表列宽、dataIndex、排序等行为。 */
    private String listConfig;
    /** 表单项配置 JSON，控制控件属性、弹窗选择、校验等行为。 */
    private String formItemConfig;
    /** 字段级扩展 JavaScript。 */
    private String extendJs;

    public void setId(String id) {
        this.id = id;
    }

    public void setGenTableId(String genTableId) {
        this.genTableId = genTableId;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public void setIsOneLine(String isOneLine) {
        this.isOneLine = isOneLine;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setJavaField(String javaField) {
        this.javaField = javaField;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    public void setIsForm(String isForm) {
        this.isForm = isForm;
    }

    public void setIsQuery(String isQuery) {
        this.isQuery = isQuery;
    }

    public void setIsList(String isList) {
        this.isList = isList;
    }

    public void setFormSort(String formSort) {
        this.formSort = formSort;
    }

    public void setSearchSort(String searchSort) {
        this.searchSort = searchSort;
    }

    public void setIsReadonly(String isReadonly) {
        this.isReadonly = isReadonly;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setListSort(String listSort) {
        this.listSort = listSort;
    }

    public String getId() {
        return id;
    }

    public String getGenTableId() {
        return genTableId;
    }

    public String getShowType() {
        return showType==null?"":showType;
    }

    public String getIsOneLine() {
        return isOneLine==null?"":isOneLine;
    }

    public String getIsNull() {
        return isNull==null?"":isNull;
    }

    public String getComments() {
        return comments;
    }

    public String getCommentsEn() {
        return commentsEn;
    }

    public void setCommentsEn(String commentsEn) {
        this.commentsEn = commentsEn;
    }

    public String getJavaField() {
        return javaField;
    }

    public String getMaxLength() {
        return maxLength==null?"":maxLength;
    }

    public String getMinLength() {
        return minLength==null?"":minLength;
    }

    public String getMinValue() {
        return minValue==null?"":minValue;
    }

    public String getMaxValue() {
        return maxValue==null?"":maxValue;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getValidateType() {
        return validateType==null?"":validateType;
    }

    public String getIsForm() {
        return isForm;
    }

    public String getIsQuery() {
        return isQuery;
    }

    public String getIsList() {
        return isList;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getFormSort() {
        String sort = "";
        if(this.formSort != null){
            int num = this.formSort.indexOf(".");
            if(num != -1){
                sort = this.formSort.substring(0,num);
            }else {
                sort = this.formSort;
            }
        }
        return sort;
    }

    public String getSearchSort() {
        String sort = "";
        if(this.searchSort != null){
            int num = this.searchSort.indexOf(".");
            if(num != -1){
                sort = this.searchSort.substring(0,num);
            }else {
                sort = this.searchSort;
            }
        }
        return sort;
    }

    public String getIsReadonly() {
        return isReadonly;
    }

    public String getDefaultValue() {
        return defaultValue==null?"":defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getListSort() {
        String sort = "";
        if(this.listSort != null){
            int num = this.listSort.indexOf(".");
            if(num != -1){
                sort = this.listSort.substring(0,num);
            }else {
                sort = this.listSort;
            }
        }
        return sort;
    }

    public void setFriendlyJdbcType(String friendlyJdbcType) {
        this.friendlyJdbcType = friendlyJdbcType;
    }

    public String getTableName() {
        return tableName==null?"":tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldLabels() {
        return fieldLabels==null?"":fieldLabels;
    }

    public void setFieldLabels(String fieldLabels) {
        this.fieldLabels = fieldLabels;
    }

    public String getFieldKeys() {
        return fieldKeys==null?"":fieldKeys;
    }

    public void setFieldKeys(String fieldKeys) {
        this.fieldKeys = fieldKeys;
    }

    public String getSearchLabel() {
        return searchLabel==null?"":searchLabel;
    }

    public void setSearchLabel(String searchLabel) {
        this.searchLabel = searchLabel;
    }

    public String getSearchKey() {
        return searchKey==null?"":searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getDictType() {
        return dictType==null?"":dictType;
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

    public List<DictGenView> getDictList() {
        if(this.dictList == null){
            this.dictList = new ArrayList<>();
        }
        return dictList;
    }

    public void setDictList(List<DictGenView> dictList) {
        this.dictList = dictList;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public GenTableColumnFormItemConfig getGenTableColumnFormItemConfig() {
        try {
            String nullValue = "null";
            GenTableColumnFormItemConfig config = new GenTableColumnFormItemConfig();
            if (this.formItemConfig != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(this.formItemConfig);

                JsonNode theNode = rootNode.path("colProps").path("span");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSpan(theNode.asText());
                }
                theNode = rootNode.path("formItemProps").path("extra");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setExtra(theNode.asText());
                }

                theNode = rootNode.path("formControlProps").path("placeholder");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setPlaceholder(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("min");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMin(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("max");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMax(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("tableFilterData");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTableFilterData(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("formatFuncStr");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFormatFuncStr(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("minValue");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMinValue(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("maxValue");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMaxValue(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("dataScope");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setDataScope(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetOrgId");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTargetOrgId(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("modalWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModalWidth(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("hideLoginName");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setHideLoginName(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("freeChoice");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFreeChoice(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("showRank");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setShowRank(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("rootAreaId");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setRootAreaId(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("accepts");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setAccepts(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("fileCount");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFileCount(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("maxSize");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMaxSize(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetFormNo");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTargetFormNo(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTargetField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("columns");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setColumns(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("searchLabelWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSearchLabelWidth(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("searchConfig");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSearchConfig(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("modalTitle");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModalTitle(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("formUpdateMap");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFormUpdateMap(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("nameDataIndex");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setNameDataIndex(theNode.asText());
                }
            }
            return config;
        } catch (Exception e) {
            log.warn("genTableColumn," + e.getMessage());
            return new GenTableColumnFormItemConfig();
        }
    }
}
