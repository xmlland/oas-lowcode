package com.jeestudio.bpm.common.entity.gen;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 代码生成表视图
 */
@Slf4j
public class GenTableView implements Serializable {

    /** 表单配置 ID。 */
    private String id;
    /** 数据库表名或动态表单业务表名。 */
    private String name;
    /** 子表关联的父表名。 */
    private String parentTable;
    /** 子表关联父表的外键字段。 */
    private String parentTableFk;
    /** 表中文说明，通常用于运行页标题。 */
    private String comments;

    @JsonProperty(value = "comments_EN")
    @JSONField(name = "comments_EN")
    /** 表英文说明，用于多语言或英文界面展示。 */
    private String commentsEn;

    /** 表单类型：普通表、树表、左树右表等。 */
    private String tableType;

    /** 是否生成移动端入口。 */
    private String isMobile;
    /** 移动端菜单或首页图标。 */
    private String mobileIcon;
    /** 流程定义分类，存在时运行页按流程表单处理。 */
    private String processDefinitionCategory;
    /** 绑定的流程模型名称。 */
    private String processModelName;
    /** 列表是否横向滚动。 */
    private String isScroll;
    /** 是否启用密级或保密相关能力。 */
    private String isBuildSecret;
    /** 是否启用行内编辑。 */
    private String isRowedit;
    /** 是否生成导入入口。 */
    private String isBuildImport;
    /** 是否生成导出入口。 */
    private String isBuildExport;
    /** 导出字段配置。 */
    private String exportList;
    /** 导出模板路径。 */
    private String exportTemplatePath;
    /** 导出规则名称。 */
    private String exportRuleName;
    /** 导入字段配置。 */
    private String importList;
    /** 导入模板文件名。 */
    private String importTemplateFile;
    /** 导出模板文件名。 */
    private String exportTemplateFile;
    /** 子表视图配置列表。 */
    private List<GenTableChildren> children;
    /** 前端模板扩展片段。 */
    private String extJsp;
    /** JavaScript 扩展片段。 */
    private String extJs;
    /** Java 扩展规则片段。 */
    private String extJava;
    /** 是否为自定义表单配置。 */
    private String isCustom;
    /** 扩展参数 1，保留给项目级自定义能力。 */
    private String blockChainParam1;
    /** 扩展参数 2，保留给项目级自定义能力。 */
    private String blockChainParam2;
    /** 扩展参数 3，保留给项目级自定义能力。 */
    private String blockChainParam3;
    /** 扩展参数 4，保留给项目级自定义能力。 */
    private String blockChainParam4;
    /** 动态表单编辑页打开方式：空或 1 为弹窗，2 为页面编辑，3 为抽屉。 */
    private String blockChainParam5;
    /** 动态表单页面说明文案，为空时不显示说明。 */
    private String blockChainParam6;
    /** 所属业务模块标识。 */
    private String module;
    /** 是否启用版本留痕表。 */
    private String isVersion;

    /** 表单页面属性 JSON，对应 GenTableFormProps。 */
    private String formProps;

    /** 运行时扩展 JavaScript。 */
    private String extendJs;

    /** 表单排序值。 */
    private String tableSort;
    /** 主键字段名配置，默认为 id。 */
    private String pkColumnName;



    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentTable(String parentTable) {
        this.parentTable = parentTable;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCommentsEn(String commentsEn) {
        this.commentsEn = commentsEn;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public void setIsMobile(String isMobile) {
        this.isMobile = isMobile;
    }

    public void setMobileIcon(String mobileIcon) {
        this.mobileIcon = mobileIcon;
    }

    public void setChildren(List<GenTableChildren> children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentTable() {
        return parentTable;
    }

    public String getComments() {
        return comments;
    }

    public String getCommentsEn() {
        return commentsEn;
    }

    public String getIsMobile() {
        return isMobile;
    }

    public String getMobileIcon() {
        return mobileIcon;
    }

    public List<GenTableChildren> getChildren() {
        return children;
    }

    public String getProcessDefinitionCategory() {
        return processDefinitionCategory;
    }

    public void setProcessDefinitionCategory(String processDefinitionCategory) {
        this.processDefinitionCategory = processDefinitionCategory;
    }

    public String getProcessModelName() {
        return processModelName;
    }

    public void setProcessModelName(String processModelName) {
        this.processModelName = processModelName;
    }

    public String getIsScroll() {
        return isScroll;
    }

    public void setIsScroll(String isScroll) {
        this.isScroll = isScroll;
    }

    public String getIsBuildSecret() {
        return isBuildSecret;
    }

    public void setIsBuildSecret(String isBuildSecret) {
        this.isBuildSecret = isBuildSecret;
    }

    public String getIsRowedit() {
        return isRowedit;
    }

    public void setIsRowedit(String isRowedit) {
        this.isRowedit = isRowedit;
    }

    public String getParentTableFk() {
        return parentTableFk;
    }

    public void setParentTableFk(String parentTableFk) {
        this.parentTableFk = parentTableFk;
    }

    public String getIsBuildImport() {
        return isBuildImport;
    }

    public void setIsBuildImport(String isBuildImport) {
        this.isBuildImport = isBuildImport;
    }

    public String getIsBuildExport() {
        return isBuildExport;
    }

    public void setIsBuildExport(String isBuildExport) {
        this.isBuildExport = isBuildExport;
    }

    public String getExportList() {
        return exportList;
    }

    public void setExportList(String exportList) {
        this.exportList = exportList;
    }

    public String getExportTemplatePath() {
        return exportTemplatePath;
    }

    public void setExportTemplatePath(String exportTemplatePath) {
        this.exportTemplatePath = exportTemplatePath;
    }

    public String getExportRuleName() {
        return exportRuleName;
    }

    public void setExportRuleName(String exportRuleName) {
        this.exportRuleName = exportRuleName;
    }

    public String getImportList() {
        return importList;
    }

    public void setImportList(String importList) {
        this.importList = importList;
    }

    public String getImportTemplateFile() {
        return importTemplateFile;
    }

    public void setImportTemplateFile(String importTemplateFile) {
        this.importTemplateFile = importTemplateFile;
    }

    public String getExportTemplateFile() {
        return exportTemplateFile;
    }

    public void setExportTemplateFile(String exportTemplateFile) {
        this.exportTemplateFile = exportTemplateFile;
    }

    public String getExtJsp() {
        return extJsp;
    }

    public void setExtJsp(String extJsp) {
        this.extJsp = extJsp;
    }

    public String getExtJs() {
        return extJs;
    }

    public void setExtJs(String extJs) {
        this.extJs = extJs;
    }

    public String getExtJava() {
        return extJava;
    }

    public void setExtJava(String extJava) {
        this.extJava = extJava;
    }

    public String getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(String isCustom) {
        this.isCustom = isCustom;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getIsVersion() {
        return isVersion;
    }

    public void setIsVersion(String isVersion) {
        this.isVersion = isVersion;
    }

    public String getFormProps() {
        return formProps;
    }

    public void setFormProps(String formProps) {
        this.formProps = formProps;
    }

    public String getExtendJs() {
        return extendJs;
    }

    public void setExtendJs(String extendJs) {
        this.extendJs = extendJs;
    }

    public String getTableSort() {
        return tableSort;
    }

    public void setTableSort(String tableSort) {
        this.tableSort = tableSort;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public void setPkColumnName(String pkColumnName) {
        this.pkColumnName = pkColumnName;
    }

    public GenTableFormProps getGenTableFormProps() {
        try {
            String nullValue = "null";
            GenTableFormProps config = new GenTableFormProps();
            if (this.formProps != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(this.formProps);

                JsonNode theNode = rootNode.path("modal__Full");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModal__Full(theNode.asText());
                }
                theNode = rootNode.path("modal__Width");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModal__Width(theNode.asText());
                }
                theNode = rootNode.path("list__modalTitle");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setList__modalTitle(theNode.asText());
                }
                theNode = rootNode.path("list__modalTitleFuncStr");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setList__modalTitleFuncStr(theNode.asText());
                }
                theNode = rootNode.path("labelWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setLabelWidth(theNode.asText());
                }
                theNode = rootNode.path("mainTableTitle");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMainTableTitle(theNode.asText());
                }
                theNode = rootNode.path("subTableType");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSubTableType(theNode.asText());
                }
                theNode = rootNode.path("anchorWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setAnchorWidth(theNode.asText());
                }
                theNode = rootNode.path("anchorLocation");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setAnchorLocation(theNode.asText());
                }
                theNode = rootNode.path("getExtendSaveDataFuncStr");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setGetExtendSaveDataFuncStr(theNode.asText());
                }
            }
            return config;
        } catch (Exception e) {
            log.warn("genTable," + e.getMessage());
            return new GenTableFormProps();
        }
    }
}
