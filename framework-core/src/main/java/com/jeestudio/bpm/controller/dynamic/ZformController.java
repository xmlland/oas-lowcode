package com.jeestudio.bpm.controller.dynamic;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.around.AroundControllerI;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.common.ActExtentions;
import com.jeestudio.bpm.common.entity.common.ExportExtraEntity;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.system.DictResult;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.ImportParam;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.common.view.common.TreeView;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.oa.entity.OaTaskFallbackPermissionsSettingEntity;
import com.jeestudio.bpm.modules.oa.service.OaTaskFallbackPermissionsSettingServiceI;
import com.jeestudio.bpm.service.common.StatisticsVo;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.DataService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.excel.ExcelImportUtil;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import com.jeestudio.tools.security.utils.security.SM2Util;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;

import io.swagger.v3.oas.annotations.Operation;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * @Description: 通用数据访问
 */
@Tag(name = "动态表单")
@RestController
@RequestMapping("${adminPath}/dynamic/zform")
public class ZformController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ZformController.class);

    @Value("${fileRoot}")
    private String fileRoot;

    @Value("${oaSecLevelSwitch}")
    protected Boolean oaSecLevelSwitch;

    @Value("${tomcatPath}")
    private String tomcatPath;

    @Value("${uploadPath}")
    private String uploadPath;

    @Value("${docTemplateRoot}")
    private String docTemplateRoot;

    @Autowired
    private SecLogService secLogService;

    @Autowired
    private ZformService zformService;

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private DataService dataService;

    @Autowired
    DictDataService dictDataService;

    @Autowired
    OaTaskFallbackPermissionsSettingServiceI oaTaskFallbackPermissionsSettingService;

    @Autowired
    ProjectProperties projectProperties;

    @Value("${adminPath}")
    String adminPath;

    /**
     * 根据ID获取表单数据
     */
    @Deprecated
    @Operation(summary = "根据ID获取表单数据")
    @RequiresPermissions("user")
    @GetMapping("/getZform")
    public ResultJson getZform(@RequestParam("formNo") String formNo, @RequestParam("id") String id) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = zformService.get(id, currentUserName.get(), genTable);
        return ResultJson.success().put("data", zform);
    }

    /**
     * 根据ID获取表单Map数据
     */
    @RequiresPermissions("user")
    @GetMapping("/getZformMap")
    public ResultJson getZformMap(@RequestParam("formNo") String formNo, @RequestParam("id") String id) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        LinkedHashMap zform = zformService.getMap(id, genTable, "", currentUserName.get());
        return ResultJson.success().put("data", zform);
    }

    /**
     * 查询留痕记录
     */
    @Operation(summary = "获取表单版本数据")
    @RequiresPermissions("user")
    @GetMapping("/getZformMapVersion")
    public ResultJson getZformMapVersion(@RequestParam("formNo") String formNo, @RequestParam("id") String id) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        List<LinkedHashMap> changeList = zformService.getMapVersion(id, genTable, "", currentUserName.get());
        return ResultJson.success().setRows(changeList).setTotal(changeList.size());
    }

    /**
     * 获取移动端表单Map数据
     */
    @Operation(summary = "获取移动端表单Map数据")
    @RequiresPermissions("user")
    @GetMapping("/getMobileZformMap")
    public ResultJson getMobileZformMap(@RequestParam("formNo") String formNo, @RequestParam("id") String id) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        LinkedHashMap zform = zformService.getMap(id, genTable, "", currentUserName.get());
        LinkedHashMap<String, Object> nameList = new LinkedHashMap<>();
        List<GenTableColumn> columnList = genTable.getColumnList();
        HashMap<String, GenTableColumn> columnMap = new HashMap<>();
        HashMap<String, HashMap<String, DictResult>> dictMap = new HashMap<>();
        columnList.stream().filter(k -> StringUtil.isNotEmpty(k.getDictType())).forEach(k -> {
            columnMap.put(k.getName(), k);
            HashMap<String, DictResult> dict = new HashMap<>();
            List<DictResult> dictList = dictDataService.getDictList(k.getDictType(), false);
            dictList.forEach(d -> {
                dict.put(d.getMember(), d);
            });
            dictMap.put(k.getDictType(), dict);
        });
        zform.forEach((k, v) -> {
            if (columnMap.containsKey(k) && v != null && StringUtil.isNotEmpty(v.toString())) {
                GenTableColumn c = columnMap.get(k);
                if (c == null || !dictMap.containsKey(c.getDictType())) {
                    return;
                }
                String[] values = v.toString().split(",");
                List<String> translatedValues = new ArrayList<>();
                for (String val : values) {
                    DictResult dictResult = dictMap.get(c.getDictType()).get(val.trim());
                    if (dictResult != null) {
                        translatedValues.add(dictResult.getMemberName());
                    }
                }
                if (translatedValues.size() <= 0) {
                    return;
                }
                zform.put(k, String.join(",", translatedValues));
            }
        });
        zform.forEach((key, obj) -> {
            if (obj instanceof HashMap) {
                HashMap<String, Object> objMap = (HashMap<String, Object>) obj;
                if (objMap.containsKey("name")) {
                    nameList.put(key + "__name", objMap.get("name"));
                }
            }
        });
        nameList.forEach((key, obj) -> {
            zform.put(key, obj);
        });
        List<GenTable> childTableList = genTable.getChildList();
        childTableList.forEach(childTable -> {
            String tableName = childTable.getName();
            JSONObject childZFormMap = new JSONObject();
            JSONObject pageParam = new JSONObject();
            pageParam.put("pageNo", 1);
            pageParam.put("pageSize", 100);
            childZFormMap.put("pageParam", pageParam);
            zform.put(tableName, mobileDatamap(childZFormMap, "", "path", tableName, "", id).getRows());
        });
        return ResultJson.success().put("data", zform);
    }

    /**
     * 获取表单Map查看数据
     */
    @GetMapping("/getZformMapView")
    public ResultJson getZformMapView(@RequestParam("formNo") String formNo, @RequestParam("id") String id) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        LinkedHashMap zform = zformService.getMap(id, genTable, GenTable.EXT_FLAG_VIEW, currentUserName.get());
        return ResultJson.successView().put("data", zform);
    }

    /**
     * 获取带流程的表单数据
     */
    @RequiresPermissions("user")
    @GetMapping("/getZformWithAct")
    public ResultJson getZformWithAct(@RequestParam("formNo") String formNo, @RequestParam("id") String id, @RequestParam("procDefKey") String procDefKey) throws Exception {
        String loginName = currentUserName.get();
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = null;
        if (StringUtil.isEmpty(id)) {
            zform = new Zform();
            zform.setFormNo(formNo);
            zform.setProcDefKey(procDefKey);
        } else {
            zform = zformService.get(id, loginName, genTable);
        }
        //流程表单
        if (false == StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
            if (StringUtil.isBlank(zform.getProcInsId())) {
                procDefKey = zform.getProcDefKey().replaceAll("'", "");
                ProcessDefinition processDefinition = zformService.getProcessDefinition(procDefKey);
                zform.getAct().setProcDefId(processDefinition.getId());
                zform.getAct().setTaskDefKey(processDefinition.getDescription().split(",")[1]);
            } else {
                zformService.setAct(zform, loginName);
            }
            zformService.setRuleArgs(zform, loginName);
        }
        return ResultJson.success().put("data", zform);
    }

    /**
     * 获取带流程的表单Map数据
     */
    @RequiresPermissions("user")
    @GetMapping("/getZformWithActMap")
    public ResultJson getZformWithActMap(@RequestParam("formNo") String formNo, @RequestParam("id") String id, @RequestParam("procDefKey") String procDefKey, @RequestParam(value = "procTaskId", required = false) String procTaskId) throws Exception {
        String loginName = currentUserName.get();
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = null;
        LinkedHashMap zformMap = null;
        if (StringUtil.isEmpty(id)) {
            zform = new Zform();
            zform.setFormNo(formNo);
            zform.setProcDefKey(procDefKey);
            zformMap = Maps.newLinkedHashMap();
            zformMap.put("formNo", formNo);
            zformMap.put("procDefKey", procDefKey);
        } else {
            zform = zformService.get(id, loginName, genTable);
            zformMap = zformService.getMap(id, genTable, "", loginName);
        }
        //流程表单
        if (false == StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
            if (StringUtil.isBlank(zform.getProcInsId())) {
                procDefKey = zform.getProcDefKey().replaceAll("'", "");
                ProcessDefinition processDefinition = zformService.getProcessDefinition(procDefKey);
                zform.getAct().setProcDefId(processDefinition.getId());
                zform.getAct().setTaskDefKey(processDefinition.getDescription().split(",")[1]);
                zformMap.put("act", zform.getAct());
            } else {
                zformService.setAct(zform, procTaskId, loginName);
                zformMap.put("act", zform.getAct());
            }
            zformService.setRuleArgs(zform, loginName);
            zformMap.put("ruleArgs", zform.getRuleArgs());
        }
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(formNo);
        if (aroundService != null) {
            zformMap = aroundService.afterGetZformWithActMap(zformMap, formNo, id, procDefKey, procTaskId);
        }
        return ResultJson.success().put("data", zformMap);
    }

    /**
     * 获取表单列表
     *
     * @param path
     * @param zform
     * @return Json格式的表单列表
     */
    @Deprecated
    @Operation(summary = "获取表单列表")
    @RequiresPermissions("user")
    @PostMapping("/data")
    public ResultJson data(@RequestBody Zform zform, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag) {
        try {
            ResultJson resultJson = zformService.data(zform, path, formNo, traceFlag, currentUserName.get());
            resultJson.setToken(token.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_QUERY);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to get zform data: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_QUERY);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取表单Map列表
     *
     * @param path
     * @param zformMap
     * @return Json格式的表单列表
     */
    @RequiresPermissions("user")
    @PostMapping("/datamap/{t}"
    )
    public ResultJson datamap(@RequestBody JSONObject zformMap, @PathVariable("t") String t, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam("parentId") String parentId) {
        try {
            JSONObject zformMapCopy = JSONObject.parseObject(zformMap.toJSONString());
            zformMap.put("formNo", formNo);
            Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get(), true);
            //Page<Zform> page = datasourceFeign.findZformDataMap(zform, path, currentUserName.get(), traceFlag, "");
            GenTable zformTable = new GenTable();
            zformTable.setName("z_form");
            AroundServiceI zformAroundService = AroundUtil.getAroundServiceI(zformTable);
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (projectProperties.isCheckSelectPermission()){
                // 权限约束：当前用户需要具备该表至少一种查询、添加、编辑或删除权限。
                zformService.checkSelectPermission(currentUserName.get(), genTable.getName());
            }
            List<GenTableColumn> columnList = genTable.getColumnList();
            AroundControllerI aroundController = AroundUtil.getAroundControllerI(genTable);
            if (aroundController != null) {
                ResultJson resultJsonRest = aroundController.customReturnResult(currentUserName.get(), genTable, zform, zformMapCopy, parentId);
                if (resultJsonRest != null) {
                    return resultJsonRest;
                }
            }

            if (zform.getPageParam() == null) {
                zform.setPageParam(new PageParam());
            }
            Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
            page.setFromIndex(zform.getPageParam().getFromIndex());
            Page<Zform> pageMap = zformService.findPageMap(page,
                    zform,
                    path,
                    currentUserName.get(),
                    genTable,
                    traceFlag,
                    parentId,
                    "");
            if (zformAroundService != null) {
                for (GenTableColumn column : columnList) {
                    String settings = column.getSettings();
                    if (StringUtil.isEmpty(settings)) continue;
                    JSONObject jsonObject = JSONObject.parseObject(settings);
                    if (!jsonObject.containsKey("attachment")) continue;
                    String attachment = jsonObject.getString("attachment");
                    if (StringUtil.isEmpty(attachment)) continue;
                    String columnJSONString =JSON.toJSONString(column);
                    zformAroundService.afterFindPageMap(pageMap, page, zform, path, currentUserName.get(), genTable, traceFlag, parentId, columnJSONString);
                }
            }
            ResultJson resultJson = ResultJson.success().setRows(pageMap.getMap()).setTotal(pageMap.getCount());
            if (aroundController != null) {
                aroundController.beforeReturnDataMap(resultJson, pageMap, genTable, zform, zformMap, parentId);
                aroundController.beforeReturnDataMapWithCopyZformMap(resultJson, pageMap, genTable, zform, zformMapCopy, parentId,currentUserName.get());
            }
            long statisticsCount = columnList.stream().filter(k -> k.getGenTableColumnSetting() != null && k.getGenTableColumnSetting().getStatisticsConfigList() != null && k.getGenTableColumnSetting().getStatisticsConfigList().size() > 0).count();
            resultJson.put("has_statistics", statisticsCount > 0);//判断是否有统计字段
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_QUERY);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to get zform map data: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_QUERY);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 统计数据
     *
     * @param
     * @param zformMap
     * @return Json格式的表单列表
     */
    @Operation(summary = "获取统计数据")
    @RequiresPermissions("user")
    @PostMapping("/statisticsData/{t}")
    public ResultJson statisticsData(@RequestBody JSONObject zformMap, @PathVariable("t") String t, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam("parentId") String parentId) {
        try {
            zformMap.put("formNo", formNo);
            Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get(), true);
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            StatisticsVo statisticsVo = zformService.statisticsData(zform, currentUserName.get(), path, genTable, traceFlag, parentId, "", true);
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_QUERY);
            return ResultJson.success().put("statisticsVo", statisticsVo);
        } catch (Exception e) {
            logger.error("Error occurred while trying to statisticsData: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_QUERY);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取移动端列表数据
     *
     * @param path
     * @param zformMap
     * @return Json格式的表单列表
     */
    @Operation(summary = "获取移动端列表数据")
    @RequiresPermissions("user")
    @PostMapping("/mobileDatamap/{t}")
    public ResultJson mobileDatamap(@RequestBody JSONObject zformMap, @PathVariable("t") String t, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam("parentId") String parentId) {
        try {
            zformMap.put("formNo", formNo);
            Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get());
            //Page<Zform> page = datasourceFeign.findZformDataMap(zform, path, currentUserName.get(), traceFlag, "");

            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (projectProperties.isCheckSelectPermission()){
                //校验当前用户是否有该表的查询权限 需要当前用户具备对该表的至少一种 查询/添加/编辑/删除权限
                zformService.checkSelectPermission(currentUserName.get(), genTable.getName());
            }
            if (zform.getPageParam() == null) {
                zform.setPageParam(new PageParam());
            }
            Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
            page.setFromIndex(zform.getPageParam().getFromIndex());
            page = zformService.findPageMap(page,
                    zform,
                    path,
                    currentUserName.get(),
                    genTable,
                    traceFlag,
                    parentId,
                    "");
            List<LinkedHashMap> dataMap = page.getMap();

            List<GenTableColumn> columnList = genTable.getColumnList();

            HashMap<String, GenTableColumn> columnMap = new HashMap<>();
            HashMap<String, HashMap<String, DictResult>> dictMap = new HashMap<>();
            columnList.stream().filter(k -> StringUtil.isNotEmpty(k.getDictType())).forEach(k -> {
                columnMap.put(k.getName(), k);
                HashMap<String, DictResult> dict = new HashMap<>();
                List<DictResult> dictList = dictDataService.getDictList(k.getDictType(), false);
                dictList.forEach(d -> {
                    dict.put(d.getMember(), d);
                });
                dictMap.put(k.getDictType(), dict);
            });

            dataMap.forEach(row -> {
                LinkedHashMap<String, Object> valueTransMap = new LinkedHashMap<>();
                LinkedHashMap<String, Object> addNameMap = new LinkedHashMap<>();
                row.forEach((key, value) -> {
                    String k = (String) key;
                    if (columnMap.containsKey(k) && value != null && StringUtil.isNotEmpty(value.toString())) {
                        GenTableColumn c = columnMap.get(k);
                        if (c == null || !dictMap.containsKey(c.getDictType())) {
                            return;
                        }
//               解决字典person_post="INSPECTORS,SIGNATORY"需要翻译成person_post="检测员,授权签字人"  by zjq
                        String[] values = value.toString().split(",");
                        List<String> translatedValues = new ArrayList<>();
                        for (String v : values) {
                            DictResult dictResult = dictMap.get(c.getDictType()).get(v.trim());
                            if (dictResult != null) {
                                translatedValues.add(dictResult.getMemberName());
                            }
                        }
                        if (translatedValues.size() <= 0) {
                            return;
                        }
                        valueTransMap.put((String) key, String.join(",", translatedValues));
                    }
                });
                valueTransMap.forEach((key, value) -> {
                    row.put(key, value);
                });
                row.forEach((key, value) -> {
                    if (value instanceof HashMap) {
                        addNameMap.put(key + "__name", ((HashMap) value).get("name"));
                    }
                });
                addNameMap.forEach((key, value) -> {
                    row.put(key, value);
                });
            });
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, SecLogService.ACTION_QUERY);
            return ResultJson.success().setRows(dataMap).setTotal(page.getCount());
        } catch (Exception e) {
            logger.error("Error occurred while trying to get zform map data: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, SecLogService.ACTION_QUERY);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取表单Map查看列表
     *
     * @param path
     * @param zformMap
     * @return Json格式的表单列表
     */
    @PostMapping("/datamapView/{t}")
    public ResultJson datamapView(@RequestBody JSONObject zformMap, @PathVariable("t") String t, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam("parentId") String parentId) {
        try {
            zformMap.put("formNo", formNo);
            Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get());
            //Page<Zform> page = datasourceFeign.findZformDataMap(zform, path, currentUserName.get(), traceFlag, "");

            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (zform.getPageParam() == null) {
                zform.setPageParam(new PageParam());
            }
            Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
            page.setFromIndex(zform.getPageParam().getFromIndex());
            page = zformService.findPageMap(page,
                    zform,
                    path,
                    currentUserName.get(),
                    //currentUserNameGuest,
                    genTable,
                    traceFlag,
                    parentId,
                    GenTable.EXT_FLAG_VIEW);
            secLogService.saveSecLogZform(currentUserNameGuest, ip.get(), Global.YES, formNo, secLogService.ACTION_QUERY);
            return ResultJson.successView().setRows(page.getMap()).setTotal(page.getCount());
        } catch (Exception e) {
            logger.error("Error occurred while trying to get zform map data: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserNameGuest, ip.get(), Global.NO, formNo, secLogService.ACTION_QUERY);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取子表数据
     */
    @Operation(summary = "获取子表数据")
    @RequiresPermissions("user")
    @PostMapping("/dataChildren")
    public ResultJson dataChildren(@RequestBody Zform zform, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam("parentId") String parentId) throws Exception {
        zform.setFormNo(formNo);
        Page<Zform> page = new Page<Zform>();
        if (StringUtil.isNotEmpty(parentId)) {
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (zform.getPageParam() == null) {
                zform.setPageParam(new PageParam());
            }
            page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
            page.setFromIndex(zform.getPageParam().getFromIndex());
            page = zformService.findPage(page,
                    zform,
                    path,
                    currentUserName.get(),
                    genTable,
                    traceFlag,
                    parentId);
        }
        return ResultJson.success().setRows(page.getList()).setTotal(page.getCount());
    }

    /**
     * 获取表格选择数据
     */
    @Operation(summary = "获取表格选择数据")
    @RequiresPermissions("user")
    @PostMapping("/gridselectData")
    public ResultJson gridselectData(@RequestBody GridselectParam gridselectParam) {
        if (gridselectParam.getPageParam().getPageNo() == 0) {
            gridselectParam.getPageParam().setPageNo(1);
            gridselectParam.getPageParam().setPageSize(9999);
        }
        ResultJson resultJson = zformService.gridselectData(gridselectParam);
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 获取表格选择Map数据
     */
    @Operation(summary = "获取表格选择Map数据")
    @RequiresPermissions("user")
    @PostMapping("/gridselectDataMap")
    public ResultJson gridselectDataMap(@RequestBody GridselectParam gridselectParam) {
        if (gridselectParam.getPageParam().getPageNo() == 0) {
            gridselectParam.getPageParam().setPageNo(1);
            gridselectParam.getPageParam().setPageSize(9999);
        }
        ResultJson resultJson = zformService.gridselectDataMap(gridselectParam);
        resultJson.setToken(token.get());
        return resultJson;
    }

    private int sumTreeDataCount(List<Zform> dataList, Map<String, Integer> mapCount, String targetId) {
        int sum = 0;
        if (mapCount == null || mapCount.isEmpty()) {
            return sum;
        }
        if (mapCount.containsKey(targetId)) {
            sum += mapCount.get(targetId);
        }
        for (Zform zform : dataList) {
            String id = zform.getId();
            String parentId = zform.getParent() != null ? zform.getParent().getId() : "0";
            if (targetId.equals(parentId)) {
                if (zform.isHasChildren()) {
                    sum += sumTreeDataCount(dataList, mapCount, id);
                } else if (mapCount.containsKey(id)) {
                    sum += mapCount.get(id);
                }
            }
        }
        return sum;
    }

    /**
     * 获取树形数据
     */
    @Operation(summary = "获取树形数据")
    @RequiresPermissions("user")
    @PostMapping("/treeData")
    public ResultJson treeData(@RequestParam("parentId") String parentId, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag, @RequestParam(value = "rightTableFormNo", required = false) String rightTableFormNo,
                               @RequestBody(required = false) JSONObject zformMap) throws Exception {
        if(zformMap == null) {
            zformMap = new JSONObject();
        }
        Zform zform = new Zform();
        zform.setParent(new Zform(parentId, formNo));
        zform.setFormNo(formNo);
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        //上级菜单数据，过滤掉按钮和链接，只列出菜单，type_ = 1
        if ("sys_menu".equals(formNo)) {
            zform.setS10(Global.YES);
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPage(page,
                zform,
                "",
                currentUserName.get(),
                genTable,
                traceFlag,
                "");
        String userCount = null;
        List<TreeView> treeViewList = Lists.newArrayList();
        Map<String, Integer> mapCount = new HashMap<>();
        if (StringUtil.isNotEmpty(rightTableFormNo) && !"undefined".equals(rightTableFormNo)) {
            GenTable genTableRight = genTableService.getGenTableWithDefination(rightTableFormNo);
            if (genTableRight != null) {
                zformMap.put("formNo", rightTableFormNo);
                Zform zformRight = zformService.getZformFromMap(zformMap, currentUserName.get());
                mapCount = zformService.countByParentId(genTableRight, zformRight);
            }
        }
        for (Zform theZform : page.getList()) {
            if (StringUtil.isEmpty(theZform.getS49()) || Global.NO.equals(theZform.getS49())) {
                userCount = "";
            } else {
                userCount = "(" + theZform.getS49() + ")";
            }
            TreeView treeView = new TreeView();
            treeView.setId(theZform.getId());
            treeView.setName(theZform.getS01() + userCount);
            treeView.setName_EN(theZform.getS50() + userCount);
            treeView.setParentId(theZform.getParent() != null ? theZform.getParent().getId() : "0");
            treeView.setSort(theZform.getSort() != null ? theZform.getSort() : 0);
            treeView.setHasChildren(theZform.isHasChildren());
            treeView.setNo(theZform.getS02());
            treeView.setDataCount(sumTreeDataCount(page.getList(), mapCount, theZform.getId()));
            treeViewList.add(treeView);
        }
        return ResultJson.success().put("data", treeViewList);
    }

    /**
     * 获取列表数据总数
     */
    @Operation(summary = "获取列表数据总数")
    @RequiresPermissions("user")
    @PostMapping("/dataCount")
    public ResultJson dataCount(@RequestBody JSONObject zformMap, @RequestParam("path") String path, @RequestParam("formNo") String formNo) throws Exception {
        zformMap.put("formNo", formNo);
        Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get());

        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (projectProperties.isCheckSelectPermission()){
            //校验当前用户是否有该表的查询权限 需要当前用户具备对该表的至少一种 查询/添加/编辑/删除权限
            zformService.checkSelectPermission(currentUserName.get(), genTable.getName());
        }
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        String countValue = zformService.findCount(new Page<Zform>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy()),
                zform,
                path,
                currentUserName.get(),
                genTable);
        return ResultJson.success().put("data", countValue);
    }

    /**
     * 保存表单数据
     */
    @Deprecated
    @Operation(summary = "保存表单数据")
    @RequiresPermissions("user")
    @PostMapping("/save")
    public ResultJson save(@RequestBody Zform zform) {
        try {
            ResultJson resultJson = zformService.saveZform(zform, currentUserName.get(), "/dynamic/zform");
            resultJson.setToken(token.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, zform.getFormNo(), secLogService.ACTION_SAVE);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to save zform: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, zform.getFormNo(), secLogService.ACTION_SAVE);
            return ResultJson.failed(e.getMessage());
        }
    }

    private void processActSettings(JSONObject zformMap) throws Exception {
        if (zformMap.containsKey("id") && zformMap.containsKey("formNo") && zformMap.containsKey("procDefKey")) {
            String id = StringUtil.getString(zformMap.get("id"));
            String formNo = StringUtil.getString(zformMap.get("formNo"));
            String procDefKey = StringUtil.getString(zformMap.get("procDefKey"));
            if (StringUtil.isNotEmpty(procDefKey)) {
                Zform theZform = zformService.getZformWithAct(formNo, id, procDefKey, currentUserName.get());
                String actor = theZform.getRuleArgs().get("formExtend").get("act.actor");
                String actime = theZform.getRuleArgs().get("formExtend").get("act.actime");
                if (StringUtil.isNotEmpty(actor)) {
                    zformMap.put(actor, currentUserId.get());
                }
                if (StringUtil.isNotEmpty(actime)) {
                    zformMap.put(actime, DateUtil.getDateTime());
                }
                logger.info(theZform.getId());
            }
        }
    }

    /**
     * 保存表单Map数据
     */
    @Operation(summary = "保存表单Map数据")
    @RequiresPermissions("user")
    @PostMapping("/savemap")
    public ResultJson savemap(@RequestBody JSONObject zformMap) {
        Zform zform = null;
        //获取设置为NULL字段的map
        HashMap<String, Object> updateNullParamMap = zformMap.getObject("updateNullParamMap", new TypeReference<HashMap<String, Object>>() {
        });
        String formNo = "";
        try {
            this.processActSettings(zformMap);
            zform = zformService.getZformFromMap(zformMap, currentUserName.get());
            formNo = zform.getFormNo();
            if (StringUtil.isNotEmpty(formNo)) {
                ResultJson resultJson = zformService.saveZform(zform, currentUserName.get(), "/dynamic/zform", updateNullParamMap);
                resultJson.setToken(token.get());
                secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, zform.getFormNo(), secLogService.ACTION_SAVE);
                return resultJson;
            } else {
                return ResultJson.failed("参数错误，缺少formNo");
            }
        } catch (Exception e) {
            logger.error("Error occurred while trying to save zform: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, zform.getFormNo(), secLogService.ACTION_SAVE);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 保存主表（子表前置）
     */
    @Operation(summary = "保存主表（子表前置）")
    @RequiresPermissions("user")
    @PostMapping("/beforeSave")
    public ResultJson beforeSave(@RequestBody Zform zform) {
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (false == zform.getIsNewRecord()) {
            return ResultJson.failed();
        } else {
            //新增
            User currentUser = UserUtil.getByLoginName(currentUserName.get());
            zform.setOwnerCode(currentUser.getCompany().getCode());
            zform.setCreateBy(currentUser);
            zformService.beforeSave(zform, genTable);
            return ResultJson.success().setInsertedId(zform.getId());
        }
    }

    /**
     * 删除表单数据
     */
    @Deprecated
    @Operation(summary = "删除表单数据")
    @RequiresPermissions("user")
    @PostMapping("/delete")
    public ResultJson delete(@RequestParam("formNo") String formNo, @RequestParam("ids") String ids) {
        try {
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            zformService.deleteAll(ids, formNo, genTable, currentUserName.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_DELETE);
            return ResultJson.success("删除成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to delete zform: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_DELETE);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 批量删除表单数据
     */
    @Operation(summary = "批量删除表单数据")
    @RequiresPermissions("user")
    @PostMapping("/deleteBatch")
    public ResultJson deleteBatch(@RequestParam("formNo") String formNo, @RequestBody List<String> idList) {
        try {
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            zformService.deleteAll(StrUtil.join(",", idList), formNo, genTable, currentUserName.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_DELETE);
            return ResultJson.success("删除成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to delete zform: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_DELETE);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 批量操作
     */
    @Operation(summary = "批量操作")
    @RequiresPermissions("user")
    @PostMapping("/batchOperation")
    public ResultJson batchOperation(@RequestBody Zform zform,
                                     @RequestParam("formNo") String formNo,
                                     @RequestParam("ids") String ids,
                                     @RequestParam("data") String data,
                                     @RequestParam("extData") String extData) {
        try {
            ResultJson resultJson = new ResultJson();
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            String msg = zformService.batchOperation(zform, ids, formNo, genTable, data, extData, currentUserName.get());
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg(StringUtil.isEmpty(msg) ? "批量操作成功" : msg);
            resultJson.setMsg_en("Batch operation was successful");
            resultJson.setToken(token.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, "批量操作");
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to delete zform: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, "批量操作");
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 批量保存选择
     */
    @Operation(summary = "批量保存选择")
    @RequiresPermissions("user")
    @PostMapping("/batchSaveSelect")
    public ResultJson batchSaveSelect(@RequestBody List<JSONObject> data,
                                      @RequestParam("formNo") String formNo) {
        try {
            zformService.batchSaveSelect(data, formNo, currentUserName.get());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, "批量操作");
            return ResultJson.success();
        } catch (Exception e) {
            logger.error("Error occurred while trying to Batch save select: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, "批量操作");
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取流程用户列表
     */
    @Operation(summary = "获取流程用户列表")
    @RequiresPermissions("user")
    @PostMapping("/getUserList")
    public ResultJson getUserList(@RequestBody JSONObject zformMap) throws Exception {
        // 已知限制：流程分支判断字段后续应改为真实业务字段
        Zform zform = zformService.getZformFromMap(zformMap, currentUserName.get());
        Object _formExtend = zform.getActRuleArgs().get(ActExtentions.formExtend);
        JSONObject actRuleArgs = zform.getActRuleArgs();
        Act act = zform.getAct();
        Map formExtend = null;
        if (_formExtend instanceof JSONObject) {
            formExtend = ((JSONObject) _formExtend).getInnerMap();
        } else if (_formExtend instanceof LinkedHashMap) {
            formExtend = (LinkedHashMap) _formExtend;
        } else {
            throw new RuntimeException("getTargetUserList:formExtend类型不支持");
        }
        LinkedHashMap<String, Object> targetUserInfo = Maps.newLinkedHashMap();
        if (formExtend.containsKey(ActExtentions.actQueryDataInJava)) {
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            zform = zformService.get(zform.getId(), currentUserName.get(), genTable);
            targetUserInfo.put("queryDataInJava", zformService.getMapByZform(zform));
        }
        zform.setActRuleArgs(actRuleArgs);
        zform.setAct(act);
        targetUserInfo.putAll(zformService.getTargetUserList(zform, currentUserName.get()));
        List<User> userList = Lists.newArrayList();
        if (StringUtil.isNotEmpty(zform.getSecLevel()) && this.oaSecLevelSwitch) {
            List<User> unfilterUserList = (List<User>) targetUserInfo.get("userList");
            if (unfilterUserList != null && unfilterUserList.size() > 0) {
                for (User user : unfilterUserList) {
                    if (StringUtil.isNotEmpty(user.getSecLevel()) && zform.getSecLevel().compareTo(user.getSecLevel()) <= 0) {
                        userList.add(user);
                    }
                }
            }
            targetUserInfo.put("userList", userList);
        } else {
            //无需过滤
        }
        return ResultJson.success().setData(targetUserInfo);
    }

    /**
     * 获取流程节点列表
     */
    @Operation(summary = "获取流程节点列表")
    @RequiresPermissions("user")
    @PostMapping("/getNodeList")
    public ResultJson getNodeList(@RequestBody Zform zform) {
        zform.setTempRuleArgsClass(TaskPermission.class.getSimpleName());
        LinkedHashMap<String, Object> targetNodeInfo = zformService.getNodeList(zform, currentUserName.get());

        // 回退权限过滤
        Object nodeListObj = targetNodeInfo.get("nodeList");
        if (nodeListObj instanceof List) {
            Act act = zform.getAct();
            String taskDefKey = act.getTaskDefKey();
            QueryWrapper<OaTaskFallbackPermissionsSettingEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("task_def_key", taskDefKey);
            List<OaTaskFallbackPermissionsSettingEntity> permissionSettingList = oaTaskFallbackPermissionsSettingService.list(wrapper);

            if (!permissionSettingList.isEmpty()) {
                Set<String> returnableTaskIdSet = new HashSet<>();
                List<HistoricTaskInstance> nNodeList = new LinkedList<>();
                List<HistoricTaskInstance> nodeList = (List<HistoricTaskInstance>) nodeListObj;
                permissionSettingList.forEach(e -> {
                    returnableTaskIdSet.add(e.getReturnableTo());
                });
                nodeList.forEach(node -> {
                    if (returnableTaskIdSet.contains(ConvertUtil.getString(node.getTaskDefinitionKey()))) {
                        nNodeList.add(node);
                    }
                });
                targetNodeInfo.put("nodeList", nNodeList);
            }
        }
        return ResultJson.success().setData(targetNodeInfo);
    }

    /**
     * 获取当前用户名
     */
    @Deprecated
    @Operation(summary = "获取当前用户名")
    @RequiresPermissions("user")
    @GetMapping("/getCurrentUser")
    public ResultJson getCurrentUser() {
        try {
            String userName = UserUtil.getByLoginName(currentUserName.get()).getName();
            return ResultJson.success().put("data", userName);
        } catch (Exception e) {
            logger.error("Error occurred while trying to get current user name: " + ExceptionUtils.getStackTrace(e));
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取规则变量
     */
    @Operation(summary = "获取规则变量")
    @RequiresPermissions("user")
    @PostMapping("/getRuleArgs")
    public ResultJson getRuleArgs(@RequestBody Zform zform, @RequestParam("procDefKey") String procDefKey) {
        zform.setTempRuleArgsClass(TaskPermission.class.getSimpleName());
        ProcessDefinition processDefinition = zformService.getProcessDefinition(procDefKey);
        zform.getAct().setProcDefId(processDefinition.getId());
        zform.getAct().setTaskDefKey(processDefinition.getDescription().split(",")[1]);
        zformService.setRuleArgs(zform, currentUserName.get());
        return ResultJson.success().put("data", zform);
    }

    /**
     * 创建流程节点
     */
    @Operation(summary = "创建流程节点")
    @RequiresPermissions("user")
    @PostMapping("/createNode")
    public ResultJson createNode(@RequestBody Zform zform) {
        LinkedHashMap<String, Object> map = zformService.createNode(zform, currentUserName.get());
        map.put("tempRuleArgsClass", TaskPermission.class.getSimpleName());
        return ResultJson.success().setData(map);
    }

    /**
     * 删除流程节点
     */
    @Operation(summary = "删除流程节点")
    @RequiresPermissions("user")
    @PostMapping("/deleteNode")
    public ResultJson deleteNode(@RequestBody Zform zform) {
        return ResultJson.success().setData(zformService.deleteNode(zform, currentUserName.get()));
    }

    /**
     * 退回任务
     */
    @Operation(summary = "退回任务")
    @RequiresPermissions("user")
    @PostMapping("/backward")
    public ResultJson backward(@RequestParam("formNo") String formNo, @RequestParam("ids") String ids) throws Exception {
        LinkedHashMap<String, Object> resultMap = Maps.newLinkedHashMap();
        User currentUser = UserUtil.getByLoginName(currentUserName.get());
        for (String id : ids.split(",")) {
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            Zform zform = zformService.get(id, genTable);
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
            if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
                zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
            }
            zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, genTable.getSqlInsert());
            zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, genTable.getSqlUpdate());
            zformService.backward(zform, currentUser.getId(), currentUserName.get(), resultMap);
        }
        return ResultJson.success().setData(resultMap);
    }

    /**
     * 撤销任务
     */
    @Operation(summary = "撤销任务")
    @RequiresPermissions("user")
    @PostMapping("/revoke")
    public ResultJson revoke(@RequestParam("formNo") String formNo, @RequestParam("ids") String ids) throws Exception {
        LinkedHashMap<String, Object> resultMap = Maps.newLinkedHashMap();
        for (String id : ids.split(",")) {
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            Zform zform = zformService.get(id, genTable);
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
            if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
                zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
            }
            zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, genTable.getSqlInsert());
            zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, genTable.getSqlUpdate());
            zformService.revoke(zform, resultMap);
        }
        return ResultJson.success().setData(resultMap);
    }

    /**
     * 创建知会节点
     */
    @Operation(summary = "创建知会节点")
    @RequiresPermissions("user")
    @PostMapping("/notifyNode")
    public ResultJson notifyNode(@RequestBody Zform zform) {
        return ResultJson.success().setData(zformService.notifyNode(zform, currentUserName.get()));
    }

    /**
     * 创建分发节点
     */
    @Operation(summary = "创建分发节点")
    @RequiresPermissions("user")
    @PostMapping("/distributeNode")
    public ResultJson distributeNode(@RequestBody Zform zform) {
        return ResultJson.success().setData(zformService.distributeNode(zform, currentUserName.get()));
    }

    /**
     * 检查是否可退回
     */
    @Operation(summary = "检查是否可退回")
    @RequiresPermissions("user")
    @GetMapping("/rollBackCheck")
    public ResultJson rollBackCheck(@RequestParam("procInsId") String procInsId) {
        return ResultJson.success().setData(zformService.rollBackCheck(procInsId, currentUserName.get()));
    }

    /**
     * 获取流程定义列表
     */
    @Operation(summary = "获取流程定义列表")
    @RequiresPermissions("user")
    @GetMapping("/getProcDefList")
    public ResultJson getProcDefList(@RequestParam(value = "formNo") String formNo) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        List<ProcessDefinition> procDefList = zformService.getProcessDefinitionList(genTable.getProcessDefinitionCategory(), currentUserName.get());
        List<Map<String, String>> list = Lists.newArrayList();
        for (ProcessDefinition processDefinition : procDefList) {
            Map<String, String> map = Maps.newHashMap();
            map.put("procDefKey", processDefinition.getKey());
            map.put("procDefName", processDefinition.getName());
            list.add(map);
        }
        return ResultJson.success().put("procDefList", list);
    }

    /**
     * 检查缓存查看状态
     */
    @Operation(summary = "检查缓存查看状态")
    @RequiresPermissions("user")
    @GetMapping("/isCacheView")
    public ResultJson isCacheView(@RequestParam(value = "processInstanceId") String processInstanceId) {
        return ResultJson.success().put("data", zformService.isCacheView(processInstanceId));
    }

    /**
     * 检查缓存数据状态
     */
    @Operation(summary = "检查缓存数据状态")
    @RequiresPermissions("user")
    @GetMapping("/isCacheData")
    public ResultJson isCacheData(@RequestParam(value = "processInstanceId") String processInstanceId) {
        return ResultJson.success().put("data", zformService.isCacheData(processInstanceId, currentUserName.get()));
    }

    @Autowired
    HistoryService historyService;

    /**
     * 获取流程跟踪图
     */
    @Operation(summary = "获取流程跟踪图")
    @RequiresPermissions("user")
    @GetMapping("/tracePhoto")
    public ResultJson tracePhoto(@RequestParam(value = "procDefId", required = false) String procDefId, @RequestParam(value = "procInsId") String procInsId) throws IOException {
            /*if (StringUtil.isEmpty(procDefId)) {
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId).singleResult();
                procDefId = historicProcessInstance.getProcessDefinitionId();
            }*/
        InputStream imageStream = zformService.tracePhotoNew(procInsId);
        byte[] bytes = IOUtils.toByteArray(imageStream);
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return ResultJson.success().put("data", encoded);
    }

    /**
     * 获取历史流转记录
     */
    @Operation(summary = "获取历史流转记录")
    @RequiresPermissions("user")
    @GetMapping("/histoicFlow")
    public ResultJson histoicFlow(@RequestParam(value = "procInsId") String procInsId) {
        return ResultJson.success().put("data", zformService.histoicFlowList(procInsId));
    }

    /**
     * 获取任务列表
     */
    @Operation(summary = "获取任务列表")
    @RequiresPermissions("user")
    @PostMapping("/getTaskList")
    public ResultJson getTaskList(@RequestBody Zform zform,
                                  @RequestParam("path") String path) {
        ResultJson resultJson = ResultJson.success();
        Map<String, Object> taskMap = this.getTaskListData(zform, path, currentUserName.get());
        if (taskMap != null) {
            resultJson.setRows(taskMap.get("rows"));
            resultJson.setTotal(taskMap.get("total"));
        } else {
            resultJson.setRows(Lists.newArrayList());
            resultJson.setTotal("0");
        }
        return resultJson;
    }

    private Map<String, Object> getTaskListData(Zform zform,
                                                String path,
                                                String loginName) {
        List<String> categoryList = Lists.newArrayList();
        String parentid = zform.getParent().getId();
        Zform temp = new Zform();
        temp.setFormNo("oa_process_tree_setting");
        GenTable genTable = genTableService.getGenTableWithDefination("oa_process_tree_setting");
        List<Zform> zformList = zformService.findList(temp, genTable);
        if (StringUtil.isNotEmpty(parentid)) {
            for (int i = 0; i < zformList.size(); i++) {
                if (zformList.get(i).getId().equals(parentid) || zformList.get(i).getParentIds().contains(parentid)) {
                    if (StringUtil.isNotEmpty(zformList.get(i).getS02())) {
                        categoryList.add(zformList.get(i).getS02());
                    }
                }
            }
        } else {
            for (int i = 0; i < zformList.size(); i++) {
                if (StringUtil.isNotEmpty(zformList.get(i).getS02())) {
                    categoryList.add(zformList.get(i).getS02());
                }
            }
        }
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
            zform.getPageParam().setParamMap(Maps.newHashMap());
        }
        Map<String, Object> taskMap = zformService.getTaskList(categoryList,
                path,
                loginName,
                zform.getPageParam().getPageNo(),
                zform.getPageParam().getPageSize(),
                zform.getPageParam().getParamMap());
        return taskMap;
    }

    /**
     * 获取工作数据
     */
    @Operation(summary = "获取工作数据")
    @RequiresPermissions("user")
    @PostMapping("/getWorkData")
    public ResultJson getWorkData() {
        GenTable genTable = new GenTable();
        genTable.setIsMobile(Global.YES);
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(1);
        pageParam.setPageSize(Integer.MAX_VALUE);
        genTable.setPageParam(pageParam);
        Page<GenTable> page = genTableService.findPage(new Page<GenTable>(genTable.getPageParam().getPageNo(),
                        genTable.getPageParam().getPageSize(),
                        genTable.getPageParam().getOrderBy()),
                genTable);
        List<GenTable> list = page.getList();
        JSONArray array = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            GenTable gen = list.get(i);
            JSONObject obj = new JSONObject();
            obj.put("id", gen.getName());
            obj.put("name", gen.getComments());
            obj.put("func", gen.getName() + "/list.html");
            obj.put("icon", gen.getMobileIcon());
            obj.put("choose", false);
            obj.put("index", 0);
            obj.put("url", "");
            array.add(obj);
        }
        return ResultJson.success().put("data", array);
    }

    /**
     * 导出数据到队列
     */
    @Parameters({@Parameter(name = "path", description = "path", required = true),
            @Parameter(name = "loginName", description = "loginName", required = true),
            @Parameter(name = "formNo", description = "formNo", required = true),
            @Parameter(name = "traceFlag", description = "traceFlag", required = false),
            @Parameter(name = "parentId", description = "parentId", required = false)})
    @PostMapping("/exportDataToQueue")
    public ResultJson exportDataToQueue(@RequestBody JSONObject zformMap,
                                        @RequestParam("formNo") String formNo,
                                        @RequestParam("path") String path,
                                        @RequestParam("traceFlag") String traceFlag,
                                        @RequestParam("parentId") String parentId) throws Exception {
        return zformService.exportDataToQueue(zformMap, formNo, path, traceFlag, parentId);
    }

    /**
     * 按URL导出Excel
     */
    @Operation(summary = "按URL导出Excel")
    //@RequiresPermissions("user")
    @Parameters({@Parameter(name = "path", description = "path", required = true),
            @Parameter(name = "url", description = "url", required = true)
    })
    @PostMapping("/expdataByUrl")
    public ResultJson expdataByUrl(HttpServletRequest request,
                                   @RequestBody JSONObject zformMap,
                                   @RequestParam("url") String url, @RequestParam(value = "formNo",required = false) String formNo
    ) throws Exception {
        String fileName = zformService.getExcelExportFileName(zformMap);
        if (fileName == null) {
            return ResultJson.failed("导出数据异常,未设置导出文件名");
        }
        List<ExcelField> excelFieldList = zformService.getExcelExportFieldList(zformMap);
        if (excelFieldList == null || excelFieldList.size() == 0) {
            return ResultJson.failed("导出数据异常,未设置导出字段");
        }
        zformMap.remove("exportAsTableColumns");
        zformMap.remove("exportWithSubTables");

        JSONArray mergeColumnConfig = new JSONArray();
        if (zformMap.containsKey("mergeColumnConfig")) {
            mergeColumnConfig = zformMap.getJSONArray("mergeColumnConfig");
            zformMap.remove("mergeColumnConfig");
        }
        //获取当前项目的http请求路径
        String serverPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" + adminPath + "/";
        if (StringUtil.isNotEmpty(projectProperties.getServerPath())) {
            serverPath = projectProperties.getServerPath();
        }
        String decodeUrl = Base64Decoder.decodeStr(url);
        String publicKey = request.getHeader("serverPublicKey");
        if (StringUtil.isNotEmpty(publicKey)) {
            logger.info("expdataByUrl decodeUrl 加密前: {}", decodeUrl);
            //判断decodeUrl 是否携带参数 将参数加密
            if (decodeUrl.contains("?")) {
                String[] split = decodeUrl.split("\\?");
                String plainUrl = split[0] + "?";
                String param = split[1];
                String[] split1 = param.split("&");
                for (int i = 0; i < split1.length; i++) {
                    String[] split2 = split1[i].split("=");
                    String key = split2[0];
                    String value = split2[1];
                    if (StringUtil.isNotEmpty(value)) {
                        plainUrl += key + "=" + SM2Util.encrypt(publicKey, value) + "&";
                    }
                }
                decodeUrl = plainUrl.substring(0, plainUrl.length() - 1);
            }
        }
        String path = serverPath + decodeUrl;
        AroundServiceI aroundService = null;
        if (StringUtil.isNotEmpty(formNo)){
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            aroundService = AroundUtil.getAroundServiceI(genTable);
            if (aroundService != null) {
                aroundService.beforeExpdataByUrl(fileName, excelFieldList,zformMap,path);
            }
        }
        logger.info("expdataByUrl url: {}", path);
        HttpRequest post = HttpUtil.createPost(path);
        post.header("token", token.get());
        JSONObject pageParam = zformMap.getJSONObject("pageParam");
        if (pageParam == null) {
            pageParam = new JSONObject();
        }
        pageParam.put("pageNo", 1);
        pageParam.put("pageSize", Integer.MAX_VALUE);
        zformMap.put("pageParam", pageParam);

        AsymmetricKey asymmetricKey = SM2Util.generateKey();
        if (StringUtil.isNotEmpty(publicKey)) {
            logger.info("expdataByUrl publicKey: {}", publicKey);
            JSONObject data = new JSONObject();
            logger.info("expdataByUrl postData 加密前: {}", zformMap);
            data.put("text", SM2Util.encrypt(publicKey, JSONObject.toJSONString(zformMap)));
            zformMap = data;
            post.header("clientPublicKey", asymmetricKey.getPublicKey());
            post.header("serverPublicKey", publicKey);
        }
        post.body(JSON.toJSONString(zformMap));
        logger.info("expdataByUrl postData: {}", zformMap);
        HttpResponse execute = post.execute();
        String body = execute.body();
        JSONObject jsonObject = JSON.parseObject(body);
        if (StringUtil.isNotEmpty(publicKey)) {
            String data = jsonObject.getString("data");
            body = SM2Util.decrypt(asymmetricKey.getPrivateKey(), data);
        }
        ResultJson resultJson = jsonObject.parseObject(body, ResultJson.class);
        logger.info("expdataByUrl resultJson: size:{}", resultJson.getTotal());
        Object jsonRows = resultJson.getRows();
        if (jsonRows instanceof JSONArray) {
            ExportExtraEntity extraData = new ExportExtraEntity();
            if (aroundService != null) {
                aroundService.afterExpdataByUrl(fileName, excelFieldList, (JSONArray) jsonRows, false, mergeColumnConfig,extraData);
            }

            TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle = extraData.getDataCellStyle();

            return zformService.exportExcel(fileName + "导出.xlsx", excelFieldList, zformService.parseExportData((JSONArray) jsonRows), false, mergeColumnConfig, dataCellStyle);
        } else if (jsonRows == null) {
            return ResultJson.failed("导出数据异常,返回数据为null");
        } else {
            return ResultJson.failed("导出数据异常,返回数据格式不正确," + jsonRows.getClass());
        }
    }

    /**
     * 导出Excel
     */
    //@RequiresPermissions("user")
    @Parameters({@Parameter(name = "path", description = "path", required = true),
            @Parameter(name = "loginName", description = "loginName", required = true),
            @Parameter(name = "formNo", description = "formNo", required = true),
            @Parameter(name = "traceFlag", description = "traceFlag", required = false),
            @Parameter(name = "parentId", description = "parentId", required = false)})
    @PostMapping("/expdata")
    public ResultJson expdata(@RequestBody JSONObject zformMap,
                              @RequestParam("formNo") String formNo,
                              @RequestParam("path") String path,
                              @RequestParam("traceFlag") String traceFlag,
                              @RequestParam("parentId") String parentId
    ) throws Exception {
        return zformService.expdata(zformMap, formNo, path, traceFlag, parentId, currentUserName.get());
    }

    private List<List<LinkedHashMap<String, String>>> expdataOk(Zform zform,
                                                                String path,
                                                                String loginName,
                                                                String traceFlag,
                                                                String parentId) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = zformService.findPage(new Page<Zform>(
                        zform.getPageParam().getPageNo(),
                        Integer.MAX_VALUE,
                        zform.getPageParam().getOrderBy()),
                zform,
                path,
                loginName,
                genTable,
                traceFlag,
                parentId);
        List<List<LinkedHashMap<String, String>>> listResult;
        listResult = zformService.exportData(page, zform, genTable, loginName);
        return listResult;
    }

    /**
     * 导入Excel
     * 从第2行开始读取并解析上传的excel文件
     *
     * @return List<LinkedHashMap < String, String>>
     */
    @Operation(summary = "导入Excel")
    @Parameters({
            @Parameter(name = "formNo", description = "formNo", required = false),
            @Parameter(name = "parentFormNo", description = "parentFormNo", required = false),
            @Parameter(name = "uniqueId", description = "uniqueId", required = false),
            @Parameter(name = "toCompany", description = "toCompany", required = false),
            @Parameter(name = "parentId", description = "parentId", required = false)})
    @PostMapping("/impdata")
    public ResultJson importExcel2Table(@RequestParam("formNo") String formNo,
                                        @RequestParam("parentFormNo") String parentFormNo,
                                        @RequestParam("uniqueId") String uniqueId,
                                        @RequestParam("toCompany") String toCompany,
                                        @RequestParam("fileId") String fileId,
                                        @RequestParam("parentId") String parentId,
                                        @RequestParam(value = "areaId", required = false, defaultValue = "") String areaId,
                                        HttpServletRequest request) {


        ResultJson resultJson = new ResultJson();
        File file = sysFileService.getFirstByGroupId(fileId, fileRoot);

        if (file.exists()) {
            try {
                ImportParam importParam = ImportParam.getImportParam(request);

                GenTable genTable = genTableService.getGenTableWithDefination(formNo);
                User currentUser = UserUtil.getByLoginName(currentUserName.get());
                String ownerCode = currentUser.getOffice().getCode();
                if (StringUtil.isNotEmpty(toCompany)) {
                    ownerCode = currentUser.getCompany().getCode();
                }
                List<ExcelField> importFieldList = genTableService.getExcelImportFieldList(genTable, areaId);

                importParam.setCurrentUser(currentUser);
                importParam.setOwnerCode(ownerCode);
                importParam.setImportFieldList(importFieldList);
                importParam.setGenTable(genTable);

                // 判断文件大小，超过 5MB 使用流式分批导入，避免大文件全量加载到内存导致 OOM
                boolean isLargeFile = file.length() > 5 * 1024 * 1024;
                String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                int totalCount;

                if (isLargeFile && "xlsx".equalsIgnoreCase(ext)) {
                    // 大文件：流式分批导入，每批 1000 行，内存中始终只有 1 批数据
                    final String finalOwnerCode = ownerCode;
                    totalCount = ExcelImportUtil.importDataSaxBatched(
                            null, importFieldList,
                            Files.newInputStream(file.toPath()), file.getName(),
                            new ArrayList<>(), 1000,
                            batch -> {
                                try {
                                    ImportParam batchParam = ImportParam.getImportParam(request);
                                    batchParam.setCurrentUser(currentUser);
                                    batchParam.setOwnerCode(finalOwnerCode);
                                    batchParam.setImportFieldList(importFieldList);
                                    batchParam.setMapList(batch);
                                    batchParam.setGenTable(genTable);
                                    zformService.importData(batchParam);
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                } else {
                    // 小文件：保持原有逻辑，一次性加载
                    List<Map<String, Object>> mapList = ExcelImportUtil.importData(null, importFieldList, Files.newInputStream(file.toPath()), file.getName(), new ArrayList<>());
                    importParam.setMapList(mapList);
                    zformService.importData(importParam);
                    totalCount = mapList.size();
                }

                if (null == genTable.getRespondProblemInstructions() || genTable.getRespondProblemInstructions().isEmpty()) {
                    resultJson.setMsg("成功导入" + totalCount + "条数据");
                    resultJson.setMsg_en("Import data success");
                    resultJson.setCode(ResultJson.CODE_SUCCESS);
                } else {
                    String message = "成功导入" + genTable.getDoneCount() + "条数据；导入数据错误原因：";
                    if (!genTable.getRespondProblemInstructions().isEmpty()) {
                        message = message + "<br>" + genTable.getRespondProblemInstructions();
                    }
                    resultJson.setMsg( message);
                    resultJson.setMsg_en("Import data error:" + message);
                    resultJson.setCode(ResultJson.DATA_IMPORT_ERROR);
                }
            } catch (Exception e) {
                logger.error("Error while importing data:" + ExceptionUtils.getStackTrace(e));
                resultJson.setMsg("导入数据错误：" + e.getMessage());
                resultJson.setMsg_en("Import data error:" + e.getMessage());
                resultJson.setCode(ResultJson.DATA_IMPORT_ERROR);//文件导入错误
            }
        } else {
            resultJson.setMsg("读取文件错误");
            resultJson.setMsg_en("Reading file error");
            resultJson.setCode(ResultJson.CODE_FAILED);
        }
        return resultJson;
    }

    /**
     * 获取表字典数据
     */
    @Operation(summary = "获取表字典数据")
    @Parameters({@Parameter(name = "formNo", description = "formNo", required = true),
            @Parameter(name = "propertyName", description = "propertyName", required = true),
            @Parameter(name = "orderBy", description = "orderBy", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/dictListFromTable")
    public String dictList(@RequestParam("formNo") String formNo, @RequestParam("propertyName") String propertyName, @RequestParam("orderBy") String orderBy) throws Exception {
        List<DictResult> list = Lists.newArrayList();
        Zform zform = new Zform();
        zform.setFormNo(formNo);
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(Integer.MAX_VALUE);
        if (StringUtil.isNotEmpty(orderBy)) {
            pageParam.setOrderBy(orderBy);
        } else {
            pageParam.setOrderBy("a.update_date desc");
        }
        zform.setPageParam(pageParam);
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPage(page,
                zform,
                "path",
                currentUserName.get(),
                genTable,
                "",
                "");
        String meberName;
        int i = 0;
        for (Zform obj : page.getList()) {
            DictResult dict = new DictResult();
            dict.setType(formNo);
            dict.setDictionaryID(obj.getId());
            dict.setHasChildren(false);
            dict.setMember(obj.getId());
            if (propertyName.equalsIgnoreCase("s01")) {
                meberName = obj.getS01();
            } else if (propertyName.equalsIgnoreCase("s02")) {
                meberName = obj.getS02();
            } else if (propertyName.equalsIgnoreCase("s03")) {
                meberName = obj.getS03();
            } else if (propertyName.equalsIgnoreCase("s04")) {
                meberName = obj.getS04();
            } else if (propertyName.equalsIgnoreCase("s05")) {
                meberName = obj.getS05();
            } else {
                break;
            }
            dict.setMemberName(meberName);
            dict.setMemberNameEn(meberName);
            dict.setSort(i++);
            list.add(dict);
        }
        return JsonConvertUtil.objectToJson(ResultJson.success().put("dict", list));
    }

    /**
     * 根据Key获取哈希值
     */
    @Deprecated
    @Operation(summary = "根据Key获取哈希值")
    @Parameters({@Parameter(name = "key", description = "key", required = true)})
    @GetMapping("/getHashByKey")
    public ResultJson getHashByKey(@RequestParam("key") String key) {
        return ResultJson.success().put("data", zformService.getHashByKey(key));
    }

    /**
     * 获取PDF路径
     */
    @Deprecated
    @GetMapping("/getPdfPath")
    public ResultJson getPdfPath(@RequestParam("id") String id,
                                 @RequestParam("formNo") String formNo) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = zformService.get(id, currentUserName.get(), genTable);
        String tempPath = genTable.getExportRuleName();
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isEmpty(tomcatPath)) {
            resultJson.setMsg("未配置前端tomcat访问路径！");
            resultJson.setCode(ResultJson.CODE_FAILED);
            return resultJson;
        }
        if (StringUtil.isEmpty(uploadPath)) {
            resultJson.setMsg("未配置单据pdf文件暂存路径！");
            resultJson.setCode(ResultJson.CODE_FAILED);
            return resultJson;
        }
        //讲规则中的pdf路径抽取出来
        if (StringUtil.isEmpty(tempPath)) {
            resultJson.setMsg("未配置表单规则");
            resultJson.setCode(ResultJson.CODE_FAILED);
            return resultJson;
        } else if (tempPath.indexOf("pdf:") != -1) {
            tempPath = tempPath.replaceFirst("pdf:", "");
        }
        new File(uploadPath).mkdirs();
        resultJson = zformService.exportPdf(zform, tempPath, tomcatPath, uploadPath);
        resultJson.setToken(token.get());
        return resultJson;
    }

    @GetMapping("/nextId")
    public ResultJson nextId() {
        return ResultJson.success().put("data", dataService.nextId());
    }

    /**
     * 导出弹窗文本到Excel
     */
    @Operation(summary = "导出弹窗文本到Excel")
    @PostMapping("/exportModalText")
    public ResultJson exportModalText(@RequestBody JSONObject request) throws Exception {
        JSONArray modalTextArr = request.getJSONArray("modalText");

        List<ExcelField> excelExportFieldList = new ArrayList<>();
        excelExportFieldList.add(new ExcelField("err", "错误信息"));
        List<Map<String, Object>> msgList = new LinkedList<>();
        modalTextArr.forEach(t -> {
            HashMap<String, Object> m = new HashMap<>();
            m.put("err", t);
            msgList.add(m);
        });
        return exportExcel("错误信息", excelExportFieldList, msgList);
    }
}
