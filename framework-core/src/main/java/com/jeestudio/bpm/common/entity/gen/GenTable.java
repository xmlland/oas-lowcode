package com.jeestudio.bpm.common.entity.gen;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;
//import net.sf.json.JSONArray;
import com.alibaba.fastjson.JSONArray;

import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 表单配置，数据库表元数据定义
 */
@Slf4j
public class GenTable extends DataEntity<GenTable> {

    private static final long serialVersionUID = 1L;

    public static final String SQLMAP_SQLCOLUMNS = "sqlColumns";
    public static final String SQLMAP_SQLCOLUMNS_FRIENDLY = "sqlColumnsFriendly";
    public static final String SQLMAP_SQLINSERT = "sqlInsert";
    public static final String SQLMAP_SQLINSERTV = "sqlInsertV";
    public static final String SQLMAP_SQLJOINS = "sqlJoins";
    public static final String SQLMAP_SQLUPDATE = "sqlUpdate";
    public static final String SQLMAP_SQLORDERBY = "sqlOrderby";

    private static final String DEFAULT_STATUS_START = "10";
    private static final String DEFAULT_STATUS_BREAK = "00";
    private static final String DEFAULT_STATUS_END = "99";

    public static final String TABLE_TYPE_SIMPLE = "0";
    public static final String TABLE_TYPE_TREE = "3";
    public static final String TABLE_TYPE_RIGHTTABLE = "4";

    public static final String MODULE_DATAHOUSE = "datahouse";

    /** 匿名查询接口标志，用于datamapView时优先读取 扩展SQL1 */
    public static final String EXT_FLAG_VIEW = "view";
    /** 对外提供接口标志，用于对外提供接口查询时，优先读取 扩展SQL2 */
    public static final String EXT_FLAG_OUTER = "outer";

    /** 数据库表名，可能包含 schema 前缀。 */
    private String name;
    /** 修改表名时记录的原表名，用于同步物理表结构。 */
    private String oldName;
    /** 表中文说明，通常作为动态表单标题和菜单标题来源。 */
    private String comments;

    @JsonProperty(value = "comments_EN")
    @JSONField(name = "comments_EN")
    /** 表英文说明，用于多语言或英文代码生成场景。 */
    private String commentsEn;

    /** 表单类型：普通表、树表、左树右表等。 */
    private String tableType;
    /** 生成代码时使用的 Java 类名。 */
    private String className;
    /** 子表关联的父表名。 */
    private String parentTable;
    /** 子表关联父表的外键字段。 */
    private String parentTableFk;
    /** 父表类名 */
    private String parentClassName;
    /** 父表请求路径 */
    private String parentUrlPrefix;
    /** 外键字段对应的主表simpleJavaField */
    private String parentSimpleJavaField;
    /** 外键字段对应的主表parentJavaFieldId */
    private String parentJavaFieldId;
    /** 外键字段对应的主表parentJavaFieldName */
    private String parentJavaFieldName;
    /** 是否已同步物理表结构。 */
    private String isSync;
    /** 当前表的字段配置列表。 */
    private List<GenTableColumn> columnList = (List) Lists.newArrayList();
    /** 表名模糊查询条件。 */
    private String nameLike;
    /** 主键字段列表。 */
    private List<String> pkList;
    /** 父表配置对象，子表场景按需加载。 */
    private GenTable parent;
    /** 子表配置列表。 */
    private List<GenTable> childList = (List) Lists.newArrayList();

    /** JSP 或前端模板扩展片段。 */
    private String extJsp;
    /** JavaScript 扩展片段。 */
    private String extJs;
    /** Java 代码或扩展规则片段。 */
    private String extJava;

    /** 按扩展块标识解析后的 JSP 扩展片段。 */
    private HashMap<String, String> extJspMap;
    /** 按扩展块标识解析后的 JavaScript 扩展片段。 */
    private HashMap<String, String> extJsMap;
    /** 按扩展块标识解析后的 Java 扩展片段。 */
    private HashMap<String, String> extJavaMap;

    /** 是否生成新增入口。 */
    private String isBuildAdd;
    /** 是否生成编辑入口。 */
    private String isBuildEdit;
    /** 是否生成删除入口。 */
    private String isBuildDel;
    /** 是否生成导入入口。 */
    private String isBuildImport;
    /** 是否生成操作列。 */
    private String isBuildOperate;

    /** 数据源标识。 */
    private String datasource;
    /** 是否启用版本留痕表。 */
    private String isVersion;

    /** 是否作为流程表单使用。 */
    private String isProcessDefinition;
    /** 流程定义分类，存在时视为流程表单。 */
    private String processDefinitionCategory;
    /** 绑定的流程模型名称。 */
    private String processModelName;

    /** 是否生成动态表单运行页。 */
    private String isBuildXform;
    /** 是否生成密级或保密相关能力。 */
    private String isBuildSecret;
    /** 是否生成正文内容能力。 */
    private String isBuildContent;

    /** 表单布局或页面类型。 */
    private String formType;

    /** 短文本数 */
    private String scount;
    /** 中文本数 */
    private String mcount;
    /** 长文本数 */
    private String lcount;
    /** 日期数 */
    private String dcount;

    /** 是否已发布为运行时动态表单。 */
    private String isRelease;
    /** 是否生成移动端入口。 */
    private String isMobile;

    /** 列表查询 SQL 字段片段。 */
    private String sqlColumns;
    /** 带显示名或友好别名的查询字段片段。 */
    private String sqlColumnsFriendly;
    /** 查询关联表 JOIN 片段。 */
    private String sqlJoins;
    /** 新增数据 SQL 片段。 */
    private String sqlInsert;
    /** 更新数据 SQL 片段。 */
    private String sqlUpdate;
    /** 默认排序 SQL 片段。 */
    private String sqlSort;
    /** 设计器字段 JSON。 */
    private JSONArray json = new JSONArray();
    /** 子表字段 JSON。 */
    private JSONArray children;
    /** 移动端菜单或首页图标。 */
    private String mobileIcon;

    /** 启动状态码 */
    private String statusStart;
    /** 终止状态码 */
    private String statusBreak;
    /** 结束状态码 */
    private String statusEnd;
    /** 数据库名称或数据源库名。 */
    private String dbName;
    /** 是否存在子表。 */
    private boolean hasChildren;

    /** 列表是否横向滚动。 */
    private String isScroll;
    /** 是否启用行内编辑。 */
    private String isRowedit;

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

    /** 扩展 SQL 1，常用于匿名视图或特殊查询入口。 */
    private String extSql01;
    /** 扩展 SQL 2，常用于对外接口或特殊查询入口。 */
    private String extSql02;

    /** 追加到友好查询字段中的扩展字段片段。 */
    private String sqlColumnsFriendlyExt = "";
    /** 追加到查询 JOIN 中的扩展关联片段。 */
    private String sqlJoinsExt = "";

    /** 扩展表单编号，用于运行时特殊路由或外部集成。 */
    private String formNoExt;

    /** 表单页面属性 JSON，对应 GenTableFormProps。 */
    private String formProps;

    /** 运行时扩展 JavaScript。 */
    private String extendJs;

    /** 表单排序值。 */
    private Integer tableSort = 10;
    /** 问题反馈说明。 */
    private String respondProblemInstructions;
    /** 扩展统计字段：成功处理条数。 */
    private Integer doneCount;

    /** 主键字段名配置，默认为 id，可配置为 pk_id、row_id 等合法字段名。 */
    private String pkColumnName = "id";

    public GenTable() {
    }

    public GenTable(String id) {
        super(id);
    }

    public GenTable(GenTable parent) {
        this.parent = parent;
        this.parentTable = parent.getName();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getName() {
        return StringUtils.lowerCase(this.name);
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

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getParentTable() {
        return StringUtils.lowerCase(this.parentTable);
    }

    public void setParentTable(String parentTable) {
        this.parentTable = parentTable;
    }

    public String getParentTableFk() {
        return StringUtils.lowerCase(this.parentTableFk);
    }

    public void setParentTableFk(String parentTableFk) {
        this.parentTableFk = parentTableFk;
    }

    public String getParentClassName() {
        if (StringUtils.isEmpty(this.parentClassName) && this.getParent() != null) {
            return this.getParent().getClassName();
        } else {
            return this.parentClassName;
        }
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public String getParentUrlPrefix() {
        return parentUrlPrefix;
    }

    public void setParentUrlPrefix(String parentUrlPrefix) {
        this.parentUrlPrefix = parentUrlPrefix;
    }


    public String getParentSimpleJavaField() {
        return parentSimpleJavaField;
    }

    public void setParentSimpleJavaField(String parentSimpleJavaField) {
        this.parentSimpleJavaField = parentSimpleJavaField;
    }

    public String getParentJavaFieldId() {
        return parentJavaFieldId;
    }

    public void setParentJavaFieldId(String parentJavaFieldId) {
        this.parentJavaFieldId = parentJavaFieldId;
    }

    public String getParentJavaFieldName() {
        return parentJavaFieldName;
    }

    public void setParentJavaFieldName(String parentJavaFieldName) {
        this.parentJavaFieldName = parentJavaFieldName;
    }

    public List<String> getPkList() {
        return this.pkList;
    }

    public void setPkList(List<String> pkList) {
        this.pkList = pkList;
    }

    public String getNameLike() {
        return this.nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    @JsonBackReference
    public GenTable getParent() {
        return this.parent;
    }

    public void setParent(GenTable parent) {
        this.parent = parent;
    }

    public List<GenTableColumn> getColumnList() {
        return this.columnList;
    }

    public void setColumnList(List<GenTableColumn> columnList) {
        this.columnList = columnList;
    }

    public List<GenTable> getChildList() {
        return this.childList;
    }

    public void setChildList(List<GenTable> childList) {
        this.childList = childList;
    }

    public String getNameAndComments() {
        return getName() + (this.comments == null ? "" : new StringBuilder(":").append(this.comments).toString());
    }

    public List<String> getImportGridJavaList() {
        List importList = (List) Lists.newArrayList();
        for (GenTableColumn column : getColumnList()) {
            if ((column.getTableName() != null) && (false == column.getTableName().equals(""))) {
                if ((StringUtils.indexOf(column.getJavaType(), ".") != -1) && (false == importList.contains(column.getJavaType()))) {
                    importList.add(column.getJavaType());
                }
            }
        }
        return importList;
    }

    public List<String> getImportGridJavaDaoList() {
        boolean isNeedList = false;
        List importList = (List) Lists.newArrayList();
        for (GenTableColumn column : getColumnList()) {
            if ((column.getTableName() != null) && (false == column.getTableName().equals(""))) {
                if ((StringUtils.indexOf(column.getJavaType(), ".") != -1) && (false == importList.contains(column.getJavaType()))) {
                    importList.add(column.getJavaType());
                    isNeedList = true;
                }
            }
        }
        if ((isNeedList) &&
                (false == importList.contains("java.util.List"))) {
            importList.add("java.util.List");
        }

        return importList;
    }

    public Boolean getParentExists() {
        return StringUtils.isNotBlank(this.parentTableFk) ? Boolean.valueOf(true) : Boolean.valueOf(false);
    }

    public Boolean getCreateDateExists() {
        for (GenTableColumn c : this.columnList) {
            if ("create_date".equalsIgnoreCase(c.getName())) {
                return Boolean.valueOf(true);
            }
        }
        return Boolean.valueOf(false);
    }

    public Boolean getUpdateDateExists() {
        for (GenTableColumn c : this.columnList) {
            if ("update_date".equalsIgnoreCase(c.getName())) {
                return Boolean.valueOf(true);
            }
        }
        return Boolean.valueOf(false);
    }

    public Boolean getDelFlagExists() {
        for (GenTableColumn c : this.columnList) {
            if ("del_flag".equalsIgnoreCase(c.getName())) {
                return Boolean.valueOf(true);
            }
        }
        return Boolean.valueOf(false);
    }

    public Boolean getOwnerCodeExists() {
        for (GenTableColumn c : this.columnList) {
            if ("owner_code".equalsIgnoreCase(c.getName())) {
                return Boolean.valueOf(true);
            }
        }
        return Boolean.valueOf(false);
    }

    public void setIsSync(String isSync) {
        this.isSync = isSync;
    }

    public String getIsSync() {
        return this.isSync;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableType() {
        return this.tableType;
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

    public HashMap<String, String> getExtJspMap() {
        extJspMap = new HashMap<String, String>();
        this.buildExtMap(extJspMap, this.extJsp);
        return extJspMap;
    }

    public HashMap<String, String> getExtJsMap() {
        extJsMap = new HashMap<String, String>();
        this.buildExtMap(extJsMap, this.extJs);
        return extJsMap;
    }

    public HashMap<String, String> getExtJavaMap() {
        extJavaMap = new HashMap<String, String>();
        this.buildExtMap(extJavaMap, this.extJava);
        return extJavaMap;
    }

    public String getIsBuildAdd() {
        return isBuildAdd;
    }

    public void setIsBuildAdd(String isBuildAdd) {
        this.isBuildAdd = isBuildAdd;
    }

    public String getIsBuildEdit() {
        return isBuildEdit;
    }

    public void setIsBuildEdit(String isBuildEdit) {
        this.isBuildEdit = isBuildEdit;
    }

    public String getIsBuildDel() {
        return isBuildDel;
    }

    public void setIsBuildDel(String isBuildDel) {
        this.isBuildDel = isBuildDel;
    }

    public String getIsBuildImport() {
        return isBuildImport;
    }

    public void setIsBuildImport(String isBuildImport) {
        this.isBuildImport = isBuildImport;
    }

    public String getIsBuildOperate() {
        return isBuildOperate;
    }

    public void setIsBuildOperate(String isBuildOperate) {
        this.isBuildOperate = isBuildOperate;
    }


    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getIsVersion() {
        return isVersion;
    }

    public void setIsVersion(String isVersion) {
        this.isVersion = isVersion;
    }

    public String getIsProcessDefinition() {
        if (StringUtils.isNotEmpty(this.processDefinitionCategory)) {
            return Global.YES;
        } else {
            return isProcessDefinition;
        }
    }

    public void setIsProcessDefinition(String isProcessDefinition) {
        this.isProcessDefinition = isProcessDefinition;
    }

    public String getProcessDefinitionCategory() {
        return processDefinitionCategory;
    }

    public void setProcessDefinitionCategory(String processDefinitionCategory) {
        this.processDefinitionCategory = processDefinitionCategory;
    }

    public String getIsBuildXform() {
        return isBuildXform;
    }

    public void setIsBuildXform(String isBuildXform) {
        this.isBuildXform = isBuildXform;
    }

    public String getIsBuildSecret() {
        return isBuildSecret;
    }

    public void setIsBuildSecret(String isBuildSecret) {
        this.isBuildSecret = isBuildSecret;
    }

    public String getIsBuildContent() {
        return isBuildContent;
    }

    public void setIsBuildContent(String isBuildContent) {
        this.isBuildContent = isBuildContent;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getScount() {
        return scount;
    }

    public void setScount(String scount) {
        this.scount = scount;
    }

    public String getMcount() {
        return mcount;
    }

    public void setMcount(String mcount) {
        this.mcount = mcount;
    }

    public String getLcount() {
        return lcount;
    }

    public void setLcount(String lcount) {
        this.lcount = lcount;
    }

    public String getDcount() {
        return dcount;
    }

    public void setDcount(String dcount) {
        this.dcount = dcount;
    }

    public String getIsRelease() {
        return isRelease;
    }

    public void setIsRelease(String isRelease) {
        this.isRelease = isRelease;
    }

    public String getIsMobile() {
        return isMobile;
    }

    public void setIsMobile(String isMobile) {
        this.isMobile = isMobile;
    }

    private void buildExtMap(HashMap<String, String> extMap, String content) {
        if (false == StringUtils.isEmpty(content)) {
            String[] extBlocks = content.split("<!-- ext ");
            for (int i = 1; i < extBlocks.length; i++) {
                String tempBlock = extBlocks[i];
                String tempKey = tempBlock.substring(0, tempBlock.indexOf("-->")).trim();
                String tempValue = tempBlock.substring(tempBlock.indexOf("-->") + 3);
                extMap.put(tempKey, tempValue);
            }
        }
    }

    public String getSqlColumns() {
        return sqlColumns;
    }

    public void setSqlColumns(String sqlColumns) {
        this.sqlColumns = sqlColumns;
    }

    public String getSqlColumnsFriendly() {
        return sqlColumnsFriendly;
    }

    public String getSqlColumnsFriendlyWithExt() {
        if (StringUtil.isNotEmpty(sqlColumnsFriendlyExt)) {
            if (sqlColumnsFriendly != null && false == sqlColumnsFriendly.endsWith(this.getSqlColumnsFriendlyExt())) {
                return sqlColumnsFriendly + " " + sqlColumnsFriendlyExt;
            } else {
                return sqlColumnsFriendly;
            }
        } else {
            return sqlColumnsFriendly;
        }
    }

    public void setSqlColumnsFriendly(String sqlColumnsFriendly) {
        this.sqlColumnsFriendly = sqlColumnsFriendly;
    }

    public String getSqlJoins() {
        return sqlJoins;
    }

    public String getSqlJoinsWithExt() {
        if (StringUtil.isNotEmpty(sqlJoinsExt)) {
            if (sqlJoins != null && false == sqlJoins.endsWith(sqlJoinsExt)) {
                return sqlJoins + " " + sqlJoinsExt;
            } else {
                return sqlJoins;
            }
        } else {
            return sqlJoins;
        }
    }

    public void setSqlJoins(String sqlJoins) {
        this.sqlJoins = sqlJoins;
    }

    public String getSqlInsert() {
        return sqlInsert;
    }

    public void setSqlInsert(String sqlInsert) {
        this.sqlInsert = sqlInsert;
    }

    public String getSqlUpdate() {
        return sqlUpdate;
    }

    public void setSqlUpdate(String sqlUpdate) {
        this.sqlUpdate = sqlUpdate;
    }

    public String getSqlSort() {
        return sqlSort;
    }

    public void setSqlSort(String sqlSort) {
        this.sqlSort = sqlSort;
    }

    public JSONArray getJson() {
        return json;
    }

    public void setJson(JSONArray json) {
        this.json = json;
    }

    public JSONArray getChildren() {
        return children;
    }

    public void setChildren(JSONArray children) {
        this.children = children;
    }

    public String getMobileIcon() {
        return mobileIcon;
    }

    public void setMobileIcon(String mobileIcon) {
        this.mobileIcon = mobileIcon;
    }

    public String getStatusStart() {
        if (StringUtil.isEmpty(statusStart)) {
            return DEFAULT_STATUS_START;
        } else {
            return statusStart;
        }
    }

    public void setStatusStart(String statusStart) {
        this.statusStart = statusStart;
    }

    public String getStatusBreak() {
        if (StringUtil.isEmpty(statusBreak)) {
            return DEFAULT_STATUS_BREAK;
        } else {
            return statusBreak;
        }
    }

    public void setStatusBreak(String statusBreak) {
        this.statusBreak = statusBreak;
    }

    public String getStatusEnd() {
        if (StringUtil.isEmpty(statusEnd)) {
            return DEFAULT_STATUS_END;
        } else {
            return statusEnd;
        }
    }

    public void setStatusEnd(String statusEnd) {
        this.statusEnd = statusEnd;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getProcessModelName() {
        return processModelName;
    }

    public void setProcessModelName(String processModelName) {
        this.processModelName = processModelName;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getIsScroll() {
        return isScroll;
    }

    public void setIsScroll(String isScroll) {
        this.isScroll = isScroll;
    }

    public String getIsRowedit() {
        return isRowedit;
    }

    public void setIsRowedit(String isRowedit) {
        this.isRowedit = isRowedit;
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

    public String getExtSql01() {
        return extSql01;
    }

    public void setExtSql01(String extSql01) {
        this.extSql01 = extSql01;
    }

    public String getExtSql02() {
        return extSql02;
    }

    public void setExtSql02(String extSql02) {
        this.extSql02 = extSql02;
    }

    public String getSqlColumnsFriendlyExt() {
        return sqlColumnsFriendlyExt;
    }

    public void setSqlColumnsFriendlyExt(String sqlColumnsFriendlyExt) {
        this.sqlColumnsFriendlyExt = sqlColumnsFriendlyExt;
    }

    public String getSqlJoinsExt() {
        return sqlJoinsExt;
    }

    public void setSqlJoinsExt(String sqlJoinsExt) {
        this.sqlJoinsExt = sqlJoinsExt;
    }

    public String getFormNoExt() {
        return formNoExt;
    }

    public void setFormNoExt(String formNoExt) {
        this.formNoExt = formNoExt;
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


    public Integer getTableSort() {
        return tableSort;
    }

    public void setTableSort(Integer tableSort) {
        this.tableSort = tableSort;
    }

    public List<GenTableExtRuleManyToMany> getExtRuleManyToMany() {
        //Construct ManyToMany List
        List<GenTableExtRuleManyToMany> list = Lists.newArrayList();
        try {
            if (StringUtils.isNotEmpty(this.extJava)) {
                JSONArray array = JSONArray.parseArray(this.extJava);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    GenTableExtRuleManyToMany obj = JSONObject.toJavaObject(jsonObject, GenTableExtRuleManyToMany.class);
                    list.add(obj);
                }
            }
        } catch (Exception e) {
            log.warn("解析多对多扩展规则失败", e);
        }
        return list;
    }

    /**
     * 模式名称
     *
     * @return
     */
    public String getSchema() {
        if (this.name != null && this.name.indexOf(".") != -1) {
            return this.name.substring(0, this.name.indexOf("."));
        } else {
            return "";
        }
    }

    /**
     * 返回去掉模式名的表名
     *
     * @return
     */
    public String getNameWithoutSchema() {
        if (this.name != null && this.name.indexOf(".") != -1) {
            return this.name.substring(this.name.indexOf(".") + 1);
        } else {
            return this.name;
        }
    }

    /**
     * 查询用于保存版本表的Insert Sql，关系表updateDate来自主表，用于区分多次留痕
     *
     * @return
     */
    public String getSqlInsertV() {
        String sqlInsertV = this.getSqlInsert();
        if (sqlInsertV != null) {
            sqlInsertV = sqlInsertV.replaceFirst("\\(", "(vdate,");
            sqlInsertV = sqlInsertV.replaceFirst("VALUES \\(", "VALUES (#{updateDate},");
        }
        return sqlInsertV;
    }

    public String getJavaInstanceName() {
        return StringUtil.toCamelCase(this.getNameWithoutSchema());
    }

    public String getJavaClassName() {
        return StringUtil.toCapitalizeCamelCase(this.getNameWithoutSchema());
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

    public String getSimpleTableName() {
        if (this.name.indexOf(".") == -1) {
            return StringUtils.lowerCase(this.name);
        } else {
            return StringUtils.lowerCase(this.name.substring(this.name.indexOf(".") + 1));
        }
    }

    /**
     * 问题反馈说明
     * @param respondProblemInstructions
     */
    public void setRespondProblemInstructions(String respondProblemInstructions) {
        this.respondProblemInstructions = respondProblemInstructions;
    }
    /**
     * 问题反馈说明
     * @return
     */
    public String getRespondProblemInstructions() {
        return respondProblemInstructions;
    }
    /**
     * 已处理数量
     * @param doneCount
     */
    public void setDoneCount(Integer doneCount) {
        this.doneCount = doneCount;
    }
    /**
     * 已处理数量
     * @return
     */
    public Integer getDoneCount() {
        return doneCount;
    }

    // ==================== 主键字段名配置方法 ====================

    /**
     * 获取主键字段名，默认为"id"
     */
    public String getPkColumnName() {
        if (StringUtil.isEmpty(pkColumnName)) {
            pkColumnName = "id";
        }
        return pkColumnName;
    }

    /**
     * 设置主键字段名，仅允许字母、数字、下划线，防止SQL注入
     */
    public void setPkColumnName(String pkColumnName) {
        if (pkColumnName != null && !pkColumnName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            this.pkColumnName = "id";
        } else {
            this.pkColumnName = pkColumnName;
        }
    }

    /**
     * 获取主键WHERE条件（用于动态SQL）
     * 单主键：pk_column_name = ?
     */
    public String getPkWhereClause() {
        return getPkColumnName() + " = #{id}";
    }

    /**
     * 获取主键WHERE条件（用于动态SQL，指定别名）
     * @param alias 表别名
     */
    public String getPkWhereClause(String alias) {
        String prefix = StringUtil.isNotEmpty(alias) ? alias + "." : "";
        return prefix + getPkColumnName() + " = #{id}";
    }
}
