package com.jeestudio.bpm.service.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.around.AroundControllerI;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.around.SubTableExportConfig;
import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.common.ActExtentions;
import com.jeestudio.bpm.common.entity.common.BaseEntity;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.gen.GenTableColumnSetting;
import com.jeestudio.bpm.common.entity.gen.GenTableExtRuleManyToMany;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.common.enums.QueryTypeEnum;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.common.view.common.DictView;
import com.jeestudio.bpm.common.view.system.UserView;
import com.jeestudio.bpm.feign.RequestVo;
import com.jeestudio.bpm.feign.WordExportFeignClient;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueEntity;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueExportStatus;
import com.jeestudio.bpm.modules.admin.entity.SysFileQueueExportThread;
import com.jeestudio.bpm.modules.admin.service.SysFileQueueServiceI;
import com.jeestudio.bpm.modules.oa.dao.OaWordTemplateMapper;
import com.jeestudio.bpm.service.act.NextNodeUserService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.base.utils.JSONHelper;
import com.jeestudio.tools.excel.ExcelExportUtil;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.excel.ExcelMergedRegionUtil;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.annotation.PreDestroy;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jeestudio.bpm.utils.ResultJson.success;

/**
 * @Description: 动态表单服务
 */
@Service
public class ZformService extends ZformBaseService {

    protected static final Logger logger = LoggerFactory.getLogger(ZformService.class);

    @Value("${fileRoot}")
    protected String urlFile;

    protected static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    WordExportFeignClient wordExportFeignClient;

    @Autowired
    OaWordTemplateMapper oaWordTemplateMapper;

    @Autowired
    SysFileQueueServiceI sysFileQueueService;

    //    @Override
    public Zform get(String id, String loginName, GenTable genTable) throws Exception {
        Zform zform = super.get(id, genTable);
        this.processBlockChainGet(zform, genTable);
        this.extendAfterGet(zform, loginName);
        return zform;
    }

    @Override
    public LinkedHashMap getMap(String id, GenTable genTable, String extFlag, String loginName) throws Exception {
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            aroundService.beforeGetMap(id, genTable, extFlag, loginName);
        }
        LinkedHashMap map = super.getMap(id, genTable, extFlag, loginName);
        this.extAfterGetMap(map, id, genTable, extFlag);
        if (aroundService != null) {
            map = aroundService.afterGetMap(map, id, genTable, extFlag, loginName);
        }
        return map;
    }

    /**
     * 查询留痕表并构造
     *
     * @param id
     * @param genTable
     * @param extFlag
     * @param loginName
     * @return
     * @throws Exception
     */
    public List<LinkedHashMap> getMapVersion(String id, GenTable genTable, QueryWrapper<Zform> wrapper, boolean skipWrapperSet, String extFlag, String loginName) throws Exception {
        Zform zform = new Zform(id, genTable.getName());
        zform.setVersionSchema(versionSchema);
        zform.setQueryWrapper(wrapper);
        zform.setPageParam(new PageParam());
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = this.findPageMapV(page,
                zform,
                PATH_QUERY,
                loginName,
                genTable,
                "",
                "",
                extFlag,
                skipWrapperSet
        );
        List<LinkedHashMap> list = page.getMap();
        //构造变更记录，如果所有字段都没变，查看remarks，有值则创建变更记录，无值则不创建变更记录
        //表名，操作时间，操作人，备注，变更内容
        List<DictView> keys = this.findKeyListByGentable(genTable);
        LinkedHashMap[] maps = list.stream().toArray(LinkedHashMap[]::new);
        List<LinkedHashMap> changeList = Lists.newArrayList();
        for (int i = 0; i < maps.length; i++) {
            if (i < maps.length - 1) {
                //排除最后一条数据，将最近两条数据比较
                LinkedHashMap changeMap = this.getChangeMap(genTable.getName(), keys, maps[i], maps[i + 1]);
                if (changeMap != null) {
                    changeList.add(changeMap);
                }
            }
        }
        return changeList;
    }

    public List<LinkedHashMap> getMapVersion(String id, GenTable genTable, String extFlag, String loginName) throws Exception {
        return getMapVersion(id, genTable, new QueryWrapper<>(), false, extFlag, loginName);
    }

    /**
     * 查询两次留痕变更记录：表名，操作时间，操作人，备注，变更内容
     *
     * @param formNo
     * @param keys
     * @param current
     * @param last
     * @return
     */
    private LinkedHashMap getChangeMap(String formNo, List<DictView> keys, LinkedHashMap current, LinkedHashMap last) {
        List<LinkedHashMap> list = Lists.newArrayList();
        Object object = null;
        for (DictView key : keys) {
            if (StringUtil.isNotEmpty(key.getDictId()) && "object".equals(key.getDictId())) {
                //是对象时
                object = current.get(key.getCode());
                if (object instanceof String) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", object);
                    object = map;
                }
                HashMap currentObj = object == null ? null : (HashMap) object;
                object = last.get(key.getCode());
                if (object instanceof String) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", object);
                    object = map;
                }
                HashMap lastObj = object == null ? null : (HashMap) object;
                if (currentObj != null && lastObj == null) {
                    //从空变为有值
                    String currentObjName = StringUtil.getString(currentObj.get("name"));
                    LinkedHashMap theMap = Maps.newLinkedHashMap();
                    theMap.put("name", key.getName());
                    theMap.put("code", key.getCode());
                    theMap.put("old", "空");
                    theMap.put("new", currentObjName);
                    list.add(theMap);
                } else if (currentObj == null && lastObj != null) {
                    //从有值变为空
                    String lastObjName = StringUtil.getString(lastObj.get("name"));
                    LinkedHashMap theMap = Maps.newLinkedHashMap();
                    theMap.put("name", key.getName());
                    theMap.put("code", key.getCode());
                    theMap.put("old", lastObjName);
                    theMap.put("new", "空");
                    list.add(theMap);
                } else if (currentObj != null && lastObj != null) {
                    //都有值时，继续判断值是否变化
                    String currentObjName = StringUtil.getString(currentObj.get("name"));
                    String lastObjName = StringUtil.getString(lastObj.get("name"));
                    if (false == currentObjName.equals(lastObjName)) {
                        //值有变化时，创建变更记录
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", lastObjName);
                        theMap.put("new", currentObjName);
                        list.add(theMap);
                    }
                }
            } else if (StringUtil.isNotEmpty(key.getDictId())) {
                //是系统字典时
                object = current.get(key.getCode());
                String currentObj = object == null ? null : StringUtil.getString(object);
                object = last.get(key.getCode());
                String lastObj = object == null ? null : StringUtil.getString(object);
                if (currentObj != null && lastObj == null) {
                    //从空变为有值
                    String currentObjName = dictDataService.getDictLabels(currentObj, key.getDictId(), "", "Zh-CN");
                    if (StringUtil.isNotEmpty(currentObjName)) {
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", "空");
                        theMap.put("new", currentObjName);
                        list.add(theMap);
                    }
                } else if (currentObj == null && lastObj != null) {
                    //从有值变为空
                    String lastObjName = dictDataService.getDictLabels(lastObj, key.getDictId(), "", "Zh-CN");
                    if (StringUtil.isNotEmpty(lastObjName)) {
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", lastObjName);
                        theMap.put("new", "空");
                        list.add(theMap);
                    }
                } else if (currentObj != null && lastObj != null) {
                    //都有值时，继续判断值是否变化
                    String currentObjName = dictDataService.getDictLabels(currentObj, key.getDictId(), "", "Zh-CN");
                    String lastObjName = dictDataService.getDictLabels(lastObj, key.getDictId(), "", "Zh-CN");
                    if (false == currentObjName.equals(lastObjName)) {
                        //值有变化时，创建变更记录
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", lastObjName);
                        theMap.put("new", currentObjName);
                        list.add(theMap);
                    }
                }
            } else {
                //简单属性
                object = current.get(key.getCode());
                String currentObj = object == null ? null : StringUtil.getString(object);
                object = last.get(key.getCode());
                String lastObj = object == null ? null : StringUtil.getString(object);
                if (currentObj != null && lastObj == null) {
                    //从空变为有值
                    String currentObjName = currentObj;
                    if (StringUtil.isNotEmpty(currentObjName)) {
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", "空");
                        theMap.put("new", currentObjName);
                        list.add(theMap);
                    }
                } else if (currentObj == null && lastObj != null) {
                    //从有值变为空
                    String lastObjName = lastObj;
                    if (StringUtil.isNotEmpty(lastObjName)) {
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", lastObjName);
                        theMap.put("new", "空");
                        list.add(theMap);
                    }
                } else if (currentObj != null && lastObj != null) {
                    //都有值时，继续判断值是否变化
                    String currentObjName = currentObj;
                    String lastObjName = lastObj;
                    if (false == currentObjName.equals(lastObjName)) {
                        //值有变化时，创建变更记录
                        LinkedHashMap theMap = Maps.newLinkedHashMap();
                        theMap.put("name", key.getName());
                        theMap.put("code", key.getCode());
                        theMap.put("old", lastObjName);
                        theMap.put("new", currentObjName);
                        list.add(theMap);
                    }
                }
            }
        }
        if (list.size() > 0) {
            //表名，操作时间，操作人，备注，变更内容
            LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
            map.put("formNo", formNo);
            map.put("update_date", StringUtil.getString(current.get("update_date")));
            HashMap updateBy = (HashMap) current.get("updateBy");
            UserView updateUser = userDao.getUserView(StringUtil.getString(updateBy.get("id")));
            map.put("update_by", StringUtil.getString(updateBy.get("name")));
            if (updateUser != null) {
                map.put("update_company", updateUser.getCompany());
                map.put("update_office", updateUser.getOffice());
            }
            map.put("remarks", StringUtil.getString(current.get("remarks")));
            map.put("change_content", list);
            map.put("vdate", current.get("vdate"));
            map.put("vdateStr", DateUtil.formatDateTime((Date) current.get("vdate")));
            return map;
        } else {
            return null;
        }
    }

    /**
     * 根据genTable定义查找非基本字段名列表
     *
     * @param genTable
     * @return
     */
    private List<DictView> findKeyListByGentable(GenTable genTable) {
        List<DictView> list = Lists.newArrayList();
        for (GenTableColumn column : genTable.getColumnList()) {
            if (!StringUtil.equalsIgnoreCase(column.getName(), "id")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "proc_ins_id")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "proc_def_key")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "create_by")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "create_date")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "update_by")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "update_date")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "owner_code")
                    && !StringUtil.equalsIgnoreCase(column.getName(), "del_flag")) {
                if (false == Global.YES.equals(column.getIsForm())) {
                    continue;
                }
                DictView dictView = new DictView();
                dictView.setCode(column.getName());
                dictView.setName(column.getComments());
                if (column.getJavaFieldId().contains(".")) {
                    //是对象标记
                    dictView.setDictId("object");
                } else if (StringUtil.isNotEmpty(column.getDictType())) {
                    //是系统字典标记
                    dictView.setDictId(column.getDictType());
                }
                list.add(dictView);
            }
        }
        return list;
    }

    @Override
    public List<Zform> findList(Zform zform, GenTable genTable) {
        return super.findList(zform, genTable);
    }

    @Override
    public Page<Zform> findPage(Page<Zform> page, Zform zform, GenTable genTable) {
        return super.findPage(page, zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(Zform zform, GenTable genTable) throws Exception {
        this.extendBeforeSave(zform, genTable);
        this.processBlockChainSet(zform, genTable);
        super.save(zform, genTable);
        this.extendAfterSave(zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void saveTree(Zform zform, GenTable genTable) {
        super.saveTree(zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void beforeSave(Zform zform, GenTable genTable) {
        super.beforeSave(zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Zform zform, GenTable genTable) throws Exception {
        this.processBlockChainDel(zform, genTable);
        super.delete(zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCascade(Zform zform, GenTable genTable) {
        super.deleteCascade(zform, genTable);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {
        if (StringUtil.isEmpty(genTable.getParentTable())) {
            this.checkDeletePermission(loginName, formNo);
        }
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            aroundService.beforeDeleteAll(ids, formNo, genTable, loginName);
        }
        super.deleteAll(ids, formNo, genTable, loginName);
        this.extAfterDelete(ids, formNo, genTable, loginName);
        if (aroundService != null) {
            aroundService.afterDeleteAll(ids, formNo, genTable, loginName);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public String saveAct(String businessKey, Zform zform, String loginName, GenTable genTable) throws Exception {
        this.extendBeforeSaveAct(zform, loginName, genTable);
        String actStatus = super.saveAct(businessKey, zform, loginName, genTable);
        this.extendAfterSaveAct(zform, loginName, genTable);
        return actStatus;
    }

    @Override
    public LinkedHashMap<String, Object> getStartingUserList(Zform zform, String loginName) {
        return super.getStartingUserList(zform, loginName);
    }

    @Override
    public LinkedHashMap<String, Object> getTargetUserList(Zform zform, String loginName) {
        LinkedHashMap<String, Object> targetUserInfo = super.getTargetUserList(zform, loginName);
        // region 通过在 工作流 权限配置 表单 填写act.nextNodeUser 得到自定义实现类 获取下一节点审批人
        Object _currentTaskNodeFormExtend = zform.getActRuleArgs().get(ActExtentions.formExtend);
        Map currentTaskNodeFormExtend = null;
        if (_currentTaskNodeFormExtend instanceof JSONObject) {
            currentTaskNodeFormExtend = ((JSONObject) _currentTaskNodeFormExtend).getInnerMap();
        } else if (_currentTaskNodeFormExtend instanceof LinkedHashMap) {
            currentTaskNodeFormExtend = (LinkedHashMap) _currentTaskNodeFormExtend;
        }
        if (currentTaskNodeFormExtend != null) {
            String beanName = ConvertUtil.getString(currentTaskNodeFormExtend.get(ActExtentions.nextNodeUser));
            if (StringUtils.isNotBlank(beanName)) {
                NextNodeUserService nextNodeUserService = SpringUtil.getBean(beanName, NextNodeUserService.class);
                targetUserInfo = nextNodeUserService.getTargetUserList(zform, loginName, targetUserInfo);
                return targetUserInfo;
            }
        }
        // endregion

        if (zform.getActRuleArgs() != null) {
            Object _formExtend = zform.getActRuleArgs().get(ActExtentions.formExtend);
            Map formExtend = null;
            if (_formExtend instanceof JSONObject) {
                formExtend = ((JSONObject) _formExtend).getInnerMap();
            } else if (_formExtend instanceof LinkedHashMap) {
                formExtend = (LinkedHashMap) _formExtend;
            } else {
                throw new RuntimeException("getTargetUserList:formExtend类型不支持");
            }

            if (formExtend.containsKey(ActExtentions.actNextUser)) {
                String actUserName = StringUtil.getString(formExtend.get(ActExtentions.actNextUser));
                // 验证列名是否为合法SQL标识符，防止SQL注入
                SqlInjectionUtil.validateIdentifier(actUserName);
                SqlInjectionUtil.validateIdentifier(zform.getFormNo());
                // 使用参数化查询，避免 SQL 注入
                String sql = "select " + actUserName + " from " + zform.getFormNo() + " where del_flag = '0' and id = #{param.zformId}";
                java.util.Map<String, Object> param = new java.util.HashMap<>();
                param.put("zformId", zform.getId());
                String actUserValue = zformDao.findStringBySqlParm(sql, param);
                if (StringUtil.isNotEmpty(actUserValue)) {
                    List<User> unfilterUserList = Lists.newArrayList();
                    String[] split = actUserValue.split(",");
                    if (split.length > 0) {
                        for (String s : split) {
                            User theUser = userDao.get(s);
                            if (theUser != null) {
                                unfilterUserList.add(theUser);
                            }
                        }
                        if (unfilterUserList.size() > 0) {
                            targetUserInfo.put(ActExtentions.userList, unfilterUserList);
                        }
                    }

                }
            }
        }
        if (targetUserInfo.containsKey("nextNode")) {
            Map<String, Object> nextNodeMap = (Map<String, Object>) targetUserInfo.get("nextNode");
            if (nextNodeMap.containsKey("id")) {
                Zform nextTask = new Zform(zform.getId());
                Act act = new Act();
                act.setProcDefId(zform.getAct().getProcDefId());
                act.setTaskDefKey(ConvertUtil.getString(nextNodeMap.get("id")));
                nextTask.setAct(act);
                setRuleArgs(nextTask, loginName);
                if (nextTask.getRuleArgs() != null) {
                    Object _formExtend = nextTask.getRuleArgs().get(ActExtentions.formExtend);
                    Map formExtend = null;
                    if (_formExtend instanceof JSONObject) {
                        formExtend = ((JSONObject) _formExtend).getInnerMap();
                    } else if (_formExtend instanceof LinkedHashMap) {
                        formExtend = (LinkedHashMap) _formExtend;
                    } else {
                        throw new RuntimeException("getTargetUserList:formExtend类型不支持");
                    }
                    // 根据工作流表单配置解析下一节点审批人
                    if (formExtend.containsKey(ActExtentions.actPrevTaskAssignee)) {
                        String actPrevTaskAssignee = StringUtil.getString(formExtend.get(ActExtentions.actPrevTaskAssignee));
                        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().
                                processInstanceId(zform.getAct().getProcInsId()).
                                taskDefinitionKey(actPrevTaskAssignee).
                                taskDeleteReason("completed").
                                orderByHistoricTaskInstanceEndTime().desc().list();
                        HistoricTaskInstance historicTaskInstance = null;
                        if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
                            historicTaskInstance = historicTaskInstances.get(0);
                        }
                        if (historicTaskInstance != null) {
                            User user = new User();
                            user.setLoginName(historicTaskInstance.getAssignee());
                            User byLoginName = userDao.getByLoginName(user);
                            List<User> unfilterUserList = Lists.newArrayList();
                            unfilterUserList.add(byLoginName);
                            targetUserInfo.put(ActExtentions.userList, unfilterUserList);
                        }
                    }
                }
            }
        }

        return targetUserInfo;
    }

    @Override
    @Deprecated
    public Page<Zform> findPage(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId) throws Exception {
        User currentUser = UserUtil.getByLoginName(loginName);
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            aroundService.beforeSetSqlMap(zform, genTable, currentUser);
        }
        this.setSqlMap(zform, genTable, currentUser);
        Page<Zform> thePage = super.findPage(page, zform, path, loginName, genTable, traceFlag, parentId);
        this.processBlockChainGet(thePage, genTable);
        return thePage;
    }

    @Override
    public Page<Zform> findPageMap(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) throws Exception {
        User currentUser = UserUtil.getByLoginName(loginName);
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            aroundService.beforeSetSqlMap(zform, genTable, currentUser);
        }
        this.setQueryWrapper(zform, genTable, currentUser);
        if (aroundService != null) {
            aroundService.beforeFindPageMap(page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
        Page<Zform> pageMap = super.findPageMap(page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        this.extAfterFindPageMap(pageMap, zform, loginName, genTable);
        if (aroundService != null) {
            pageMap = aroundService.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
        super.appendDictName(pageMap.getMap(), genTable);
        return pageMap;
    }


    public StatisticsVo statisticsData(Zform zform, String loginName, String path, GenTable genTable, String traceFlag, String parentId, String extFlag, boolean groupData) {
        List<GenTableColumn> statisticsColumnList = genTable.getColumnList().stream().filter(k -> k.getGenTableColumnSetting() != null && k.getGenTableColumnSetting().getStatisticsConfigList() != null && k.getGenTableColumnSetting().getStatisticsConfigList().size() > 0).collect(Collectors.toList());
        if (statisticsColumnList.size() == 0) {
            return new StatisticsVo();
        }
        StatisticsVo statisticsVo = new StatisticsVo();
        int rows = 10000;
        try {
            Page<Zform> pageResult = this.findPageMap(new Page<>(1, rows), zform, path, loginName, genTable, traceFlag, parentId, extFlag);
            List<Supplier<Page<Zform>>> suppliers = new ArrayList<>();
            suppliers.add(() -> pageResult);
            Long total = pageResult.getCount();
            if (total > rows) {
                int totalPage = (int) Math.ceil(total / (rows * 1.0));
                for (int i = 2; i <= totalPage; i++) {
                    int finalI = i;
                    suppliers.add(() -> {
                        try {
                            return this.findPageMap(new Page<>(finalI, rows), zform, path, loginName, genTable, traceFlag, parentId, extFlag);
                        } catch (Exception e) {
                            throw new BusinessException("统计数据失败：" + ExceptionUtil.getMessage(e));
                        }
                    });
                }
            }
            Map<String, BigDecimal> sumMap = Maps.newHashMap();
            Map<String, Integer> precisionMap = Maps.newHashMap();
            LinkedHashMap<String, LinkedHashMap<String, Object>> groupMap = Maps.newLinkedHashMap();
            Map<String, String> groupNameMap = Maps.newHashMap();
            Map<String, String> groupDictMap = Maps.newHashMap();
            for (Supplier<Page<Zform>> supplier : suppliers) {
                Page<Zform> zformPage = supplier.get();
                List<LinkedHashMap> mapList = zformPage.getMap();
                for (LinkedHashMap map : mapList) {
                    for (GenTableColumn genTableColumn : statisticsColumnList) {
                        String name = genTableColumn.getName();
                        GenTableColumnSetting genTableColumnSetting = genTableColumn.getGenTableColumnSetting();
                        for (GenTableColumnSetting.StatisticsConfig statisticsConfig : genTableColumnSetting.getStatisticsConfigList()) {
                            if (GenTableColumnSetting.StatisticsType.SUM.name().equals(statisticsConfig.getType())) {
                                precisionMap.put(statisticsConfig.getType(), statisticsConfig.getPrecision());
                                Object value = map.get(name);
                                if (value != null && StringUtil.isNotEmpty(value.toString()) && NumberUtil.isNumber(value.toString())) {
                                    BigDecimal sValue = sumMap.getOrDefault(name, new BigDecimal(0));
                                    sValue = sValue.add(new BigDecimal(value.toString()));
                                    sumMap.put(name, sValue);
                                }
                            } else if (GenTableColumnSetting.StatisticsType.GROUP.name().equals(statisticsConfig.getType()) && groupData) {
                                String key = name + "_" +  statisticsConfig.getType() + "_" + statisticsConfig.getGroupType();
                                groupNameMap.put(key, genTableColumn.getComments());
                                groupDictMap.put(key, genTableColumn.getDictType());
                                LinkedHashMap<String, Object> groupVal = groupMap.getOrDefault(key, Maps.newLinkedHashMap());
                                precisionMap.put(key, statisticsConfig.getPrecision());
                                if (GenTableColumnSetting.GroupType.COUNT.name().equals(statisticsConfig.getGroupType())) {
                                    String group = ConvertUtil.getString(map.get(name));
                                    int count =  ConvertUtil.getInteger(groupVal.getOrDefault(group, 0));
                                    count++;
                                    groupVal.put(group, count);
                                }
                                groupMap.put(key,groupVal);
                            }

                        }
                    }
                }
            }

            Map<String, Object> sumResMap = Maps.newHashMap();
            for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
                BigDecimal value = entry.getValue();
                Integer precision = precisionMap.get(entry.getKey());
                if (precision != null) {
                    value = value.setScale(precision, RoundingMode.HALF_UP);
                }
                sumResMap.put(entry.getKey(), value.stripTrailingZeros().toPlainString());
            }
            statisticsVo.setSumMap(sumResMap);

            LinkedHashMap<String, LinkedHashMap<String, Object>> groupResMap = new LinkedHashMap<>();
            for (Map.Entry<String, LinkedHashMap<String, Object>> entry : groupMap.entrySet()) {
                LinkedHashMap<String, Object> value = entry.getValue();
                LinkedHashMap<String, Object> res = Maps.newLinkedHashMap();
                String dictType = groupDictMap.get(entry.getKey());
                if (StringUtil.isNotEmpty(dictType)) {
                    for (DictResult dict : dictDataService.getDictList(dictType, false)) {
                        res.put(dict.getMemberName(), value.get(dict.getMember()));
                    }
                } else {
                    res = value;
                }
                // 后续扩展：求和、均值、最大值、最小值可继续支持精度设置
                groupResMap.put(groupNameMap.get(entry.getKey()), res);
            }
            statisticsVo.setGroupMap(groupResMap);
        } catch (Exception e) {
            throw new BusinessException("统计数据失败：" + ExceptionUtil.getMessage(e));
        }
        return statisticsVo;
    }

    /**
     * 对数据进行求和
     *
     * @param zform     查询条件
     * @param loginName 登录名
     * @param path
     * @param genTable
     * @param traceFlag
     * @param parentId
     * @param extFlag
     * @return
     */
    public Map<String, Object> sumData(Zform zform, String loginName, String path, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        StatisticsVo statisticsVo = this.statisticsData(zform, loginName, path, genTable, traceFlag, parentId, extFlag, false);
        return statisticsVo.getSumMap();
    }

    /**
     * 查询数据，用于导出Word，带字典名称和子表数据
     *
     * @param page
     * @param zform
     * @param path
     * @param loginName
     * @param genTable
     * @param traceFlag
     * @param parentId
     * @param extFlag
     * @return
     * @throws Exception
     */
    public List<LinkedHashMap> findPageMapData(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) throws Exception {
        Page<Zform> pageMap = findPageMap(page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        List<LinkedHashMap> hashMap = pageMap.getMap();
        //添加字典名称
        for (LinkedHashMap map : hashMap) {
            for (GenTableColumn column : genTable.getColumnList()) {
                if (StringUtil.isNotEmpty(column.getDictType())) {
                    map.put(column.getName() + "_name",
                            dictDataService.getDictLabels(StringUtil.getString(map.get(column.getName())),
                                    column.getDictType(),
                                    "",
                                    "zh-CN"));
                }
            }
            //添加子表数据
            for (GenTable childGenTable : genTable.getChildList()) {
                childGenTable = genTableService.getGenTableWithDefination(childGenTable.getName());
                Page<Zform> childPage = new Page<>(1, Integer.MAX_VALUE, childGenTable.getSqlSort());
                Zform childZform = new Zform();
                childZform.setFormNo(childGenTable.getName());
                List<LinkedHashMap> childMap = findPageMapData(childPage, childZform, path, loginName, childGenTable, traceFlag, map.get("id").toString(), extFlag);
                map.put(childGenTable.getName(), childMap);
            }
        }
        return hashMap;
    }

    @Override
    public Page<Zform> findPageMap(Page<Zform> page, Zform zform, GenTable genTable) {
        return super.findPageMap(page, zform, genTable);
    }

    @Override
    public List<Zform> findDataList(Zform zform, String path, String processDefinitionCategory, String loginName, GenTable genTable) {
        return super.findDataList(zform, path, processDefinitionCategory, loginName, genTable);
    }

    @Override
    public String findCount(Page<Zform> page, Zform zform, GenTable genTable) {
        return super.findCount(page, zform, genTable);
    }

    @Override
    public String findCount(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable) {
        return super.findCount(page, zform, path, loginName, genTable);
    }

    @Override
    public void setAct(Zform zform, String loginName) {
        super.setAct(zform, loginName);
    }

    @Override
    @Deprecated
    public void setSqlMap(Zform zform, GenTable genTable, User currentUser) {
        super.setSqlMap(zform, genTable, currentUser);
    }

    @Override
    public void setQueryWrapper(Zform zform, GenTable genTable, User currentUser) {
        super.setQueryWrapper(zform, genTable, currentUser);
    }

    @Override
    @Transactional(readOnly = false)
    public void buildParentIdsForChildren(String rootParentIds, Zform zform, GenTable gentable) {
        super.buildParentIdsForChildren(rootParentIds, zform, gentable);
    }

    @Override
    @Transactional(readOnly = false)
    public void superSave(Zform zform, GenTable genTable) {
        super.superSave(zform, genTable);
    }

    @Override
    public ResultJson gridselectData(GridselectParam gridselectParam) {
        return super.gridselectData(gridselectParam);
    }

    public void filterGridSelectParam(List<GridselectParam.FilterData> filterList) {
        if (filterList == null) {
            return;
        }
        for (GridselectParam.FilterData filterData : filterList) {
            if (filterData.getChildren() != null && filterData.getChildren().size() > 0) {
                filterGridSelectParam(filterData.getChildren());
            } else if (QueryTypeEnum.apply.name().equals(filterData.getType())) {
                throw new BusinessException("不允许使用type为apply的过滤条件");
            }
        }
    }

    @Override
    public ResultJson gridselectDataMap(GridselectParam gridselectParam) {
        this.filterGridSelectParam(gridselectParam.getFilterList());
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTableService.getGenTableWithDefination(gridselectParam.getTableName()));
        if (aroundService != null) {
            aroundService.beforeGridselectDataMap(gridselectParam);
        }
        // 处理启用拼音查询的过滤条件。
        List<GridselectParam.FilterData> filterList = gridselectParam.getFilterList();
        List<GridselectParam.FilterData> filterListNew = new ArrayList<>();
        if (filterList != null) {
            for (GridselectParam.FilterData filterData : filterList) {
                if (filterData.isPinyin()) {
                    // 如果设置了拼音查询。
                    String value = ConvertUtil.getString(filterData.getValue());
                    if (StringUtil.isNotEmpty(value)) {
                        List<String> idList = new ArrayList<>();
                        //查询出所有权限内的数据
                        List<LinkedHashMap> dataList = this.findMapListByDataPermission(gridselectParam.getTableName(), new QueryWrapper<>());
                        for (LinkedHashMap map : dataList) {
                            //遍历需要查找的列转为拼音
                            String findValue = ConvertUtil.getString(map.get(filterData.getKey()));
                            if (StringUtil.isEmpty(findValue)) {
                                String key = filterData.getKey();
                                if (key.contains(".")) {
                                    String[] split = key.split("\\.");
                                    findValue = ConvertUtil.getString(map.get(split[split.length - 1]));
                                }
                            }
                            if (StringUtil.isEmpty(findValue)) {
                                continue;
                            }
                            String pinyin = PinyinUtil.getPinyin(StrUtil.trim(findValue), "");
                            //如果前端传入值包含拼音
                            if (ConvertUtil.getString(pinyin).contains(value)) {
                                idList.add(ConvertUtil.getString(map.get("id")));
                            }
                        }
                        if (idList.size() > 0) {
                            //构造根据符合拼音的in查询条件
                            //and (field like '%Val%' or a.id in ('',''))
                            GridselectParam.FilterData child = new GridselectParam.FilterData();
                            GridselectParam.FilterData child1 = new GridselectParam.FilterData(filterData.getKey(), filterData.getValue(), filterData.getType());
                            GridselectParam.FilterData child2 = new GridselectParam.FilterData("a.id", idList, QueryTypeEnum.in.name());
                            child2.setOr(true);
                            List<GridselectParam.FilterData> children = new ArrayList<>();
                            children.add(child1);
                            children.add(child2);
                            child.setChildren(children);
                            filterListNew.add(child);
                        } else {
                            filterListNew.add(filterData);
                        }
                    }

                } else {
                    filterListNew.add(filterData);
                }
            }
            gridselectParam.setFilterList(filterListNew);
        }
        //处理使用拼音查询的逻辑结束
        return super.gridselectDataMap(gridselectParam);
    }

    @Override
    public ResultJson gridselectData(GridselectParam gridselectParam, String dsfP, String sqlJoin, String sqlCol) {
        return super.gridselectData(gridselectParam, dsfP, sqlJoin, sqlCol);
    }

    @Override
    public Map<String, Object> getTaskList(List<String> categoryList, String path, String loginName, int pageNo, int pageSize, Map<String, String> paramMap) {
        return super.getTaskList(categoryList, path, loginName, pageNo, pageSize, paramMap);
    }

    @Override
    @Transactional(readOnly = false)
    public void importData(String ownerCode, String columns, String formNo, String parentFormNo, String parentId, String uniqueId, List<LinkedHashMap<String, String>> dataList, User currentUser) throws Exception {
        if ("con_hash".equals(formNo)) {
            this.importConData(ownerCode, columns, formNo, parentFormNo, parentId, uniqueId, dataList, currentUser);
        } else {
            super.importData(ownerCode, columns, formNo, parentFormNo, parentId, uniqueId, dataList, currentUser);
        }
    }

    @Override
    public List<List<LinkedHashMap<String, String>>> exportData(Page<Zform> page, Zform zform, GenTable genTable, String loginName) throws Exception {
        if ("con_hash".equals(genTable.getName())) {
            return this.exportConData(page, zform, genTable, loginName);
        } else {
            return super.exportData(page, zform, genTable, loginName);
        }
    }

    private LinkedHashMap<String, String> getMapValue(Zform obj, GenTable genTable) throws Exception {
        String idValue = this.getIdValue(obj, genTable);
        String hashKey = this.getHashKey(idValue);
        String getFunctionName = "get";
        String conAddress = this.getConAddress(genTable.getBlockChainParam1());
        String hashValue = BlockChainUtil.getValue(conAddress, getFunctionName, hashKey);
        LinkedHashMap<String, String> mapValue = Maps.newLinkedHashMap();
        if (StringUtil.isNotEmpty(hashValue)) {
            mapValue = JsonConvertUtil.gsonBuilder().fromJson(hashValue, new TypeToken<LinkedHashMap<String, String>>() {
            }.getType());
        }
        return mapValue;
    }

    protected LinkedHashMap<String, String> getMapValue(Zform obj, GenTable genTable, String idValue) throws Exception {
        String hashKey = this.getHashKey(idValue);
        String getFunctionName = "get";
        String conAddress = this.getConAddress(genTable.getBlockChainParam1());
        String hashValue = BlockChainUtil.getValue(conAddress, getFunctionName, hashKey);
        LinkedHashMap<String, String> mapValue = Maps.newLinkedHashMap();
        if (StringUtil.isNotEmpty(hashValue)) {
            mapValue = JsonConvertUtil.gsonBuilder().fromJson(hashValue, new TypeToken<LinkedHashMap<String, String>>() {
            }.getType());
        }
        return mapValue;
    }

    protected String getIdValue(Zform obj, GenTable genTable) throws IllegalAccessException {
        Field fieldPk = Reflections.getThisField(obj.getClass(), "id");
        String idValue = null;
        if (fieldPk != null) {
            fieldPk.setAccessible(true);
            idValue = (String) fieldPk.get(obj);
        }
        return idValue;
    }

    protected void processBlockChainGet(Page<Zform> thePage, GenTable genTable) throws Exception {
        //this.initCfx();
        if (StringUtil.isNotEmpty(genTable.getBlockChainParam1())) {
            String blockChainColumns = this.getGenTableBlockChainColumns(genTable);
            if (StringUtil.isNotEmpty(blockChainColumns)) {
                for (Zform obj : thePage.getList()) {
                    String idValue = this.getIdValue(obj, genTable);
                    String hashKey = this.getHashKey(idValue);
                    obj.setHashKey(hashKey);
                    LinkedHashMap<String, String> mapValue = this.getMapValue(obj, genTable, idValue);
                    for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                        String javaField = genTableColumn.getJavaField();
                        if (blockChainColumns.indexOf("," + javaField + ",") == -1) continue;
                        Field field = Reflections.getThisField(obj.getClass(), javaField);
                        if (field != null) {
                            field.setAccessible(true);
                            String value = mapValue.get(javaField);
                            if (Global.YES.equals(genTableColumn.getBlockChainParam2())) {
                                value = Aes.aesDecrypt(value);
                            }
                            if (StringUtil.isNotEmpty(value)) {
                                field.set(obj, value);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void processBlockChainGet(Zform obj, GenTable genTable) throws Exception {
        if (obj.getId() != null) {
            this.initCfx();
            if (StringUtil.isNotEmpty(genTable.getBlockChainParam1())) {
                String idValue = this.getIdValue(obj, genTable);
                String hashKey = this.getHashKey(idValue);
                obj.setHashKey(hashKey);
                String blockChainColumns = this.getGenTableBlockChainColumns(genTable);
                if (StringUtil.isNotEmpty(blockChainColumns)) {
                    LinkedHashMap<String, String> mapValue = this.getMapValue(obj, genTable, idValue);
                    for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                        String javaField = genTableColumn.getJavaField();
                        if (blockChainColumns.indexOf("," + javaField + ",") == -1) continue;
                        Field field = Reflections.getThisField(obj.getClass(), javaField);
                        if (field != null) {
                            field.setAccessible(true);
                            String value = mapValue.get(javaField);
                            if (Global.YES.equals(genTableColumn.getBlockChainParam2())) {
                                value = Aes.aesDecrypt(value);
                            }
                            field.set(obj, value);
                        }
                    }
                }
            }
        }
    }

    protected void processBlockChainSet(Zform obj, GenTable genTable) throws Exception {
        this.initCfx();
        if (StringUtil.isNotEmpty(genTable.getBlockChainParam1())) {
            String blockChainColumns = this.getGenTableBlockChainColumns(genTable);
            String setFunctionName = "insert";
            String idValue = obj.getId();
            if (obj.getIsNewRecord()) {
                obj.setPreId(IdGen.uuid());
                idValue = obj.getPreId();
            }
            if (StringUtil.isNotEmpty(blockChainColumns)) {
                String conAddress = this.getConAddress(genTable.getBlockChainParam1());
                String privateKey = this.getPrivateKey();
                String rowKey = idValue;
                LinkedHashMap<String, String> rowValueMap = Maps.newLinkedHashMap();
                for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                    String javaField = genTableColumn.getJavaField();
                    if (blockChainColumns.indexOf("," + javaField + ",") == -1) continue;
                    Field field = Reflections.getThisField(obj.getClass(), javaField);
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(obj);
                        String value = "";
                        if (object != null) {
                            value = (String) object;
                            rowKey += "," + javaField;
                            if (Global.YES.equals(genTableColumn.getBlockChainParam2())) {
                                value = Aes.aesEncrypt(value);
                            }
                        }
                        rowValueMap.put(javaField, value);
                    }
                }

                if (rowKey.indexOf(",") != -1) {
                    String timeStamp = Long.toString(System.currentTimeMillis());
                    rowKey += "," + timeStamp;
                    String rowValue = new Gson().toJson(rowValueMap);
                    String existsRowValue = BlockChainUtil.getValue(conAddress, "get", rowKey);
                    if (StringUtil.isEmpty(existsRowValue) || false == existsRowValue.equals(rowValue)) {
                        String hash = BlockChainUtil.setValue(conAddress, setFunctionName, privateKey, rowKey, rowValue);
                        this.setHashValue(genTable.getBlockChainParam1(), rowKey, hash, true, Global.NO);
                    }
                }
            }
        }
    }

    protected void processBlockChainDel(Zform obj, GenTable genTable) throws Exception {
        this.initCfx();
        if (StringUtil.isNotEmpty(genTable.getBlockChainParam1())) {
            String blockChainColumns = this.getGenTableBlockChainColumns(genTable);
            String setFunctionName = "insert";
            String idValue = obj.getId();
            if (StringUtil.isNotEmpty(blockChainColumns)) {
                String conAddress = this.getConAddress(genTable.getBlockChainParam1());
                String privateKey = this.getPrivateKey();
                String rowKey = idValue;
                for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                    String javaField = genTableColumn.getJavaField();
                    if (blockChainColumns.indexOf("," + javaField + ",") == -1) continue;
                    Field field = Reflections.getThisField(obj.getClass(), javaField);
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(obj);
                        if (object != null) {
                            rowKey += "," + javaField;
                        }
                    }
                }
                if (rowKey.indexOf(",") != -1) {
                    String timeStamp = Long.toString(System.currentTimeMillis());
                    rowKey += "," + timeStamp;
                    String hash = BlockChainUtil.setValue(conAddress, setFunctionName, privateKey, rowKey, "deleted");
                    this.setHashValue(genTable.getBlockChainParam1(), rowKey, hash, true, Global.NO);
                }
            }
        }
    }

    protected void setHashValue(String conAddressId, String key, String hash, Boolean isNewRecord, String delFlag) throws Exception {
        String formNo = "con_hash";
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = new Zform();
        zform.setFormNo(formNo);
        if (isNewRecord) {
            zform.setPreId(key);
        } else {
            zform.setId(key);
        }
        zform.setS01(key);
        zform.setS02(hash);
        Zform g01 = new Zform(conAddressId, "con_address");
        zform.setG01(g01);
        zform.setDelFlag(delFlag);
        super.save(zform, genTable);
    }

    protected String getHashKey(String idValue) {
        String hashKey = null;
        if (StringUtil.isNotEmpty(idValue)) {
            String formNo = "con_hash";
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            Zform zform = new Zform();
            zform.setFormNo(formNo);
            Page<Zform> page = new Page<Zform>();
            page.setPageSize(1);
            page.setOrderBy("a.key_ desc");
            zform.setPage(page);
            SqlInjectionUtil.filterContent(idValue);
            zform.getSqlMap().put("dsf", " AND a.key_ LIKE '" + idValue + "%'");
            List<Zform> list = this.findList(zform, genTable);
            if (list.size() > 0) {
                hashKey = list.get(0).getS01();
            }
        }
        return hashKey;
    }

    protected String getGenTableBlockChainColumns(GenTable genTable) {
        String blockChainColumns = ",";
        for (GenTableColumn column : genTable.getColumnList()) {
            if (Global.YES.equals(column.getBlockChainParam1())) {
                blockChainColumns += column.getJavaField() + ",";
            }
        }
        if (blockChainColumns.length() == 1) {
            blockChainColumns = "";
        }
        return blockChainColumns;
    }

    public void initCfx() throws Exception {
        /*if (BlockChainUtil.getCfx() == null) {
            Zform zform = new Zform();
            zform.setFormNo("con_network");
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            zform = super.get("1", genTable);
            BlockChainUtil.initCfx(zform.getS02(), Long.parseLong(zform.getS04()));
        }*/
    }

    protected String getPrivateKey() {
        return super.getFirstValueByKey("con_manager", "private_key", "id", "1");
    }

    protected String getConAddress(GenTable genTable) {
        return super.getFirstValueByKey("con_address", "contract_address", "id", genTable.getBlockChainParam1());
    }

    protected String getConAddress(String addressId) {
        return super.getFirstValueByKey("con_address", "contract_address", "id", addressId);
    }

    //Extend business logic
    public String getHashByKey(String key) {
        if (key.startsWith("log")) {
            return this.getHashKey(key);
        } else {
            return super.getFirstValueByKey("con_hash", "hash_value", "key_", key);
        }
    }

    @Transactional(readOnly = false)
    public void extendBeforeSave(Zform zform, GenTable genTable) throws Exception {
        this.initCfx();
        if ("con_manager".equalsIgnoreCase(genTable.getName())) {
            if (StringUtil.isNotEmpty(zform.getS03())) {
                //import private key
                zform.setS02(BlockChainUtil.getAddress(zform.getS03()));
            }
        } else if ("con_template".equalsIgnoreCase(genTable.getName())) {
            if (StringUtil.isNotEmpty(zform.getS04())) {
                String hashValue = BlockChainUtil.deploy(this.getPrivateKey(), zform.getS04());
                zform.setS04("");
                zform.setS05(hashValue);
            }
        }
    }

    @Transactional(readOnly = false)
    public void extendAfterSave(Zform zform, GenTable genTable) {
        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            if ("sys_menu".equalsIgnoreCase(genTable.getName())) {
                zformDao.updateSysMenuIsShowCascade(zform.getParentIds() + zform.getId() + ",", zform.getS07());
            } else if ("sys_dictionary".equalsIgnoreCase(genTable.getName()) && zform.getParentIds().indexOf("data-params") != -1) {
                dictDataService.refreshDictCacheByType("data-params");
            }
        }
    }

    public void extendAfterSaveAct(Zform zform, String loginName, GenTable genTable) throws Exception {
        if (Global.YES.equals(genTable.getBlockChainParam2())) {
            List<Act> list = histoicFlowList(zform.getProcInsId());
            String timeStamp = Long.toString(System.currentTimeMillis());
            String rowKey = "log," + zform.getId() + "," + timeStamp;
            String rowValue = new Gson().toJson(list);
            String setFunctionName = "insert";
            String conAddress = this.getConAddress(genTable.getBlockChainParam1());
            String privateKey = this.getPrivateKey();
            String hash = BlockChainUtil.setValue(conAddress, setFunctionName, privateKey, rowKey, rowValue);
            this.setHashValue(genTable.getBlockChainParam1(), rowKey, hash, true, Global.NO);
        }
    }

    @Transactional(readOnly = false)
    public void importConData(String ownerCode, String columns, String formNo, String parentFormNo, String parentId, String uniqueId, List<LinkedHashMap<String, String>> dataList, User currentUser) throws Exception {
        int i = 0;
        String newConAddress = null;
        for (LinkedHashMap row : dataList) {
            if (i++ == 0) continue;
            if (newConAddress == null) {
                newConAddress = this.getConAddress((String) row.get("g01.id|name"));
            }
            String key = (String) row.get("s01");
            String value = (String) row.get("remarks");
            BlockChainUtil.setValue(newConAddress, "insert", this.getPrivateKey(), key, value);
        }
    }

    public List<List<LinkedHashMap<String, String>>> exportConData(Page<Zform> page, Zform zform, GenTable genTable, String loginName) throws Exception {
        List<List<LinkedHashMap<String, String>>> data = super.exportData(page, zform, genTable, loginName);
        String addressId = null;
        String conAddress = null;
        for (ListIterator<List<LinkedHashMap<String, String>>> it = data.listIterator(); it.hasNext(); ) {
            List<LinkedHashMap<String, String>> row = it.next();
            String newAddressId = row.get(0).get("g01.id|name");
            if (addressId == null || false == newAddressId.equals(addressId)) {
                conAddress = this.getConAddress(newAddressId);
            }
            String key = row.get(1).get("s01");
            String value = BlockChainUtil.getValue(conAddress, "get", key);
            row.get(2).put("remarks", value);
        }
        return data;
    }

    protected void extendAfterGet(Zform zform, String loginName) {
        this.buildCountersign(zform, loginName);
    }

    protected void buildCountersign(Zform zform, String loginName) {
        //构造会签的值
        if (zform != null) {
            if (StringUtil.isNotBlank(zform.getCountersign01())) {
                super.getFieldValue(zform, "tempcountersign01", "countersign01", loginName);
            }
            if (StringUtil.isNotBlank(zform.getCountersign02())) {
                super.getFieldValue(zform, "tempcountersign02", "countersign02", loginName);
            }
            if (StringUtil.isNotBlank(zform.getCountersign03())) {
                super.getFieldValue(zform, "tempcountersign03", "countersign03", loginName);
            }
            if (StringUtil.isNotBlank(zform.getCountersign04())) {
                super.getFieldValue(zform, "tempcountersign04", "countersign04", loginName);
            }
            if (StringUtil.isNotBlank(zform.getCountersign05())) {
                super.getFieldValue(zform, "tempcountersign05", "countersign05", loginName);
            }
        }
    }

    public ResultJson exportPdf(Zform zform, String tempPath, String tomcatPath, String uploadPath) {
        ResultJson resultJson = new ResultJson();
        Map<String, Object> data = null;
        String zformString = JsonConvertUtil.gsonBuilder().toJson(zform);
        if (StringUtil.isNotEmpty(zformString)) {
            data = JsonConvertUtil.gsonBuilder().fromJson(zformString, new TypeToken<Map<String, Object>>() {
            }.getType());
        } else {
            resultJson.setMsg("不存在可以导出的数据！");
            resultJson.setCode(ResultJson.CODE_FAILED);
            return resultJson;
        }
        // 1.指定解析器
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        OutputStream bs = null;
        PdfStamper ps = null;
        PdfReader reader = null;
        try {
            if (tomcatPath.lastIndexOf("/") == tomcatPath.length() - 1 && uploadPath.indexOf("/") != 0) {
                tomcatPath = tomcatPath.substring(0, tomcatPath.lastIndexOf("/"));
            }
            String dataSplitChar = "-";
            Calendar calendar = Calendar.getInstance();
            String dateStr = calendar.get(Calendar.YEAR) + dataSplitChar + (calendar.get(Calendar.MONTH) + 1) + dataSplitChar + calendar.get(Calendar.DATE);
            String path = tomcatPath + uploadPath + "/" + dateStr;
            String pdfName = UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
            FileUtil.createDirectory(path);
            String pdfRealPath = path + "/" + pdfName;
            File pdf = new File(pdfRealPath);
            if (!pdf.exists()) {
                pdf.createNewFile();
            }
            File file = new File(tempPath);
            if (!file.exists()) {
                //模板不存在的情况下
                resultJson.setMsg("该路径下不存在对应模板文件！");
                resultJson.setCode(ResultJson.CODE_FAILED);
                return resultJson;
            }
            bs = new FileOutputStream(pdf);
            // 2 读入pdf表单
            reader = new PdfReader(file.getPath());
            // 3 根据表单生成一个新的pdf
            ps = new PdfStamper(reader, bs);
            // 4 获取pdf表单
            AcroFields form = ps.getAcroFields();
            // 5给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
            //系统识别
            //系统识别
            BaseFont bf;
            if (EPlatform.Windows.equals(OSinfo.getOSname())) {
                bf = BaseFont.createFont("C:/WINDOWS/Fonts/SIMSUN.TTC,1",
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } else {
                bf = BaseFont.createFont();
            }
            form.addSubstitutionFont(bf);
            // 6查询数据================================================
            // 7遍历data 给pdf表单表格赋值
            for (String key : data.keySet()) {
                form.setField(key, data.get(key) == null ? "" : data.get(key).toString());
            }
            bs.flush();
            String returnPath = uploadPath + "/" + dateStr + "/" + pdfName;
            ps.setFormFlattening(true);
            resultJson.setMsg("导出文件成功！");
            resultJson.getData().put("pdfPath", returnPath);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            return resultJson;
        } catch (Exception e) {
            logger.error(e.getMessage());
            resultJson.setMsg("导出文件失败！");
            resultJson.setCode(ResultJson.CODE_FAILED);
            return resultJson;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (bs != null) {
                    bs.close();
                }
            } catch (Exception e) {
                logger.warn("exportPdf:" + e.getMessage());
            }
        }
    }

    /**
     * 保存动态表单数据，统一处理权限校验、流程启动/办理、多对多关系、版本表和扩展服务回调。
     */
    @Transactional(readOnly = false)
    public ResultJson saveZform(Zform zform,
                                String loginName,
                                String businessKey) throws Exception {
        return saveZform(zform,loginName,businessKey,null);
    }

    /**
     * 保存动态表单数据，并允许指定需要写入空值的字段集合。
     */
    @Transactional(readOnly = false)
    public ResultJson saveZform(Zform zform,
                                String loginName,
                                String businessKey,
                                Map<String, Object> updateNullParamMap) throws Exception {
        ResultJson resultJson = new ResultJson();
        try {
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            if (StringUtil.isEmpty(genTable.getParentTable())) {
                this.checkSavePermission(loginName, zform.getFormNo(), zform.getIsNewRecord());
            }
            AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
            if (aroundService != null) {
                aroundService.beforeSaveZform(zform, loginName, businessKey);
            }
            //Start process, deploy model
            User currentUser = UserUtil.getByLoginName(loginName);
            zform.setTempRuleArgsClass(TaskPermission.class.getSimpleName());
            Zform afterZform = zform;
            if (false == zform.getIsNewRecord()) {
                //Update
                Zform t = this.get(zform.getId(), genTable);
                afterZform = t;
                if (updateNullParamMap != null) {
                    List<GenTableColumn> columnList = genTable.getColumnList();
                    Map<String, Object> updateNullZformNameMap = new HashMap<>();
                    columnList.forEach(column -> {
                        if (updateNullParamMap.containsKey(column.getName())) {
                            updateNullZformNameMap.put(column.getJavaField(), updateNullParamMap.get(column.getName()));
                        }
                    });
                    BeanUtil.copyBeanHasNull2Bean(zform, t, updateNullZformNameMap);
                } else {
                    BeanUtil.copyBeanNotNull2Bean(zform, t);
                }
                 /*t.setUpdateBy(currentUser);
                t.setUpdateDate(new Date());*/
                String actStatus = null;
                if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
                    this.save(t, genTable);
                } else {
                    actStatus = this.saveAct(businessKey, t, loginName, genTable);
                    if (StringUtil.isNotEmpty(actStatus)) {
                        resultJson.put("actStatus", actStatus);
                    }
                }
                resultJson.put("data", t.getParentIds());
                resultJson.put("entityId", t.getId());
            } else {
                //Insert
                if (StringUtil.isEmpty(zform.getPreId())) {
                    zform.setPreId(IdGen.uuid());
                }
                /*zform.setCreateBy(currentUser);
                zform.setUpdateBy(currentUser);*/
                if (currentUser != null && currentUser.getOffice() != null && StringUtil.isNotBlank(currentUser.getOffice().getCode())) {
                    zform.setOwnerCode(currentUser.getOffice().getCode());
                } else if (StringUtil.isEmpty(zform.getOwnerCode())) {
                    zform.setOwnerCode("-1");//匿名接口，没有用户
                }
                String actStatus = null;
                if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
                    this.save(zform, genTable);
                } else {
                    actStatus = this.saveAct(businessKey, zform, loginName, genTable);
                    if (StringUtil.isNotEmpty(actStatus)) {
                        resultJson.put("actStatus", actStatus);
                    }
                }
                resultJson.put("data", zform.getParentIds());
                resultJson.put("entityId", zform.getId());
            }
            if (Global.YES.equals(genTable.getIsVersion())) {
                if (StringUtil.isBlank(versionSchema)) {
                    super.saveV(zform);
                } else {
                    zform.setVersionSchema(versionSchema);
                    super.saveVersionSchema(zform);
                    zform.setVersionSchema(null);
                }
            }
            //处理多对多关系表数据，先删除再插入
            for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
                List<DictView> dictViewList = Lists.newArrayList();
                if (manyToMany.getName().equalsIgnoreCase("dictViewList01")) {
                    if (zform.isSkipDictViewList01()){
                        continue;
                    }
                    dictViewList = zform.getDictViewList01();
                } else if (manyToMany.getName().equalsIgnoreCase("dictViewList02")) {
                    if (zform.isSkipDictViewList02()){
                        continue;
                    }
                    dictViewList = zform.getDictViewList02();
                } else if (manyToMany.getName().equalsIgnoreCase("dictViewList03")) {
                    if (zform.isSkipDictViewList03()){
                        continue;
                    }
                    dictViewList = zform.getDictViewList03();
                } else if (manyToMany.getName().equalsIgnoreCase("dictViewList04")) {
                    if (zform.isSkipDictViewList04()){
                        continue;
                    }
                    dictViewList = zform.getDictViewList04();
                } else if (manyToMany.getName().equalsIgnoreCase("dictViewList05")) {
                    if (zform.isSkipDictViewList05()){
                        continue;
                    }
                    dictViewList = zform.getDictViewList05();
                }
                StringBuilder sql = new StringBuilder();
                sql.append("delete from ");
                sql.append(manyToMany.getRelTable());
                sql.append(" where ");
                sql.append(manyToMany.getRelColumn());
                sql.append(" = '");
                sql.append(zform.getId());
                sql.append("' ");
                zformDao.deleteSql(sql.toString());
                if (dictViewList != null && dictViewList.size() > 0) {
                    for (DictView dictView : dictViewList) {
                        String theFormNo = manyToMany.getRelTable();
                        GenTable theGenTable = genTableService.getGenTableWithDefination(theFormNo);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(manyToMany.getRelColumn(), zform.getId());
                        jsonObject.put(manyToMany.getRelManyColumn(), dictView.getDictId());
                        jsonObject.put("formNo", theFormNo);
                        Zform theZform = this.getZformFromMap(jsonObject, loginName);
                        theZform.setCreateDate(zform.getCreateDate());
                        this.save(theZform, theGenTable);
                        if (Global.YES.equals(genTable.getIsVersion())) {
                            if (StringUtil.isBlank(versionSchema)) {
                                super.saveV(theZform);
                            } else {
                                theZform.setVersionSchema(versionSchema);
                                super.saveVersionSchema(theZform);
                                theZform.setVersionSchema(null);
                            }
                        }
                    }
                }
            }

            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("保存成功");
            resultJson.setMsg_en("Save success");
            if (aroundService != null) {
                //使用正确的数据 而不是工作流相关操作之前的
                resultJson = aroundService.afterSaveZform(resultJson, afterZform, loginName, businessKey);
            }
        } catch (Exception e) {
            logger.info("Zform name:" + zform.getName() + " 。Zform formNo:" + zform.getFormNo());
            logger.error("Save zform error:" + ExceptionUtils.getStackTrace(e));
            throw e;
        }
        return resultJson;
    }

    @Transactional(readOnly = false)
    protected void extAfterDelete(String ids, String formNo, GenTable genTable, String loginName) {

    }

    /**
     * 查询动态表单列表数据，封装为前端表格需要的 rows/total 响应结构。
     */
    public ResultJson data(Zform zform,
                           String path,
                           String formNo,
                           String traceFlag,
                           String loginName) throws Exception {
        ResultJson resultJson = new ResultJson();
        zform.setFormNo(formNo);
        Page<Zform> page = this.zformData(zform, path, loginName, traceFlag);
        resultJson.setRows(page.getList());
        resultJson.setTotal(page.getCount());
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("操作成功");
        resultJson.setMsg_en("Operation was successful");
        return resultJson;
    }

    /**
     * 根据表单编号和流程路径查询动态表单分页数据。
     */
    public Page<Zform> zformData(Zform zform, String path, String loginName, String traceFlag) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = this.findPage(page,
                zform,
                path,
                loginName,
                genTable,
                traceFlag,
                "");
        return page;
    }

    /**
     * 读取带流程上下文的动态表单数据，新建时会初始化流程定义和当前节点信息。
     */
    public Zform getZformWithAct(String formNo,
                                 String id,
                                 String procDefKey,
                                 String loginName) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = null;
        if (StringUtil.isEmpty(id)) {
            zform = new Zform();
            zform.setFormNo(formNo);
            zform.setProcDefKey(procDefKey);
        } else {
            zform = this.get(id, loginName, genTable);
        }
        //Act form
        if (false == StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
            if (StringUtil.isBlank(zform.getProcInsId())) {
                // 已知限制：流程定义数据可能为空，后续需补充空值保护
                //procDefKey = zform.getProcDefKey().replaceAll("'", "");
                ProcessDefinition processDefinition = this.getProcessDefinition(procDefKey);
                zform.getAct().setProcDefId(processDefinition.getId());
                zform.getAct().setTaskDefKey(processDefinition.getDescription().split(",")[1]);
            } else {
                this.setAct(zform, loginName);
            }
            this.setRuleArgs(zform, loginName);
        }
        return zform;
    }

    /**
     * 自定义批量操作
     *
     * @param ids
     * @param formNo
     * @param genTable
     * @throws Exception
     */
    /**
     * 执行动动态表单批量操作，并委托表单扩展服务处理具体业务逻辑。
     */
    @Transactional(readOnly = false)
    public String batchOperation(Zform zform, String ids, String formNo, GenTable genTable, String data, String extData, String loginName) throws Exception {
        String msg = null;
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            msg = aroundService.onBatchOperation(zform, ids, formNo, genTable, data, extData, loginName, null);
        }
        return msg;
    }

    /**
     * 批量保存选择的数据
     *
     * @param data      选择的数据
     * @param formNo    表单编号
     * @param loginName 登录名
     * @throws Exception
     */
    @Transactional(readOnly = false)
    public void batchSaveSelect(List<JSONObject> data, String formNo, String loginName) throws Exception {
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(formNo);
        if (aroundService != null) {
            aroundService.beforeBatchSaveSelect(data, loginName, formNo);
        }
        List<Zform> zforms = new ArrayList<>();
        // 后续优化：改为批量 insert 以提升大数据量保存性能
        for (JSONObject zformMap : data) {
            zformMap.put("formNo", formNo);
            Zform zform = this.getZformFromMap(zformMap, loginName);
            this.saveZform(zform, loginName, "/dynamic/zform", zformMap.getObject("updateNullParamMap", new TypeReference<HashMap<String, Object>>() {
            }));
            zforms.add(zform);
        }
        if (aroundService != null) {
            aroundService.afterBatchSaveSelect(zforms, loginName, formNo);
        }
    }

    /**
     * 批量保存Zform
     *
     * @param data      选择的数据
     * @param formNo    表单编号
     * @param loginName 登录名
     * @throws Exception
     */
    @Transactional(readOnly = false)
    public void batchSaveZform(List<Zform> data, String formNo, String loginName) throws Exception {
        // 后续优化：改为批量 insert 以提升大数据量保存性能
        for (Zform zform : data) {
            zform.setFormNo(formNo);
            this.saveZform(zform, loginName, "/dynamic/zform", null);
        }
    }

    /**
     * 实体类转Zform，需TableField注解
     *
     * @param formNo
     * @param entity
     * @param loginName
     * @param isChildren 当isChildren = true时，即本身就是下一级子表数据时，不再继续向下处理；false时处理树表，向下一级
     * @param <T>
     * @return
     */
    public <T> Zform getZformByEntity(String formNo, T entity, String loginName, Boolean isChildren) throws Exception {
        Zform zform = null;
        JSONObject zformMap = ReflectUtils.convertToJSONObjectByTableField(entity);
        if (zformMap != null) {
            zformMap.put("formNo", formNo);
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);
            for (GenTableColumn column : genTable.getColumnList()) {
                if (zformMap.containsKey(column.getName())) {
                    //判断属性是否为对象：
                    if ("parentId".equals(column.getShowType()) || "parent.id".equals(column.getJavaField())) {
                        JSONObject p = new JSONObject();
                        p.put("id", zformMap.getString(column.getName()));
                        zformMap.put("parent", p);
                        if (false == "parent".equals(column.getName())) {
                            zformMap.remove(column.getName());
                        }
                    } else if ("areaselect".equals(column.getShowType())
                            || "treeselectRedio".equals(column.getShowType())
                            || "gridselect".equals(column.getShowType())
                            || "officeselectTree".equals(column.getShowType())
                            || "treeselectCheck".equals(column.getShowType())) {
                        JSONObject p = new JSONObject();
                        p.put("id", zformMap.getString(column.getName()));
                        zformMap.put(column.getName(), p);
                    }
                }
            }
            zform = getZformFromMap(zformMap, loginName, isChildren);
        }
        return zform;
    }

    /**
     * 将动态表单 Map 数据转换为带 TableName 注解的实体对象。
     */
    public <T> T getEntityByMap(LinkedHashMap map, Class<T> clazz) {
        TableName tableName = clazz.getDeclaredAnnotation(TableName.class);
        if (tableName != null) {
            String currentLoginName = UserUtil.getCurrentLoginName();
            String formNo = tableName.value();
            JSONObject object = JSONHelper.toJSONObject(map);
            object.put("formNo", formNo);
            try {
                Zform zform = this.getZformFromMap(object, currentLoginName);
                return this.getEntityByZform(zform, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Zform转实体类，zform需有formNo属性值
     *
     * @param zform
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getEntityByZform(Zform zform, Class<T> clazz) {
        JSONObject jsonObject = this.getMapByZform(zform);
        if (jsonObject != null) {
            T entity = JSONObject.toJavaObject(jsonObject, clazz);
            return entity;
        }
        return null;
    }

    /**
     * Zform转map
     *
     * @param zform
     * @return
     */
    public JSONObject getMapByZform(Zform zform) {
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            for (GenTableColumn column : genTable.getColumnList()) {
                //读取zfrom属性值
                Object value = ReflectUtils.getValue(zform, column.getSimpleJavaField());
                if (value != null) {
                    if (value instanceof BaseEntity) {
                        jsonObject.put(column.getName(), ((BaseEntity) value).getId());
                    } else {
                        jsonObject.put(column.getName(), value);
                    }

                }
            }
        } catch (Exception err) {
            logger.warn("getMapByZform wrong, " + err.getMessage());
        }
        return jsonObject;
    }




    /* 扩展逻辑处理 */
    private void appendExtManyToMany(List<LinkedHashMap> list, GenTableExtRuleManyToMany manyToMany) {
        if (list == null || list.size() == 0) {
            logger.warn("appendExtManyToMany list is null or empty");
            return;
        }
        if (list.size() > 1000) {
            throw new RuntimeException("The number of appendExtManyToMany the limit of 1000");
        }
        StringBuilder sql = null;
        sql = new StringBuilder();
        sql.append("select a.id as \"dictId\", a.");
        sql.append(manyToMany.getCodeFiled());
        sql.append(" as \"code\", a.");
        sql.append(manyToMany.getNameFiled());
        sql.append(" as \"name\", rel."+manyToMany.getRelColumn()+" as \"relId\" from ");
        if (Global.YES.equals(manyToMany.getGenTableSql())) {
            //在编辑/查看数据时使用genTable的sql查询数据，可以显示关联查询中的字段
            String querySql = this.getQuerySql(manyToMany.getFormNo());
            sql.append("( ");
            sql.append(querySql);
            sql.append(" ) ");
        } else {
            sql.append(manyToMany.getFormNo());
        }
        sql.append(" a inner join ");
        sql.append(manyToMany.getRelTable());
        sql.append(" rel on rel.");
        sql.append(manyToMany.getRelManyColumn());
        sql.append(" = a.id ");
        sql.append(" where a.del_flag='0' and rel.");
        sql.append(manyToMany.getRelColumn());
        sql.append(" in (");
        for (int i = 0; i < list.size(); i++) {
            LinkedHashMap map = list.get(i);
            String id = ConvertUtil.getString(map.get("id"));
            SqlInjectionUtil.filterContent(id);
            if (i > 0) {
                sql.append(",");
            }
            sql.append("'");
            sql.append(id);
            sql.append("'");
        }
        sql.append(") ");
        if (StringUtil.isNotEmpty(manyToMany.getDsfPlus())) {
            sql.append(manyToMany.getDsfPlus());
        }
        sql.append(" order by a." + manyToMany.getCodeFiled());
        List<LinkedHashMap> mapBySql = zformDao.findMapBySql(sql.toString());
        Map<String, List<LinkedHashMap>> resultMap = mapBySql.stream().collect(Collectors.groupingBy(k -> ConvertUtil.getString(k.get("relId")), Collectors.toList()));
        for (LinkedHashMap map : list) {
            String id = ConvertUtil.getString(map.get("id"));
            List<LinkedHashMap> resultList = resultMap.getOrDefault(id, new ArrayList<>());
            map.put(manyToMany.getName(), resultList);
        }

    }
    /**
     * 追加多对多关系表数据
     * @param list  查询出来的数据
     * @param genTable 表定义
     */
    public void appendExtManyToMany(List<LinkedHashMap> list, GenTable genTable) {
        //每1000个查询一次
        int size = list.size();
        int count = size / 1000;
        int mod = size % 1000;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                List<LinkedHashMap> subList = list.subList(i * 1000, (i + 1) * 1000);
                genTable.getExtRuleManyToMany().forEach(manyToMany -> {
                    this.appendExtManyToMany(subList, manyToMany);
                });
            }
        }
        if (mod > 0) {
            List<LinkedHashMap> subList = list.subList(count * 1000, size);
            genTable.getExtRuleManyToMany().forEach(manyToMany -> {
                this.appendExtManyToMany(subList, manyToMany);
            });
        }

    }

    /**
     * getZformMap,getZformMapView 查询后
     *
     * @param map
     * @param id
     * @param genTable
     * @param extFlag
     * @throws Exception
     */
    protected void extAfterGetMap(LinkedHashMap map, String id, GenTable genTable, String extFlag) throws Exception {
        //处理多对多查询，将多对多关系表数据查询出来
        this.appendExtManyToMany(Collections.singletonList(map), genTable);
    }

    /**
     * datamap,datamapView 查询后逻辑处理
     *
     * @param pageMap
     * @param zform
     * @param loginName
     * @param genTable
     */
    protected void extAfterFindPageMap(Page<Zform> pageMap, Zform zform, String loginName, GenTable genTable) {
        this.appendExtManyToMany(pageMap.getMap(), genTable);
    }

    protected void extendBeforeSaveAct(Zform zform, String loginName, GenTable genTable) {
        if ("prt_information".equals(zform.getFormNo()) && "20".equals(zform.getStatus())) {
            Date theDate = new Date();
            zform.setS10(loginName);
            zform.setS11(loginName);
            zform.setD02(theDate);
            zform.setD03(theDate);
        }
        this.saveCountersign(zform, loginName);
    }

    protected void saveCountersign(Zform zform, String loginName) {
        //构造会签的值
        if (zform != null) {
            if (StringUtil.isNotBlank(zform.getTempcountersign01())) {
                super.setFieldValue(zform, "tempcountersign01", "countersign01", loginName);
            }
            if (StringUtil.isNotBlank(zform.getTempcountersign02())) {
                super.setFieldValue(zform, "tempcountersign02", "countersign02", loginName);
            }
            if (StringUtil.isNotBlank(zform.getTempcountersign03())) {
                super.setFieldValue(zform, "tempcountersign03", "countersign03", loginName);
            }
            if (StringUtil.isNotBlank(zform.getTempcountersign04())) {
                super.setFieldValue(zform, "tempcountersign04", "countersign04", loginName);
            }
            if (StringUtil.isNotBlank(zform.getTempcountersign05())) {
                super.setFieldValue(zform, "tempcountersign05", "countersign05", loginName);
            }
        }
    }

    private String getTableName(String formNo) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        if (genTable.getName().toLowerCase().endsWith(GenUtil.VIEW)) {
            if (StringUtil.isNotEmpty(genTable.getFormNoExt())) {
                return genTable.getFormNoExt().trim();
            } else {
                return genTable.getName().substring(0, genTable.getName().length() - 5);
            }
        } else {
            return formNo;
        }
    }

    public String getQuerySql(String formNo) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        boolean runtimeSourceSqlView = isRuntimeSourceSqlView(genTable);
        String sqlFriendly;
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            sqlFriendly = genTable.getExtSql02() + " " + (genTable.getSqlColumnsFriendlyExt() != null ? genTable.getSqlColumnsFriendlyExt() : "");
        } else {
            sqlFriendly = genTable.getSqlColumnsFriendly() + " " + (genTable.getSqlColumnsFriendlyExt() != null ? genTable.getSqlColumnsFriendlyExt() : "");
        }
        StringBuilder querySql = new StringBuilder();
        querySql.append("SELECT ");
        querySql.append(sqlFriendly);
        querySql.append(" FROM  ");
        querySql.append(runtimeSourceSqlView ? getRuntimeTableOrViewName(genTable) : this.getTableName(formNo));
        querySql.append(" a ");
        if (!runtimeSourceSqlView) {
            querySql.append(StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }

        return querySql.toString();
    }

    private String getQuerySql(String formNo, String genTableFormNo) {
        GenTable genTable = genTableService.getGenTableWithDefination(genTableFormNo);
        boolean runtimeSourceSqlView = isRuntimeSourceSqlView(genTable);
        String sqlFriendly;
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            sqlFriendly = genTable.getExtSql02() + " " + (genTable.getSqlColumnsFriendlyExt() != null ? genTable.getSqlColumnsFriendlyExt() : "");
        } else {
            sqlFriendly = genTable.getSqlColumnsFriendly() + " " + (genTable.getSqlColumnsFriendlyExt() != null ? genTable.getSqlColumnsFriendlyExt() : "");
        }
        StringBuilder querySql = new StringBuilder();
        querySql.append("SELECT ");
        querySql.append(sqlFriendly);
        querySql.append(" FROM  ");
        querySql.append(runtimeSourceSqlView ? getRuntimeTableOrViewName(genTable) : formNo);
        querySql.append(" a ");
        if (!runtimeSourceSqlView) {
            querySql.append(StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }

        return querySql.toString();
    }

    /**
     * 根据 formNo  查询数据 默认拼接删除标记
     *
     * @param formNo formNo
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo) {
        return this.findMapList(formNo, new QueryWrapper(), true);
    }

    /**
     * 根据 formNo 及id 查询唯一数据 默认拼接删除标记
     *
     * @param formNo formNo
     * @param id     id
     * @return map对象
     */
    public LinkedHashMap getMap(String formNo, String id) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a.id", id);
        List<LinkedHashMap> mapList = this.findMapList(formNo, queryWrapper, true);
        return mapList.size() > 0 ? mapList.get(0) : null;
    }

    /**
     * 根据 formNo 及某个字段 查询唯一数据 默认拼接删除标记
     *
     * @param formNo formNo
     * @param column 列名
     * @param value  值
     * @return map对象
     */
    public LinkedHashMap getMap(String formNo, String column, Object value) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        SqlInjectionUtil.filterContent(column);
        queryWrapper.eq(column, value);
        List<LinkedHashMap> mapList = this.findMapList(formNo, queryWrapper, true);
        return mapList.size() > 0 ? mapList.get(0) : null;
    }

    /**
     * 根据 formNo 及parentId 查询数据 默认拼接删除标记
     *
     * @param formNo   formNo
     * @param parentId parentId
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo, String parentId) {

        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a.parent_id", parentId);
        return this.findMapList(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo 及某列的值 查询数据 默认拼接删除标记
     *
     * @param formNo formNo
     * @param column 列名
     * @param value  值
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo, String column, Object value) {

        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        SqlInjectionUtil.filterContent(column);
        queryWrapper.eq(column, value);
        return this.findMapList(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo queryWrapper 查询数据 默认拼接删除标记
     *
     * @param formNo       formNo
     * @param queryWrapper queryWrapper
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo, QueryWrapper queryWrapper) {
        return this.findMapList(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo queryWrapper 查询数据
     *
     * @param formNo       formNo
     * @param queryWrapper queryWrapper
     * @param isDelFlag    是否拼接删除标记
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo, QueryWrapper queryWrapper, boolean isDelFlag) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        if (isDelFlag && genTable.getDelFlagExists()) {
            queryWrapper.eq("a.del_flag", '0');
        }
        String querySql = getQuerySql(formNo);
        return zformDao.findMapList(querySql, queryWrapper);
    }

    /**
     * 根据 formNo genTableFormNo queryWrapper 查询数据
     *
     * @param formNo         formNo
     * @param genTableFormNo genTable表名
     * @param queryWrapper   queryWrapper
     * @param isDelFlag      是否拼接删除标记
     * @return
     */
    public List<LinkedHashMap> findMapList(String formNo, String genTableFormNo, QueryWrapper queryWrapper, boolean isDelFlag) {
        GenTable genTable = genTableService.getGenTableWithDefination(genTableFormNo);
        if (isDelFlag && genTable.getDelFlagExists()) {
            queryWrapper.eq("a.del_flag", '0');
        }
        String querySql = getQuerySql(formNo, genTableFormNo);
        return zformDao.findMapList(querySql, queryWrapper);
    }

    /**
     * 根据数据权限查询数据 默认拼接删除标记
     *
     * @param formNo 表单编号
     * @return
     */
    public List<LinkedHashMap> findMapListByDataPermission(String formNo) {

        return this.findMapListByDataPermission(formNo, new QueryWrapper<>(), true);
    }

    /**
     * 根据数据权限查询数据 默认拼接删除标记
     *
     * @param formNo   表单编号
     * @param parentId parentId
     * @return
     */
    public List<LinkedHashMap> findMapListByDataPermission(String formNo, String parentId) {

        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a.parent_id", parentId);
        return this.findMapListByDataPermission(formNo, queryWrapper, true);
    }

    /**
     * 根据数据权限查询数据 默认拼接删除标记
     *
     * @param formNo 表单编号
     * @param column 列名
     * @param value  值
     * @return
     */
    public List<LinkedHashMap> findMapListByDataPermission(String formNo, String column, Object value) {

        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        SqlInjectionUtil.filterContent(column);
        queryWrapper.eq(column, value);
        return this.findMapListByDataPermission(formNo, queryWrapper, true);
    }


    /**
     * 根据数据权限查询数据 默认拼接删除标记
     *
     * @param formNo       表单编号
     * @param queryWrapper 查询条件
     * @return
     */
    public List<LinkedHashMap> findMapListByDataPermission(String formNo, QueryWrapper queryWrapper) {
        return this.findMapListByDataPermission(formNo, queryWrapper, true);
    }

    /**
     * 根据数据权限查询数据
     *
     * @param formNo       表单编号
     * @param queryWrapper 查询条件
     * @param isDelFlag    是否删除
     * @return
     */
    public List<LinkedHashMap> findMapListByDataPermission(String formNo, QueryWrapper queryWrapper, boolean isDelFlag) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        if (isDelFlag && genTable.getDelFlagExists()) {
            queryWrapper.eq("a.del_flag", '0');
        }
        String querySql = getQuerySql(formNo);
        super.permissionDataSet(UserUtil.getCurrentUser(), formNo, queryWrapper);
        return zformDao.findMapList(querySql, queryWrapper);
    }


    /**
     * 根据 formNo 查询数据条数 默认拼接删除标记
     *
     * @param formNo formNo
     * @return
     */
    public long count(String formNo) {
        return this.count(formNo, new QueryWrapper<>(), true);
    }

    /**
     * 根据 formNo 查询数据条数 默认拼接删除标记
     *
     * @param formNo      formNo
     * @param countColumn count列
     * @return
     */
    public long count(String formNo, String countColumn) {
        return this.count(formNo, countColumn, new QueryWrapper<>(), true);
    }

    /**
     * 根据 formNo 及parentId 查询数据条数 默认拼接删除标记
     *
     * @param formNo   formNo
     * @param parentId parentId
     * @return 数据条数
     */
    public long countByParentId(String formNo, String parentId) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("a.parent_id", parentId);
        return this.count(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo 及某列的值 查询数据条数 默认拼接删除标记
     *
     * @param formNo formNo
     * @param column 列名
     * @param value  值
     * @return 数据条数
     */
    public long count(String formNo, String column, Object value) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        SqlInjectionUtil.filterContent(column);
        queryWrapper.eq(column, value);
        return this.count(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo 及某列的值 查询数据条数 默认拼接删除标记
     *
     * @param formNo      formNo
     * @param countColumn count列
     * @param column      列名
     * @param value       值
     * @return 数据条数
     */
    public long count(String formNo, String countColumn, String column, Object value) {
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        SqlInjectionUtil.filterContent(column);
        queryWrapper.eq(column, value);
        return this.count(formNo, countColumn, queryWrapper, true);
    }

    /**
     * 根据 formNo 和 queryWrapper 查询数据条数 默认拼接删除标记
     *
     * @param formNo       formNo
     * @param queryWrapper queryWrapper
     * @return 数据条数
     */
    public long count(String formNo, QueryWrapper queryWrapper) {
        return this.count(formNo, queryWrapper, true);
    }

    /**
     * 根据 formNo 和 queryWrapper 查询数据条数 默认拼接删除标记
     *
     * @param formNo       formNo
     * @param countColumn  count列
     * @param queryWrapper queryWrapper
     * @return 数据条数
     */
    public long count(String formNo, String countColumn, QueryWrapper queryWrapper) {
        return this.count(formNo, countColumn, queryWrapper, true);
    }

    /**
     * 根据 formNo 和 queryWrapper 查询数据条数
     *
     * @param formNo       formNo
     * @param queryWrapper queryWrapper
     * @param isDelFlag    是否拼接删除标记
     * @return 数据条数
     */
    public long count(String formNo, QueryWrapper queryWrapper, boolean isDelFlag) {

        return this.count(formNo, "1", queryWrapper, isDelFlag);
    }

    /**
     * 根据 formNo 和 queryWrapper 查询数据条数
     *
     * @param formNo       formNo
     * @param countColumn  count列
     * @param queryWrapper queryWrapper
     * @param isDelFlag    是否拼接删除标记
     * @return 数据条数
     */
    public long count(String formNo, String countColumn, QueryWrapper queryWrapper, boolean isDelFlag) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        if (isDelFlag && genTable.getDelFlagExists()) {
            queryWrapper.eq("a.del_flag", '0');
        }
        SqlInjectionUtil.filterContent(countColumn);
        return zformDao.count(this.getQuerySql(formNo), countColumn, queryWrapper);
    }

    /**
     * 导出word
     *
     * @param templateId 模板id
     * @param configMap  配置参数
     * @param dataMap    数据参数
     * @param wordName   导出文件名 不带后缀
     * @return 返回文件id
     */
    public String exportWord(String templateId, Map<String, Object> configMap, Map<String, Object> dataMap, String wordName) {
        RequestVo requestVo = new RequestVo();
        requestVo.setConfigMap(configMap);
        requestVo.setDataMap(dataMap);
        return this.exportWord(templateId, requestVo, wordName);
    }

    /**
     * 导出word
     *
     * @param templateId 模板id
     * @param requestVo  请求参数
     * @param wordName   导出文件名 不带后缀
     * @return 返回文件id
     */
    public String exportWord(String templateId, RequestVo requestVo, String wordName) {
        SysFile sysFile = exportWordFile(templateId, requestVo, wordName);
        return sysFile.getId();
    }

    /**
     * 导出word
     *
     * @param templateId 模板id
     * @param requestVo  请求参数
     * @param wordName   导出文件名 不带后缀
     * @return 返回文件对象
     */
    /**
     * 导出 Word 文件并返回系统文件对象，内部会调用 Word 导出服务并落库文件记录。
     */
    @Transactional
    public SysFile exportWordFile(String templateId, RequestVo requestVo, String wordName) {
        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        return exportWordFile(groupId, templateId, requestVo, wordName);
    }

    /**
     * 按指定文件组导出 Word 文件，适用于需要把导出结果挂到已有附件组的场景。
     */
    @Transactional
    public SysFile exportWordFile(String templateId, RequestVo requestVo, String wordName, String groupId) {
        return exportWordFile(groupId, templateId, requestVo, wordName);
    }

    /**
     * 导出word
     *
     * @param groupId    文件组id
     * @param templateId 模板id
     * @param requestVo  请求参数
     * @param wordName   导出文件名 不带后缀
     * @return 返回文件对象
     */
    @Transactional
    public SysFile exportWordFile(String groupId, String templateId, RequestVo requestVo, String wordName) {
        SysFile sysFileTemplate = oaWordTemplateMapper.getFileByTemplateId(templateId);
        logger.info("导出word文件，模板文件：{}，模板id：{}，文件名：{}", sysFileTemplate.getId(), templateId, wordName);
        byte[] bytes = wordExportFeignClient.exportBySysFile(sysFileTemplate, requestVo);
        return sysFileService.saveExportSysFile(wordName + ".docx", bytes, groupId);
    }

    /**
     * 合并word
     *
     * @param fileNames 文件名列表
     * @param wordName  导出文件名 不带后缀
     * @return 返回文件对象
     */
    public SysFile exportMergeMultiWordFile(List<String> fileNames, String wordName) {
        List<String> realFileNames = fileNames.stream().map(item -> urlFile + item).collect(Collectors.toList());
        byte[] bytes = wordExportFeignClient.exportByMergeMultiWordFSysFile(realFileNames);
        return sysFileService.saveExportSysFile(wordName + ".docx", bytes);
    }

    /**
     * 检查编辑权限
     *
     * @param loginName
     * @param formNo
     */
    private void checkSavePermission(String loginName, String formNo, Boolean isNewRecord) throws Exception {
        if ("sys_user_role".equals(formNo)) {
            return;
        }
        if ("sys_user_datarole".equals(formNo)) {
            return;
        }
        if ("oa_sys_announcement_read".equals(formNo)) {
            return;
        }
        if ("oa_sys_msg".equals(formNo)) {
            return;
        }
        User currentUser = UserUtil.getCurrentUser();
        if (currentUser == null || currentUser.isAdmin()) {
            return;
        }
        List<String> permissionList = UserUtil.getMenuPermissionList(UserUtil.getByLoginName(loginName));
        if (isNewRecord) {
            if (false == this.judgePermissionList(permissionList, formNo, "add")) {
                throw new Exception(formNo + "添加操作未授权");
            }
        } else {
            if (false == this.judgePermissionList(permissionList, formNo, "edit") && false == this.judgePermissionList(permissionList, formNo, "handle")) {
                throw new Exception(formNo + "编辑操作未授权");
            }
        }
    }


    /**
     * 判断是否有权限 模糊匹配
     *
     * @param permissionList 权限列表
     * @param formNo         表单编号
     * @param permission     权限
     * @return
     */
    private boolean judgePermissionList(List<String> permissionList, String formNo, String permission) {
        if (permissionList == null || permissionList.size() == 0) {
            return false; //当前用户没有任何权限
        }
        for (String s : permissionList) {
            if (ConvertUtil.getString(s).contains("app:" + formNo + ":" + permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查查询权限
     *
     * @param loginName 登录名
     * @param formNo   表单编号
     */
    public void checkSelectPermission(String loginName, String formNo) throws Exception {
        User currentUser = UserUtil.getByLoginName(loginName);
        if (currentUser == null || currentUser.isAdmin()) {
            return;
        }
        List<String> encryptWhiteList = projectProperties.getSelectPermissionWhiteList();
        if (encryptWhiteList.contains(formNo)) {
            return;
        }

        List<String> permissionList = UserUtil.getMenuPermissionList(currentUser);
        if (false == this.judgePermissionList(permissionList, formNo, "")
                && false == this.judgePermissionList(permissionList, this.getTableName(formNo), "")) {
            logger.error("越权操作,loginName:{},formNo:{}", loginName, formNo);
            throw new Exception("操作未授权");
        }
    }

    /**
     * 检查删除权限
     *
     * @param loginName
     * @param formNo
     */
    private void checkDeletePermission(String loginName, String formNo) throws Exception {
        User currentUser = UserUtil.getByLoginName(loginName);
        if (currentUser == null || currentUser.isAdmin()) {
            return;
        }
        List<String> permissionList = UserUtil.getMenuPermissionList(currentUser);
        if (false == this.judgePermissionList(permissionList, formNo, "del")) {
            throw new Exception(formNo + "删除操作未授权");
        }
    }


    public Map<String, Integer> countByParentId(GenTable genTable, Zform zform) {
        Page<Zform> page = new Page<>(1, Integer.MAX_VALUE, "");
        String path = "path", loginName = UserUtil.getCurrentLoginName(), traceFlag = "", parentId = "", extFlag = "";
        Map<String, Integer> countMap2 = new HashMap<>();
        try {
            genTable.setSqlColumnsFriendlyExt("");
            genTable.setSqlColumnsFriendly(" a.parent_id \"parent_id\",count(a.id) \"data_count\" ");
            page.setOrderBy("group by a.parent_id ");
            Page<Zform> pageMap = this.findPageMap(page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
            for (LinkedHashMap map : pageMap.getMap()) {
                countMap2.put(map.get("parent_id").toString(), Integer.parseInt(map.get("data_count").toString()));
            }
        } catch (Exception e) {
            logger.error("countByParentId失败,{}", ExceptionUtil.stacktraceToString(e));
        }
        return countMap2;
    }

    public void createDataRowMap(Workbook workbook, List<ExcelField> excelExportFieldList, List<LinkedHashMap> data) {
        List<Map<String, Object>> mapList = new ArrayList<>(data.size());
        for (Map<String, Object> map : data) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.putAll(map);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> child = (Map) entry.getValue();
                    for (Map.Entry<String, Object> childEntry : child.entrySet()) {
                        JSONArray keys = new JSONArray();
                        keys.add(entry.getKey());
                        keys.add(childEntry.getKey());
                        dataMap.put(keys.toString(), childEntry.getValue());
                    }
                }
            }
            mapList.add(dataMap);
        }
        ExcelExportUtil.createDataRowMap(workbook, workbook.getSheetAt(0), excelExportFieldList, mapList, 1, false);
    }

    public ResultJson exportExcel(String sheetName, JSONObject requestMap, List<Map<String, Object>> listResult) throws Exception {
        JSONArray tableColumns = requestMap.getJSONArray("tableColumns");
        List<ExcelField> excelExportFieldList = JSONHelper.toList(tableColumns.toJSONString(), ExcelField.class);
        for (int i = 0; i < tableColumns.size(); i++) {
            JSONObject jsonObject = tableColumns.getJSONObject(i);
            if (jsonObject.containsKey("dictType")) {
                String dictType = jsonObject.getString("dictType");
                List<DictResult> dictList = dictDataService.getDictList(dictType, false);
                ExcelField excelField = excelExportFieldList.get(i);
                LinkedHashMap<Object, Object> dataMap = Maps.newLinkedHashMap();
                for (DictResult dict : dictList) {
                    dataMap.put(dict.getMember(), dict.getMemberName());
                }
                excelField.setDataMap(dataMap);
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>(listResult.size());
        for (Map<String, Object> map : listResult) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.putAll(map);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> child = (Map) entry.getValue();
                    for (Map.Entry<String, Object> childEntry : child.entrySet()) {
                        JSONArray keys = new JSONArray();
                        keys.add(entry.getKey());
                        keys.add(childEntry.getKey());
                        dataMap.put(keys.toString(), childEntry.getValue());
                    }
                }
            }
            dataList.add(dataMap);
        }

        return exportExcel(sheetName, excelExportFieldList, dataList);
    }

    public ResultJson exportExcel(String excelName, List<ExcelField> excelExportFieldList, List<Map<String, Object>> listResult) throws Exception {
        return exportExcel(excelName, excelExportFieldList, listResult, true);
    }

    public ResultJson exportExcel(String excelName, List<ExcelField> excelExportFieldList, List<Map<String, Object>> listResult, boolean haveSuffix) throws Exception {
        return this.exportExcel(excelName, excelExportFieldList, listResult, haveSuffix, null,null);
    }

    /**
     * 创建excel的标题行
     *
     * @param workBook             工作簿
     * @param sheet                sheet
     * @param excelExportFieldList 导出字段
     * @param startRowIndex        开始行
     * @param startColIndex        开始列
     * @param titleIndexMap        标题索引
     * @param excelFieldList       实际有数据的字段
     * @return
     */
    public int[] createExcelTitle(Workbook workBook, Sheet sheet, List<ExcelField> excelExportFieldList, int startRowIndex, int startColIndex, Map<String, Integer> titleIndexMap, List<ExcelField> excelFieldList) {
        int maxDeep = startRowIndex + 1;
        Row row = sheet.getRow(startRowIndex);
        if (row == null) {
            // 创建行
            row = sheet.createRow(startRowIndex);
        }
        int i = startColIndex;
        for (ExcelField excelField : excelExportFieldList) {

            Cell cell = row.createCell(i);
            cell.setCellValue(excelField.getFieldTitle());      //设置列名列
            cell.setCellStyle(ExcelExportUtil.getTitleStyle(workBook));

            //记录标题的索引
            titleIndexMap.put(excelField.getFieldValue(), i);
            if (excelField.getChildren() != null && excelField.getChildren().size() > 0) {
                //递归创建标题列
                int[] deepAndColCount = createExcelTitle(workBook, sheet, excelField.getChildren(), startRowIndex + 1, i, titleIndexMap, excelFieldList);
                int deep1 = deepAndColCount[0];
                int colCount = deepAndColCount[1];
                if (deep1 > maxDeep) {
                    maxDeep = deep1;
                }
                i += colCount;
            } else {
                //实际有数据的字段
                excelFieldList.add(excelField);
                if (startRowIndex > maxDeep) {
                    maxDeep = startRowIndex;
                }
                i++;
            }
        }
        return new int[]{maxDeep, i - startColIndex};
    }

    private ExcelField getFirstColumn(List<ExcelField> columnList) {
        for (ExcelField column : columnList) {
            if (column.getChildren() != null && column.getChildren().size() > 0) {
                return getFirstColumn(column.getChildren());
            } else {
                return column;
            }
        }
        return null;
    }
    private ExcelField getLastColumn(List<ExcelField> columnList) {
        for (int i = columnList.size() - 1; i >= 0; i--) {
            ExcelField column = columnList.get(i);
            if (column.getChildren() != null && column.getChildren().size() > 0) {
                return getLastColumn(column.getChildren());
            } else {
                return column;
            }
        }
        return null;
    }

    /**
     * 合并excel标题
     *
     * @param workBook             工作簿
     * @param sheet                sheet
     * @param excelExportFieldList 导出字段
     * @param titleIndexMap        标题索引
     * @param titleHeight          标题高度
     * @param currentRow           当前行
     */

    private void mergeExcelTitle(Workbook workBook, Sheet sheet, List<ExcelField> excelExportFieldList, Map<String, Integer> titleIndexMap, int titleHeight, int currentRow) {
        for (ExcelField excelField : excelExportFieldList) {
            List<ExcelField> children = excelField.getChildren();
            if (children != null && children.size() > 0) {
                //如果有子级
                if (children.size()>1){
                    //当子级大于1时，合并当前列到最后一个子级的列
                    ExcelField first = getFirstColumn(children);//递归获取第一个子级
                    ExcelField last = getLastColumn(children);//递归获取最后一个子级
                    Integer firstIndex = titleIndexMap.get(first.getFieldValue());//获取第一个子级的索引
                    Integer lastIndex = titleIndexMap.get(last.getFieldValue());//获取最后一个子级的索引
                    for (int i = firstIndex + 1; i <= lastIndex; i++) {
                        //设置被合并的单元格的样式
                        Cell cell = sheet.getRow(currentRow).createCell(i);
                        cell.setCellValue(StrUtil.EMPTY);
                        cell.setCellStyle(ExcelExportUtil.getTitleStyle(workBook));
                    }
                    ExcelExportUtil.addMergedRegion(sheet, currentRow, currentRow, firstIndex, lastIndex);
                }
                //递归合并子级
                this.mergeExcelTitle(workBook, sheet, children, titleIndexMap, titleHeight, currentRow + 1);
            } else if (currentRow < titleHeight - 1) {
                //合并从第一行到titleHeight行，第i列
                sheet.getRow(currentRow).getCell(titleIndexMap.get(excelField.getFieldValue())).getCellStyle().setVerticalAlignment(VerticalAlignment.CENTER);
                for (int i = currentRow + 1; i < titleHeight; i++) {
                    Cell cell = sheet.getRow(i).createCell(titleIndexMap.get(excelField.getFieldValue()));
                    cell.setCellValue(StrUtil.EMPTY);
                    cell.setCellStyle(ExcelExportUtil.getTitleStyle(workBook));
                }
                ExcelExportUtil.addMergedRegion(sheet, currentRow, titleHeight - 1, titleIndexMap.get(excelField.getFieldValue()), titleIndexMap.get(excelField.getFieldValue()));
            }
        }
    }

    /**
     * 自动调整excel列宽
     * @param sheet
     */
    public void autoSetColumnWidth(Sheet sheet){
        JSONObject cellMaxChar = new JSONObject();

        for(Row row : sheet){
            int index = 0;
            for(Cell cell : row){
                String cellChar = cellMaxChar.getString("cell" + index);
                String stringCellValue = cell.getStringCellValue();
                if(cellChar == null){
                    cellMaxChar.put("cell" + index,stringCellValue);
                }else{
                    if(stringCellValue.length() > cellChar.length()){
                        cellMaxChar.put("cell" + index,stringCellValue);
                    }
                }
                index++;
            }
        }

        for(short index = 0; index < cellMaxChar.size() + 1; ++index) {
            String maxCellValue = cellMaxChar.getString("cell" + index);

            try {
                if (maxCellValue != null) {
                    int bytes = maxCellValue.getBytes("GBK").length;
                    int newWidth = bytes * 260 + 1000;
                    if (newWidth > 20000) {
                        sheet.setColumnWidth(index, 20000);
                    } else {
                        int columnWidth = sheet.getColumnWidth(index);
                        if (newWidth > columnWidth) {
                            sheet.setColumnWidth(index, newWidth);
                        }
                    }
                }
            } catch (UnsupportedEncodingException var29) {
                logger.error(ExceptionUtil.stacktraceToString(var29));
                throw new BusinessException("导出失败");
            }
        }
    }

    /**
     * 导出excel
     *
     * @param excelName            导出文件名
     * @param excelExportFieldList 导出字段
     * @param listResult           数据
     * @param haveSuffix           是否带后缀
     * @param mergeColumnConfig    合并数据配置
     * @return
     * @throws Exception
     */
    public ResultJson exportExcel(String excelName, List<ExcelField> excelExportFieldList, List<Map<String, Object>> listResult, boolean haveSuffix, JSONArray mergeColumnConfig, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) throws Exception {
        String sheetName = excelName;
        if (sheetName.endsWith(".xlsx")) {
            sheetName = sheetName.substring(0, excelName.length() - 5);
        }
        //创建工作簿
        try(Workbook workBook = new SXSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            //记录标题的索引
            Map<String, Integer> titleIndexMap = new HashMap<>();
            //实际有数据的字段
            List<ExcelField> excelFieldList = new ArrayList<>();
            Sheet sheet = workBook.createSheet(sheetName);
            //添加序号列
            ExcelField indexColumn = new ExcelField();
            indexColumn.setFieldTitle("序号");
            indexColumn.setFieldValue("__row_index__");
            excelExportFieldList.add(0, indexColumn);
            //创建标题 并记录标题的索引 以及实际有数据的字段
            int titleHeight = this.createExcelTitle(workBook, sheet, excelExportFieldList, 0, 0, titleIndexMap, excelFieldList)[0];

            if (titleHeight > 1) {
                //如果是多级标题 合并标题
                this.mergeExcelTitle(workBook, sheet, excelExportFieldList, titleIndexMap, titleHeight, 0);
            }

            //根据标题自动调整列宽
            autoSetColumnWidth(sheet);

            Map<String, ExcelMergedRegionUtil> mergedRegionUtilMap = new HashMap<>();
            if (mergeColumnConfig != null && mergeColumnConfig.size() > 0) {
                for (int i = 0; i < mergeColumnConfig.size(); i++) {
                    JSONObject jsonObject = mergeColumnConfig.getJSONObject(i);
                    //合并列的id 某列的值相同时合并对应的列
                    String colId = jsonObject.getString("colId");
                    //合并的列
                    JSONArray mergeColumns = jsonObject.getJSONArray("mergeColumns");
                    ExcelMergedRegionUtil util = new ExcelMergedRegionUtil(sheet, titleHeight);
                    mergeColumns.forEach(item -> {
                        //如果不是前端的 序号列 和 操作列
                        if (!ConvertUtil.getString(item).equals("_index") && !ConvertUtil.getString(item).equals("_opt")){
                            //获取合并列的索引
                            util.setMergeColumns(titleIndexMap.get(ConvertUtil.getString(item)));
                        }
                    });
                    mergedRegionUtilMap.put(colId, util);
                }
            }

            int rowIndex = 0;
            //遍历数据 添加合并配置 以及序号列的值
            for (Map<String, Object> map : listResult) {
                for (Map.Entry<String, ExcelMergedRegionUtil> entry : mergedRegionUtilMap.entrySet()) {
                    String idVal = ConvertUtil.getString(map.get(entry.getKey()));
                    entry.getValue().addDataId(idVal, rowIndex);
                }
                map.put("__row_index__", rowIndex + 1);
                rowIndex++;
            }

            //创建数据行
            ExcelExportUtil.createDataRowMap(workBook, sheet, excelFieldList, listResult, titleHeight, false,dataCellStyle);
            //冻结标题行及序号列
            sheet.createFreezePane(1, titleHeight, 1, titleHeight);

            for (Map.Entry<String, ExcelMergedRegionUtil> entry : mergedRegionUtilMap.entrySet()) {
                //根据合并配置合并数据
                entry.getValue().merge();
            }

            workBook.write(out);
            String filename;
            if (haveSuffix) {
                filename = excelName + "导出.xlsx";
            } else {
                filename = excelName;
            }
            SysFile sysFile = sysFileService.saveExportSysFile(filename, out.toByteArray());
            return success().put("fileId", sysFile.getId()).put("file", sysFile);
        }catch (Exception e){
            ExceptionUtil.stacktraceToString(e);
            throw new BusinessException("导出失败");
        }
    }


    public ResultJson exportExcel(Workbook workbook, String excelName) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            String filename = excelName.endsWith(".xlsx") ? excelName : excelName + "导出.xlsx";
            SysFile sysFile = sysFileService.saveExportSysFile(filename, out.toByteArray());
            return success().put("fileId", sysFile.getId()).put("file", sysFile);
        }
    }

    ExecutorService exportQueueExecutor = Executors.newFixedThreadPool(10);

    @PreDestroy
    public void destroy() {
        shutdownGracefully(exportQueueExecutor, 30); // 等待最多30秒
    }

    /**
     * 优雅关闭线程池的工具方法
     * @param pool 线程池实例
     * @param timeoutSec 最大等待时间（秒）
     */
    private void shutdownGracefully(ExecutorService pool, int timeoutSec) {
        pool.shutdown(); // 1. 停止接收新任务，已提交任务继续执行
        try {
            // 2. 等待已提交任务完成，设置超时避免无限等待
            if (!pool.awaitTermination(timeoutSec, TimeUnit.SECONDS)) {
                // 3. 如果超时后仍有任务未完成，尝试强制终止
                List<Runnable> droppedTasks = pool.shutdownNow();
                logger.warn("警告：线程池未在指定时间内完全关闭，已丢弃 " + droppedTasks.size() + " 个任务。");
            }
        } catch (InterruptedException e) {
            // 4. 如果等待过程被中断，再次尝试强制关闭
            pool.shutdownNow();
            Thread.currentThread().interrupt(); // 保留中断状态
        }
    }

    /**
     * 导出到队列文件
     *
     * @param fileGroup 文件分组
     * @param supplier  文件生成方法
     * @return
     */
    public SysFileQueueEntity exportToQueue(String fileGroup, Supplier<SysFile> supplier) {
        SysFileQueueEntity queueEntity = new SysFileQueueEntity();
        queueEntity.setExportStatus(SysFileQueueExportStatus.EXPORTING.name());
        queueEntity.setFileFrom(fileGroup);
        sysFileQueueService.save(queueEntity);
        exportQueueExecutor.execute(() -> {
            try {
                SysFile sysFile = supplier.get();
                queueEntity.setFileId(sysFile.getId());
                queueEntity.setExportStatus(SysFileQueueExportStatus.SUCCESS.name());
            } catch (Exception e) {
                logger.error(ExceptionUtil.stacktraceToString(e));
                queueEntity.setExportStatus(SysFileQueueExportStatus.FAIL.name());
            }
            sysFileQueueService.updateById(queueEntity);
        });
        return queueEntity;
    }

    /**
     * 将动态表单 Excel 导出任务写入文件队列，异步生成导出文件。
     */
    public ResultJson exportDataToQueue(JSONObject zformMap, String formNo, String path, String traceFlag, String parentId) throws Exception {
        SysFileQueueEntity item = new SysFileQueueEntity();
        String queueId = cn.hutool.core.lang.UUID.fastUUID().toString();
        item.setId(queueId);
        item.setExportStatus(SysFileQueueExportStatus.EXPORTING.name());
        item.setCreateDate(new Date());
        item.setUpdateDate(new Date());
        item.setCreateBy(UserUtil.getCurrentUser().getId());
        item.setOwnerCode(UserUtil.getCurrentUser().getCompany().getCode());
        item.setFileFrom(zformMap.getString("exportQueueFileFrom"));
        zformMap.remove("exportQueueFileFrom");
        sysFileQueueService.save(item);
        Map<String, Object> requestParams = new HashMap<String, Object>() {{
            put("formNo", formNo);
            put("path", path);
            put("traceFlag", traceFlag);
            put("parentId", parentId);
        }};
        SysFileQueueExportThread fileThread = new SysFileQueueExportThread(this, sysFileQueueService, queueId, zformMap, formNo, path, traceFlag, parentId, UserUtil.getCurrentLoginName(), requestParams, RequestContextHolder.getRequestAttributes());
        Thread thread = new Thread(fileThread);
        thread.start();
        return success().put("queueId", queueId);
    }

    /**
     * 从前端传入的json中获取导出字段
     *
     * @param zformMap
     * @return
     */
    public List<ExcelField> getExcelExportFieldList(JSONObject zformMap) {
        JSONArray tableColumns = zformMap.getJSONArray("tableColumns");
        List<ExcelField> excelExportFieldList = JSONHelper.toList(tableColumns.toJSONString(), ExcelField.class);
        this.setDictDataMap(tableColumns, excelExportFieldList);
        zformMap.remove("tableColumns");
        return excelExportFieldList;
    }

    /**
     * 设置字典数据
     *
     * @param tableColumns
     * @param excelFieldList
     */
    private void setDictDataMap(JSONArray tableColumns, List<ExcelField> excelFieldList) {
        for (int i = 0; i < tableColumns.size(); i++) {
            JSONObject jsonObject = tableColumns.getJSONObject(i);
            ExcelField excelField = excelFieldList.get(i);
            if (jsonObject.containsKey("children") && excelField.getChildren() != null) {
                //递归设置字典数据
                this.setDictDataMap(jsonObject.getJSONArray("children"), excelField.getChildren());
            } else if (StringUtils.isNotBlank(jsonObject.getString("dictType"))) {
                String dictType = jsonObject.getString("dictType");
                List<DictResult> dictList = dictDataService.getDictList(dictType, false);
                LinkedHashMap<Object, Object> dataMap = Maps.newLinkedHashMap();
                for (DictResult dict : dictList) {
                    dataMap.put(dict.getMember(), dict.getMemberName());
                }
                excelField.setDataMap(dataMap);
            }
        }
    }

    /**
     * 从前端传入的json中获取导出文件名
     *
     * @param zformMap
     * @return
     */
    public String getExcelExportFileName(JSONObject zformMap) {
        String filename = null;
        if (zformMap.containsKey("exportExcelFileName")) {
            filename = zformMap.getString("exportExcelFileName");
            zformMap.remove("exportExcelFileName");
        }
        return filename;
    }

    /**
     * 解析导出数据 将map的key为json格式的字符串
     *
     * @param list 导出数据
     * @return 解析后的数据
     */
    public List<Map<String, Object>> parseExportData(JSONArray list) {
        List<Map<String, Object>> dataList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JSONObject hashMap = list.getJSONObject(i);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.putAll(hashMap);
            for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> child = (Map) entry.getValue();
                    for (Map.Entry<String, Object> childEntry : child.entrySet()) {
                        JSONArray keys = new JSONArray();
                        keys.add(entry.getKey());
                        keys.add(childEntry.getKey());
                        dataMap.put(keys.toString(), childEntry.getValue());
                    }
                }
            }
            dataList.add(dataMap);
        }
        return dataList;
    }

    /**
     * 解析导出数据 将map的key为json格式的字符串
     *
     * @param list 导出数据
     * @return 解析后的数据
     */
    public List<Map<String, Object>> parseExportData(List<LinkedHashMap> list) {
        List<Map<String, Object>> dataList = new ArrayList<>(list.size());
        for (LinkedHashMap<String, Object> hashMap : list) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.putAll(hashMap);
            for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> child = (Map) entry.getValue();
                    for (Map.Entry<String, Object> childEntry : child.entrySet()) {
                        JSONArray keys = new JSONArray();
                        keys.add(entry.getKey());
                        keys.add(childEntry.getKey());
                        dataMap.put(keys.toString(), childEntry.getValue());
                    }
                }
            }
            dataList.add(dataMap);
        }
        return dataList;
    }

    /**
     * 导出动态表单数据，支持普通导出、队列导出、指定表格列和子表合并导出。
     */
    public ResultJson expdata(JSONObject zformMap, String formNo, String path, String traceFlag, String parentId, String paramLoginName) throws Exception {
        zformMap.put("formNo", formNo);
        boolean exportToFileQueue = false;

        if (zformMap.containsKey("exportToFileQueue")) {
            exportToFileQueue = zformMap.getBoolean("exportToFileQueue");
            zformMap.remove("exportToFileQueue");
        }
        if (exportToFileQueue) {
            return exportDataToQueue(zformMap, formNo, path, traceFlag, parentId);
        }

        List<ExcelField> excelExportFieldList = Lists.newArrayList();
        //移除tableColumns 其他自定义导出可能会用到
        boolean exportAsTableColumns = false;
        if (zformMap.containsKey("exportAsTableColumns")) {
            exportAsTableColumns = zformMap.getBoolean("exportAsTableColumns");
            zformMap.remove("exportAsTableColumns");
        }

        JSONArray mergeColumnConfig = new JSONArray();
        if (zformMap.containsKey("mergeColumnConfig")) {
            mergeColumnConfig = zformMap.getJSONArray("mergeColumnConfig");
            zformMap.remove("mergeColumnConfig");
        }

        JSONArray exportWithSubTables = null;
        if (zformMap.containsKey("exportWithSubTables")) {
            /*
             * tableName： String 表名
             * tableAs： String 别名
             * orderBy： Array 排序方式
             *  [{
             *      name： String 字段名
             *      type： Boolean 排序方式
             *          true:ASC false:DESC null:ASC
             *      castAs： String 类型名
             *      },
             *  ]
             * */
            exportWithSubTables = zformMap.getJSONArray("exportWithSubTables");
            zformMap.remove("exportWithSubTables");
        }
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);

        String filename = this.getExcelExportFileName(zformMap);
        if (filename == null) {
            filename = genTable.getComments();
        }

        AroundControllerI aroundController = AroundUtil.getAroundControllerI(genTable);

        // 子表联查
        if (exportWithSubTables != null) {
            List<LinkedHashMap<String, Object>> subTableList = new LinkedList<>();
            Set<String> subTableSet = new HashSet<>();                    // 用于查询配置中是否有这个表
            Map<String, String> subTableAsNameMap = new HashMap<>();     // 存储表名和别名的对应关系
            List<String> subTableJoinSqlList = new LinkedList<>();        // 存储联查的字符串SQL列表
            List<String> fieldSqlList = new LinkedList<>();             // 存储联查的字段SQL列表
            List<String> sortSqlList = new LinkedList<>();              // 存储联查的排序SQL列表
            String[] sqlInjectionCharList = {"(", ")", " ", ".", "<", ">", "?", "%", "*", "/", "-", "!", "+", "=", "&", "$", "#", ",", "{", "}", "|"};
            // 防止SQL注入的字符列表

            // 解决不同数据库的兼容性问题
            String dbType = DbTypeUtil.getDbType();
            String leftQuo = "\"";
            String rightQuo = "\"";

            switch (dbType) {
                case "kingbase":
                    break;
                case "mysql":
                    leftQuo = "`";
                    rightQuo = "`";
                    break;
                default:
            }

            for (Object o : exportWithSubTables) {
                LinkedHashMap<String, Object> t = (LinkedHashMap<String, Object>) o;
                String tableName = ConvertUtil.getString(t.get("tableName"));
                String tableAs = ConvertUtil.getString(t.get("tableAs"));
                List<LinkedHashMap<String, Object>> orderByList;
                if (t.get("orderBy") == null) {
                    orderByList = new LinkedList<>();
                } else {
                    orderByList = (List<LinkedHashMap<String, Object>>) t.get("orderBy");
                }

                // 检查sql注入
                SqlInjectionUtil.filterContent(tableName);
                SqlInjectionUtil.filterContent(tableAs);

                // 生成排序SQL
                for (LinkedHashMap<String, Object> order : orderByList) {
                    String orderByName = ConvertUtil.getString(order.get("name"));
                    Object orderByAsObj = order.get("type");
                    if (orderByAsObj == null) {
                        orderByAsObj = true;
                    }
                    Boolean orderByAs = (Boolean) orderByAsObj;
                    SqlInjectionUtil.filterContent(orderByName);
                    String orderByType = orderByAs ? "ASC" : "DESC";
                    String orderByField = tableAs + "__" + orderByName;
                    Object castAsObj = order.get("castAs");
                    if (castAsObj != null) {
                        String castAs = ConvertUtil.getString(order.get("castAs"));
                        // 防止SQL注入
                        SqlInjectionUtil.filterContent(castAs);
                        for (String r : sqlInjectionCharList) {
                            castAs = castAs.replace(r, "");
                        }

                        orderByField = "CAST(" + orderByField + " AS " + castAs + ")";
                    }
                    sortSqlList.add(orderByField + " " + orderByType);
                }

                subTableList.add(t);
                subTableSet.add(tableName);
                subTableAsNameMap.put(tableName, tableAs);
            }

            // 子表SQL
            for (GenTable child : genTable.getChildList()) {
                if (subTableSet.contains(child.getName())) {
                    String tableName = child.getName();
                    String tableAs = subTableAsNameMap.get(tableName);
                    SubTableExportConfig subTableExportConfig = new SubTableExportConfig();
                    subTableExportConfig.setGenTable(child);
                    subTableExportConfig.setParentGenTable(genTable);
                    subTableExportConfig.setTableName(tableName);
                    subTableExportConfig.setTableAs(tableAs);
                    if (aroundController != null) {
                        aroundController.beforeSetSubTableSql(subTableExportConfig, genTable, child, tableName, tableAs);
                    }
                    String columns = child.getSqlColumnsFriendly();
                    if (StringUtil.isNotEmpty(child.getSqlColumnsFriendlyExt())) {
                        columns += child.getSqlColumnsFriendlyExt();
                    }
                    columns += ",a.parent_id AS \"__parent_id\"";

                    // 生成联查sql
                    String joinSql;
                    if (subTableExportConfig.getJoinSql() != null) {
                        joinSql = subTableExportConfig.getJoinSql();
                    } else {
                        joinSql = "LEFT JOIN (SELECT " + columns +
                                " FROM " + tableName + " AS a " +
                                child.getSqlJoins() + ConvertUtil.getString(child.getSqlJoinsExt()) + " WHERE a.del_flag='0' " + subTableExportConfig.getWhere() + " ) AS " + tableAs +
                                " ON " + tableAs + "." + leftQuo + "__parent_id" + rightQuo + " = a.id \n";
                    }
                    subTableJoinSqlList.add(joinSql);

                    // 生成字段sql
                    Pattern pattern = Pattern.compile("AS\\s+\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(columns);
                    List<String> nameList;
                    if (subTableExportConfig.getNameList() != null) {
                        nameList = subTableExportConfig.getNameList();
                    } else {
                        nameList = new ArrayList<>();
                        while (matcher.find()) {
                            nameList.add(matcher.group(1));
                        }
                    }
                    List<String> nameSqlList;
                    if (subTableExportConfig.getNameSqlList() != null) {
                        nameSqlList = subTableExportConfig.getNameSqlList();
                    } else {
                        nameSqlList = new LinkedList<>();
                        for (String name : nameList) {
                            String sql = tableAs + "." + leftQuo + name + rightQuo + " AS \"" + tableAs + "__" + name + "\"";
                            nameSqlList.add(sql);
                        }
                    }
                    fieldSqlList.addAll(nameSqlList);
                    fieldSqlList.add(tableAs + ".parent_id AS \"" + tableAs + ".parent_id\"");
                }
            }

            String sqlColumns = genTable.getSqlColumnsFriendly();
            StringBuilder fieldSql = new StringBuilder(sqlColumns);
            fieldSqlList.forEach(s -> {
                fieldSql.append(",").append(s).append("\n");
            });

            StringBuilder subTableJoins = new StringBuilder(genTable.getSqlJoins() + "\n");
            for (String sql : subTableJoinSqlList) {
                subTableJoins.append(sql);
            }

            String originSqlSort = genTable.getSqlSort();
            if (originSqlSort == null) {
                originSqlSort = "";
            }
            StringBuilder sortSql = new StringBuilder(originSqlSort);
            for (String s : sortSqlList) {
                sortSql.append(",").append(s).append(" ");
            }
            if (StrUtil.isEmpty(originSqlSort) || StrUtil.isBlank(originSqlSort)) {
                if (!sortSqlList.isEmpty()) {
                    sortSql.delete(0, 1);
                }
            }

            genTable.setSqlSort(sortSql.toString());
            genTable.setSqlJoins(subTableJoins.toString());
            genTable.setSqlColumnsFriendly(fieldSql.toString());
        }

        if (exportAsTableColumns) {
            excelExportFieldList = this.getExcelExportFieldList(zformMap);
        } else {
            excelExportFieldList = genTableService.getExcelExportFieldList(genTable);
        }
        zformMap.remove("tableColumns");
        Zform zform = getZformFromMap(zformMap, StringUtil.isNotEmpty(paramLoginName) ? paramLoginName : UserUtil.getCurrentLoginName());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        List<Map<String, Object>> listResult;
        List<Map<String, Object>> dataList = new LinkedList<>();
        List<Supplier<List<Map<String, Object>>>> supplierList = new LinkedList<>();
        Page<Zform> page = null;
        try {
            if (aroundController == null || (!aroundController.selectDataListByAround() && !aroundController.selectDataListBySupplierByAround())) {
                page = findPageMap(new Page<Zform>(
                                zform.getPageParam().getPageNo(),
                                Integer.MAX_VALUE,
                                zform.getPageParam().getOrderBy()),
                        zform,
                        path,
                        StringUtil.isNotEmpty(paramLoginName) ? paramLoginName : UserUtil.getCurrentLoginName(),
                        genTable,
                        traceFlag,
                        parentId, null);
            } else if (aroundController.selectDataListBySupplierByAround()) {
                supplierList = aroundController.selectDataListBySupplier(genTable, zform, parentId, excelExportFieldList);
            } else {
                dataList = aroundController.selectDataList();
            }
        } catch (Exception e) {
            logger.error("查询导出数据失败", e);
            throw new BusinessException("数据库异常");
        }

        if (exportAsTableColumns) {
            if (page != null) {
                List<LinkedHashMap> map = page.getMap();
                dataList = this.parseExportData(map);
            }
            if (aroundController != null) {
                if (aroundController.selectDataListBySupplierByAround()) {
                    return aroundController.customExpDataBySupplier(excelExportFieldList, supplierList);
                }
                return aroundController.customExpData(excelExportFieldList, dataList, filename);
            }
            return exportExcel(filename + ".xlsx", excelExportFieldList, dataList, false, mergeColumnConfig,null);
        }
        Zform[] zformDataAarray = new Zform[page.getMap().size()];

        logger.info("导出excel-线程池开启");
        final CountDownLatch countDownLatch = new CountDownLatch(page.getMap().size()); //
        ExecutorService executor = Executors.newFixedThreadPool(16);
        int k = 0;
        for (LinkedHashMap map : page.getMap()) {
            int finalK = k;
            executor.submit(() -> {
                try {
                    JSONObject jsonObject = JSONHelper.toJSONObject(map);
                    jsonObject.put("formNo", formNo);
                    zformDataAarray[finalK] = getZformFromMap(jsonObject, StringUtil.isNotEmpty(paramLoginName) ? paramLoginName : UserUtil.getCurrentLoginName());
                } catch (Exception e) {
                    logger.error(ExceptionUtil.stacktraceToString(e));
                } finally {
                    countDownLatch.countDown();
                }
            });
            k++;
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
        logger.info("导出excel-线程池关闭");


        page.setList(Arrays.asList(zformDataAarray));
        listResult = exportDataMap(page, zform, genTable, StringUtil.isNotEmpty(paramLoginName) ? paramLoginName : UserUtil.getCurrentLoginName());
        if (aroundController != null) {
            if (aroundController.selectDataListBySupplierByAround()) {
                return aroundController.customExpDataBySupplier(excelExportFieldList, supplierList);
            }
            return aroundController.customExpData(excelExportFieldList, listResult, filename);
        }
       /* String exportTemplatePath = genTableService.getExportFilePathByFormNo(formNo, fileRoot);
        ByteArrayInputStream in = ExcelUtil.LinkedHashListToExcel(listResult,1, exportTemplatePath);*/
        return exportExcel(filename + ".xlsx", excelExportFieldList, listResult, false, mergeColumnConfig,null);
    }


}
