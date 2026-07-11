package com.jeestudio.bpm.controller.gen;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.*;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.common.view.common.TreeView;
import com.jeestudio.bpm.service.ai.TransService;
import com.jeestudio.bpm.service.ai.GenAIService;
import com.jeestudio.bpm.service.gen.GenRealmService;
import com.jeestudio.bpm.service.gen.GenTableDeriveSyncService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.HttpUtil;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 表单与代码生成配置
 */
@Tag(name = "表单配置")
@RestController
@RequestMapping("${adminPath}/gen/genTable")
public class GenTableController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(GenTableController.class);

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private GenTableDeriveSyncService genTableDeriveSyncService;

    @Autowired
    private GenRealmService genRealmService;

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private TransService transService;

    @Autowired
    private GenAIService genAIService;

    /**
     * 查询动态表列表
     */
    @Operation(summary = "查询动态表列表")
    @RequiresPermissions("user")
    @PostMapping("/findDynamicList")
    public ResultJson findDynamicList(@RequestBody GenTable genTable) {
        if (genTable.getPageParam() == null) genTable.setPageParam(new PageParam());
        Page<GenTable> page = genTableService.findPage(new Page<GenTable>(
                        genTable.getPageParam().getPageNo(),
                        genTable.getPageParam().getPageSize(),
                        genTable.getPageParam().getOrderBy()),
                genTable);
        return ResultJson.success().setRows(page.getList()).setTotal(page.getCount());
    }

    /**
     * 按查询条件同步表
     */
    @Operation(summary = "同步表和版本表")
    @RequiresPermissions("user")
    @PostMapping("/syncTables")
    public ResultJson syncTables(@RequestBody GenTable genTable) {
        //name=demo_0709&comments=&module=&parentTable=
        if (genTable.getPageParam() == null) genTable.setPageParam(new PageParam());
        Page<GenTable> page = genTableService.findPage(new Page<GenTable>(
                        genTable.getPageParam().getPageNo(),
                        Integer.MAX_VALUE,
                        genTable.getPageParam().getOrderBy()),
                genTable);
        genTableService.syncTables(page.getList());
        return ResultJson.success("同步表及留痕表成功");
    }

    /**
     * 按查询条件同步表
     */
    @Operation(summary = "切换数据库")
    @RequiresPermissions("user")
    @PostMapping("/changeDb")
    public ResultJson changeDb(@RequestBody GenTable genTable) throws Exception {
        if (genTable.getPageParam() == null) genTable.setPageParam(new PageParam());
        Page<GenTable> page = genTableService.findPage(new Page<GenTable>(
                        genTable.getPageParam().getPageNo(),
                        Integer.MAX_VALUE,
                        genTable.getPageParam().getOrderBy()),
                genTable);
        genTableService.changeDb(page.getList());
        return ResultJson.success("换库重构Sql成功");
    }

    /**
     * 获取树表列表
     */
    @Operation(summary = "获取树表列表")
    @RequiresPermissions("user")
    @GetMapping("/getTreeTableList")
    public ResultJson getTreeTableList() {
        GenTable genTable = new GenTable();
        genTable.setTableType(GenTable.TABLE_TYPE_TREE);
        genTable.setPageParam(new PageParam());
        genTable.getPageParam().setPageSize(Integer.MAX_VALUE);
        Page<GenTable> page = genTableService.findPage(new Page<GenTable>(
                        genTable.getPageParam().getPageNo(),
                        genTable.getPageParam().getPageSize(),
                        genTable.getPageParam().getOrderBy()),
                genTable);
        List<TreeView> list = Lists.newArrayList();
        int i = 0;
        for (GenTable theGenTable : page.getList()) {
            TreeView treeView = new TreeView();
            treeView.setId(theGenTable.getName());
            treeView.setName(theGenTable.getNameAndComments());
            treeView.setParentId(Global.DEFAULT_ROOT_CODE);
            treeView.setHasChildren(false);
            treeView.setSort(i++);
            list.add(treeView);
        }
        return ResultJson.success().put("data", list);
    }

    /**
     * 获取表单编辑数据
     */
    @Operation(summary = "获取表单编辑数据")
    @RequiresPermissions("user")
    @PostMapping("/editForm")
    public ResultJson editForm(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.editForm(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取表单定义【旧】
     */
    @Operation(summary = "获取表单定义【旧】")
    @RequiresPermissions("user")
    @PostMapping("/getDefinition")
    public ResultJson getDefinition(@RequestBody GenTable genTable) {
        return ResultJson.success().put("genTable", genTableService.get(genTable.getId()));
    }

    /**
     * 获取表单定义
     */
    @Operation(summary = "获取表单及定义")
    @RequiresPermissions("user")
    @PostMapping("/getGenTableWithDefination")
    public ResultJson getGenTableWithDefination(@RequestParam("formNo") String formNo) {
        return ResultJson.success().put("genTable", genTableService.getGenTableWithDefination(formNo));
    }

    /**
     * 获取域数据
     */
    @Operation(summary = "获取域数据")
    @RequiresPermissions("user")
    @PostMapping("/realmData")
    public ResultJson realmData(@RequestParam("genRealm") String[] genRealm) {
        ResultJson resultJson = genRealmService.realmData(genRealm);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 保存表单配置
     */
    @Operation(summary = "保存表单配置")
    @RequiresPermissions("user")
    @PostMapping("/saveGenTable")
    public ResultJson saveGenTable(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.saveGenTable(genTable, false);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 解析视图或统计表来源 SQL 的字段元数据。
     */
    @Operation(summary = "解析来源SQL字段")
    @RequiresPermissions("user")
    @PostMapping("/parseSourceSqlFields")
    public ResultJson parseSourceSqlFields(@RequestBody JSONObject reqBody) {
        ResultJson resultJson = genTableService.parseSourceSqlFields(reqBody);
        resultJson.setToken(token.get());
        return resultJson;
    }

    @Operation(summary = "手动同步统计表数据")
    @RequiresPermissions("user")
    @PostMapping("/syncDerivedSummary")
    public ResultJson syncDerivedSummary(@RequestBody JSONObject reqBody) {
        ResultJson resultJson = genTableDeriveSyncService.syncSummaryNow(reqBody);
        resultJson.setToken(token.get());
        return resultJson;
    }

    @Operation(summary = "保存移动端表单配置")
    @RequiresPermissions("user")
    @PostMapping("/saveGenTableMobile")
    public ResultJson saveGenTableMobile(@RequestBody GenTable genTable) {
        String extJs = genTable.getExtJs();
        genTable = genTableService.get(genTable.getId());
        genTable.setExtJs(extJs);
        genTableService.save(genTable);
        return ResultJson.success();
    }

    /**
     * 同步动态表单
     */
    @Operation(summary = "同步动态表单")
    @RequiresPermissions("user")
    @PostMapping("/syncDynamic")
    public ResultJson syncDynamic(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.syncDynamic(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 同步表单版本
     */
    @Operation(summary = "同步表单版本")
    @RequiresPermissions("user")
    @PostMapping("/syncDynamicVersion")
    public ResultJson syncDynamicVersion(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.syncDynamicVersion(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 同步表单数据表
     */
    @Operation(summary = "同步表单数据表")
    @RequiresPermissions("user")
    @PostMapping("/syncDynamicTable")
    public ResultJson syncDynamicTable(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.syncDynamicTable(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 同步版本表
     */
    @Operation(summary = "同步版本表")
    @RequiresPermissions("user")
    @PostMapping("/syncVersionTable")
    public ResultJson syncVersionTable(@RequestBody GenTable genTable) {
        genTableService.syncVersionTable(genTable);
        return ResultJson.success("同步留痕表成功");
    }

    /**
     * 同步全部版本表
     */
    @Operation(summary = "同步全部版本表")
    @RequiresPermissions("user")
    @PostMapping("/syncAllVersionTable")
    public ResultJson syncAllVersionTable() {
        String currentTableName = "";
        GenTable theGentable = new GenTable();
        theGentable.setIsVersion(Global.YES);
        List<GenTable> list = genTableService.findList(theGentable);
        for (GenTable genTable : list) {
            currentTableName = genTable.getName();
            if (currentTableName.contains("@view") || currentTableName.contains("@sum")) {
                continue;
            }
            genTable = genTableService.getGenTableWithDefination(genTable.getName());
            genTableService.syncVersionTable(genTable);
        }
        return ResultJson.success("检查并同步留痕表完成");
    }

    /**
     * 移除表单配置
     */
    @Operation(summary = "移除表单配置")
    @RequiresPermissions("user")
    @PostMapping("/removeDynamic")
    public ResultJson removeDynamic(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.removeDynamic(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 删除表单配置
     */
    @Operation(summary = "删除表单配置")
    @RequiresPermissions("user")
    @PostMapping("/deleteDynamic")
    public ResultJson deleteDynamic(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.deleteDynamic(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 复制表单配置
     */
    @Operation(summary = "复制表单配置")
    @RequiresPermissions("user")
    @PostMapping("/copyDynamic")
    public ResultJson copyDynamic(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.copyDynamic(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取待导入表列表
     */
    @Operation(summary = "获取待导入表列表")
    @RequiresPermissions("user")
    @PostMapping("/importListDynamic")
    public ResultJson importListDynamic() {
        ResultJson resultJson = genTableService.importListDynamic();
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取待导入表列表(Schema)
     */
    @Operation(summary = "获取待导入表列表(Schema)")
    @RequiresPermissions("user")
    @PostMapping("/importListDynamicSchema")
    public ResultJson importListDynamicSchema() {
        ResultJson resultJson = genTableService.importListDynamicSchema();
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取表单字典
     */
    @Operation(summary = "获取表单字典")
    @RequiresPermissions("user")
    @GetMapping("/dictDynamic")
    public ResultJson dictDynamic(@RequestParam("key") String key) {
        return ResultJson.success().put("dictList", dictDataService.findDictListLike(key));
    }

    /**
     * 从数据库导入表
     */
    @Operation(summary = "从数据库导入表")
    @RequiresPermissions("user")
    @PostMapping("/importDynamic")
    public ResultJson importDynamic(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.importDynamic(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 从其他Schema导入表
     */
    @Operation(summary = "从其他Schema导入表")
    @RequiresPermissions("user")
    @PostMapping("/importDynamicSchema")
    public ResultJson importDynamicSchema(@RequestBody GenTable genTable) {
        ResultJson resultJson = genTableService.importDynamicSchema(genTable);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 预览表单
     */
    @Operation(summary = "预览表单")
    @RequiresPermissions("user")
    @PostMapping("/buildViewDynamic")
    public ResultJson buildViewDynamic(@RequestBody GenScheme genScheme) {
        ResultJson resultJson = genTableService.buildViewDynamic(genScheme);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 发布表单
     */
    @Operation(summary = "发布表单")
    @RequiresPermissions("user")
    @PostMapping("/buildDynamic")
    public ResultJson buildDynamic(@RequestBody GenScheme genScheme) throws Exception {
        ResultJson resultJson = genTableService.buildDynamic(genScheme, currentUserName.get());
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取查看字典
     */
    @Operation(summary = "获取查看字典")
    @RequiresPermissions("user")
    @GetMapping("/dictViewDynamic")
    public ResultJson dictViewDynamic(@RequestParam("key") String key) {
        return ResultJson.success().put("dictList", dictDataService.getDictGenView(key));
    }

    /**
     * 刷新表单缓存
     */
    @Operation(summary = "刷新表单缓存")
    @RequiresPermissions("user")
    @GetMapping("/refreshGenTableCache")
    public ResultJson refreshGenTableCache() {
        genTableService.refreshGenTableCache("");
        return ResultJson.success();
    }

    private ResultJson getRealmColumnsByByAI(String mainTableModule, String tableComments) {
        try {
            String columnNames = genAIService.getColumnNamesByTableComments(tableComments, currentUserName.get());
            if (StringUtil.isNotEmpty(columnNames)) {
                return this.getRealmColumnsByNamesOk(columnNames, mainTableModule);
            }
        } catch (Exception err) {
            logger.warn("Call getRealmColumnsByByAI error, " + err.getMessage());
        }
        return null;
    }

    private GenTableColumnView genTableColumnByAI(String columnComments) {
        try {
            return genAIService.getColumnDefinitionByComments(columnComments, currentUserName.get());
        } catch (Exception err) {
            logger.warn("Call genTableColumnByAI error, " + err.getMessage());
        }
        return null;
    }

    private ResultJson getRealmColumnsByNamesOk(String names, String mainTableModule) {
        try {
            ResultJson resultJson = new ResultJson();
            resultJson.setCode(0);
            resultJson.setMsg("获取领域成功");
            resultJson.setMsg_en("Got realm columns successfully");
            if (StringUtil.isNotEmpty(names)) {
                names = names.replaceAll("，", ",");

                boolean aiAvailable = genAIService.isAvailable(currentUserName.get());

                //调用AI预缓存字段定义
                if (aiAvailable) {
                    this.genTableColumnByAI(names.replaceAll(",", " "));
                }

                names = names.replaceAll(" ", ",");
                String[] nameArray = names.split(",");
                List<GenTableColumnView> list = Lists.newArrayList();
                for (int i = 0; i < nameArray.length; i++) {
                    String sort = String.valueOf(10 * i);
                    GenTableColumnView column = genTableService.getByComments(nameArray[i], mainTableModule);
                    //call AI
                    boolean isfromopenai = false;
                    if (column == null && aiAvailable) {
                        column = this.genTableColumnByAI(nameArray[i]);
                        if (column != null) {
                            isfromopenai = true;
                        }
                    }
                    if (column == null) column = genRealmService.getByName(nameArray[i]);
                    if (column != null && column.getShowType() != null && false == isfromopenai) {
                        column.setFormSort(sort);
                        column.setListSort(sort);
                        column.setSearchSort(sort);
                    } else {
                        // AI 来源的字段已有英文名，跳过翻译；否则降级调用百度翻译
                        String nameEn = "";
                        if (false == isfromopenai) {
                            nameEn = transService.getTransResult(nameArray[i], "auto", "en");
                            if (nameEn.indexOf("dst") != -1) {
                                nameEn = nameEn.substring(nameEn.indexOf("dst") + 6, nameEn.lastIndexOf("\""));
                            } else {
                                nameEn = "";
                            }
                        }
                        if (column == null) column = new GenTableColumnView();
                        column.setFormSort(sort);
                        column.setListSort(sort);
                        column.setSearchSort(sort);
                        column.setIsForm(Global.YES);
                        column.setIsQuery(Global.NO);
                        column.setIsList(Global.YES);
                        column.setComments(nameArray[i]);
                        if (false == isfromopenai) column.setName(StringUtil.toLowerCaseWith_(nameEn));
                        column.setCommentsEn(nameEn);
                        column.setIsOneLine(Global.NO);
                        column.setIsReadonly(Global.NO);
                        column.setIsNull(Global.YES);

                        if (nameArray[i].indexOf("日期") != -1) {
                            column.setJavaField("d");
                            column.setShowType("dateselect");
                            column.setJdbcType("datetime");
                            column.setJavaType("java.util.Date");
                            column.setDateType("yyyy-MM-dd");
                        } else if (nameArray[i].indexOf("时间") != -1) {
                            column.setJavaField("d");
                            column.setShowType("dateselect");
                            column.setJdbcType("datetime");
                            column.setJavaType("java.util.Date");
                            column.setDateType("yyyy-MM-dd HH:mm:ss");
                        } else if (nameArray[i].indexOf("复选") != -1 || nameArray[i].indexOf("多项") != -1 || nameArray[i].indexOf("多选") != -1) {
                            column.setJavaField("c");
                            column.setShowType("checkbox");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("String");
                            column.setDictType("yes_no");
                        } else if (nameArray[i].indexOf("单选") != -1 || nameArray[i].indexOf("单项") != -1) {
                            column.setJavaField("s");
                            column.setShowType("radiobox");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("String");
                            column.setDictType("yes_no");
                            column.setIsNull(Global.NO);
                        } else if (nameArray[i].indexOf("下拉") != -1 || nameArray[i].indexOf("选择") != -1 || nameArray[i].indexOf("选项") != -1) {
                            column.setJavaField("s");
                            column.setShowType("select");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("String");
                            column.setDictType("yes_no");
                        } else if (nameArray[i].indexOf("人员") != -1) {
                            column.setJavaField("user");
                            column.setShowType("treeselectRedio");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("com.jeestudio.bpm.common.entity.system.User");
                        } else if (nameArray[i].indexOf("部门") != -1 && nameArray[i].indexOf("归属") != -1) { //|| nameArray[i].indexOf("机构") != -1
                            column.setJavaField("office");
                            column.setShowType("officeselectTree");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("com.jeestudio.bpm.common.entity.system.Office");
                        } else if (nameArray[i].indexOf("区域") != -1 || nameArray[i].indexOf("地区") != -1 || nameArray[i].indexOf("行政区") != -1) {
                            column.setJavaField("area");
                            column.setShowType("areaselect");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("com.jeestudio.bpm.common.entity.system.Area");
                        } else if (nameArray[i].indexOf("附件") != -1 || nameArray[i].indexOf("文件") != -1 || nameArray[i].indexOf("上传") != -1) {
                            column.setJavaField("s");
                            column.setShowType("fileupload");
                            column.setJdbcType("varchar(64)");
                            column.setJavaType("String");
                            column.setIsOneLine(Global.YES);
                        } else if (nameArray[i].endsWith("名称") || nameArray[i].endsWith("标题") || nameArray[i].endsWith("地点")) {
                            column.setJavaField("s");
                            column.setShowType("input");
                            if (false == isfromopenai) column.setJdbcType("varchar(100)");
                            column.setJavaType("String");
                            if (false == isfromopenai) column.setMaxLength("100");
                        } else if (nameArray[i].endsWith("链接")) {
                            column.setJavaField("s");
                            column.setShowType("input");
                            if (false == isfromopenai) column.setJdbcType("varchar(255)");
                            column.setJavaType("String");
                            if (false == isfromopenai) column.setMaxLength("255");
                        } else if (nameArray[i].endsWith("描述") || nameArray[i].endsWith("概况") || nameArray[i].endsWith("情况") || nameArray[i].endsWith("说明") || nameArray[i].endsWith("要求")) {
                            column.setJavaField("s");
                            column.setShowType("textarea");
                            if (false == isfromopenai) column.setJdbcType("varchar(500)");
                            column.setJavaType("String");
                            if (false == isfromopenai) column.setMaxLength("500");
                            column.setIsOneLine("1");
                        } else {
                            column.setJavaField("s");
                            column.setShowType("input");
                            if (false == isfromopenai) column.setJdbcType("varchar(64)");
                            column.setJavaType("String");
                        }
                    }
                    column.setBlockChainParam1("0");
                    column.setBlockChainParam2("0");
                    column.setBlockChainParam3("0");
                    list.add(column);
                }
                resultJson.put("realm", list);
                resultJson.setRows(list);
                resultJson.setTotal(list.size());
            }
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to get realm columns by names: " + ExceptionUtils.getStackTrace(e));
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 根据名称获取域字段
     */
    @Operation(summary = "根据名称获取域字段")
    @RequiresPermissions("user")
    @PostMapping("/getRealmColumnsByNames")
    public ResultJson getRealmColumnsByNames(@RequestParam("names") String names,
                                             @RequestParam("mainTableModule") String mainTableModule) {
        if (names.indexOf(":") == 0 || names.indexOf("：") == 0) {
            names = names.substring(1);
            return this.getRealmColumnsByByAI(mainTableModule, names);
        }
        return this.getRealmColumnsByNamesOk(names, mainTableModule);
    }

    /**
     * 保存导入导出设置
     */
    @Operation(summary = "保存导入导出设置")
    @RequiresPermissions("user")
    @PostMapping("/saveImportAndExport")
    public ResultJson saveGenTable(@RequestBody GenTableView genTable) {
        genTableService.saveImportAndExport(genTable);
        genTableService.refreshGenTableCache(genTable.getName());
        return ResultJson.success("保存导入导出设置成功");
    }

    /**
     * 解锁SQL
     */
    @Operation(summary = "解锁SQL")
    @RequiresPermissions("user")
    @PostMapping("/unlockSql")
    public ResultJson unlockSql(String id) {
        genTableService.unlockSql(id);
        return ResultJson.success();
    }

    /**
     * 创建包含所有非隐藏字段的App表单定义，在App设计中调用
     */
    @Operation(summary = "构建移动端表单定义")
    @RequiresPermissions("user")
    @PostMapping("/buildFormDefinitionForApp")
    public ResultJson buildFormDefinitionForApp(@RequestParam("formId") String formId) {
        return ResultJson.success().put("data", genTableService.buildFormDefinitionForApp(formId));
    }

    /**
     * 字典生成Enum
     */
    @Operation(summary = "生成枚举类")
    @RequiresPermissions("user")
    @PostMapping("/genEnum")
    public ResultJson genEnum(@RequestBody JSONObject reqBody) throws Exception {
        ResultJson resultJson = genTableService.genEnum(reqBody, currentUserName.get());
        resultJson.setToken(token.get());
        return resultJson;
    }
}
