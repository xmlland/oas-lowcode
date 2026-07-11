package com.jeestudio.bpm.service.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.jeestudio.bpm.common.around.AroundDaoI;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.common.BaseEntity;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.gen.GenTableColumnFormItemConfig;
import com.jeestudio.bpm.common.entity.gen.GenTableDeriveConfig;
import com.jeestudio.bpm.common.entity.gen.GenTableExtRuleManyToMany;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.common.enums.QueryTypeEnum;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.ImportParam;
import com.jeestudio.bpm.common.view.system.UserView;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.fastjson.MyDateDeserializer;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.mapper.base.system.UserDao;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.security.storage.SecretHandler;
import com.jeestudio.bpm.service.act.ActService;
import com.jeestudio.bpm.service.act.TaskPermissionService;
import com.jeestudio.bpm.service.act.TaskSettingVersionService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.*;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.enums.DesensitiseTypeEnum;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.base.utils.JSONHelper;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.security.utils.DesensitizedUtil;
import org.flowable.engine.HistoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 动态表单基础服务
 */
public class ZformBaseService extends ActService<ZformDao, Zform> {

    protected static final Logger logger = LoggerFactory.getLogger(ZformBaseService.class);

    @Value("${versionSchema}")
    protected String versionSchema;

    /*@Value("${spring.datasource.dbType}")*/
    protected String dbType = DbTypeUtil.getDbType();

    private static final String[] SOURCE_SQL_DANGEROUS_KEYWORDS = {
            ";", "--", "/*", "*/",
            " insert ", " update ", " delete ", " drop ", " truncate ",
            " alter ", " create ", " execute ", " exec ", " merge ",
            " grant ", " revoke ", " call ", " replace ", " into ",
            " outfile ", " dumpfile "
    };

    @Autowired
    protected ZformDao zformDao;

    @Autowired
    protected GenTableService genTableService;

    @Autowired
    protected OfficeService officeService;

    @Autowired
    @Lazy
    UserService userService;

    @Autowired
    @Lazy
    AreaService areaService;

    @Autowired
    @Lazy
    RoleService roleService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected TaskSettingVersionService taskSettingVersionService;

    @Autowired
    protected TaskPermissionService taskPermissionService;

    @Autowired
    protected SysFileService sysFileService;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected DictDataService dictDataService;

    @Autowired
    DynamicRoutingDataSource dataSource;

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    SecretHandler secretHandler;

    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    DatahouseService datahouseService;


    protected static final String NODE_MARK_CREATE = "create";
    protected static final String NODE_MARK_NOTIFY = "notify";
    protected static final String UNDERLINE = "_";
    protected static final String NODE_MARK_DISTRIBUTE = "distribute";

    protected static final String COUNT_RESULT_MORETHAN9 = "9+";

    protected static final String DBNAME_ORACLE = "oracle";
    protected static final String DBNAME_MYSQL = "mysql";
    protected static final String DBNAME_MSSQL = "mssql";
    protected static final String DBNAME_DM = "dm";
    protected static final String DBNAME_POSTGRESQL = "postgresql";

    protected static final String QUERYTYPE_LIKE = "like";
    protected static final String QUERYTYPE_LEFT_LIKE = "left_like";
    protected static final String QUERYTYPE_RIGHT_LIKE = "right_like";

    protected static final String JAVATYPE_SGTRING = "String";
    protected static final String JAVATYPE_LONG = "Long";
    protected static final String JAVATYPE_INTEGER = "Integer";
    protected static final String JAVATYPE_DOUBLE = "Double";
    protected static final String JAVATYPE_BOOLEAN = "Boolean";
    protected static final String JAVATYPE_BIGDECIMAL = "java.math.BigDecimal";
    protected static final String JAVATYPE_DATE = "java.util.Date";
    protected static final String JAVATYPE_USER = "com.jeestudio.bpm.common.entity.system.User";
    protected static final String JAVATYPE_OFFICE = "com.jeestudio.bpm.common.entity.system.Office";
    protected static final String JAVATYPE_AREA = "com.jeestudio.bpm.common.entity.system.Area";
    protected static final String JAVATYPE_ZFORM = "com.jeestudio.bpm.common.entity.common.Zform";
    protected static final String JAVATYPE_THIS = "This";

    protected static final String PATH_QUERY = "path";

    //树类型，用户，部门，行政区
    public static final String TREE_TYPE_USER = "sys_user";
    public static final String TREE_TYPE_OFFICE = "sys_office";
    public static final String TREE_TYPE_AREA = "sys_area";

    //直辖市
    protected static final List<String> zxCity = new ArrayList<>();

    static {
        zxCity.add("110000");
        zxCity.add("120000");
        zxCity.add("310000");
        zxCity.add("500000");
    }

    /**
     * Rule variables
     */
    protected enum RuleArgs {
        //format
        key, value,
        //scope
        form, content, hand, automatic, extend, operation, formExtend,
        //disabled
        flag, reject;
    }

    /**
     * Get zform and children
     */
    public Zform get(String id, GenTable genTable) throws Exception {
        Zform zform = new Zform();
        zform.setId(id);
        zform.setFormNo(genTable.getName());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + ConvertUtil.getString(genTable.getSqlJoinsExt()));
        }
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        String procInsId = zform.getProcInsId();
        List<Zform> list;
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, this.replaceWithExtSqlPairMap(genTable.getExtSql02(), null));
            list = datahouseService.getList(zform);
        } else {
            list = zformDao.getList(zform);
        }
        zform = list != null && list.size() > 0 ? list.get(0) : zform;
        if (zform == null) {
            //Business data has been deleted, clear process data
            zform = new Zform();
            if (false == StringUtil.isEmpty(procInsId)) {
                zform.setProcInsId(procInsId);
                this.deleteAct(zform);
                zform.setProcInsId(null);
            }
        }
        zform.setFormNo(genTable.getName());
        return this.buildGridselectValue(zform, genTable);
    }

    /**
     * Get zform and children
     */
    public LinkedHashMap getMap(String id, GenTable genTable, String extFlag, String loginName) throws Exception {
        Zform zform = new Zform();
        zform.setId(id);
        zform.setFormNo(genTable.getName());
        zform.setFormNoExt(genTable.getFormNoExt());
        boolean runtimeSourceSqlView = isRuntimeSourceSqlView(genTable);
        if (runtimeSourceSqlView) {
            zform.setTableOrViewName(getRuntimeTableOrViewName(genTable));
        }
        String sqlFriendly = genTable.getSqlColumnsFriendly();
        sqlFriendly += " " + StringUtil.getString(genTable.getSqlColumnsFriendlyExt());

        if (GenTable.EXT_FLAG_VIEW.equals(extFlag) && StringUtil.isNotEmpty(genTable.getExtSql01())) {
            sqlFriendly = genTable.getExtSql01();
        } else if (GenTable.EXT_FLAG_OUTER.equals(extFlag) && StringUtil.isNotEmpty(genTable.getExtSql02())) {
            sqlFriendly = genTable.getExtSql02();
        }
        List<GenTableColumn> columnList = genTable.getColumnList();
        Map<String, GenTableColumn> columnMap = ConvertUtil.listToMap(columnList, GenTableColumn::getName);
        StringBuilder sql = new StringBuilder();
        for (GenTableColumn column : columnList) {
            String encryptPrefix = "encrypt_";
            String remarks = getRemarks(column);
            if (oConvertUtils.getString(remarks).startsWith(encryptPrefix)) {
                //加密字段
                String encryptName = encryptPrefix + column.getName();
                if (columnMap.containsKey(encryptName)) {
                    sql.append(",a.").append(encryptName).append(" as \"").append(encryptName).append("\"");
                }
            }
        }
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, sqlFriendly + sql);
        if (!runtimeSourceSqlView && (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt()))) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        AroundDaoI aroundDao = AroundUtil.getAroundDaoI(genTable);
        if (aroundDao != null) {
            if (aroundDao.isCustomSqlColumnsFriendly()){
                zform.getSqlMap().put("sqlColumnsFriendly", aroundDao.getCustomSqlColumnsFriendly(zform));
            }
            if (aroundDao.isCustomSqlJoins()){
                zform.getSqlMap().put("sqlJoins", aroundDao.getCustomSqlJoins(zform));
            }
        }
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        List<LinkedHashMap> zformMap;
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            sqlFriendly = genTable.getExtSql02();
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, this.replaceWithExtSqlPairMap(sqlFriendly, null));
            zformMap = datahouseService.getMapList(zform);
        } else {
            zformMap = zformDao.getMapList(zform);
        }
        if (zformMap.size() > 0) {
            LinkedHashMap result = zformMap.get(0);
            this.decryptData(result, genTable);
            // 树表处理：如果 parent.id 有值但 parent.name 为空，补全 parent 名称
            if (GenTable.TABLE_TYPE_TREE.equals(genTable.getTableType())) {
                Object parentObj = result.get("parent");
                if (parentObj instanceof Map) {
                    Map<String, Object> parent = (Map<String, Object>) parentObj;
                    String parentId = ConvertUtil.getString(parent.get("id"));
                    String parentName = ConvertUtil.getString(parent.get("name"));
                    if (StringUtil.isNotEmpty(parentId) && StringUtil.isEmpty(parentName)) {
                        // 从查询结果中查找父节点名称（根节点通常在第一条）
                        String nameField = null;
                        for (GenTableColumn col : genTable.getColumnList()) {
                            if ("name".equalsIgnoreCase(col.getName()) || "s01".equalsIgnoreCase(col.getName())) {
                                nameField = col.getName();
                                break;
                            }
                        }
                        if (nameField != null) {
                            // 使用友好查询中的父节点名称字段（如果有）
                            Object friendlyParentName = result.get("parentName");
                            if (friendlyParentName != null) {
                                parent.put("name", ConvertUtil.getString(friendlyParentName));
                            }
                        }
                    }
                }
                //处理树表，向下一级查询数据
                Page<Zform> page = new Page<>(1, Integer.MAX_VALUE, "");
                page = this.findPageMap(page, zform, "", loginName, genTable, "", id, "");
                result.put("children", page.getMap());
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Find zform list
     */
    public List<Zform> findList(Zform zform, GenTable genTable) {
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        return super.findList(zform);
    }

    /**
     * Find zform list by page
     */
    public Page<Zform> findPage(Page<Zform> page, Zform zform, GenTable genTable) {
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        return super.findPage(page, zform);
    }

    /**
     * Find zform map by page
     */
    public Page<Zform> findPageMap(Page<Zform> page, Zform zform, GenTable genTable) {
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, genTable.getSqlColumnsFriendly()
                + " " + StringUtil.getString(genTable.getSqlColumnsFriendlyExt()));
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        return super.findPageMap(page, zform);
    }

    /**
     * Save zform
     *
     * @param zform
     * @param genTable
     */
    public void save(Zform zform, GenTable genTable) throws Exception {
        List<GenTableColumn> columnList = genTable.getColumnList();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(zform);
        for (GenTableColumn genTableColumn : columnList) {
            String rule = genTableColumn.getValidateType();
            //Java对象转化为JSON对象
            String value = jsonObject.getString(genTableColumn.getJavaField());
            if (StringUtil.isNotEmpty(rule) && StringUtil.isNotEmpty(value)) {
                boolean validateBool = ValidatorUtils.validateField(rule, value);
                if (!validateBool) {
                    throw new Exception(genTableColumn.getComments() + "内容格式不正确，" + ValidatorUtils.getErrorMessage(rule) + " ");
                }
            }
        }
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, genTable.getSqlInsert());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERTV, genTable.getSqlInsertV());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, genTable.getSqlUpdate());
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            // datahouse 数据源: 直接调用 DatahouseService 保存
            this.defaultValueHandle(zform, genTable);
            zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
            if ("1".equals(genTable.getIsVersion())) {
                datahouseService.saveWithTrace(zform);
            } else {
                datahouseService.save(zform);
            }
        } else if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            //Tree table, rebuild parent_ids
            String rootParentIds = Global.DEFAULT_ROOT_CODE + ",";
            Zform parent = null;
            if (zform.getParent() != null) {
                parent = this.get(zform.getParent().getId(), genTable);
            }
            if (parent != null && StringUtil.isNotEmpty(parent.getId()) && StringUtil.isNotEmpty(parent.getParentIds())) {
                rootParentIds = parent.getParentIds();
            } else {
                parent = new Zform();
                parent.setId(Global.DEFAULT_ROOT_CODE);
                parent.setParentIds(rootParentIds);
                zform.setParent(parent);
            }
            this.buildParentIdsForChildren(rootParentIds, zform, genTable);
        } else {
            this.superSave(zform, genTable);
        }
    }

    /**
     * Save tree
     *
     * @param zform
     * @param genTable
     */
    @Transactional(readOnly = false)
    public void saveTree(Zform zform, GenTable genTable) {
        this.superSave(zform, genTable);
    }

    /**
     * Save a new zform
     *
     * @param zform
     */
    @Transactional(readOnly = false)
    public void beforeSave(Zform zform, GenTable genTable) {
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, genTable.getSqlInsert());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERTV, genTable.getSqlInsertV());
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        this.superSave(zform, genTable);
    }

    /**
     * Delete a zform and children
     *
     * @param zform
     */
    @Transactional(readOnly = false)
    public void delete(Zform zform, GenTable genTable) throws Exception {
        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            // datahouse 数据源: 多对多级联删除
            for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
                StringBuilder sql = new StringBuilder();
                sql.append("delete from ").append(manyToMany.getRelTable())
                   .append(" where ").append(manyToMany.getRelColumn())
                   .append(" = #{param.id}");
                Map<String, Object> param = new HashMap<>();
                param.put("id", zform.getId());
                datahouseService.deleteSqlParm(sql.toString(), param);
            }
            zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
            datahouseService.delete(zform);

            // 级联子表删除
            if (genTable.getChildList() != null && genTable.getChildList().size() > 0) {
                for (GenTable child : genTable.getChildList()) {
                    Zform childZform = new Zform();
                    childZform.setTableOrViewName(child.getName());
                    Zform parent = new Zform();
                    parent.setId(zform.getId());
                    childZform.setParent(parent);
                    childZform.getSqlMap().put("fk", child.getParentTableFk());
                    datahouseService.deleteChildren(childZform);
                }
            }
            return;  // datahouse 分支处理完后直接返回
        }
        //级联删除多对多关系表
        for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(manyToMany.getRelTable());
            sql.append(" where ");
            sql.append(manyToMany.getRelColumn());
            sql.append(" = '");
            sql.append(zform.getId());
            sql.append("' ");
            zformDao.deleteSql(sql.toString());
        }
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        super.delete(zform);
        List<GenTable> subGenTableList = genTableService.findList(new GenTable(genTable));
        subGenTableList = subGenTableList.stream()
                .filter(item -> !item.getName().toLowerCase(Locale.ROOT).endsWith("@view"))
                .collect(Collectors.toList());
        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            //树表存在右表，只允许删除一级，且级联删除右表数据
            if (subGenTableList.size() > 0) {
                if (zformDao.hasChildren(zform)) {
                    //有子表——左树右表时，尝试删除多级树，提示以下错误信息；原因：向下级联多级删除右表关联的数据开销不可预知
                    throw new RuntimeException("有子表时，不持支删除多级" + genTable.getComments());
                } else {
                    //有子表，只删除树的叶子节点时，级联删除子表数据，这里的子表是左树右表
                    for (int i = 0; i < subGenTableList.size(); i++) {
                        GenTable subGenTable = genTableService.getGenTableWithDefination(subGenTableList.get(i).getName());
                        if (subGenTable.getName().toLowerCase().endsWith(GenUtil.VIEW)) {
                            continue;
                        } else {
                            Zform subZform = new Zform(zform);
                            subZform.setFormNo(subGenTable.getName());
                            subZform.setFk(subGenTable.getParentTableFk());
                            subZform.getSqlMap().put("pkColumnName", subGenTable.getPkColumnName());
                            List<Zform> subList = zformDao.findChildrenForDelete(subZform);
                            for (Zform obj : subList) {
                                obj.setFormNo(subGenTable.getName());
                                this.deleteCascade(obj, subGenTable);
                            }
                        }
                    }
                }
            }
            zformDao.deleteChildrenForTree(zform);
            //级联删除树表下级时，也同时删除下级数据多对多关系表数据
            for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
                // 验证表名和列名是否为合法SQL标识符
                SqlInjectionUtil.validateIdentifier(manyToMany.getRelTable());
                SqlInjectionUtil.validateIdentifier(manyToMany.getRelColumn());
                SqlInjectionUtil.validateIdentifier(genTable.getPkColumnName());
                SqlInjectionUtil.validateIdentifier(genTable.getName());
                
                // 使用参数化查询，避免 SQL 注入
                StringBuilder sql = new StringBuilder();
                sql.append("delete from ");
                sql.append(manyToMany.getRelTable());
                sql.append(" where ");
                sql.append(manyToMany.getRelColumn());
                sql.append(" in (");
                sql.append(" select " + genTable.getPkColumnName() + " from " + genTable.getName() + " where parent_ids like #{param.parentIdsPattern}");
                sql.append(") ");
                
                Map<String, Object> param = new java.util.HashMap<>();
                param.put("parentIdsPattern", zform.getParentIds() + zform.getId() + ",%");
                zformDao.deleteSqlParm(sql.toString(), param);
            }
        } else {
            //List<GenTable> subGenTableList = genTableService.findList(new GenTable(genTable));
            for (int i = 0; i < subGenTableList.size(); i++) {
                GenTable subGenTable = genTableService.getGenTableWithDefination(subGenTableList.get(i).getName());
                if (subGenTable.getName().toLowerCase().endsWith(GenUtil.VIEW)) {
                    continue;
                } else {
                    Zform subZform = new Zform(zform);
                    subZform.setFormNo(subGenTable.getName());
                    subZform.setFk(subGenTable.getParentTableFk());
                    subZform.getSqlMap().put("pkColumnName", subGenTable.getPkColumnName());
                    List<Zform> subList = zformDao.findChildrenForDelete(subZform);
                    this.deleteCascade(subList, subGenTable);
                    /*for (Zform obj : subList) {
                        obj.setFormNo(subGenTable.getName());
                        this.deleteCascade(obj, subGenTable);
                    }*/
                }
            }
        }
        super.deleteAct(zform);
    }

    /**
     * 删除子级
     * @param zformList 待删除的子级
     * @param genTable 子表的genTable
     */
    public void deleteCascade(List<Zform> zformList, GenTable genTable) {
        // 记录删除级联操作的信息，包括表名和待删除记录的数量
        logger.info("deleteCascade:{},size:{}", genTable.getName(), zformList.size());

        // 如果待删除的记录列表为空，则直接返回，不执行后续操作
        if (zformList == null || zformList.size() == 0) {
            return;
        }

        // 提取待删除记录的ID列表，用于后续的级联删除操作
        List<String> idList = zformList.stream().map(Zform::getId).collect(Collectors.toList());

        // 级联删除多对多关系表
        for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(manyToMany.getRelTable());
            sql.append(" where ");
            sql.append(manyToMany.getRelColumn());
            sql.append(" in ");
            sql.append("('").append(StringUtils.join(idList, "','")).append("')");
            zformDao.deleteSql(sql.toString());
        }

        // 调用父类的批量删除方法，执行当前表的批量删除操作
        super.batchDelete(genTable.getName(), zformList);

        // 如果当前表是树形结构，则执行树形结构的级联删除操作
        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            for (Zform zform : zformList) {
                zform.setFormNo(genTable.getName());
                zformDao.deleteChildrenForTree(zform);
            }
        } else {
            // 获取当前表的所有子表列表，并过滤掉视图类型的子表
            List<GenTable> subGenTableList = genTableService.findList(new GenTable(genTable));
            subGenTableList = subGenTableList.stream()
                    .filter(item -> !item.getName().toLowerCase(Locale.ROOT).endsWith("@view"))
                    .collect(Collectors.toList());

            // 遍历所有子表，执行级联删除操作
            for (int i = 0; i < subGenTableList.size(); i++) {
                GenTable subGenTable = genTableService.getGenTableWithDefination(subGenTableList.get(i).getName());
                Zform subZform = new Zform(zformList.get(0));
                subZform.setFormNo(subGenTable.getName());
                subZform.setFk(subGenTable.getParentTableFk());
                subZform.getSqlMap().put("pkColumnName", subGenTable.getPkColumnName());
                List<Zform> subList = zformDao.findChildrenForDeleteByList(subZform, idList);
                this.deleteCascade(subList, subGenTable);
            }
        }
    }

    public void deleteCascade(Zform zform, GenTable genTable) {
        //级联删除多对多关系表
        for (GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(manyToMany.getRelTable());
            sql.append(" where ");
            sql.append(manyToMany.getRelColumn());
            sql.append(" = '");
            sql.append(zform.getId());
            sql.append("' ");
            zformDao.deleteSql(sql.toString());
        }
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        super.delete(zform);
        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            zformDao.deleteChildrenForTree(zform);
        } else {
            List<GenTable> subGenTableList = genTableService.findList(new GenTable(genTable));
            subGenTableList = subGenTableList.stream()
                    .filter(item -> !item.getName().toLowerCase(Locale.ROOT).endsWith("@view"))
                    .collect(Collectors.toList());
            for (int i = 0; i < subGenTableList.size(); i++) {
                GenTable subGenTable = genTableService.getGenTableWithDefination(subGenTableList.get(i).getName());
                Zform subZform = new Zform(zform);
                subZform.setFormNo(subGenTable.getName());
                subZform.setFk(subGenTable.getParentTableFk());
                subZform.getSqlMap().put("pkColumnName", subGenTable.getPkColumnName());
                List<Zform> subList = zformDao.findChildrenForDelete(subZform);
                for (Zform obj : subList) {
                    obj.setFormNo(subGenTable.getName());
                    this.deleteCascade(obj, subGenTable);
                }
            }
        }
    }

    /**
     * Delete zform by ids
     *
     * @param ids
     * @param formNo
     * @param genTable
     */
    public void deleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {
        String idArray[] = ids.split(",");
        for (String id : idArray) {
            Zform zform = this.get(id, genTable);
            zform.setUpdateDate(new Date());
            this.delete(zform, genTable);
            if (Global.YES.equals(genTable.getIsVersion())) {
                zform.setDelFlag(Global.YES);
                zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERTV, genTable.getSqlInsertV());
                if (StringUtil.isBlank(versionSchema)) {
                    super.saveV(zform);
                } else {
                    zform.setVersionSchema(versionSchema);
                    super.saveVersionSchema(zform);
                    zform.setVersionSchema(null);
                }
            }
            // 根据配置自动禁用 sys_office 及 sys_user。
            this.autoDisableOrgAndUser(zform, loginName, genTable);

            // 根据配置自动禁用 sys_user。
            this.autoDisableUser(zform, loginName, genTable);
        }
    }
    /**
     * 根据配置自动禁用sys_office 及 sys_user
     * @param zform zform
     * @param genTable genTable
     */
    public void autoDisableOrgAndUser(Zform zform, String loginName, GenTable genTable) {
        List<GenTableColumn> columnList = genTable.getColumnList();
        List<GenTableColumn> offList = columnList.stream().filter(k -> "officeselectTree".equals(k.getShowType())).collect(Collectors.toList());
        if (offList.size() > 0) {
            for (GenTableColumn k : offList) {
                GenTableColumnFormItemConfig formItemConfig = k.getGenTableColumnFormItemConfig();
                if (formItemConfig != null && Global.YES.equals(formItemConfig.getCreateSysOffice())) {
                    logger.info("根据配置自动禁用sys_office 及 sys_user开始,{},{}", genTable.getName(), k.getName());
                    try {
                        Office Office = (Office) ReflectUtils.getValue(zform, k.getSimpleJavaField());
                        Office officeDb;
                        if (Office != null) {
                            List<User> userList = userService.findUserListByParentId(Office.getParentId());
                            for (User user : userList) {
                                user.setUseable(Global.NO);
                                user.setLoginFlag(Global.NO);
                                userService.saveUser(user, loginName);
                            }
                            logger.info("根据配置禁用sys_user成功,{}个用户", userList.size());
                            officeDb = officeService.get(Office.getId());
                            officeDb.setUseable(Global.NO);
                            officeService.save(officeDb);
                            logger.info("根据配置禁用sys_office,{},{}", Office.getId(), officeDb.getName());
                        }

                        logger.info("根据配置自动禁用sys_office 及 sys_user成功,{},{}", genTable.getName(), k.getName());
                    } catch (Exception e) {
                        logger.error("根据配置自动禁用sys_office 及 sys_user失败,{}", ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException("自动禁用机构及用户操作失败");
                    }
                    return;
                }
            }
        }
    }
    /**
     * 根据配置自动禁用sys_user
     * @param zform zform
     * @param genTable genTable
     */
    public void autoDisableUser(Zform zform, String loginName, GenTable genTable) {
        List<GenTableColumn> columnList = genTable.getColumnList();
        List<GenTableColumn> userList = columnList.stream().filter(k -> "treeselectRedio".equals(k.getShowType())).collect(Collectors.toList());
        if (userList.size() > 0) {
            for (GenTableColumn k : userList) {
                GenTableColumnFormItemConfig formItemConfig = k.getGenTableColumnFormItemConfig();
                if (formItemConfig != null && Global.YES.equals(formItemConfig.getCreateSysUser())) {
                    logger.info("根据配置自动禁用sys_user开始,{},{}", genTable.getName(), k.getName());
                    try {
                        User user = (User) ReflectUtils.getValue(zform, k.getSimpleJavaField());
                        User userDb;
                        if (user != null) {
                            userDb = userService.get(user.getId());
                            // 如果可以查到用户则禁用用户，否则不做处理
                            if (userDb != null) {
                                if (Global.YES.equals(userDb.getUseable())){
                                    userService.disableUser(userDb.getLoginName());
                                    logger.info("根据配置禁用sys_office,loginName={}", userDb.getLoginName());
                                }else{
                                    logger.info("根据配置禁用sys_office,loginName={}已禁用", userDb.getLoginName());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("根据配置自动禁用sys_user失败,{}", ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException("自动禁用用户操作失败");
                    }
                    return;
                }
            }
        }
    }


    private boolean isFileSecLevelValid(Zform zform, GenTable genTable) throws NoSuchFieldException, IllegalAccessException {
        boolean result = true;
        if (StringUtil.isNotEmpty(zform.getSecLevel())) {
            int secLevel = Integer.parseInt(zform.getSecLevel());
            List<String> fileColumnList = Lists.newArrayList();
            String fileColumns = ",";
            for (GenTableColumn column : genTable.getColumnList()) {
                if ("fileuploadsec".equals(column.getShowType())) {
                    Field field = zform.getClass().getDeclaredField(column.getJavaField());
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(zform);
                        String groupId = "";
                        if (object != null) {
                            groupId = object.toString();
                        }
                        if (StringUtil.isNotEmpty(groupId)) {
                            List<SysFile> fileList = sysFileService.getFileListByGroupId(groupId);
                            for (SysFile sysFile : fileList) {
                                if (secLevel < Integer.parseInt(sysFile.getSecretLevel())) {
                                    result = false;
                                    break;
                                }
                            }
                            if (false == result) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void saveChildren(Zform zform, String loginName, GenTable genTable) throws Exception {
        zform.getAllChildrenLists().stream()
                .filter(childrenList -> childrenList != null && childrenList.size() > 0)
                .forEach(childrenList -> {
                    try {
                        this.saveChildrenOnce(zform, childrenList, loginName);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            this.saveChildrenOnce(zform, zform.getChildren(), loginName);
        }
    }

    private void saveChildrenOnce(Zform zform, List<Zform> childList, String loginName) throws Exception {
        GenTable theGenTable = null;
        //判断childList是否为nul
        if (childList == null || childList.size() == 0 ) {
            return;
        }
        theGenTable = genTableService.getGenTableWithDefination(childList.get(0).getFormNo());
        if (zform.getId() == null && StringUtil.isEmpty(zform.getPreId())) {
            zform.setPreId(IdGen.uuid());
        }
        Zform parent = new Zform(zform.getId());//先使用主表的id作为parentId
        if (StringUtil.isNotEmpty(zform.getPreId())) {
            parent = new Zform(zform.getPreId());
        }
        if (Global.YES.equals(theGenTable.getIsVersion()) || theGenTable.isHasChildren()) {
            for (Zform obj : childList) {
                if (theGenTable == null) {
                    theGenTable = genTableService.getGenTableWithDefination(obj.getFormNo());
                }
                obj.setParent(parent);
                if (theGenTable.isHasChildren()) {
                    //向下查询子表保存
                    this.saveChildren(obj, loginName, theGenTable);
                }
                if (Global.YES.equals(obj.getDelFlag()) && StringUtil.isNotEmpty(obj.getId())) {
                    //del
                    this.delete(obj, theGenTable);
                } else {
                    this.save(obj, theGenTable);
                }
            }
        }else{
            this.superBatchSave(childList, theGenTable, loginName, parent);
        }

    }

    /**
     * Save zform with act
     *
     * @param businessKey
     * @param zform
     * @param loginName
     */
    public String saveAct(String businessKey, Zform zform, String loginName, GenTable genTable) throws Exception {
        ActCacheContext.init();
        try {
        zform.setEdocType(genTable.getProcessDefinitionCategory());
        String actStatus = "";//作出改动
        if (StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
            //Non workflow form, call save
            this.save(zform, genTable);
        } else {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, genTable.getSqlInsert());
            zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERTV, genTable.getSqlInsertV());
            zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, genTable.getSqlUpdate());

            Map<String, Object> vars = Maps.newHashMap();
            String procDefKey = null;
            if (false == StringUtil.isEmpty(zform.getProcInsId())) {
                procDefKey = getProcIns(zform.getProcInsId()).getProcessDefinitionKey();
            } else {
                procDefKey = zform.getAct().getProcDefKey();
            }
            super.saveAct(zform, genTable.getComments(), procDefKey, businessKey + ":" + genTable.getModule(), vars, loginName);
            if (StringUtil.isNotBlank(zform.getProcInsId())) {
                Task activeTask = this.getActiveTask(zform.getProcInsId());
                if (activeTask != null) {
                    zform.getAct().setProcDefId(this.getProcIns(zform.getProcInsId()).getProcessDefinitionId());
                    zform.getAct().setTaskDefKey(activeTask.getTaskDefinitionKey());
                    this.setRuleArgs(zform, loginName);//
                    if (zform.getRuleArgs().get("form") != null) {
                        String taskStatusValue = zform.getRuleArgs().get("formExtend").get("taskStatus");
                        if (StringUtil.isNotBlank(taskStatusValue)) {
                            //To save the status of process node settings, you need to add "taskstatus" in the "form" rule variable in the process node.
                            // When you reach this node, the status value of business data
                            zform.setStatus(taskStatusValue);
                            this.save(zform);
                            actStatus = taskStatusValue;
                        }
                    }
                } else {
                    Map<String, String> paramMap = new Gson().fromJson(Encodes.unescapeHtml(zform.getAct().getParam()), Map.class);
                    String type = paramMap.get("type");
                    if (type.equalsIgnoreCase("SAVEANDTERMINATE")) {
                        //Terminate 00
                        zform.setStatus(genTable.getStatusBreak());
                        zform.setProcInsId(null);
                        actStatus = genTable.getStatusBreak();
                    } else {
                        //There are no active tasks in the current process, which is equivalent to the end of all tasks.
                        // 9 or 99 should be changed to the corresponding status of "finish"
                        zform.setStatus(genTable.getStatusEnd());
                        actStatus = genTable.getStatusEnd();
                    }
                    this.save(zform);
                }
            } else {
                //No process has been entered. Save the status corresponding to "staging".
                // Here 0 or 10 should be changed to the status corresponding to "staging"
                zform.setStatus(genTable.getStatusStart());
                actStatus = genTable.getStatusStart();
                this.save(zform);
            }
        }
        //检查并保存子表数据
        if (genTable.isHasChildren()
                || (GenTable.TABLE_TYPE_TREE.equals(genTable.getTableType())
                && zform.getChildren() != null
                && zform.getChildren().size() > 0)) {
            this.saveChildren(zform, loginName, genTable);
        }
        return actStatus;
        } finally {
            ActCacheContext.destroy();
        }
    }

    /**
     * Get user list when the process starting
     *
     * @param zform
     * @param loginName
     * @return user list when the process starting
     */
    public LinkedHashMap<String, Object> getStartingUserList(Zform zform, String loginName) {
        String procDefKey = null;
        if (false == StringUtil.isEmpty(zform.getProcInsId())) {
            procDefKey = getProcIns(zform.getProcInsId()).getProcessDefinitionKey();
        } else {
            procDefKey = zform.getAct().getProcDefKey();
        }
        zform.getAct().setProcDefKey(procDefKey);
        return super.getStartingUserList(zform, loginName);
    }

    /**
     * Get target user list when submitting a process
     *
     * @param zform
     * @param loginName
     * @return target user list when submitting a process
     */
    public LinkedHashMap<String, Object> getTargetUserList(Zform zform, String loginName) {
        String procDefKey = null;
        if (false == StringUtil.isEmpty(zform.getProcInsId())) {
            procDefKey = getProcIns(zform.getProcInsId()).getProcessDefinitionKey();
        } else {
            procDefKey = zform.getAct().getProcDefKey();
        }
        if (StringUtil.isEmpty(zform.getId()) || StringUtil.isEmpty(zform.getProcInsId())) {
            zform.getAct().setProcDefKey(procDefKey);
            return super.getStartingUserList(zform, loginName);
        } else {
            zform.getAct().setProcDefKey(procDefKey);
            return super.getTargetUserList(zform, loginName);
        }
    }

    /**
     * Read the corresponding list through to do, being done, done, to be sent and sent by page
     *
     * @param page
     * @param zform
     * @param path
     * @param loginName
     * @param genTable
     * @param traceFlag while the table tpye is tree table, not empty for trace all children
     * @return zform list page
     */
    public Page<Zform> findPage(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId) throws Exception {
        if (StringUtil.isEmpty(path)) path = PATH_QUERY;
        String processDefinitionCategory = genTable.getProcessDefinitionCategory();
        String sqlColumns = zform.getSqlMap().get(GenTable.SQLMAP_SQLCOLUMNS);
        String sqlJoins = zform.getSqlMap().get(GenTable.SQLMAP_SQLJOINS);
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, (sqlColumns == null) ? genTable.getSqlColumns() : genTable.getSqlColumns() + sqlColumns);
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            String baseJoins = StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt());
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, (sqlJoins == null) ? baseJoins : baseJoins + sqlJoins);
        }
        if (StringUtil.isEmpty(page.getOrderBy()) && StringUtil.isNotEmpty(genTable.getSqlSort())) {
            page.setOrderBy(genTable.getSqlSort());
        } else if (StringUtil.isEmpty(page.getOrderBy()) && genTable.getSqlColumns().indexOf("a.sort AS \"sort\"") > -1) {
            page.setOrderBy(" a.sort asc ");
        }
        User currentUser = UserUtil.getByLoginName(loginName);
        //this.setSqlMap(zform, genTable, currentUser);
        if (StringUtil.isNotEmpty(parentId)) {
            String querySql = " AND a.parent_id = '" + parentId + "' ";
            if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                String dsf = zform.getSqlMap().get("dsf");
                dsf = dsf == null ? "" : dsf;
                dsf += querySql;
                // 安全加固：校验dsf，防止SQL注入
                dsf = SqlInjectionUtil.validateDsf(dsf);
                zform.getSqlMap().put("dsf", dsf);
            } else {
                // 安全加固：校验dsf，防止SQL注入
                zform.getSqlMap().put("dsf", SqlInjectionUtil.validateDsf(querySql));
            }
        }

        if (StringUtil.isEmpty(processDefinitionCategory) || PATH_QUERY.equals(path)) {
            //return findPage(page, zform);
            String querySql;
            if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                page.setPageSize(Integer.MAX_VALUE);

                String parentIdRes = "0";
                String parentIdsLike = null;


                if (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) {
                    parentIdRes = zform.getParent().getId();
                }


                if (StringUtil.isNotEmpty(traceFlag)) {
                    Zform theZform = this.get(zform.getId(), genTable);
                    String parentIds = "0,";

                    if (theZform != null && StringUtil.isNotEmpty(theZform.getId()) && StringUtil.isNotEmpty(theZform.getParentIds())) {
                        parentIds = theZform.getParentIds();
                    }

                    parentIdsLike = parentIds + "%";

                    // 防止 SQL 注入
                    querySql = " AND (a.parent_id = #{sqlMap.parentId} OR a.parent_ids LIKE #{sqlMap.parentIdsLike})";

                } else {
                    // 防止 SQL 注入
                    querySql = " AND a.parent_id = #{sqlMap.parentId}";
                }

                Map<String, String> sqlMap = zform.getSqlMap();
                if (sqlMap == null) {
                    sqlMap = new HashMap<>();
                    zform.setSqlMap(sqlMap);
                }
                String dsf = sqlMap.getOrDefault("dsf", "");
                sqlMap.put("dsf", dsf + querySql);
                sqlMap.put("parentId", parentIdRes);
                sqlMap.put("parentIdsLike", parentIdsLike);

                Page<Zform> pageResult = buildGridselectValues(findPage(page, zform), genTable);
                return pageResult;

            } else if (genTable.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)) {
                if (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId()) && false == "0".equals(zform.getParent().getId())) {
                    parentId = zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())
                            ? zform.getParent().getId()
                            : "0";

                    String parentIdsLike = "%," + parentId + ",%";

                    querySql = " AND (b.id = #{sqlMap.parentIdEq} OR b.parent_ids LIKE #{sqlMap.parentIdsLike})";

                    Map<String, String> sqlMap = zform.getSqlMap();
                    if (sqlMap != null && sqlMap.size() > 0) {
                        String dsf = sqlMap.get("dsf");
                        dsf = dsf == null ? "" : dsf;
                        dsf += querySql;
                        sqlMap.put("dsf", dsf);
                    } else {
                        sqlMap.put("dsf", querySql);
                    }

                    sqlMap.put("parentIdEq", parentId);
                    sqlMap.put("parentIdsLike", parentIdsLike);

                }
                return buildGridselectValues(findPage(page, zform), genTable);
            } else {
                return buildGridselectValues(findPage(page, zform), genTable);
            }
        } else {
            if (zform != null && zform.getCreateTimeEnd() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(zform.getCreateTimeEnd());
                calendar.add(Calendar.HOUR, 24);
                calendar.add(Calendar.MILLISECOND, -1);
                zform.setCreateTimeEnd(calendar.getTime());
            }
            //Unsent
            if (super.isUnsent(path)) {
                zform.setPage(page);
                if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    //dsf += " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    dsf += " AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    //zform.getSqlMap().put("dsf", " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                    zform.getSqlMap().put("dsf", " AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                }
                page.setList(dao.findList(zform));
            }
            //To do, doing, done, sent
            else {
                Map<String, Act> processInstanceIdMap = super.getProcessInstanceIdList(processDefinitionCategory, path, loginName, zform.getUserTaskKey());
                if (processInstanceIdMap.size() > 0) {
                    zform.setPage(page);
                    zform.setProcInsIdList(new ArrayList<>(processInstanceIdMap.keySet()));
                    page.setList(dao.findListByProc(zform));
                } else {
                    zform.setPage(page);
                    List<Zform> list = Lists.newArrayList();
                    page.setList(list);
                }
            }
            return buildGridselectValues(page, genTable);
        }
    }

    public Page<Zform> findPageMapV(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag,boolean skipWrapperSet) throws Exception {
        String sqlFriendly = genTable.getSqlColumnsFriendly();
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, sqlFriendly + " ,a.vdate ");
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        page.setOrderBy(" a.vdate desc ");
        if (zform.getQueryWrapper() == null) {
            throw new RuntimeException("queryWrapper is null");
        }
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        if(!skipWrapperSet){
            queryWrapper.eq("a.id", zform.getId());
            if (StringUtil.isNotEmpty(parentId)) {
                //String querySql = " AND a.parent_id = '" + parentId + "' ";
                queryWrapper.eq("a.parent_id", parentId);
            }
        }
        page.setPageSize(Integer.MAX_VALUE);
        if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
            //" AND a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' ";
            String zformParentId = (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0";
            if (StringUtil.isNotEmpty(traceFlag)) {
                Zform theZform = this.get(zform.getId(), genTable);
                String parentIds = "0,";
                if (theZform != null && StringUtil.isNotEmpty(theZform.getId()) && StringUtil.isNotEmpty(theZform.getParentIds())) {
                    parentIds = theZform.getParentIds()+",";
                }
                String finalParentIds = parentIds;
                queryWrapper.and(w -> w.eq("a.parent_id", zformParentId).or().likeRight("a.parent_ids", finalParentIds));
                //querySql = " AND (a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' OR a.parent_ids LIKE '" + parentIds + "%') ";
            } else if (StringUtil.isNotEmpty(parentId)) {
                queryWrapper.eq("a.parent_id", parentId);
                //querySql = " AND a.parent_id = '" + parentId + "' ";
            } else {
                queryWrapper.eq("a.parent_id", zformParentId);
                //querySql = " AND a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' ";
            }
               /* if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    dsf += querySql;
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    zform.getSqlMap().put("dsf", querySql);
                }*/
            Page<Zform> pageResult = findPageMap(page, zform);
            return pageResult;
        } else if (genTable.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)) {
            if (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId()) && false == "0".equals(zform.getParent().getId())) {
                //querySql = " AND (b.id = '" + zform.getParent().getId() + "' OR b.parent_ids LIKE '%," + zform.getParent().getId() + ",%' ) ";
                queryWrapper.and(w -> w.eq("b.id", zform.getParent().getId()).or().like("b.parent_ids", ","+zform.getParent().getId()+","));
                    /*if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                        String dsf = zform.getSqlMap().get("dsf");
                        dsf = dsf == null ? "" : dsf;
                        dsf += querySql;
                        zform.getSqlMap().put("dsf", dsf);
                    } else {
                        zform.getSqlMap().put("dsf", querySql);
                    }*/
            }
            return findPageMap(page, zform, true);
        } else {
            return findPageMap(page, zform, true);
        }
    }

    public Page<Zform> findPageMapV(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) throws Exception {
        return findPageMapV(page,zform,path,loginName,genTable,traceFlag,parentId,extFlag,false);
    }

    public Page<Zform> findPageMap(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) throws Exception {
        zform.setFormNoExt(genTable.getFormNoExt());
        boolean runtimeSourceSqlView = isRuntimeSourceSqlView(genTable);
        if (runtimeSourceSqlView) {
            zform.setTableOrViewName(getRuntimeTableOrViewName(genTable));
        }
        if (StringUtil.isEmpty(path)) path = PATH_QUERY;
        String processDefinitionCategory = genTable.getProcessDefinitionCategory();
        String sqlFriendly = genTable.getSqlColumnsFriendly();
        sqlFriendly += " " + StringUtil.getString(genTable.getSqlColumnsFriendlyExt());

        if (GenTable.EXT_FLAG_VIEW.equals(extFlag) && StringUtil.isNotEmpty(genTable.getExtSql01())) {
            sqlFriendly = genTable.getExtSql01();
        } else if (GenTable.EXT_FLAG_OUTER.equals(extFlag) && StringUtil.isNotEmpty(genTable.getExtSql02())) {
            sqlFriendly = genTable.getExtSql02();
        }
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, this.replaceWithExtSqlPairMap(sqlFriendly, zform.getPageParam() != null ? zform.getPageParam().getExtSqlPairMap() : null));
        if (!runtimeSourceSqlView && (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt()))) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, this.replaceWithExtSqlPairMap(StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()), zform.getPageParam() != null ? zform.getPageParam().getExtSqlPairMap() : null));
        }
        if (StringUtil.isEmpty(page.getOrderBy()) && StringUtil.isNotEmpty(genTable.getSqlSort())) {
            page.setOrderBy(genTable.getSqlSort());
        } else if (StringUtil.isEmpty(page.getOrderBy()) && genTable.getSqlColumns().indexOf("a.sort AS \"sort\"") > -1) {
            page.setOrderBy(" a.sort asc ");
        }
        User currentUser = UserUtil.getByLoginName(loginName);
        if (zform.getQueryWrapper() == null) {
            throw new RuntimeException("queryWrapper is null");
        }
        //this.setSqlMap(zform, genTable, currentUser);
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        if (StringUtil.isNotEmpty(parentId)) {
            //String querySql = " AND a.parent_id = '" + parentId + "' ";
            queryWrapper.eq("a.parent_id", parentId);
        }

        if (StringUtil.isEmpty(processDefinitionCategory) || PATH_QUERY.equals(path)) {
            //return findPage(page, zform);
            String querySql;
            if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                if ("sys_dictionary".equals(genTable.getName())) {
                    if (zform.getParent()==null||!"data-params".equals(zform.getParent().getId())){
                        page.setPageSize(Integer.MAX_VALUE);
                    }
                }else{
                    page.setPageSize(Integer.MAX_VALUE);
                }
                //" AND a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' ";
                String zformParentId = (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0";
                if (StringUtil.isNotEmpty(traceFlag)) {
                    Zform theZform = this.get(zform.getId(), genTable);
                    String parentIds = "0,";
                    if (theZform != null && StringUtil.isNotEmpty(theZform.getId()) && StringUtil.isNotEmpty(theZform.getParentIds())) {
                        parentIds = theZform.getParentIds()+",";
                    }
                    String finalParentIds = parentIds;
                    // 兼容说明：GBase 的 text 类型字段在 like 查询时需要转换为 char 类型。
                    if (DbType.gbase.name().equals(DbTypeUtil.getDbType())) {
                        queryWrapper.and(w -> w.eq("a.parent_id", zformParentId).or().likeRight("to_char(a.parent_ids)", finalParentIds));
                    } else {
                        queryWrapper.and(w -> w.eq("a.parent_id", zformParentId).or().likeRight("a.parent_ids", finalParentIds));
                    }
                    //querySql = " AND (a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' OR a.parent_ids LIKE '" + parentIds + "%') ";
                } else if (StringUtil.isNotEmpty(parentId)) {
                    queryWrapper.eq("a.parent_id", parentId);
                    //querySql = " AND a.parent_id = '" + parentId + "' ";
                } else {
                    queryWrapper.eq("a.parent_id", zformParentId);
                    //querySql = " AND a.parent_id = '" + ((zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId())) ? zform.getParent().getId() : "0") + "' ";
                }
               /* if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    dsf += querySql;
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    zform.getSqlMap().put("dsf", querySql);
                }*/
                Page<Zform> pageResult = findPageMap(page, zform);
                return pageResult;
            } else if (genTable.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)) {
                if (zform.getParent() != null && StringUtil.isNotEmpty(zform.getParent().getId()) && false == "0".equals(zform.getParent().getId())) {
                    //querySql = " AND (b.id = '" + zform.getParent().getId() + "' OR b.parent_ids LIKE '%," + zform.getParent().getId() + ",%' ) ";
                    // 兼容说明：GBase 的 text 类型字段在 like 查询时需要转换为 char 类型。
                    if (DbType.gbase.name().equals(DbTypeUtil.getDbType())) {
                        queryWrapper.and(w -> w.eq("b.id", zform.getParent().getId()).or().like("to_char(b.parent_ids)", "," + zform.getParent().getId() + ","));
                    } else {
                        queryWrapper.and(w -> w.eq("b.id", zform.getParent().getId()).or().like("b.parent_ids", "," + zform.getParent().getId() + ","));
                    }
                    /*if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                        String dsf = zform.getSqlMap().get("dsf");
                        dsf = dsf == null ? "" : dsf;
                        dsf += querySql;
                        zform.getSqlMap().put("dsf", dsf);
                    } else {
                        zform.getSqlMap().put("dsf", querySql);
                    }*/
                }
                return findPageMap(page, zform);
            } else {
                // 增加从数仓查询数据潜规则
                // Page<Zform> pageResult = findPageMap(page, zform);
                Page<Zform> pageResult = null;
                if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
                    sqlFriendly = genTable.getExtSql02();
                    zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, this.replaceWithExtSqlPairMap(sqlFriendly, zform.getPageParam() != null ? zform.getPageParam().getExtSqlPairMap() : null));
                    pageResult = datahouseService.findPageMap(page, zform);
                } else {
                    pageResult = findPageMap(page, zform, runtimeSourceSqlView ? getRuntimeTableOrViewName(genTable) : genTable.getName());
                }
                return pageResult;
            }
        } else {
            if (zform != null && zform.getCreateTimeEnd() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(zform.getCreateTimeEnd());
                calendar.add(Calendar.HOUR, 24);
                calendar.add(Calendar.MILLISECOND, -1);
                zform.setCreateTimeEnd(calendar.getTime());
            }
            //Unsent
            if (super.isUnsent(path)) {
                zform.setPage(page);
                //queryWrapper.eq("a.create_by", currentUser.getId());
                queryWrapper.and(w -> w.isNull("a.proc_ins_id").or().eq("a.proc_ins_id", ""));
                /*if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    dsf += " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    zform.getSqlMap().put("dsf", " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                }*/
                IPage<Zform> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), page.getPageSize());
                zform.getSqlMap().put("orderBy", page.getOrderBy());
                IPage<LinkedHashMap> listMap = dao.findListMap(iPage, zform, zform.getTableOrViewName(), zform.getQueryWrapper(), zform.getSqlMap());
                page.setMap(listMap.getRecords());
                page.setCount(listMap.getTotal());
            }
            //To do, doing, done, sent
            else {
                Map<String, Act> processInstanceIdMap = super.getProcessInstanceIdList(processDefinitionCategory, path, loginName, zform.getUserTaskKey());
                if (processInstanceIdMap.size() > 0) {
                    zform.setPage(page);
                    zform.setProcInsIdList(new ArrayList<>(processInstanceIdMap.keySet()));
                    addFilter(queryWrapper,QueryTypeEnum.in,"a.proc_ins_id",zform.getProcInsIdList(),null);
                    IPage<Zform> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), page.getPageSize());
                    zform.getSqlMap().put("orderBy", page.getOrderBy());
                    IPage<LinkedHashMap> listMap = dao.findListMap(iPage, zform, zform.getTableOrViewName(), zform.getQueryWrapper(), zform.getSqlMap());
                    for (LinkedHashMap record : listMap.getRecords()) {
                        String proc_ins_id = ConvertUtil.getString(record.get("proc_ins_id"));
                        Act act = processInstanceIdMap.get(proc_ins_id);
                        if (super.isSuspend(path)){
                            act.setIsSuspend(true);
                        }
                        record.put("act",act);
                    }
                    page.setMap(listMap.getRecords());
                    page.setCount(listMap.getTotal());
                } else {
                    zform.setPage(page);
                    List<LinkedHashMap> map = Lists.newArrayList();
                    page.setMap(map);
                }
            }
            return page;
        }
    }

    /**
     * Read the corresponding list through to do, being done, done, to be sent and sent
     *
     * @param zform
     * @param path
     * @param processDefinitionCategory
     * @param loginName
     * @return zform list
     */
    public List<Zform> findDataList(Zform zform, String path, String processDefinitionCategory, String loginName, GenTable genTable) {
        if (StringUtil.isEmpty(path)) path = PATH_QUERY;
        if (StringUtil.isEmpty(processDefinitionCategory) || PATH_QUERY.equals(path)) {
            return findList(zform);
        } else {
            User currentUser = UserUtil.getByLoginName(loginName);
            List<Zform> list = new ArrayList<Zform>();
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
            if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
                zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
            }

            if (zform != null && zform.getCreateTimeEnd() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(zform.getCreateTimeEnd());
                calendar.add(Calendar.HOUR, 24);
                calendar.add(Calendar.MILLISECOND, -1);
                zform.setCreateTimeEnd(calendar.getTime());
            }
            //Unsent
            if (super.isUnsent(path)) {
                if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    //dsf += " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    dsf += " AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    //zform.getSqlMap().put("dsf", " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                    zform.getSqlMap().put("dsf", " AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                }
                list = dao.findList(zform);
            }
            //To do, doing, done, sent
            else {
                Map<String, Act> processInstanceIdMap = super.getProcessInstanceIdList(processDefinitionCategory, path, loginName, zform.getUserTaskKey());
                if (processInstanceIdMap.size() > 0) {
                    zform.setProcInsIdList(new ArrayList<>(processInstanceIdMap.keySet()));
                    list = dao.findListByProc(zform);
                } else {
                    List<Zform> zformList = Lists.newArrayList();
                    list = zformList;
                }
            }
            return list;
        }
    }

    /**
     * Find count
     *
     * @param page
     * @param zform
     * @return count
     */
    public String findCount(Page<Zform> page, Zform zform, GenTable genTable) {
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        page = super.findPage(page, zform);
        if (page.getCount() > 9) {
            return COUNT_RESULT_MORETHAN9; //9+
        } else {
            return String.valueOf(page.getCount());
        }
    }

    /**
     * Find count by params
     *
     * @param page
     * @param zform
     * @param path
     * @param loginName
     * @return count
     */
    public String findCount(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable) {
        if (StringUtil.isEmpty(path)) path = PATH_QUERY;
        String processDefinitionCategory = genTable.getProcessDefinitionCategory();
        if (StringUtil.isEmpty(processDefinitionCategory) || PATH_QUERY.equals(path)) {
            throw new RuntimeException("需要清理的代码，不应该走到这里");
            //return findCount(page, zform, genTable);
        } else {
            User currentUser = UserUtil.getByLoginName(loginName);
            this.setQueryWrapper(zform,genTable, currentUser);
            zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, "1");
            if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
                zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
            }

            if (zform != null && zform.getCreateTimeEnd() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(zform.getCreateTimeEnd());
                calendar.add(Calendar.HOUR, 24);
                calendar.add(Calendar.MILLISECOND, -1);
                zform.setCreateTimeEnd(calendar.getTime());
            }
            //Unsent
            if (super.isUnsent(path)) {
                zform.setPage(page);
                QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
                //queryWrapper.eq("a.create_by", currentUser.getId());
                queryWrapper.and(w -> w.isNull("a.proc_ins_id").or().eq("a.proc_ins_id", ""));
                /*if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    dsf += " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')";
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    zform.getSqlMap().put("dsf", " AND a.create_by = '" + currentUser.getId() + "' AND (a.proc_ins_id IS NULL OR a.proc_ins_id = '')");
                }*/
                IPage<Zform> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), 1);
                IPage<LinkedHashMap> listMap = dao.findListMap(iPage, zform, zform.getTableOrViewName(), zform.getQueryWrapper(), zform.getSqlMap());
                page.setCount(listMap.getTotal());
            }
            //To do, doing, done, sent
            else {
                Map<String, Act> processInstanceIdMap = super.getProcessInstanceIdList(processDefinitionCategory, path, loginName, zform.getUserTaskKey());
                if (processInstanceIdMap.size() > 0) {
                    zform.setPage(page);
                    zform.setProcInsIdList(new ArrayList<>(processInstanceIdMap.keySet()));
                    addFilter(zform.getQueryWrapper(),QueryTypeEnum.in,"a.proc_ins_id",zform.getProcInsIdList(),null);
                    IPage<Zform> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(), 1);
                    IPage<LinkedHashMap> listMap = dao.findListMap(iPage, zform, zform.getTableOrViewName(), zform.getQueryWrapper(), zform.getSqlMap());
                    page.setCount(listMap.getTotal());
                } else {
                    zform.setPage(page);
                    page.setCount(0);
                }
            }
            if (page.getCount() > 9) {
                return COUNT_RESULT_MORETHAN9; //9+
            } else {
                return String.valueOf(page.getCount());
            }
        }
    }

    /**
     * Set workflow information, called when the form form is opened
     *
     * @param zform
     */
    public void setAct(Zform zform, String loginName) {
        super.setAct(zform, loginName);
    }

    /**
     * Set sql map
     *
     * @param zform
     * @param genTable
     */
    @Deprecated
    public void setSqlMap(Zform zform, GenTable genTable, User currentUser) {
        try {
            String userSecLevel = currentUser.getSecLevel();
            StringBuffer stringBuffer = new StringBuffer();
            List<GenTableColumn> columnList = genTable.getColumnList();
            for (GenTableColumn genTableColumn : columnList) {
                if (StringUtil.isNotBlank(genTableColumn.getIsQuery()) && Global.YES.equals(genTableColumn.getIsQuery())) {
                    String javaField = genTableColumn.getSimpleJavaField();//getJavaField();
                    String javaFieldId = genTableColumn.getJavaFieldId();
                    if (javaFieldId.startsWith("g0")
                            || javaFieldId.startsWith("office0")
                            || javaFieldId.startsWith("user0")
                            || javaFieldId.startsWith("area0")
                            || javaFieldId.startsWith("parent")
                            || javaFieldId.equals("createBy.id")
                            || javaFieldId.equals("updateBy.id")) {
                        javaFieldId = javaFieldId.substring(0, javaFieldId.lastIndexOf("."));
                    }
                    Field field = Reflections.getThisField(zform.getClass(), javaFieldId);
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(zform);
                        String queryType = genTableColumn.getQueryType();
                        if (object != null || "between".equalsIgnoreCase(queryType)) {
                            StringBuffer subSql = new StringBuffer();
                            //The first section of SQL, splicing the SQL on the left side of the value
                            String name = genTableColumn.getName();
                            //Control whether to splice single quotation marks. Use this value to judge when the second SQL segment
                            boolean addSingleMark = true;
                            if ("=".equals(queryType) || StringUtil.isEmpty(queryType)) {
                                subSql.append(" AND a." + name + " = ");
                            } else if ("!=".equals(queryType)) {
                                subSql.append(" AND a." + name + " != ");
                            } else if ("&gt;".equals(queryType)) {
                                subSql.append(" AND a." + name + " > ");
                            } else if ("&gt;=".equals(queryType)) {
                                subSql.append(" AND a." + name + " >= ");
                            } else if ("&lt;".equals(queryType)) {
                                subSql.append(" AND a." + name + " < ");
                            } else if ("&lt;=".equals(queryType)) {
                                subSql.append(" AND a." + name + " <= ");
                            } else if ("between".equals(queryType) || "Between".equals(queryType)) {
                                //In the generated list date query criteria, the name format of the date range field is:
                                // Java attribute name + begin or Java attribute name + end
                                javaField = javaField.substring(0, 1).toUpperCase() + javaField.substring(1);
                                //String beginStr = request.getParameter("begin" + javaField);
                                //String endStr = request.getParameter("end" + javaField);


                                //If it's between, you can judge it's date type
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genTableColumn.getDateType());

                                String beginStr = "";
                                String endStr = "";
                                Field fieldBegin = zform.getClass().getDeclaredField("begin" + javaField);
                                Field fieldEnd = zform.getClass().getDeclaredField("end" + javaField);
                                fieldBegin.setAccessible(true);
                                fieldEnd.setAccessible(true);
                                if (fieldBegin != null && fieldEnd != null && fieldBegin.get(zform) != null && fieldEnd.get(zform) != null) {
                                    beginStr = simpleDateFormat.format((Date) fieldBegin.get(zform));
                                    endStr = simpleDateFormat.format((Date) fieldEnd.get(zform));
                                }

                                if (StringUtil.isNotBlank(beginStr) && StringUtil.isNotBlank(endStr)) {
                                    Date beginDate = simpleDateFormat.parse(beginStr);
                                    Date endDate = simpleDateFormat.parse(endStr);
                                    if (beginDate != null && endDate != null) {
                                        String dataType = genTableColumn.getDateType();
                                        if (dataType.length() == 4) {
                                            //year
                                            dataType += "-MM-dd";
                                            beginStr += "-01-01";
                                            endStr += "-12-31";
                                        } else if (dataType.length() == 7) {
                                            //month
                                            dataType += "-dd";
                                            beginStr += "-01";
                                            endStr += "-01";
                                            endStr = DateUtil.getMonthEnd(endStr, "yyyy-MM-dd");
                                        }
                                        if (DBNAME_ORACLE.equals(dbType)) {
                                            dataType = dataType.replaceAll("mm", "mi");
                                            dataType = dataType.replaceAll("HH", "hh24");
                                            dataType = dataType.replaceAll("MM", "mm");
                                            subSql.append(" AND a." + name + " BETWEEN to_date('" + beginStr + "','" + dataType + "') AND to_date('" + endStr + "','" + dataType + "') ");
                                            stringBuffer.append(subSql.toString());
                                        } else {
                                            subSql.append(" AND a." + name + " BETWEEN '" + beginStr + "' AND '" + endStr + "' ");
                                            stringBuffer.append(subSql.toString());
                                        }
                                    }
                                }
                                continue;
                            } else if (QUERYTYPE_LIKE.equals(queryType)) {
                                subSql.append(" AND a." + name + " like '%");
                                addSingleMark = false;
                            } else if (QUERYTYPE_LEFT_LIKE.equals(queryType)) {
                                subSql.append(" AND a." + name + " like '%");
                                addSingleMark = false;
                            } else if (QUERYTYPE_RIGHT_LIKE.equals(queryType)) {
                                subSql.append(" AND a." + name + " like '");
                                addSingleMark = false;
                            }

                            //SQL second segment, splicing value
                            String javaType = genTableColumn.getJavaType();
                            if (JAVATYPE_SGTRING.equals(javaType)) {
                                String value = object.toString();
                                if (StringUtil.isNotBlank(value)) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value + "' ");
                                    } else {
                                        subSql.append(value);
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_LONG.equals(javaType)) {
                                Long value = (Long) object;
                                if (value != null) {
                                    //Long does not need single quotes
                                    subSql.append(value);
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_INTEGER.equals(javaType)) {
                                Integer value = (Integer) object;
                                if (value != null) {
                                    //Integer does not need single quotes
                                    subSql.append(value);
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_DOUBLE.equals(javaType)) {
                                Double value = (Double) object;
                                if (value != null) {
                                    //Double does not need single quotes
                                    subSql.append(value);
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_BOOLEAN.equals(javaType)) {
                                Boolean value = (Boolean) object;
                                if (value != null) {
                                    //PostgreSQL boolean column requires true/false literals
                                    if (DBNAME_POSTGRESQL.equals(dbType)) {
                                        subSql.append(value ? "true" : "false");
                                    } else {
                                        subSql.append(value ? 1 : 0);
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_BIGDECIMAL.equals(javaType)) {
                                BigDecimal value = (BigDecimal) object;
                                if (value != null) {
                                    //BigDecimal does not need single quotes
                                    subSql.append(value);
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_DATE.equals(javaType)) {
                                Date value = (Date) object;
                                if (value != null) {
                                    if (addSingleMark) {
                                        if (DBNAME_ORACLE.equals(dbType)) {
                                            String oracleDataType = genTableColumn.getDateType();
                                            oracleDataType = oracleDataType.replaceAll("mm", "mi");
                                            oracleDataType = oracleDataType.replaceAll("HH", "hh24");
                                            oracleDataType = oracleDataType.replaceAll("MM", "mm");
                                            subSql.append("  to_date('" + value + "','" + oracleDataType + "') ");
                                            stringBuffer.append(subSql.toString());
                                        } else {
                                            subSql.append(" '" + value + "' ");
                                        }
                                    } else {
                                        subSql.append(value);
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_USER.equals(javaType)) {
                                User value = (User) object;
                                if (value != null && StringUtil.isNotBlank(value.getId())) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value.getId() + "' ");
                                    } else {
                                        subSql.append(value.getId());
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_OFFICE.equals(javaType)) {
                                Office value = (Office) object;
                                if (value != null && StringUtil.isNotBlank(value.getId())) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value.getId() + "' ");
                                    } else {
                                        subSql.append(value.getId());
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_AREA.equals(javaType)) {
                                Area value = (Area) object;
                                if (value != null && StringUtil.isNotBlank(value.getId())) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value.getId() + "' ");
                                    } else {
                                        subSql.append(value.getId());
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_ZFORM.equals(javaType)) {
                                Zform value = (Zform) object;
                                if (value != null && StringUtil.isNotBlank(value.getId())) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value.getId() + "' ");
                                    } else {
                                        subSql.append(value.getId());
                                    }
                                } else {
                                    continue;
                                }
                            } else if (JAVATYPE_THIS.equals(javaType)) {
                                Zform value = (Zform) object;
                                if (value != null && StringUtil.isNotBlank(value.getId())) {
                                    if (addSingleMark) {
                                        subSql.append(" '" + value.getId() + "' ");
                                    } else {
                                        subSql.append(value.getId());
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                //Object of custom input
                                Class<?> clazz = Class.forName(javaType);
                                if (clazz != null) {
                                    Object obj = clazz.cast(object);
                                    if (obj != null) {
                                        //Field f = obj.getClass().getDeclaredField("id");
                                        Field f = Reflections.getThisField(obj.getClass(), "id");
                                        if (f != null) {
                                            f.setAccessible(true);
                                            String id = (String) f.get(obj);
                                            if (id != null) {
                                                if (addSingleMark) {
                                                    subSql.append(" '" + id + "' ");
                                                } else {
                                                    subSql.append(id);
                                                }
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }

                            //The third section of SQL, splicing the SQL on the right side of the value
                            if (QUERYTYPE_LIKE.equals(queryType)) {
                                subSql.append("%' ");
                            } else if (QUERYTYPE_LEFT_LIKE.equals(queryType)) {
                                subSql.append("' ");
                            } else if (QUERYTYPE_RIGHT_LIKE.equals(queryType)) {
                                subSql.append("%' ");
                            }
                            stringBuffer.append(subSql.toString());
                        }
                    }
                }
            }

            if (Global.YES.equals(genTable.getIsBuildSecret())) {
                stringBuffer.append(" AND a.sec_level <= '" + userSecLevel + "' ");
            }

            if (false == currentUser.getLoginName().equals("admin") && genTable.getName().equals("sys_role")) {
                stringBuffer.append(" AND a.is_sys != '1' ");
            }

            if (false == genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE) && false == genTable.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)) {
                if (zform.getParent() != null) {
                    if (StringUtil.isNotEmpty(genTable.getParentTableFk())) {
                        stringBuffer.append(" AND a." + genTable.getParentTableFk() + " = '" + zform.getParent().getId() + "' ");
                    } else {
                        stringBuffer.append(" AND 1 <> 1 ");
                    }
                }
            }

            //Data Permission
            //{company}
            String ownerCodeCompany = currentUser.getCompany().getCode();
            //{office}
            String ownerCodeOffice = currentUser.getOffice().getCode();
            String companyAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getCompany().getArea().getCode();
            String officeAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getOffice().getArea().getCode();
            List<Role> roleList = currentUser.getRoleList();
            String roles="";
            if(roleList!=null){
                List<String> roleIdList = new LinkedList<>();
                roleList.forEach(r-> roleIdList.add(r.getId()));
                roles = String.join(",",roleIdList);
            }

            for (Datapermission datapermission : currentUser.getDatapermissionList()) {
                if (datapermission.getMainTable().equalsIgnoreCase(genTable.getName())) {
                    String expression = datapermission.getExpression();
                    expression = expression.replaceAll("\\{company\\}", ownerCodeCompany);
                    expression = expression.replaceAll("\\{office\\}", ownerCodeOffice);
                    expression = expression.replaceAll("\\{companyAreaCode\\}", companyAreaCode);
                    expression = expression.replaceAll("\\{officeAreaCode\\}", officeAreaCode);
                    expression = expression.replaceAll("\\{roles\\}", roles);
                    expression = expression.replaceAll("\\{userId\\}", currentUser.getId());
                    stringBuffer.append(" AND ");
                    stringBuffer.append(expression);
                    stringBuffer.append(" ");
                }
            }

            String querySql = stringBuffer.toString();
            if (StringUtil.isNotBlank(querySql)) {
                if (zform.getSqlMap() != null && zform.getSqlMap().size() > 0) {
                    String dsf = zform.getSqlMap().get("dsf");
                    dsf = dsf == null ? "" : dsf;
                    dsf += querySql;
                    zform.getSqlMap().put("dsf", dsf);
                } else {
                    zform.getSqlMap().put("dsf", querySql);
                }
            }
        } catch (Exception e) {
            logger.info("Set sql map for zform:" + zform.getFormNo());
            logger.error("Error while set sql map for zform:" + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 根据表单配置、查询参数、密级和数据权限构建动态表单查询条件。
     */
    public void setQueryWrapper(Zform zform, GenTable genTable, User currentUser) {
        try {
            QueryWrapper queryWrapper = zform.getQueryWrapper();
            if (queryWrapper == null) {
                queryWrapper = new QueryWrapper();
            }
            if(genTable.getName().toLowerCase().endsWith(GenUtil.SUM_VIEW) || !genTable.getDelFlagExists()) {
                //queryWrapper.eq(1, 1);
            } else {
                queryWrapper.eq("a.del_flag", "0");
            }
            Map<String, String> queryParamType = new HashMap<>();
            if (zform.getQueryParamType() != null && zform.getQueryParamType().size() > 0){
                queryParamType = zform.getQueryParamType();
            }
            String userSecLevel = currentUser.getSecLevel();//用户密级
            StringBuilder stringBuilder = new StringBuilder();
            List<GenTableColumn> columnList = genTable.getColumnList();
            for (GenTableColumn genTableColumn : columnList) {
                if ((StringUtil.isNotBlank(genTableColumn.getIsQuery()) && Global.YES.equals(genTableColumn.getIsQuery()))||queryParamType.containsKey(genTableColumn.getName())) {
                    String javaField = genTableColumn.getSimpleJavaField();//getJavaField();
                    String javaFieldId = genTableColumn.getJavaFieldId();
                    if (javaFieldId.startsWith("g0")
                            || javaFieldId.startsWith("office0")
                            || javaFieldId.startsWith("user0")
                            || javaFieldId.startsWith("area0")
                            || javaFieldId.startsWith("parent")
                            || javaFieldId.equals("createBy.id")
                            || javaFieldId.equals("updateBy.id")) {
                        javaFieldId = javaFieldId.substring(0, javaFieldId.lastIndexOf("."));
                    }
                    if (genTable.getTableType().equals(GenTable.TABLE_TYPE_RIGHTTABLE)&&javaFieldId.equals("parent")){
                        continue;
                    }
                    Field field = Reflections.getThisField(zform.getClass(), javaFieldId);
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(zform);
                        if (StringUtil.isEmpty(object)){
                            continue;
                        }
                        String javaType = genTableColumn.getJavaType();
                        if (JAVATYPE_USER.equals(javaType)) {
                            User value = (User) object;
                            if (value != null && StringUtil.isNotBlank(value.getId())) {
                                object = value.getId();
                            } else {
                                continue;
                            }
                        } else if (JAVATYPE_OFFICE.equals(javaType)) {
                            Office value = (Office) object;
                            if (value != null && StringUtil.isNotBlank(value.getId())) {
                                object = value.getId();
                            } else {
                                continue;
                            }
                        } else if (JAVATYPE_AREA.equals(javaType)) {
                            Area value = (Area) object;
                            if (value != null && StringUtil.isNotBlank(value.getId())) {
                                object = value.getId();
                            } else {
                                continue;
                            }
                        } else if (JAVATYPE_ZFORM.equals(javaType)) {
                            Zform value = (Zform) object;
                            if (value != null && StringUtil.isNotBlank(value.getId())) {
                                object = value.getId();
                            } else {
                                continue;
                            }
                        } else if (JAVATYPE_THIS.equals(javaType)) {
                            Zform value = (Zform) object;
                            if (value != null && StringUtil.isNotBlank(value.getId())) {
                                object = value.getId();
                            } else {
                                continue;
                            }
                        } else if (JAVATYPE_SGTRING.equals(javaType)||JAVATYPE_INTEGER.equals(javaType)||JAVATYPE_LONG.equals(javaType)||JAVATYPE_DOUBLE.equals(javaType)||JAVATYPE_BOOLEAN.equals(javaType)||JAVATYPE_BIGDECIMAL.equals(javaType)||JAVATYPE_DATE.equals(javaType)) {
                            object = object;
                        } else {
                            //Object of custom input
                            Class<?> clazz = Class.forName(javaType);
                            if (clazz != null) {
                                Object obj = clazz.cast(object);
                                if (obj != null) {
                                    //Field f = obj.getClass().getDeclaredField("id");
                                    Field f = Reflections.getThisField(obj.getClass(), "id");
                                    if (f != null) {
                                        f.setAccessible(true);
                                        String id = (String) f.get(obj);
                                        object = id;
                                    }
                                }
                            }
                        }
                        String queryType = genTableColumn.getQueryType();
                        if (queryParamType.containsKey(genTableColumn.getName())&&StringUtil.isNotEmpty(queryParamType.get(genTableColumn.getName()))){
                            queryType = queryParamType.get(genTableColumn.getName());
                        }
                        if (object != null || "between".equalsIgnoreCase(queryType)) {
                            //StringBuffer subSql = new StringBuffer();
                            //The first section of SQL, splicing the SQL on the left side of the value
                            String name = genTableColumn.getName();
                            //Control whether to splice single quotation marks. Use this value to judge when the second SQL segment
                            //boolean addSingleMark = true;
                            if ("=".equals(queryType) || StringUtil.isEmpty(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.eq, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " = ");
                            }else if ("in".equals(queryType)) {
                                if (object instanceof String){
                                    String[] ids = ((String) object).split(",");
                                    addFilter(queryWrapper, QueryTypeEnum.in, "a." + name, Arrays.asList(ids), null);
                                }else{
                                    addFilter(queryWrapper, QueryTypeEnum.in, "a." + name, object, null);
                                }
                                //subSql.append(" AND a." + name + " = ");
                            } else if ("!=".equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.ne, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " != ");
                            } else if ("&gt;".equals(queryType) || ">".equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.gt, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " > ");
                            } else if ("&gt;=".equals(queryType) || ">=".equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.ge, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " >= ");
                            } else if ("&lt;".equals(queryType) || "<".equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.lt, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " < ");
                            } else if ("&lt;=".equals(queryType) || "<=".equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.le, "a." + name, object, null);
                                //subSql.append(" AND a." + name + " <= ");
                            } else if ("between".equals(queryType) || "Between".equals(queryType)) {
                                //In the generated list date query criteria, the name format of the date range field is:
                                // Java attribute name + begin or Java attribute name + end
                                javaField = javaField.substring(0, 1).toUpperCase() + javaField.substring(1);
                                //String beginStr = request.getParameter("begin" + javaField);
                                //String endStr = request.getParameter("end" + javaField);


                                //If it's between, you can judge it's date type
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genTableColumn.getDateType());

                                String beginStr = "";
                                String endStr = "";
                                Field fieldBegin = zform.getClass().getDeclaredField("begin" + javaField);
                                Field fieldEnd = zform.getClass().getDeclaredField("end" + javaField);
                                fieldBegin.setAccessible(true);
                                fieldEnd.setAccessible(true);
                                if (fieldBegin != null && fieldEnd != null && fieldBegin.get(zform) != null && fieldEnd.get(zform) != null) {
                                    beginStr = simpleDateFormat.format((Date) fieldBegin.get(zform));
                                    endStr = simpleDateFormat.format((Date) fieldEnd.get(zform));
                                }

                                if (StringUtil.isNotBlank(beginStr) && StringUtil.isNotBlank(endStr)) {
                                    Date beginDate = simpleDateFormat.parse(beginStr);
                                    Date endDate = simpleDateFormat.parse(endStr);
                                    if (beginDate != null && endDate != null) {
                                        String dataType = genTableColumn.getDateType();
                                        if (dataType.length() == 4) {
                                            //year
                                            dataType += "-MM-dd";
                                            beginStr += "-01-01";
                                            endStr += "-12-31";
                                        } else if (dataType.length() == 7) {
                                            //month
                                            dataType += "-dd";
                                            beginStr += "-01";
                                            endStr += "-01";
                                            endStr = DateUtil.getMonthEnd(endStr, "yyyy-MM-dd");
                                        }
                                        addFilter(queryWrapper, QueryTypeEnum.between, "a." + name, DateUtil.strToDate(beginStr), DateUtil.strToDate(endStr));
                                    }
                                }
                                continue;
                            } else if (QUERYTYPE_LIKE.equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.like, "a." + name, object, null);
                            } else if (QUERYTYPE_LEFT_LIKE.equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.likeLeft, "a." + name, object, null);
                            } else if (QUERYTYPE_RIGHT_LIKE.equals(queryType)) {
                                addFilter(queryWrapper, QueryTypeEnum.likeRight, "a." + name, object, null);
                            } else if (StringUtil.isNotEmpty(queryType)){
                                //适配新表单配置的查询类型
                                QueryTypeEnum typeEnum = QueryTypeEnum.valueOf(queryType);
                                if (typeEnum != null){
                                    addFilter(queryWrapper, typeEnum, "a." + name, object, null);
                                }
                            }
                        }
                    }
                }
            }

            if (Global.YES.equals(genTable.getIsBuildSecret())) {
                addFilter(queryWrapper, QueryTypeEnum.le, "a.sec_level", userSecLevel, null);
                //stringBuffer.append(" AND a.sec_level <= '" + userSecLevel + "' ");
            }

            if (!"admin".equals(currentUser.getLoginName()) && genTable.getName().equals("sys_role")) {
                //stringBuffer.append(" AND a.is_sys != '1' ");
                addFilter(queryWrapper, QueryTypeEnum.ne, "a.is_sys", '1', null);
            }

            if (!GenTable.TABLE_TYPE_TREE.equals(genTable.getTableType()) && !GenTable.TABLE_TYPE_RIGHTTABLE.equals(genTable.getTableType())) {
                if (zform.getParent() != null) {
                    if (StringUtil.isNotEmpty(genTable.getParentTableFk())) {
                        addFilter(queryWrapper, QueryTypeEnum.eq, "a." + genTable.getParentTableFk(), zform.getParent().getId(), null);
                        //stringBuffer.append(" AND a." + genTable.getParentTableFk() + " = '" + zform.getParent().getId() + "' ");
                    } else {
                        //stringBuffer.append(" AND 1 <> 1 ");
                        queryWrapper.ne(1, 1);
                    }
                }
            }

            StringBuilder permissionSql = new StringBuilder();
            //Data Permission
            //{company}
            String ownerCodeCompany = currentUser.getCompany().getCode();
            //{office}
            String ownerCodeOffice = currentUser.getOffice().getCode();
            String companyAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getCompany().getArea().getCode();
            String officeAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getOffice().getArea().getCode();

            List<Role> roleList = currentUser.getRoleList();
            String roles="";
            if(roleList!=null){
                List<String> roleIdList = new LinkedList<>();
                roleList.forEach(r-> roleIdList.add(r.getId()));
                roles = String.join(",",roleIdList);
            }
            List<String> appendTableList = new LinkedList<>();
            for (Datapermission datapermission : currentUser.getDatapermissionList()) {
                if (datapermission.getMainTable().equalsIgnoreCase(genTable.getName())) {
                    if (appendTableList.contains(datapermission.getMainTable())) {
                        continue;
                    }
                    appendTableList.add(datapermission.getMainTable());
                    String expression = datapermission.getExpression();
                    expression = expression.replaceAll("\\{company\\}", ownerCodeCompany);
                    expression = expression.replaceAll("\\{office\\}", ownerCodeOffice);
                    expression = expression.replaceAll("\\{companyAreaCode\\}", companyAreaCode);
                    expression = expression.replaceAll("\\{officeAreaCode\\}", officeAreaCode);
                    expression = expression.replaceAll("\\{roles\\}", roles);
                    expression = expression.replaceAll("\\{userId\\}", currentUser.getId());
                    permissionSql.append(" ");
                    permissionSql.append(expression);
                    permissionSql.append(" ");
                    queryWrapper.apply(permissionSql.toString());
                }
            }
            zform.setQueryWrapper(queryWrapper);
        } catch (Exception e) {
            logger.info("Set sql map for zform:" + zform.getFormNo());
            logger.error("Error while set sql map for zform:" + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 判断当前表单是否使用运行时 SQL 作为数据来源。
     */
    protected boolean isRuntimeSourceSqlView(GenTable genTable) {
        GenTableDeriveConfig deriveConfig = genTable == null ? null : genTable.getDeriveConfig();
        return deriveConfig != null
                && GenTableDeriveConfig.SOURCE_MODE_SYNC_VIEW.equals(deriveConfig.getSourceMode())
                && StringUtil.isNotBlank(deriveConfig.getSourceSql());
    }

    /**
     * 将来源 SQL 包装为子查询来源，外层仍统一使用别名 a 承接查询条件、分页和排序。
     */
    protected String getRuntimeTableOrViewName(GenTable genTable) {
        GenTableDeriveConfig deriveConfig = genTable == null ? null : genTable.getDeriveConfig();
        if (deriveConfig == null || StringUtil.isBlank(deriveConfig.getSourceSql())) {
            return genTable == null ? "" : genTable.getName();
        }
        return "(" + normalizeRuntimeSourceSql(deriveConfig.getSourceSql()) + ")";
    }

    /**
     * 运行时来源 SQL 只允许查询语句，禁止把 DDL/DML 写入动态表单查询链路。
     */
    protected String normalizeRuntimeSourceSql(String sourceSql) {
        String normalized = sourceSql == null ? "" : sourceSql.trim();
        if (StringUtil.isBlank(normalized)) {
            throw new RuntimeException("视图表单来源SQL不能为空");
        }
        String lowerSql = normalized.toLowerCase(Locale.ROOT);
        String checkSql = " " + lowerSql.replaceAll("\\s+", " ") + " ";
        if (!checkSql.startsWith(" select ") && !checkSql.startsWith(" with ")) {
            throw new RuntimeException("视图表单来源SQL只允许使用查询语句");
        }
        for (String keyword : SOURCE_SQL_DANGEROUS_KEYWORDS) {
            if (checkSql.contains(keyword)) {
                throw new RuntimeException("视图表单来源SQL包含不允许的内容: " + keyword.trim());
            }
        }
        return normalized;
    }

    /**
     * 向 QueryWrapper 追加单个查询条件，统一处理等值、范围、模糊、空值等查询类型。
     */
    public void addFilter(QueryWrapper<Zform> queryWrapper, QueryTypeEnum queryType, String column, Object queryValue1, Object queryValue2) {
        if (queryValue1 instanceof Map){
            Map<String,Object> map = (Map<String, Object>) queryValue1;
            String queryValue1Str = ConvertUtil.getString(map.get("id"));
            if (StrUtil.isEmpty(queryValue1Str)){
                return;
            }
        }
        switch (queryType) {
            case gt:
                queryWrapper.gt(column, queryValue1);
                break;
            case ge:
                queryWrapper.ge(column, queryValue1);
                break;
            case lt:
                queryWrapper.lt(column, queryValue1);
                break;
            case le:
                queryWrapper.le(column, queryValue1);
                break;
            case like:
                queryWrapper.like(column, queryValue1);
                break;
            case notLike:
                queryWrapper.notLike(column, queryValue1);
                break;
            case likeLeft:
                queryWrapper.likeLeft(column, queryValue1);
                break;
            case likeRight:
                queryWrapper.likeRight(column, queryValue1);
                break;
            case between:
                queryWrapper.between(column, queryValue1, queryValue2);
                break;
            case ne:
                queryWrapper.ne(column, queryValue1);
                break;
            case in:
                if (queryValue1 instanceof List){
                    queryWrapper.in(column, (List) queryValue1);
                    break;
                }
                if (queryValue1 instanceof String && ((String) queryValue1).contains(",")){
                    queryWrapper.in(column, ((String)queryValue1).split(","));
                    break;
                }
                queryWrapper.in(column, queryValue1);
                break;
            case notIn:
                if (queryValue1 instanceof List){
                    queryWrapper.notIn(column, (List) queryValue1);
                    break;
                }
                if (queryValue1 instanceof String && ((String) queryValue1).contains(",")){
                    queryWrapper.notIn(column, ((String)queryValue1).split(","));
                    break;
                }
                queryWrapper.notIn(column, queryValue1);
                break;
            case isEmpty:
                queryWrapper.eq(column,StringUtil.EMPTY);
                break;
            case isNull:
                queryWrapper.isNull(column);
                break;
            case isNotNull:
                queryWrapper.isNotNull(column);
                break;
            case apply:
                // 安全约束：apply 条件必须由前端白名单过滤，避免 SQL 注入
                queryWrapper.apply(ConvertUtil.getString(queryValue1));
                break;
            default:
                // 兼容说明：queryValue1 为 Map 时优先取 id 值
                try {
                    JSONObject jsonObject = JSONHelper.toJSONObject(queryValue1);
                    String queryValue = (String) jsonObject.get("id");
                    if (queryValue.isEmpty()) {
                        queryWrapper.eq(column, queryValue1);
                    } else {
                        queryWrapper.eq(column, queryValue);
                    }
                } catch (Exception e) {
                    queryWrapper.eq(column, queryValue1);
                }
                break;
        }
    }

    public void buildParentIdsForChildren(String rootParentIds, Zform zform, GenTable gentable) {
        if (zform.getParent() == null || StringUtil.isEmpty(zform.getParent().getId()) || "0".equals(zform.getParent().getId())) {
            zform.setParentIds(rootParentIds);
        } else {
            zform.setParentIds(rootParentIds + zform.getParent().getId() + ",");
        }
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERT, gentable.getSqlInsert());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLINSERTV, gentable.getSqlInsertV());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, gentable.getSqlUpdate());
        zform.setFormNo(gentable.getName());
        this.saveTree(zform, gentable);
        if (zform.isHasChildren()) {
            Zform param = new Zform();
            param.setFormNo(gentable.getName());
            param.setParent(new Zform(zform.getId(), gentable.getName()));
            //List<Zform> childrenList = zformDao.findList(param);
            String querySql = " AND a.parent_id = '" + zform.getId() + "' ";
            if (param.getSqlMap() != null && param.getSqlMap().size() > 0) {
                String dsf = param.getSqlMap().get("dsf");
                dsf = dsf == null ? "" : dsf;
                dsf += querySql;
                param.getSqlMap().put("dsf", dsf);
            } else {
                param.getSqlMap().put("dsf", querySql);
            }
            List<Zform> childrenList = this.findList(param, gentable);

            for (Zform theZform : childrenList) {
                this.buildParentIdsForChildren(zform.getParentIds(), theZform, gentable);
            }
        }
    }

    /**
     * 对数据进行完整性保护处理
     *
     * @param zformList
     * @param genTable
     */
    private void integrityData(List<Zform> zformList, GenTable genTable) {
        if (projectProperties.isIntegrityProtection()) {
            //判断是否有完整性保护配置
            new Thread(() -> {
                Map<String, Map<String, String>> map = sysSecIConfigService.getColumns(genTable.getName());
                if (map != null) {
                    for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
                        sysSecIConfigService.saveRecord(entry.getKey(), entry.getValue(), zformList, genTable);
                    }
                }
            }).start();
        }
    }
    /**
     * 根据表单配置加密数据
     * @param zform
     * @param genTable
     */
    private void encryptData(Zform zform, GenTable genTable){
        if (!projectProperties.isSecretProtection()){
            return;
        }
        List<GenTableColumn> columnList = genTable.getColumnList();
        String encryptPrefix = "encrypt_";
        Map<String, GenTableColumn> columnMap = ConvertUtil.listToMap(columnList, GenTableColumn::getName);
        for (GenTableColumn column : columnList) {

            String remarks = getRemarks(column);
            if (oConvertUtils.getString(remarks).startsWith(encryptPrefix)){
                Object value = null;
                try {
                    value = ReflectUtils.getValue(zform, column.getJavaField());
                    if (value == null){
                        continue;
                    }
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }
                //加密字段
                String encryptName = encryptPrefix + column.getName();
                String desensitiseType = remarks.replaceFirst(encryptPrefix, "");
                if (columnMap.containsKey(encryptName)){
                    GenTableColumn tableColumn = columnMap.get(encryptName);
                    //DesensitizedResult desensitizedResult = DesensitizedUtil.desensitizedEncrypt(projectProperties.getSm4Key(), value.toString(), DesensitiseTypeEnum.valueOf(desensitiseType));
                    String encryptData = secretHandler.encrypt(ConvertUtil.getString(value));
                    String result = DesensitizedUtil.desensitized(value.toString(), DesensitiseTypeEnum.valueOf(desensitiseType));
                    try {
                        ReflectUtils.setValue(zform, tableColumn.getJavaField(), encryptData);
                        ReflectUtils.setValue(zform, column.getJavaField(), result);
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }

                }

            }
        }
    }

    private static String getRemarks(GenTableColumn column) {
        String remarks = "";
        if (column.getRemarks() != null) {
            remarks = column.getRemarks().replace("unique", "").replace(";", "");
        }
        return remarks;
    }

    /**
     * 根据表单配置解密数据
     * @param zformMap
     * @param genTable
     */
    private void decryptData(LinkedHashMap zformMap, GenTable genTable){
        if (!projectProperties.isSecretProtection()){
            return;
        }
        List<GenTableColumn> columnList = genTable.getColumnList();
        String encryptPrefix = "encrypt_";
        Map<String, GenTableColumn> columnMap = ConvertUtil.listToMap(columnList, GenTableColumn::getName);
        for (GenTableColumn column : columnList) {
            String remarks = getRemarks(column);
            if (oConvertUtils.getString(remarks).startsWith(encryptPrefix)) {
                //加密字段
                String encryptName = encryptPrefix + column.getName();
                if (columnMap.containsKey(encryptName)) {
                    GenTableColumn tableColumn = columnMap.get(encryptName);
                    String columnName = tableColumn.getName();
                    if (zformMap.containsKey(columnName) && !StringUtil.isEmpty(zformMap.get(columnName))) {
                        String encryptData = zformMap.get(columnName).toString();
                        //String result = SM4Util.decrypt(projectProperties.getSm4Key(), encryptData);
                        String result = secretHandler.decrypt(encryptData);
                        zformMap.put(column.getName(), result);
                    }
                }
            }
        }
    }

    public void superSave(Zform zform, GenTable genTable) {
        if (genTable.getName().equalsIgnoreCase("sys_user")) {
            String officeId = zform.getParent().getId();
            String companyId = officeService.getCompanyIdByOfficeId(officeId);
            zform.setOffice01(new Office(companyId));
            zform.setOffice02(new Office(officeId));
        }
        this.defaultValueHandle(zform, genTable);
        this.encryptData(zform, genTable);
        // ownerCode 为空时使用当前用户所属机构编码。
        User currentUser = UserUtil.getCurrentUser();
        if (StringUtil.isEmpty(zform.getOwnerCode()) && currentUser != null && currentUser.getOffice() != null && StringUtil.isNotBlank(currentUser.getOffice().getCode())) {
            zform.setOwnerCode(currentUser.getOffice().getCode());
        }
        zform.getSqlMap().put("pkColumnName", genTable.getPkColumnName());
        super.save(zform);

        // 根据配置自动创建或更新 sys_office。
        this.autoCreateOffice(zform, genTable);

        // 根据配置自动创建或更新 sys_user。
        this.autoCreateUser(zform, genTable);

        //对数据进行完整性保护处理
        this.integrityData(Collections.singletonList(zform), genTable);
    }

    /**
     * 根据配置自动创建/更新sys_office
     * @param zform zform
     * @param genTable genTable
     */
    public void autoCreateOffice(Zform zform, GenTable genTable) {
        List<GenTableColumn> columnList = genTable.getColumnList();
        List<GenTableColumn> offList = columnList.stream().filter(k -> "officeselectTree".equals(k.getShowType())).collect(Collectors.toList());
        if (offList.size() > 0) {
            for (GenTableColumn k : offList) {
                GenTableColumnFormItemConfig formItemConfig = k.getGenTableColumnFormItemConfig();
                if (formItemConfig != null && Global.YES.equals(formItemConfig.getCreateSysOffice())
                        && StrUtil.isNotEmpty(formItemConfig.getParentOrgId())
                        && StrUtil.isNotEmpty(formItemConfig.getOrgNameField())) {
                    logger.info("根据配置自动创建/更新sys_office开始,{},{}", genTable.getName(), k.getName());
                    try {
                        Office Office = (Office) ReflectUtils.getValue(zform, k.getSimpleJavaField());
                        Optional<GenTableColumn> nameColumn = columnList.stream().filter(j -> formItemConfig.getOrgNameField().equals(j.getName())).findFirst();
                        Optional<GenTableColumn> areaColumn = columnList.stream().filter(j -> formItemConfig.getAreaIdField().equals(j.getName())).findFirst();
                        String orgName = ConvertUtil.getString(ReflectUtils.getValue(zform, nameColumn.get().getSimpleJavaField()));
                        Area area = null;
                        if (areaColumn.isPresent()) {
                            area = (Area) ReflectUtils.getValue(zform, areaColumn.get().getSimpleJavaField());
                        }

                        Office officeDb;
                        if (Office != null && StringUtil.isNotEmpty(Office.getId())) {
                            officeDb = officeService.get(Office.getId());
                            officeDb.setName(orgName);
                            logger.info("根据配置更新sys_office的name,{},{}", Office.getId(), orgName);
                        } else {
                            officeDb = officeService.createNewOffice(formItemConfig.getParentOrgId(), orgName);
                            logger.info("根据配置创建sys_office,{},{}", officeDb.getCode(), orgName);
                        }
                        officeDb.setArea(area);
                        officeService.save(officeDb);
                        SqlInjectionUtil.filterContent(genTable.getName());
                        SqlInjectionUtil.filterContent(k.getName());
                        SqlInjectionUtil.filterContent(officeDb.getId());
                        SqlInjectionUtil.filterContent(zform.getId());
                        // 验证表名和列名是否为合法SQL标识符
                        SqlInjectionUtil.validateIdentifier(genTable.getName());
                        SqlInjectionUtil.validateIdentifier(k.getName());
                        // 使用参数化查询，避免 SQL 注入
                        String updateSql = "update " + genTable.getName() + " set " + k.getName() + " = #{param.officeId} where id = #{param.zformId}";
                        Map<String, Object> param = new java.util.HashMap<>();
                        param.put("officeId", officeDb.getId());
                        param.put("zformId", zform.getId());
                        zformDao.updateSqlParm(updateSql, param);
                        ReflectUtils.setValue(zform, k.getSimpleJavaField(),officeDb);
                        logger.info("根据配置自动创建/更新sys_office成功,{},{}", genTable.getName(), k.getName());
                    } catch (Exception e) {
                        logger.error("根据配置自动创建sys_office失败,{}", ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException(e.getMessage());
                    }
                    return;
                }
            }
        }
    }

    /**
     * 根据配置自动创建/更新sys_user
     * @param zform zform
     * @param genTable genTable
     */
    public void autoCreateUser(Zform zform, GenTable genTable){
        List<GenTableColumn> columnList = genTable.getColumnList();
        List<GenTableColumn> userList = columnList.stream().filter(k -> "treeselectRedio".equals(k.getShowType())).collect(Collectors.toList());
        if (userList.size() > 0) {
            User currentUser = UserUtil.getCurrentUser();
            for (GenTableColumn k : userList) {
                GenTableColumnFormItemConfig formItemConfig = k.getGenTableColumnFormItemConfig();
                if (formItemConfig != null && Global.YES.equals(formItemConfig.getCreateSysUser())
                        && StrUtil.isNotEmpty(formItemConfig.getLoginNameField())) {
                    logger.info("根据配置自动创建/更新sys_user开始,{},{}", genTable.getName(), k.getName());
                    try {
                        User user = (User) ReflectUtils.getValue(zform, k.getSimpleJavaField());
                        Optional<GenTableColumn> loginNameColumn = columnList.stream().filter(j -> formItemConfig.getLoginNameField().equals(j.getName())).findFirst();
                        Optional<GenTableColumn> nameColumn = columnList.stream().filter(j -> formItemConfig.getUserNameField().equals(j.getName())).findFirst();
                        Optional<GenTableColumn> parentOrgColumn = columnList.stream().filter(j -> formItemConfig.getParentOrgField().equals(j.getName())).findFirst();

                        String loginName = ConvertUtil.getString(ReflectUtils.getValue(zform, loginNameColumn.get().getSimpleJavaField()));
                        if (loginName.length() < 6) {
                            throw new BusinessException("登录名长度不能小于6位");
                        }
                        if (loginName.contains("_")) {
                            throw new BusinessException("登录名不能包含 _ ");
                        }
                        String name = "";
                        Office parent = null;
                        if (currentUser != null) {
                            parent = currentUser.getCompany();
                        }
                        String roleIds = ConvertUtil.getString(formItemConfig.getUserRoles());

                        if (nameColumn.isPresent()) {
                            name = ConvertUtil.getString(ReflectUtils.getValue(zform, nameColumn.get().getSimpleJavaField()));
                        }
                        if (parentOrgColumn.isPresent()) {
                            if (parentOrgColumn.get().getSimpleJavaField().startsWith("s")) {
                                parent = new Office(ConvertUtil.getString(ReflectUtils.getValue(zform, parentOrgColumn.get().getSimpleJavaField())));
                            } else {
                                parent = (Office) ReflectUtils.getValue(zform, parentOrgColumn.get().getSimpleJavaField());
                            }
                        }

                        User userDb = null;
                        if (user != null && StringUtil.isNotEmpty(user.getId())) {
                            userDb = userService.get(user.getId());
                            // 有特殊情况如果提供的关联用户ID获取不到用户信息，则进行分别判断处理
                            if ( userDb == null) {
                                // 如果通过用户ID获取不到用户信息，则通过登录名获取
                                User userByLoginName = userService.getByLoginName(loginName);
                                if (userByLoginName != null) {
                                    // 如果通过登录名获取到用户信息，则提示用户登录名已存在，不进行后续的逻辑操作
                                    throw new BusinessException("登录名" + loginName + "已存在");
                                } else {
                                    // 如果通过登录名获取不到用户信息，则创建新用户
                                    userDb = new User(parent, loginName, name);
                                    userService.saveUser(userDb, currentUser.getLoginName());
                                    logger.info("根据配置创建sys_user,loginName={},name={}", loginName, name);
                                }
                            } else {
                                // 如果通过用户ID获取到用户信息，则更新用户信息
                                userService.updateUserRealName(userDb.getLoginName(), name);
                                logger.info("根据配置更新sys_user的name,loginName={},name={}", loginName, name);
                            }
                        } else {
                            User userByLoginName = userService.getByLoginName(loginName);
                            if (userByLoginName != null) {
                                throw new BusinessException("登录名" + loginName + "已存在");
                            }
                            userDb = new User(parent, loginName, name);
                            userService.saveUser(userDb, currentUser.getLoginName());
                            logger.info("根据配置创建sys_user,loginName={},name={}", loginName, name);
                        }

                        SqlInjectionUtil.filterContent(genTable.getName());
                        SqlInjectionUtil.filterContent(k.getName());
                        SqlInjectionUtil.filterContent(userDb.getId());
                        SqlInjectionUtil.filterContent(zform.getId());
                        // 验证表名和列名是否为合法SQL标识符
                        SqlInjectionUtil.validateIdentifier(genTable.getName());
                        SqlInjectionUtil.validateIdentifier(k.getName());
                        // 使用参数化查询，避免 SQL 注入
                        String updateSql = "update " + genTable.getName() + " set " + k.getName() + " = #{param.userId} where id = #{param.zformId}";
                        Map<String, Object> param = new java.util.HashMap<>();
                        param.put("userId", userDb.getId());
                        param.put("zformId", zform.getId());
                        zformDao.updateSqlParm(updateSql, param);
                        if (StringUtil.isNotEmpty(roleIds)){
                            String[] roles = roleIds.split(",");
                            for (String role : roles) {
                                roleService.saveUserRole(userDb.getId(), role);
                                logger.info("根据配置自动创建/更新sys_user角色,{},{}", userDb.getLoginName(), role);
                            }
                        }

                        logger.info("根据配置自动创建/更新sys_user成功,{},{}", genTable.getName(), k.getName());
                    } catch (Exception e) {
                        logger.error("根据配置自动创建sys_user失败,{}", ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException(e.getMessage());
                    }
                    return;
                }
            }
        }
    }

    /**
     * 批量保存数据，自动处理插入、更新和删除。
     *
     * @param list      数据列表
     * @param genTable  genTable
     * @param loginName loginName
     */
    public void superBatchSave(List<Zform> list, GenTable genTable, String loginName) {
        this.superBatchSave(list, genTable, loginName, null);
    }

    /**
     * 批量保存数据，自动处理插入、更新和删除。
     *
     * @param list      数据列表
     * @param genTable  genTable
     * @param loginName loginName
     * @param parent parent
     */
    public void superBatchSave(List<Zform> list, GenTable genTable, String loginName, Zform parent) {
        User currentUser = UserUtil.getByLoginName(loginName);
        long start = System.currentTimeMillis();
        logger.info("开始批量处理数据,formNo:{},共{}条数据", genTable.getName(), list.size());
        int deleteCount;
        int insertCount;
        int updateCount = 0;
        List<Zform> deleteList = new ArrayList<>();
        List<Zform> addList = new ArrayList<>();
        List<Zform> updateList = new ArrayList<>();
        Map<String,String> companyMap = new HashMap<>();
        if (genTable.getName().equalsIgnoreCase("sys_user")) {
            companyMap = officeService.getCompanyIdByOfficeId(list.stream().filter(k->k.getParent()!=null).map(k->k.getParent().getId()).collect(Collectors.toList()));
        }
        //记录不需要更新的数据
        Set<String> skipUpdateSet = new HashSet<>();
        for (Zform zform : list) {
            if (genTable.getName().equalsIgnoreCase("sys_user")) {
                String officeId = zform.getParent().getId();
                String companyId = companyMap.get(officeId);
                zform.setOffice01(new Office(companyId));
                zform.setOffice02(new Office(officeId));
            }
            this.defaultValueHandle(zform, genTable);
            this.encryptData(zform, genTable);
            if (parent != null) {
                zform.setParent(parent);
            }
            if (Global.YES.equals(zform.getDelFlag()) && StringUtil.isNotEmpty(zform.getId())) {
                //del 需要删除的数据
                deleteList.add(zform);
            } else {
                if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                    //Tree table, rebuild parent_ids
                    String rootParentIds = Global.DEFAULT_ROOT_CODE + ",";
                    if (zform.getParent() != null) {
                        try {
                            parent = this.get(zform.getParent().getId(), genTable);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (parent != null && StringUtil.isNotEmpty(parent.getId()) && StringUtil.isNotEmpty(parent.getParentIds())) {
                        rootParentIds = parent.getParentIds();
                    } else {
                        parent = new Zform();
                        parent.setId(Global.DEFAULT_ROOT_CODE);
                        parent.setParentIds(rootParentIds);
                        zform.setParent(parent);
                    }
                    this.buildParentIdsForChildren(rootParentIds, zform, genTable);
                    //树表的已经insert了，需要跳过insert 及 update
                    skipUpdateSet.add(zform.getId());
                }
                if (zform.getIsNewRecord()) {
                    zform.preInsert();
                    zform.setIsNewRecord(false);
                    // ownerCode 为空时使用当前用户所属机构编码。
                    if (StringUtil.isEmpty(zform.getOwnerCode()) && currentUser != null && currentUser.getOffice() != null && StringUtil.isNotBlank(currentUser.getOffice().getCode())) {
                        zform.setOwnerCode(currentUser.getOffice().getCode());
                    }
                    addList.add(zform);
                } else if(!skipUpdateSet.contains(zform.getId())){// 兼容说明：树表数据已执行 insert 时跳过 update，避免 MySQL 同批插入与更新冲突。
                    zform.preUpdate();
                    //zform.getSqlMap().put(GenTable.SQLMAP_SQLUPDATE, genTable.getSqlUpdate());
                    //updateCount+=dao.update(zform);
                    updateList.add(zform);
                }
            }
        }
        String sqlInsert = genTable.getSqlInsert();
        String[] split = sqlInsert.split("\\) VALUES \\(");
        String sqlInsertColumn = split[0] + ") VALUES";
        String sqlInsertValues = "(" + split[1];
        sqlInsertValues = sqlInsertValues.replaceAll("#\\{", "#\\{o.");
        Map<String, String> sqlMap = new HashMap<>();
        sqlMap.put("sqlInsertColumn", sqlInsertColumn);
        sqlMap.put("sqlInsertValues", sqlInsertValues);
        sqlMap.put("tableOrViewName", genTable.getName());
        insertCount = super.batchInsert(addList, sqlMap);
        deleteCount = super.batchDelete(genTable.getName(), deleteList);
        updateCount = this.superBatchUpdate(updateList, genTable);
        long end = System.currentTimeMillis();
        logger.info("批量处理数据完成,formNo:{},insert{},update{},delete{},耗时：{}ms", genTable.getName(), insertCount, updateCount, deleteCount, end - start);

        this.integrityData(list, genTable);

    }

    /**
     * 批量更新Zform
     * @param list Zform列表
     * @param genTable genTable
     * @return
     */
    public int superBatchUpdate(List<Zform> list, GenTable genTable) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        List<Function<Zform, Object>> functionList = new ArrayList<>();
        List<String> setFieldList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(genTable.getName()).append(" SET ");
        for (GenTableColumn genTableColumn : genTable.getColumnList()) {
            if (Global.YES.equals(genTableColumn.getIsEdit())) {
                if ("procTaskPermission".equalsIgnoreCase(genTableColumn.getJavaFieldId()) || !genTableColumn.getJavaField().equals(genTableColumn.getSimpleJavaField())) {
                    functionList.add(k -> {
                        try {
                            Map<String, Object> map = BeanUtil.beanToMap(ReflectUtils.getValue(k, genTableColumn.getSimpleJavaField()));
                            if (map != null && map.containsKey("id")) {
                                return map.get("id");
                            } else {
                                return null;
                            }
                        } catch (Exception e) {
                            throw new BusinessException(e.getMessage());
                        }
                    });
                } else {
                    functionList.add(k -> {
                        try {
                            return ReflectUtils.getValue(k, genTableColumn.getJavaField());
                        } catch (Exception e) {
                            throw new BusinessException(e.getMessage());
                        }
                    });
                }
                setFieldList.add(StrUtil.format(" {} = ? ", genTableColumn.getName()));
            }
        }
        sql.append(StrUtil.join(",", setFieldList));
        sql.append(" WHERE id = ?");
        functionList.add(BaseEntity::getId);
        logger.info("superBatchUpdate,formNo:{},updateSql={},共{}条数据", genTable.getName(), sql, list.size());
        int count = this.executeByNewConnection(sql.toString(), (List<List<Object>> parameters) -> {
            for (Zform zform : list) {
                List<Object> parameter = new ArrayList<>();
                for (int i = 0; i < functionList.size(); i++) {
                    Object value = functionList.get(i).apply(zform);
                    parameter.add(value);
                }
                parameters.add(parameter);
            }
        });
        logger.info("superBatchUpdate,formNo:{}结束,更新成功{}条", genTable.getName(), count);
        return count;
    }


    /**
     * 执行sql 使用新的连接
     * @param sql sql
     * @param consumer consumer
     * @return
     */
    public int executeByNewConnection(String sql, Consumer<List<List<Object>>> consumer) {
        int result = 0;
        try {
            List<List<Object>> parameters = new ArrayList<>();
            consumer.accept(parameters);
            // 使用标准 JDBC 替代 JdbcUtil.executeBatch
            try (java.sql.Connection conn = dataSource.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                for (List<Object> row : parameters) {
                    for (int i = 0; i < row.size(); i++) {
                        ps.setObject(i + 1, row.get(i));
                    }
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                for (int r : results) {
                    result += Math.max(r, 0);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 处理默认值
     *
     */
    public void defaultValueHandle(Zform zform, GenTable genTable) {
        List<GenTableColumn> columnList = genTable.getColumnList();
        for (GenTableColumn column : columnList) {
            this.defaultValueHandle(zform, column.getDefaultValue(), column.getJavaField());
        }
    }
    /**
     * 处理默认值
     */
    public void defaultValueHandle(Zform zform, String defaultValue, String javaField) {
        if (StringUtil.isNotEmpty(defaultValue)) {
            Object value = null;
            String fieldName = javaField;
            try {
                if (fieldName == null) {
                    return;
                }
                fieldName = fieldName.split("\\|")[0];
                fieldName = fieldName.split("\\.")[0];
                value = ReflectUtils.getValue(zform, fieldName);
                if (value != null) {
                    return;
                }
                value = "";
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
            if (StringUtil.isEmpty(value.toString())) {
                try {
                    switch (defaultValue) {
                        case "${currentTime}":
                            if (fieldName.startsWith("d")){
                                ReflectUtils.setValue(zform, fieldName, new Date());
                            }else{
                                ReflectUtils.setValue(zform, fieldName, cn.hutool.core.date.DateUtil.now());
                            }
                            return;
                        case "${currentDate}":
                            String date = DateUtil.getDate() + " 00:00:00";
                            if (fieldName.startsWith("d")){
                                ReflectUtils.setValue(zform, fieldName, DateUtil.strToDate(date));
                            }else{
                                ReflectUtils.setValue(zform, fieldName, DateUtil.getDate());
                            }
                            return;
                        case "${currentYear}":
                            String currYear = DateUtil.getCurrentYear() + "-01-01 00:00:00";
                            if (fieldName.startsWith("d")){
                                ReflectUtils.setValue(zform, fieldName, DateUtil.strToDate(currYear));
                            }else{
                                ReflectUtils.setValue(zform, fieldName, DateUtil.getCurrentYear());
                            }
                            return;
                        default:
                            if (fieldName.startsWith("s") || fieldName.startsWith("c") || fieldName.startsWith("l")) {
                                //如果是字符串类型 设置默认值
                                ReflectUtils.setValue(zform, fieldName, defaultValue);
                            }
                    }

                    User user = UserUtil.getCurrentUser();
                    if (user == null) {
                        return;
                    }

                    switch (defaultValue) {
                        case "${currentUser}":
                            ReflectUtils.setValue(zform, fieldName, user);
                            return;
                        case "${currentUserName}":
                            ReflectUtils.setValue(zform, fieldName, user.getName());
                            return;
                        case "${currentCompany}":
                            ReflectUtils.setValue(zform, fieldName, user.getCompany());
                            return;
                        case "${currentCompanyArea}":
                            ReflectUtils.setValue(zform, fieldName, user.getCompany() != null ? user.getCompany().getArea() : null);
                            return;
                        case "${currentOffice}":
                            ReflectUtils.setValue(zform, fieldName, user.getOffice());
                            return;
                        default:
                    }
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }

            }

        }
    }



    /**
     * 查询自定义选择控件的简化数据源，返回基础分页数据。
     */
    public ResultJson gridselectData(GridselectParam gridselectParam) {
        ResultJson resultJson = new ResultJson();
        Zform zform = new Zform();
        zform.setFormNo(gridselectParam.getTableName());
        GenTable genTable = genTableService.getGenTableWithDefination(gridselectParam.getTableName());
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns());
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()));
        }
        zform.getSqlMap().put("dsf", " AND a." + gridselectParam.getSearchKey() + " LIKE '%" + gridselectParam.getSearchValue() + "%' ");
        String dsfPlus = gridselectParam.getDsfPlus();
        if (StringUtil.isNotBlank(dsfPlus)) {
            String dsf = zform.getSqlMap().get("dsf");
            if (StringUtil.isNotBlank(dsf)) {
                dsf += dsfPlus;
            } else {
                dsf = dsfPlus;
            }
            zform.getSqlMap().put("dsf", dsf);
        }
        Page<Zform> page = new Page<Zform>(gridselectParam.getPageParam().getPageNo(), gridselectParam.getPageParam().getPageSize(), gridselectParam.getPageParam().getOrderBy());
        page.setFromIndex(gridselectParam.getPageParam().getFromIndex());
        zform.setPage(page);
        Page<Zform> result = super.findPage(page, zform);
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setRows(result.getList());
        resultJson.setTotal(result.getCount());
        return resultJson;
    }

    private String replaceWithExtSqlPairMap(String sql, HashMap<String, String> extSqlPairMap) {
        //遍历extSqlPairMap，将替换sql中存在的字符串（格式为：${something}），替换成extSqlPairMap中的值
        if (extSqlPairMap != null) {
            for (Map.Entry<String, String> entry : extSqlPairMap.entrySet()) {
                String placeholder = "\\{" + entry.getKey() + "}";
                String replacement = entry.getValue();
                // 安全约束：替换扩展 SQL 参数前先过滤注入风险。
                SqlInjectionUtil.filterContent(replacement);
                sql = sql.replaceAll(placeholder, replacement);
            }
        }
        return sql;
    }

    /**
     * 查询自定义选择控件的数据源 Map，支持关联表过滤、扩展 SQL、字典展示和数据权限。
     */
    public Page<Zform> gridselectDataMapList(GridselectParam gridselectParam) {
        Zform zform = new Zform();
        /*QueryWrapper<Zform> queryWrapper = new QueryWrapper<Zform>();
        queryWrapper.eq("a.del_flag", "0");*/
        zform.setFormNo(gridselectParam.getTableName());
        GenTable genTable = genTableService.getGenTableWithDefination(gridselectParam.getTableName());

        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTableService.getGenTableWithDefination(gridselectParam.getTableName()));
        if (aroundService != null) {
            aroundService.beforeGridselectSetSqlMap(genTable);
        }

        zform.setFormNoExt(genTable.getFormNoExt());
        setQueryWrapper(zform, genTable , UserUtil.getCurrentUser());
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS_FRIENDLY, this.replaceWithExtSqlPairMap(genTable.getSqlColumnsFriendly() + " " + StringUtil.getString(genTable.getSqlColumnsFriendlyExt()), gridselectParam.getExtSqlPairMap()));
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, this.replaceWithExtSqlPairMap(StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()), gridselectParam.getExtSqlPairMap()));
        }
        if (StringUtil.isEmpty(gridselectParam.getPageParam().getOrderBy()) && StringUtil.isNotEmpty(genTable.getSqlSort())) {
            gridselectParam.getPageParam().setOrderBy(genTable.getSqlSort());
        }
        String targetTableName = gridselectParam.getTargetTableName();
        String targetField = gridselectParam.getTargetField();
        String targetJoinKey = gridselectParam.getTargetJoinKey();
        List<GridselectParam.FilterData> filterList = gridselectParam.getFilterList();
        if (StringUtil.isNotEmpty(targetTableName)&&StringUtil.isNotEmpty(targetField)) {
            SqlInjectionUtil.filterContent(new String[]{targetTableName, targetField});
            String joinSql = "";

            List<GridselectParam.FilterData> targetFilterList = gridselectParam.getTargetFilterList();
            if (targetFilterList!=null){
                //关联表的 过滤条件
                QueryWrapper<Zform> targetQueryWrapper = new QueryWrapper<>();
                for (GridselectParam.FilterData filterData : targetFilterList) {
                    filterDataChild(targetQueryWrapper,filterData);
                }
                String sqlSegment = targetQueryWrapper.getSqlSegment();
                Map<String, Object> paramNameValuePairs = targetQueryWrapper.getParamNameValuePairs();
                if (paramNameValuePairs != null && paramNameValuePairs.size()>0) {
                    targetJoinKey = StringUtil.isBlank(targetJoinKey) ? "id" : targetJoinKey;
                    joinSql = StrUtil.format(" LEFT JOIN {} targetTable ON targetTable.del_flag ='0' AND targetTable.{} = a.{} ", targetTableName,targetField,targetJoinKey);
                    for (Map.Entry<String, Object> entry : paramNameValuePairs.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        SqlInjectionUtil.filterContent(oConvertUtils.getString(value));
                        sqlSegment = sqlSegment.replace("#{ew.paramNameValuePairs." + key + "}", "#{ew.paramNameValuePairs.targetTable" + key + "}");
                        queryWrapper.getParamNameValuePairs().put("targetTable"+key, value);
                    }
                    joinSql += " AND " + sqlSegment;
                    if (filterList==null){
                        filterList = new ArrayList<>();
                    }
                    filterList.add(new GridselectParam.FilterData("targetTable."+targetField, null, null, QueryTypeEnum.isNull.name()));
                }
            }
            if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
                zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, this.replaceWithExtSqlPairMap(StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()) + joinSql, gridselectParam.getExtSqlPairMap()));
            }
        }
        //不为空时才拼接查询条件
        if (StringUtil.isNotBlank(gridselectParam.getSearchKey()) && StringUtil.isNotBlank(gridselectParam.getSearchValue())) {
            if (gridselectParam.getSearchKey().contains(".")) {
                addFilter(queryWrapper, QueryTypeEnum.like, gridselectParam.getSearchKey(), gridselectParam.getSearchValue(), null);
                //zform.getSqlMap().put("dsf", " AND " + gridselectParam.getSearchKey() + " LIKE '%" + gridselectParam.getSearchValue() + "%' ");
            } else {
                addFilter(queryWrapper, QueryTypeEnum.like, "a." + gridselectParam.getSearchKey(), gridselectParam.getSearchValue(), null);
                //zform.getSqlMap().put("dsf", " AND a." + gridselectParam.getSearchKey() + " LIKE '%" + gridselectParam.getSearchValue() + "%' ");
            }
        }
        //String dsfPlus = gridselectParam.getDsfPlus();

        if (filterList != null && filterList.size() > 0) {
            for (GridselectParam.FilterData filterData : filterList) {
                replaceFilterDataValue(UserUtil.getCurrentUser(), filterData);
                filterDataChild(queryWrapper, filterData);
            }
        }
        permissionDataSet(UserUtil.getCurrentUser(), genTable.getName(),queryWrapper); // 设置数据权限
        Page<Zform> page = new Page<Zform>(gridselectParam.getPageParam().getPageNo(), gridselectParam.getPageParam().getPageSize(), gridselectParam.getPageParam().getOrderBy());
        page.setFromIndex(gridselectParam.getPageParam().getFromIndex());
        zform.setPage(page);
        zform.setQueryWrapper(queryWrapper);
        Page<Zform> result = super.findPageMap(page, zform);
        if (aroundService != null) {
            aroundService.afterFindPageMap(result, page, zform, "", UserUtil.getCurrentLoginName(), genTable, "", "", "");
        }
        return result;
    }

    /**
     * 递归解析自定义选择控件过滤条件，支持 AND/OR 组合和嵌套条件。
     */
    public void filterDataChild(QueryWrapper<Zform> queryWrapper, GridselectParam.FilterData filterData) {
        if (filterData.getChildren() != null && filterData.getChildren().size() > 0) {
            if ( filterData.isOr() ){
                queryWrapper.or(wrapper -> {
                    for (GridselectParam.FilterData child : filterData.getChildren()) {
                        filterDataChild(wrapper, child);
                    }
                });
            }else{
                queryWrapper.and(wrapper -> {
                    for (GridselectParam.FilterData child : filterData.getChildren()) {
                        filterDataChild(wrapper, child);
                    }
                });
            }
        } else if (StringUtil.isNotEmpty(filterData.getKey()) && StringUtil.isNotEmpty(filterData.getType()) && ("isNull".equals(filterData.getType()) || !StringUtil.isEmpty(filterData.getValue()) || !StringUtil.isEmpty(filterData.getValue2()))) {
            SqlInjectionUtil.filterContent(filterData.getKey());
            if (filterData.isOr()) {
                queryWrapper.or();
            }
            addFilter(queryWrapper, QueryTypeEnum.valueOf(filterData.getType()), filterData.getKey(), filterData.getValue(), filterData.getValue2());
        }
    }

    /**
     * 查询自定义选择控件数据并补充字典显示名称，供前端弹窗/抽屉选择器使用。
     */
    public ResultJson gridselectDataMap(GridselectParam gridselectParam) {
        ResultJson resultJson = new ResultJson();
        Page<Zform> result = gridselectDataMapList(gridselectParam);
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        this.appendDictName(result.getMap(), genTableService.getGenTableWithDefination(gridselectParam.getTableName()));
        resultJson.setRows(result.getMap());
        resultJson.setTotal(result.getCount());
        return resultJson;
    }

    /**
     * 根据行政区划编码解析行政区划信息
     * @param areaId 行政区划编码
     * @return 行政区划信息 Map key:prov_code,prov_name,city_code,city_name,county_code,county_name,full_name 为空则表示没有对应的行政区划信息
     */
    public Map<String, String> parseAreaInfo(String areaId) {
        Map<String, String> result = new HashMap<>();
        if (StringUtil.isEmpty(areaId)) {
            return result;
        }
        if (areaId.endsWith("0000")) {
            //编码是省级的，直接取省级的名称
            TagTree prov = areaService.getArea(areaId);
            if (prov != null) {
                result.put("prov_code", prov.getId());
                result.put("prov_name", prov.getName());
                result.put("full_name", prov.getName());
            }
        } else if (areaId.endsWith("00")) {
            //编码是市级的，取市级的名称和省级的名称
            TagTree city = areaService.getArea(areaId);
            if (city != null) {
                result.put("city_code", city.getId());
                result.put("city_name", city.getName());
                String provName = "";
                if(zxCity.contains(city.getId())){
                    provName = city.getName();
                    result.put("prov_code", city.getId());
                    result.put("prov_name", city.getName());
                }else{
                    TagTree prov = areaService.getArea(city.getParentId());
                    if (prov != null) {
                        provName = prov.getName();
                        result.put("prov_code", prov.getId());
                        result.put("prov_name", prov.getName());
                    }
                }
                result.put("full_name", provName + city.getName());
            }
        } else {
            //编码是区级的，取区级的名称、市级的名称和省级的名称
            TagTree area = areaService.getArea(areaId);
            if (area != null) {
                result.put("county_code", area.getId());
                result.put("county_name", area.getName());
                TagTree city = areaService.getArea(area.getParentId());
                if (city != null) {
                    result.put("city_code", city.getId());
                    result.put("city_name", city.getName());
                    String provName = "";
                    if(zxCity.contains(city.getId())){
                        provName = city.getName();
                        result.put("prov_code", city.getId());
                        result.put("prov_name", city.getName());
                    }else{
                        TagTree prov = areaService.getArea(city.getParentId());
                        if (prov != null) {
                            provName = prov.getName();
                            result.put("prov_code", prov.getId());
                            result.put("prov_name", prov.getName());
                        }
                    }
                    result.put("full_name", provName + city.getName() + area.getName());
                } else {
                    result.put("full_name", area.getName());
                }
            }
        }
        return result;
    }

    public void appendDictName(List<LinkedHashMap> dataMap, String formNo) {
        this.appendDictName(dataMap, genTableService.getGenTableWithDefination(formNo));
    }

    public void appendDictName(List<LinkedHashMap> dataMap, GenTable genTable) {
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
        // 数据表字典 多选翻译，单选时，sql中已查询出字典值，不需要再查询数据库
        columnList.stream()
                .filter(k -> StringUtil.isNotEmpty(k.getTableName()) && k.getGenTableColumnFormItemConfig().isMultiple() )
                .forEach(k -> {
                    String tableName = k.getTableName();
                    GenTable tableGen = genTableService.getGenTableWithDefination(tableName);
                    StringBuilder querySql = new StringBuilder();
                    querySql.append("SELECT ")
                            .append(k.getSelectValuefield())
                            .append(" as member")
                            .append(", ")
                            .append(k.getSearchKey())
                            .append(" as memberName");
                    querySql.append(" FROM  ");
                    querySql.append(tableName);
                    querySql.append(" a ");
                    QueryWrapper<LinkedHashMap<String,Object>> queryWrapper = new QueryWrapper<>();
                    if( tableGen.getDelFlagExists() ){
                        queryWrapper.eq("a.del_flag", '0');
                    }
                    List<LinkedHashMap> dictList = zformDao.findMapList(querySql.toString(), queryWrapper);
                    if (dictList != null && !dictList.isEmpty()) {
                        columnMap.put(k.getName(), k);

                        HashMap<String, DictResult> dict = new HashMap<>();
                        dictList.forEach(d -> {
                            DictResult dictResult = new DictResult();
                            dictResult.setMember(String.valueOf(d.get("member")));
                            dictResult.setMemberName(String.valueOf(d.get("memberName")));
                            dict.put(dictResult.getMember(), dictResult);
                        });
                        dictMap.put(tableName, dict);
                    }
                });

        HashMap<String, GenTableColumn> areaMap = new HashMap<>();
        columnList.stream().filter(k -> "areaselect".equals(k.getShowType())).forEach(k -> {
            areaMap.put(k.getName(), k);
        });

        dataMap.forEach(row -> {
            LinkedHashMap<String, Object> valueTransMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> addNameMap = new LinkedHashMap<>();
            row.forEach((key, value) -> {
                String k = (String) key;
                if (columnMap.containsKey(k) && value != null && StringUtil.isNotEmpty(value.toString())) {
                    GenTableColumn c = columnMap.get(k);
                    if (c == null) {
                        return;
                    }
                    String dictType = c.getDictType();
                    if (StringUtil.isEmpty(dictType)) {
                        dictType = c.getTableName();
                    }
                    if (!dictMap.containsKey(dictType)){
                        return;
                    }
                    HashMap<String, DictResult> dictResultHashMap = dictMap.get(dictType);
                    if ( c.getGenTableColumnFormItemConfig().isMultiple() && value instanceof String) {
                        String[] valueList = value.toString().split(",");
                        List<String> dicNameList = new ArrayList<>();
                        Arrays.stream(valueList).forEach(v -> {
                            DictResult dictResult = dictResultHashMap.get(v);
                            if (dictResult != null) {
                                dicNameList.add( dictResult.getMemberName() );
                            }
                        });
                        valueTransMap.put((String) key, String.join(",",dicNameList));

                    }else{
                        DictResult dictResult = dictResultHashMap.get(value);
                        if (dictResult == null) {
                            return;
                        }
                        valueTransMap.put((String) key, dictResult.getMemberName());
                    }
                }
                if (areaMap.containsKey(k) && value instanceof Map) {
                    Map areaInfo = (Map) value;
                    String id = (String) areaInfo.get("id");
                    this.parseAreaInfo(id).forEach((areaKey, areaValue) -> {
                        addNameMap.put(k + "__" + areaKey, areaValue);
                    });
                }
            });
            valueTransMap.forEach((key, value) -> {
                row.put(key + "__name", value);
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
    }

    public ResultJson gridselectData(GridselectParam gridselectParam, String dsfP, String sqlJoin, String sqlCol) {
        ResultJson resultJson = new ResultJson();
        Zform zform = new Zform();
        zform.setFormNo(gridselectParam.getTableName());
        GenTable genTable = genTableService.getGenTableWithDefination(gridselectParam.getTableName());
        if (StringUtil.isEmpty(sqlJoin)) {
            sqlJoin = "";
        }
        if (StringUtil.isEmpty(sqlCol)) {
            sqlCol = "";
        }
        if (StringUtil.isEmpty(dsfP)) {
            dsfP = "";
        }
        if (StringUtil.isNotEmpty(genTable.getSqlJoins()) || StringUtil.isNotEmpty(genTable.getSqlJoinsExt())) {
            zform.getSqlMap().put(GenTable.SQLMAP_SQLJOINS, StringUtil.getString(genTable.getSqlJoins()) + " " + StringUtil.getString(genTable.getSqlJoinsExt()) + sqlJoin);
        }
        zform.getSqlMap().put(GenTable.SQLMAP_SQLCOLUMNS, genTable.getSqlColumns() + sqlCol);
        zform.getSqlMap().put("dsf", dsfP + " AND a." + gridselectParam.getSearchKey() + " LIKE '%" + gridselectParam.getSearchValue() + "%' ");
        String dsfPlus = gridselectParam.getDsfPlus();
        if (StringUtil.isNotBlank(dsfPlus)) {
            String dsf = zform.getSqlMap().get("dsf");
            if (StringUtil.isNotBlank(dsf)) {
                dsf += dsfPlus;
            } else {
                dsf = dsfPlus;
            }
            zform.getSqlMap().put("dsf", dsf);
        }
        Page<Zform> page = new Page<Zform>(gridselectParam.getPageParam().getPageNo(), gridselectParam.getPageParam().getPageSize(), gridselectParam.getPageParam().getOrderBy());
        page.setFromIndex(gridselectParam.getPageParam().getFromIndex());
        zform.setPage(page);
        Page<Zform> result = super.findPage(page, zform);
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setRows(result.getList());
        resultJson.setTotal(result.getCount());
        return resultJson;
    }

    private Page<Zform> buildGridselectValues(Page<Zform> page, GenTable genTable) {
        for (Zform zform : page.getList()) {
            zform = this.buildGridselectValue(zform, genTable);
        }
        return page;
    }

    private Zform buildGridselectValue(Zform zform, GenTable genTable) {
        //移除查询name
        /*if (zform.getG01() != null
                && StringUtil.isNotEmpty(zform.getG01().getId())) {
            String name = this.getGridselectNamesByIds(zform.getG01().getId(), "g01", genTable);
            zform.getG01().setName(name);
        }
        if (zform.getG02() != null
                && StringUtil.isNotEmpty(zform.getG02().getId())) {
            String name = this.getGridselectNamesByIds(zform.getG02().getId(), "g02", genTable);
            zform.getG02().setName(name);
        }
        if (zform.getG03() != null
                && StringUtil.isNotEmpty(zform.getG03().getId())) {
            String name = this.getGridselectNamesByIds(zform.getG03().getId(), "g03", genTable);
            zform.getG03().setName(name);
        }
        if (zform.getG04() != null
                && StringUtil.isNotEmpty(zform.getG04().getId())) {
            String name = this.getGridselectNamesByIds(zform.getG04().getId(), "g04", genTable);
            zform.getG04().setName(name);
        }
        if (zform.getG05() != null
                && StringUtil.isNotEmpty(zform.getG05().getId())) {
            String name = this.getGridselectNamesByIds(zform.getG05().getId(), "g05", genTable);
            zform.getG05().setName(name);
        }*/
        return zform;
    }

    private String getGridselectNamesByIds(String ids, String gridName, GenTable genTable) {
        String names = "";
        for (GenTableColumn column : genTable.getColumnList()) {
            if (column.getShowType().equalsIgnoreCase("gridselect")
                    && column.getJavaField().startsWith(gridName)) {
                String[] idsArray = ids.split(",");
                GenTable refGenTable = genTableService.getGenTableWithDefination(column.getTableName());
                Map<String, String> sqlMap = new HashMap<>();
                sqlMap.put("pkColumnName", refGenTable != null ? refGenTable.getPkColumnName() : "id");
                for (int i = 0; i < idsArray.length; i++) {
                    String name = zformDao.getNameById(column.getTableName(), column.getSearchKey(), idsArray[i], sqlMap);
                    if (StringUtil.isEmpty(name)) name = "";
                    names += "," + name;
                }
                break;
            }
        }
        return names.substring(1);
    }

    public Map<String, Object> getTaskList(List<String> categoryList, String path, String loginName, int pageNo, int pageSize, Map<String, String> paramMap) {
        List<String> loginNameList = null;
        if (paramMap == null) paramMap = Maps.newHashMap();
        if (paramMap.get(ProcessMap.PROC_CREATE_USER.name()) != null
                && StringUtil.isNotBlank(paramMap.get(ProcessMap.PROC_CREATE_USER.name()))) {
            loginNameList = this.getLoginNameList(paramMap.get(ProcessMap.PROC_CREATE_USER.name()));
        }
        return super.getTaskList(categoryList, path, loginName, loginNameList, pageNo, pageSize, paramMap);
    }

    private List<String> getLoginNameList(String name) {
        List<String> loginNameList = Lists.newArrayList();
        List<UserView> userViewList = userDao.findUserViewByName(name);
        for (UserView userView : userViewList) {
            loginNameList.add(userView.getLoginName());
        }
        return loginNameList;
    }

    private String getFirstProcDefKey(String formNo, String currentUserName) {
        String procDefKey = "";
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        List<ProcessDefinition> procDefList = this.getProcessDefinitionList(genTable.getProcessDefinitionCategory(), currentUserName);
        List<Map<String, String>> list = Lists.newArrayList();
        for (ProcessDefinition processDefinition : procDefList) {
            procDefKey = processDefinition.getKey();
            break;
        }
        return procDefKey;
    }

    /**
     * 导入动态表单数据，按表单字段配置完成字典、用户、机构、区域和自定义选择字段映射。
     */
    public void importData(String ownerCode,
                           String columns,
                           String formNo,
                           String parentFormNo,
                           String parentId,
                           String uniqueId,
                           List<LinkedHashMap<String, String>> dataList,
                           User currentUser) throws Exception {
        columns = "!" + columns + "!";
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        int i = 0;
        for (LinkedHashMap row : dataList) {
            if (i++ == 0) continue;
            Zform zform = new Zform();
            zform.setDelFlag(Global.NO);
            zform.setOwnerCode(ownerCode);
            zform.setFormNo(formNo);
            zform.setCreateBy(currentUser);
            zform.setUpdateBy(currentUser);
            if (Global.YES.equals(genTable.getIsProcessDefinition())) zform.setProcDefKey(this.getFirstProcDefKey(formNo, currentUser.getLoginName()));
            if (StringUtil.isNotEmpty(parentId)) zform.setParent(new Zform(parentId, parentFormNo));
            for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                String javaField = genTableColumn.getJavaField();
                if (columns.indexOf("!" + javaField + "!") == -1) continue;
                if (StringUtil.contains(javaField, "|")) {
                    javaField = javaField.substring(0, javaField.indexOf("."));
                }
                Field field = Reflections.getThisField(zform.getClass(), javaField); //zform.getClass().getDeclaredField(javaField);
                //String methname = javaField.substring(0,1).toUpperCase() + javaField.substring(1);
                //Method m = zform.getClass().getMethod("set" + methname, java.lang.String);
                if (field != null) {
                    field.setAccessible(true);
                    if (javaField.startsWith("sort")) {
                        //sort
                        String value = row.get(genTableColumn.getJavaField()).toString();
                        if (StringUtil.isNotEmpty(value)) {
                            if (value.endsWith(".0")) {
                                value = value.substring(0, value.length() - 2);
                            }
                            field.set(zform, Integer.parseInt(value));
                        }
                    } else if (javaField.startsWith("s") || javaField.startsWith("m") || javaField.startsWith("c") || javaField.equals("remarks")) {
                        String value = (String) row.get(genTableColumn.getJavaField());
                        value = dictDataService.getDictValues(value, genTableColumn.getDictType(), value, "");
                        field.set(zform, value);
                    } else if (javaField.startsWith("d")) {
                        //Date
                        Calendar calendar = new GregorianCalendar(1900, 0, -1);
                        Date d = calendar.getTime();
                        Date value = null;
                        Object dateObj = row.get(genTableColumn.getJavaField());
                        if (dateObj != null) {
                            String dateStr = StringUtil.isBlank(dateObj.toString()) ? "" : dateObj.toString().trim();

                            if (StringUtil.isNotEmpty(dateStr) && dateStr.indexOf("-") != -1) {
                                int ii = genTableColumn.getDateType().split("-").length;
                                int jj = dateStr.split("-").length;
                                if (ii == 3 && jj == 2) {
                                    dateStr += "-01";
                                }
                                value = DateUtil.parseDate(dateStr, genTableColumn.getDateType());
                            } else {
                                if (StringUtil.isNotEmpty(dateStr)) {
                                    value = DateUtil.addDays(d, Integer.valueOf(dateStr));
                                }
                            }
                        }
                        field.set(zform, value);
                    } else if (javaField.startsWith("users")) {
                        //users
                        //Zform value = new Zform();
                        //value.setId((String) row.get(genTableColumn.getJavaField()));
                        //field.set(zform, value);
                    } else if (javaField.startsWith("user")) {
                        //user
                        String name = (String) row.get(genTableColumn.getJavaField());
                        String id = this.getFirstValueByKey("sys_user", "id", "name", name);
                        User obj = new User();
                        if (StringUtil.isNotEmpty(id)) {
                            obj.setId(id);
                        }
                        field.set(zform, obj);
                    } else if (javaField.startsWith("office")) {
                        //office
                        String name = (String) row.get(genTableColumn.getJavaField());
                        String id = this.getFirstValueByKey("sys_office", "id", "name", name);
                        Office obj = new Office();
                        if (StringUtil.isNotEmpty(id)) {
                            obj.setId(id);
                        }
                        field.set(zform, obj);
                    } else if (javaField.startsWith("area")) {
                        //area
                        String name = (String) row.get(genTableColumn.getJavaField());
                        String id = this.getFirstValueByKey("sys_area", "id", "name", name);
                        Area obj = new Area();
                        if (StringUtil.isNotEmpty(id)) {
                            obj.setId(id);
                        }
                        field.set(zform, obj);
                    } else if (javaField.startsWith("g")) {
                        //gridselect
                        Zform value = new Zform();
                        String name = (String) row.get(genTableColumn.getJavaField());
                        String id = this.getFirstValueByKey(genTableColumn.getTableName(), "id", genTableColumn.getSearchKey(), name);
                        value.setId(id);
                        field.set(zform, value);
                    } else if (javaField.startsWith("t")) {
                        //tree
                        Zform value = new Zform();
                        value.setId((String) row.get(genTableColumn.getJavaField()));
                        field.set(zform, value);
                    } else {
                        String value = (String) row.get(genTableColumn.getJavaField());
                        field.set(zform, value);
                    }
                }
            }
            this.save(zform, genTable);
        }
    }

    /**
     * 解析导入数据
     * @param row map对象
     * @param formNo 表单no
     * @param parentFormNo 父表单no
     * @param genTable genTable配置
     * @param parentId 父级id
     * @param currentUser 当前用户
     * @return
     */
    public Zform parseImportData(Map<String, Object> row,
                                 String formNo,
                                 String parentFormNo,
                                 GenTable genTable,
                                 String parentId,
                                 User currentUser) {
        Map<String, GenTableColumn> columnMap = ConvertUtil.listToMap(genTable.getColumnList(), GenTableColumn::getSimpleJavaField);
        Map<String, String> columnFieldMap = genTable.getColumnList().stream().collect(Collectors.toMap(GenTableColumn::getName, GenTableColumn::getSimpleJavaField));
        return this.parseImportData(row, formNo, parentFormNo, genTable, parentId, currentUser, columnMap, columnFieldMap);
    }

    /**
     * 解析导入数据
     * @param row map对象
     * @param formNo 表单no
     * @param parentFormNo 父表单no
     * @param genTable genTable配置
     * @param parentId 父级id
     * @param currentUser 当前用户
     * @param columnMap 列配置 {getSimpleJavaField:GenTableColumn}
     * @param columnFieldMap 列名映射 {getSimpleJavaField:name}
     * @return
     */
    public Zform parseImportData(Map<String, Object> row,
                                 String formNo,
                                 String parentFormNo,
                                 GenTable genTable,
                                 String parentId,
                                 User currentUser,
                                 Map<String, GenTableColumn> columnMap,
                                 Map<String, String> columnFieldMap) {
        Zform zform = new Zform();
        try {

            String theId = null;
            if (row.containsKey("id") || row.containsKey("ID")) {
                theId = row.get("id") != null ? StringUtil.getString(row.get("id")) : StringUtil.getString(row.get("ID"));
                if (StringUtil.isNotEmpty(theId)) {
                    zform = this.get(theId, genTableService.getGenTableWithDefination(formNo));
                }
            }
            zform.setDelFlag(Global.NO);
            zform.setFormNo(formNo);
            zform.setCreateBy(currentUser);
            //zform.setUpdateBy(currentUser);
            if (Global.YES.equals(genTable.getIsProcessDefinition())) {
                zform.setProcDefKey(this.getFirstProcDefKey(formNo, currentUser.getLoginName()));
            }
            if (StringUtil.isNotEmpty(parentId)) {
                zform.setParent(new Zform(parentId, parentFormNo));
            }
            StringBuilder respondProblemInstructions = new StringBuilder();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (columnMap.containsKey(entry.getKey())) {
                    GenTableColumn genTableColumn = columnMap.get(entry.getKey());
                    String javaField = genTableColumn.getJavaField();
                    if (StringUtil.contains(javaField, "|")) {
                        javaField = javaField.substring(0, javaField.indexOf("."));
                    }
                    Field field = Reflections.getThisField(zform.getClass(), javaField); //zform.getClass().getDeclaredField(javaField);
                    //String methname = javaField.substring(0,1).toUpperCase() + javaField.substring(1);
                    //Method m = zform.getClass().getMethod("set" + methname, java.lang.String);

                    // 如果没有填写值且该字段非必填,则不做处理
                    if (null == entry.getValue() && "1".equals(genTableColumn.getIsNull())) {
                        continue;
                    }
                    // 必填校验
                    if (null == entry.getValue() && "0".equals(genTableColumn.getIsNull())) {
                        if (respondProblemInstructions.length() == 0) {
                            respondProblemInstructions = new StringBuilder(genTableColumn.getComments() + "为必填项不能为空；");
                        } else {
                            respondProblemInstructions.append(genTableColumn.getComments()).append("为必填项不能为空").append("；");
                        }
                        continue;
                    }
                    // 值校验逻辑(下面的逻辑,值必有内容)
                    // 1.正则校验
                    String rule = genTableColumn.getValidateType();
                    if (StringUtil.isNotEmpty(rule) && null != entry.getValue()) {
                        boolean validateBool = ValidatorUtils.validateField(rule, StringUtil.getString(entry.getValue()));
                        if (!validateBool) {
                            if (respondProblemInstructions.length() == 0) {
                                respondProblemInstructions = new StringBuilder(genTableColumn.getComments() + "格式不正确，" + ValidatorUtils.getErrorMessage(rule) + "；");
                            } else {
                                respondProblemInstructions.append(genTableColumn.getComments()).append("格式不正确，").append(ValidatorUtils.getErrorMessage(rule)).append("；");
                            }
                            continue;
                        }
                    }
                    // 2.字段为时间类型,需要校验内容的时间格式
                    String jdbcType = genTableColumn.getJdbcType();
                    if (StringUtil.startsWithIgnoreCase(jdbcType, "DATETIME")
                            || StringUtil.startsWithIgnoreCase(jdbcType, "DATE")
                            || StringUtil.startsWithIgnoreCase(jdbcType, "TIMESTAMP")) {
                        String javaType = genTableColumn.getJavaType();
                        if (StringUtil.equalsIgnoreCase(javaType, "java.util.Date")) {
                            // 时间格式
                            String dateFormat = genTableColumn.getDateType();
                            // 内容
                            String dateValue = StringUtil.getString(entry.getValue());
                            DateResult result = DateValidator.validateAndSuggest(dateValue, dateFormat);
                            Boolean validateBool = result.getIsValid();
                            if (!validateBool) {
                                if (respondProblemInstructions.length() == 0) {
                                    respondProblemInstructions = new StringBuilder(genTableColumn.getComments() + "时间格式不正确，格式：" + dateFormat +"，" + entry.getValue() + "；");
                                } else {
                                    respondProblemInstructions.append(genTableColumn.getComments()).append("时间格式不正确，格式：").append(dateFormat).append("，").append(entry.getValue()).append("；");
                                }
                                continue;
                            }
                        }
                    }
                    // 3.唯一值校验
                    String remarks = genTableColumn.getRemarks();
                    if (!remarks.isEmpty()) {
                        if (remarks.contains("unique")) {
                            JSONObject requestPackage = new JSONObject();
                            JSONObject queryParamType = new JSONObject();
                            JSONObject pageParam = new JSONObject();
                            pageParam.put("pageNo", 1);
                            pageParam.put("pageSize", 1);
                            queryParamType.put(genTableColumn.getName(), "=");
                            requestPackage.put("formNo", formNo);
                            requestPackage.put(genTableColumn.getName(), entry.getValue());
                            requestPackage.put("queryParamType", queryParamType);
                            requestPackage.put("pageParam", pageParam);
                            Zform zformFind = this.getZformFromMap(requestPackage, currentUser.getLoginName(), true);
                            GenTable genTableFind = genTableService.getGenTableWithDefination(zform.getFormNo());
                            Page<Zform> page = new Page<>(1, 1);
                            Page<Zform> pageFind = this.findPageMap(page, zformFind, "path", currentUser.getLoginName(), genTableFind, "1", parentId, "");
                            if (!pageFind.getMap().isEmpty()) {
                                if (respondProblemInstructions.length() == 0) {
                                    respondProblemInstructions = new StringBuilder(genTableColumn.getComments() + "已存在：" + entry.getValue() + "；");
                                } else {
                                    respondProblemInstructions.append(genTableColumn.getComments()).append("已存在：").append(entry.getValue()).append("；");
                                }
                                continue;
                            }
                        }
                    }
                    // 4. 原Excel导入逻辑
                    if (field != null) {
                        field.setAccessible(true);
                        if (javaField.startsWith("sort")) {
                            //sort
                            String value = row.get(genTableColumn.getSimpleJavaField()).toString();
                            if (StringUtil.isNotEmpty(value)) {
                                if (value.endsWith(".0")) {
                                    value = value.substring(0, value.length() - 2);
                                }
                                field.set(zform, Integer.parseInt(value));
                            }
                        } else if (javaField.startsWith("s") || javaField.startsWith("m") || javaField.startsWith("c") || javaField.equals("remarks")) {
                            String value = (String) row.get(genTableColumn.getSimpleJavaField());
                            //value = dictDataService.getDictValues(value, genTableColumn.getDictType(), value, "");
                            field.set(zform, value);
                        } else if (javaField.startsWith("d")) {
                            //Date
                            Object dateObj = row.get(genTableColumn.getSimpleJavaField());
                            if (dateObj instanceof String) {
                                dateObj = DateUtil.strToDate((String) dateObj);
                            }
                            field.set(zform, dateObj);
                        } else if (javaField.startsWith("users")) {

                        } else if (javaField.startsWith("user")) {
                            //user
                            String name = (String) row.get(genTableColumn.getSimpleJavaField());
                            String id = this.getFirstValueByKey("sys_user", "id", "name", name);
                            User obj = new User();
                            if (StringUtil.isNotEmpty(id)) {
                                obj.setId(id);
                            }
                            field.set(zform, obj);
                        } else if (javaField.startsWith("office")) {
                            //office
                            String name = (String) row.get(genTableColumn.getSimpleJavaField());
                            String id = this.getFirstValueByKey("sys_office", "id", "name", name);
                            Office obj = new Office();
                            if (StringUtil.isNotEmpty(id)) {
                                obj.setId(id);
                            }
                            field.set(zform, obj);
                        } else if (javaField.startsWith("area")) {
                            //area
                            String id = (String) row.get(genTableColumn.getSimpleJavaField());
                            Area obj = new Area();
                            if (StringUtil.isNotEmpty(id)) {
                                obj.setId(id);
                            }
                            field.set(zform, obj);
                        } else if (javaField.startsWith("g")) {
                            //gridselect
                            Zform value = new Zform();
                            String id = (String) row.get(genTableColumn.getSimpleJavaField());
                            value.setId(id);
                            field.set(zform, value);
                            String formItemConfig = genTableColumn.getFormItemConfig();
                            if (StringUtil.isNotEmpty(formItemConfig)) {
                                JSONObject formItemConfigObj = JSONObject.parseObject(formItemConfig);
                                value.setFormNo(genTableColumn.getTableName());
                                GenTable tableWithDefination = genTableService.getGenTableWithDefination(genTableColumn.getTableName());
                                LinkedHashMap dataMap = this.getMap(id, tableWithDefination, "", currentUser.getLoginName());
                                if (dataMap != null && formItemConfigObj.containsKey("formControlProps")) {
                                    JSONObject formControlProps = formItemConfigObj.getJSONObject("formControlProps");
                                    if (formControlProps.containsKey("formUpdateMap")) {
                                        JSONObject formUpdateMap = formControlProps.getJSONObject("formUpdateMap");
                                        for (Map.Entry<String, Object> updateEntry : formUpdateMap.entrySet()) {
                                            String targetField = updateEntry.getKey();
                                            String sourceField = ConvertUtil.getString(updateEntry.getValue());
                                            if(!columnFieldMap.containsKey(targetField)){
                                                continue;
                                            }
                                            Field fieldTarget = Reflections.getThisField(zform.getClass(), columnFieldMap.get(targetField));
                                            if (fieldTarget != null) {
                                                fieldTarget.setAccessible(true);
                                                fieldTarget.set(zform, ConvertUtil.getString(dataMap.get(sourceField)));
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (javaField.startsWith("t")) {
                            //tree
                            Zform value = new Zform();
                            value.setId((String) row.get(genTableColumn.getSimpleJavaField()));
                            field.set(zform, value);
                        } else if (javaField.startsWith("parent")) {
                            //parentId
                            Zform value = new Zform();
                            value.setId((String) row.get(genTableColumn.getSimpleJavaField()));
                            field.set(zform, value);
                        } else {
                            String value = (String) row.get(genTableColumn.getSimpleJavaField());
                            field.set(zform, value);
                        }
                    }
                }
            }
            if (respondProblemInstructions.length() > 0) {
                String error = genTable.getRespondProblemInstructions();
                if (null == error) {
                    genTable.setRespondProblemInstructions(respondProblemInstructions.toString());
                } else {
                    genTable.setRespondProblemInstructions(error + "<br>" + respondProblemInstructions);
                }
                return null;
            }
        } catch (Exception e) {
            logger.error("解析导入数据失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException(e.getMessage());
        }
        return zform;
    }

    /**
     *  解析导入数据
     * @param ownerCode 数据ownerCode
     * @param mapList mapList
     * @param formNo 表单no
     * @param parentFormNo 父表单no
     * @param genTable genTable配置
     * @param parentId 父级id
     * @param currentUser 当前用户
     * @return
     */
    public List<Zform> parseImportData(String ownerCode,
                                       List<Map<String, Object>> mapList,
                                       String formNo,
                                       String parentFormNo,
                                       GenTable genTable,
                                       String parentId,
                                       User currentUser){
        Map<String, GenTableColumn> columnMap = ConvertUtil.listToMap(genTable.getColumnList(), GenTableColumn::getSimpleJavaField);
        Map<String, String> columnFieldMap = genTable.getColumnList().stream().collect(Collectors.toMap(GenTableColumn::getName, GenTableColumn::getSimpleJavaField));
        List<Zform> importData = new ArrayList<>();
        // 检验Excel是否有重复数据，如果有重复数据，过滤该数据并提示原因
        this.verifyExcelForDuplicateData(mapList, genTable, columnMap);
        for (Map<String, Object> row : mapList) {
            Zform zform = this.parseImportData(row, formNo, parentFormNo, genTable, parentId, currentUser, columnMap, columnFieldMap);
            if (zform != null) {
                zform.setOwnerCode(ownerCode);
                importData.add(zform);
            }
        }
        return importData;
    }

    private void verifyExcelForDuplicateData(List<Map<String, Object>> mapList,
                                             GenTable genTable,
                                             Map<String, GenTableColumn> columnMap) {
        // 使用列表记录所有需要剔除的记录
        List<Map<String, Object>> rowsToRemove = new ArrayList<>();
        StringBuilder respondProblemInstructions = new StringBuilder();
        Set<Object> processedValues = new HashSet<>(); // 记录已经提示过的重复值

        // 遍历主列表
        for (Map<String, Object> row : mapList) {
            // 遍历当前记录的字段
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 检查字段是否定义在 columnMap 中
                if (columnMap.containsKey(key)) {
                    GenTableColumn genTableColumn = columnMap.get(key);
                    String remarks = genTableColumn.getRemarks();

                    // 增加空值检查
                    if (remarks != null && remarks.contains("unique") && value != null) {
                        // 统计该字段值的重复次数
                        long duplicateCount = mapList.stream()
                                .filter(otherRow -> value.equals(otherRow.get(key)))
                                .count();

                        if (duplicateCount > 1 && !processedValues.contains(value)) { // 如果重复值未处理
                            System.out.println("重复值: " + value + "，重复次数: " + duplicateCount);

                            // 构建提示信息
                            String duplicateMessage = String.format("%s：%s 在Excel数据中重复 %d 次，现已过滤，请检查后重新导入；",
                                    genTableColumn.getComments(), value, duplicateCount);
                            if (respondProblemInstructions.length() > 0) {
                                respondProblemInstructions.append("<br>");
                            }
                            respondProblemInstructions.append(duplicateMessage);

                            // 标记该值为已处理
                            processedValues.add(value);

                            // 标记所有包含重复值的记录用于移除
                            mapList.stream()
                                    .filter(otherRow -> value.equals(otherRow.get(key)))
                                    .forEach(rowsToRemove::add);
                        }
                    }
                }
            }
        }

        // 移除所有标记的记录
        mapList.removeAll(rowsToRemove);

        // 如果有提示信息，将其设置到 genTable 中
        if (respondProblemInstructions.length() > 0) {
            String existingError = genTable.getRespondProblemInstructions();
            if (existingError == null) {
                genTable.setRespondProblemInstructions(respondProblemInstructions.toString());
            } else {
                genTable.setRespondProblemInstructions(existingError + "<br>" + respondProblemInstructions);
            }
        }
    }

    public void importData(String ownerCode,
                           List<ExcelField> importFieldList ,
                           List<Map<String, Object>> mapList,
                           String formNo,
                           String parentFormNo,
                           GenTable genTable,
                           String parentId,
                           String uniqueId,
                           User currentUser) throws Exception {
        ImportParam importParam = new ImportParam(formNo, parentFormNo, uniqueId, null, null, parentId, null, currentUser);
        importData(importParam);
    }
    /**
     * 按导入参数执行动态表单导入，支持扩展服务回调和大文件分批保存。
     */
    public void importData(ImportParam importParam) throws Exception {
        GenTable genTable = importParam.getGenTable();
        String ownerCode = importParam.getOwnerCode();
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            // 支持旧方式
            aroundService.beforeImportData(ownerCode, importParam.getMapList(), importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
            // 新方式
            aroundService.beforeImportData(importParam);
        }

        List<Map<String, Object>> allMapList = importParam.getMapList();
        // 分批小于 1000 条的数据直接整个处理，避免额外分批开销
        final int BATCH_SIZE = 1000;
        int totalDone = 0;
        if (allMapList.size() <= BATCH_SIZE) {
            // 小文件保持原有逻辑不变
            List<Zform> importData = this.parseImportData(ownerCode, allMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getCurrentUser());
            if (aroundService != null) {
                aroundService.beforeSaveImportData(importData, ownerCode, allMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                aroundService.beforeSaveImportData(importData, ownerCode, importParam.getImportFieldList(), allMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                aroundService.beforeSaveImportData(importData, importParam);
            }
            if (Global.YES.equals(genTable.getIsVersion())) {
                for (Zform datum : importData) {
                    this.save(datum, genTable);
                }
            } else {
                superBatchSave(importData, genTable, importParam.getCurrentUser().getLoginName());
            }
            totalDone = importData.size();
            if (aroundService != null) {
                aroundService.afterImportData(importData, ownerCode, allMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                aroundService.afterImportData(importData, importParam);
            }
        } else {
            // 大文件分批处理：每批 BATCH_SIZE 条，处理完即可 GC，避免全量堆展导致 OOM
            logger.info("大文件导入：共 {} 条数据，分批处理，每批 {} 条，formNo={}", allMapList.size(), BATCH_SIZE, importParam.getFormNo());
            for (int i = 0; i < allMapList.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, allMapList.size());
                List<Map<String, Object>> batchMapList = allMapList.subList(i, end);
                List<Zform> batchImportData = this.parseImportData(ownerCode, batchMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getCurrentUser());
                if (aroundService != null) {
                    aroundService.beforeSaveImportData(batchImportData, ownerCode, batchMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                    aroundService.beforeSaveImportData(batchImportData, ownerCode, importParam.getImportFieldList(), batchMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                    aroundService.beforeSaveImportData(batchImportData, importParam);
                }
                if (Global.YES.equals(genTable.getIsVersion())) {
                    for (Zform datum : batchImportData) {
                        this.save(datum, genTable);
                    }
                } else {
                    superBatchSave(batchImportData, genTable, importParam.getCurrentUser().getLoginName());
                }
                totalDone += batchImportData.size();
                if (aroundService != null) {
                    aroundService.afterImportData(batchImportData, ownerCode, batchMapList, importParam.getFormNo(), importParam.getParentFormNo(), genTable, importParam.getParentId(), importParam.getUniqueId(), importParam.getCurrentUser());
                    aroundService.afterImportData(batchImportData, importParam);
                }
                logger.info("导入进度：{}/{}", end, allMapList.size());
            }
        }
        genTable.setDoneCount(totalDone);
    }

    /**
     * 导出动态表单数据为二维行列结构，按字段配置转换字典、日期、用户、机构、区域等显示值。
     */
    public List<List<LinkedHashMap<String, String>>> exportData(Page<Zform> data,
                                                                Zform zform,
                                                                GenTable genTable,
                                                                String loginName) throws Exception {
        String exportList = "," + genTable.getExportList() + ",";
//        String path = "path";
//        String traceFlag = "1";
//        String parentId = "";
//        Page<Zform> data = this.findPage(page, zform, path, loginName, genTable, traceFlag, parentId);
        List<List<LinkedHashMap<String, String>>> list = Lists.newArrayList();
        for (Zform obj : data.getList()) {
            List<LinkedHashMap<String, String>> row = Lists.newArrayList();
            for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                String javaField = genTableColumn.getJavaField();
                if (exportList.indexOf("," + javaField + ",") == -1) continue;
                if (StringUtil.contains(javaField, "|")) {
                    javaField = javaField.substring(0, javaField.indexOf("."));
                }
                Field field = Reflections.getThisField(zform.getClass(), javaField);
                if (field != null) {
                    field.setAccessible(true);
                    Object object = field.get(obj);
                    //if (exportList.indexOf("," + javaField + ",") != -1) {
                    LinkedHashMap<String, String> value = Maps.newLinkedHashMap();
                    if (javaField.startsWith("s") || javaField.startsWith("m") || javaField.startsWith("c")) {
                        //判断其是否为字典值
                        String sValue = "";
                        if (object != null) {
                            sValue = object.toString();
                            sValue = dictDataService.getDictLabels(sValue, genTableColumn.getDictType(), sValue, "");
                        }
                        value.put(genTableColumn.getJavaField(), sValue);
                    } else if (javaField.startsWith("d")) {
                        //Date
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genTableColumn.getDateType());
                        value.put(genTableColumn.getJavaField(), object == null ? "" : simpleDateFormat.format((Date) object));
                    } else if (javaField.startsWith("sort")) {
                        //sort
                        value.put(genTableColumn.getJavaField(), object.toString());
                    } else if (javaField.startsWith("users")) {
                        //users
                        value.put(genTableColumn.getJavaField(), "");
                    } else if (javaField.startsWith("user")) {
                        //user
                        ObjectMapper objectMapper = new ObjectMapper();
                        User user = objectMapper.convertValue(object, User.class);
                        String userName = "";
                        if (user != null) {
                            userName = user.getName();
                        }
                        value.put(genTableColumn.getJavaField(), userName);
                    } else if (javaField.startsWith("office")) {
                        //office
                        ObjectMapper objectMapper = new ObjectMapper();
                        Office office = objectMapper.convertValue(object, Office.class);
                        String officeName = "";
                        if (office != null) {
                            officeName = office.getName();
                        }
                        value.put(genTableColumn.getJavaField(), officeName);
                    } else if (javaField.startsWith("area")) {
                        //area
                        ObjectMapper objectMapper = new ObjectMapper();
                        Area area = objectMapper.convertValue(object, Area.class);
                        String areaName = "";
                        if (area != null) {
                            areaName = area.getName();
                        }
                        value.put(genTableColumn.getJavaField(), areaName);
                    } else if (javaField.startsWith("g")) {
                        //gridselect
                        ObjectMapper objectMapper = new ObjectMapper();
                        Zform grid = objectMapper.convertValue(object, Zform.class);
                        String sValue = "";
                        if (StringUtil.isNotEmpty(grid.getId())) {
                            sValue = grid.getName();
                        }
                        value.put(genTableColumn.getJavaField(), sValue);
                    } else if (javaField.startsWith("t")) {
                        //tree
                        value.put(genTableColumn.getJavaField(), object.toString());
                    } else {
                        value.put(genTableColumn.getJavaField(), object == null ? "" : object.toString());
                    }
                    row.add(value);
                    //}
                }
            }
            list.add(this.sortExportRow(row, genTable.getExportList()));
        }
        return list;
    }

    /**
     * 导出动态表单数据为 Map 列表，适用于 Excel 导出和复杂字段扁平化处理。
     */
    public List<Map<String, Object>> exportDataMap(Page<Zform> data,
                                                   Zform zform,
                                                   GenTable genTable,
                                                   String loginName) throws Exception {
        Map<String, Object>[] rowArr =new Map[data.getList().size()];
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            aroundService.beforeExportDataMap(data, zform, genTable, loginName);
        }

        logger.info("导出excel-线程池1开启");
        final CountDownLatch countDownLatch = new CountDownLatch(data.getList().size()); //
        ExecutorService executor = Executors.newFixedThreadPool(8);
        int k = 0;
        for (Zform obj : data.getList()) {
            int finalK = k;
            executor.submit(() -> {
                try {
                    if (obj == null) {
                        rowArr[finalK] = Maps.newLinkedHashMap();
                        return;
                    }
                    Map<String, Object> value = Maps.newLinkedHashMap();
                    for (GenTableColumn genTableColumn : genTable.getColumnList()) {
                        String javaField = genTableColumn.getJavaField();
                        if (StringUtil.contains(javaField, "|")) {
                            javaField = javaField.substring(0, javaField.indexOf("."));
                        }
                        Field field = Reflections.getThisField(zform.getClass(), javaField);
                        if (field != null) {
                            field.setAccessible(true);
                            Object object = field.get(obj);
                            if (object == null) {
                                continue;
                            }
                            if ("delFlag".equals(javaField)) {
                                continue;
                            }

                            if (javaField.startsWith("s") || javaField.startsWith("m") || javaField.startsWith("c")) {
                                //判断其是否为字典值
                                String sValue = "";
                                if (object != null) {
                                    sValue = object.toString();
                                    sValue = dictDataService.getDictLabels(sValue, genTableColumn.getDictType(), sValue, "");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), sValue);
                            } else if (javaField.startsWith("d")) {
                                //Date
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genTableColumn.getDateType());
                                value.put(genTableColumn.getSimpleJavaField(), object == null ? "" : simpleDateFormat.format((Date) object));
                            } else if (javaField.startsWith("sort")) {
                                //sort
                                value.put(genTableColumn.getSimpleJavaField(), object.toString());
                            } else if (javaField.startsWith("users")) {
                                //users
                                value.put(genTableColumn.getSimpleJavaField(), "");
                            } else if (javaField.startsWith("user")) {
                                //user
                                JSONObject user = JSONHelper.toJSONObject(object);
                                String userName = "";
                                if (user != null) {
                                    userName = user.getString("name");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), userName);
                            } else if (javaField.startsWith("office")) {
                                //office
                                JSONObject office = JSONHelper.toJSONObject(object);
                                String officeName = "";
                                if (office != null) {
                                    officeName = office.getString("name");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), officeName);
                            } else if (javaField.startsWith("area")) {
                                //area
                                JSONObject area = JSONHelper.toJSONObject(object);
                                String areaName = "";
                                if (area != null) {
                                    areaName = area.getString("name");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), areaName);
                            } else if (javaField.startsWith("g")) {
                                //gridselect
                                JSONObject grid = JSONHelper.toJSONObject(object);
                                String sValue = "";
                                if (StringUtil.isNotEmpty(grid.getString("id"))) {
                                    sValue = grid.getString("name");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), sValue);
                            } else if (javaField.startsWith("t")) {
                                //tree
                                value.put(genTableColumn.getSimpleJavaField(), object.toString());
                            } else if (javaField.equals("parent")) {
                                //parent
                                JSONObject grid = JSONHelper.toJSONObject(object);
                                String sValue = "";
                                if (StringUtil.isNotEmpty(grid.getString("id"))) {
                                    sValue = grid.getString("name");
                                }
                                value.put(genTableColumn.getSimpleJavaField(), sValue);
                            } else {
                                value.put(genTableColumn.getSimpleJavaField(), object.toString());
                            }

                            //}
                        }
                    }
                    rowArr[finalK] =value;
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
        logger.info("导出excel-线程池1关闭");
        if (aroundService != null) {
            return aroundService.afterExportDataMap(Arrays.asList(rowArr), data, zform, genTable, loginName);
        }
        return Arrays.asList(rowArr);
    }

    private List<LinkedHashMap<String, String>> sortExportRow(List<LinkedHashMap<String, String>> row, String columnList) {
        List<LinkedHashMap<String, String>> result = Lists.newArrayList();
        String[] columnListArray = columnList.split(",");
        for (int i = 0; i < columnListArray.length; i++) {
            for (LinkedHashMap<String, String> linkedHM : row) {
                for (Map.Entry<String, String> entry : linkedHM.entrySet()) {
                    if (columnListArray[i].equals(entry.getKey())) {
                        LinkedHashMap<String, String> obj = Maps.newLinkedHashMap();
                        obj.put(entry.getKey(), entry.getValue());
                        result.add(obj);
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected String getFirstValueByKey(String formNo, String valueColumn, String keyColumn, String key) {
        List<String> valueList = zformDao.findValueList(formNo, valueColumn, keyColumn, key);
        String value = "";
        if (valueList.size() > 0) {
            value = valueList.get(0);
        }
        return value;
    }

    public List<LinkedHashMap> treeDataMobile(String formNo, String parentId) {
        String sql;
        String hasChildren = "";
        Map<String, Object> param = new java.util.HashMap<>();
        
        if(TREE_TYPE_AREA.equals(formNo)) {
            if (StringUtil.isNotEmpty(parentId)) {
                hasChildren = " ,(select case when exists(select 1 from sys_area where parent_id = a.id) then 1 else 0 end from sys_dual) AS \"hasChildren\" ";
                sql = "select a.id value, a.name \"label\", a.parent_id \"parentId\", '1' opt " + hasChildren + " from sys_area a where a.del_flag = '0' and a.id != '1' and a.parent_id = #{param.parentId} order by a.sort ";
                param.put("parentId", parentId);
            } else {
                parentId = "1";
                sql = "select a.id value, a.name \"label\", a.parent_id \"parentId\", '1' opt from sys_area a where a.del_flag = '0' and a.id != '1' order by a.sort ";
            }
            List<LinkedHashMap> resultList = zformDao.findMapBySqlParm(sql, param);
            List<LinkedHashMap> tree = this.buildTree(resultList, parentId);
            return tree;
        } else if(TREE_TYPE_OFFICE.equals(formNo)) {
            if (StringUtil.isNotEmpty(parentId)) {
                hasChildren = " ,(select case when exists(select 1 from sys_office where parent_id = a.id) then 1 else 0 end from sys_dual) AS \"hasChildren\" ";
                sql = "select a.id value, a.name \"label\", a.parent_id parentId, '1' opt " + hasChildren + " from sys_office a where a.del_flag = '0' and a.useable = '1' and a.parent_id = #{param.parentId} order by a.sort ";
                param.put("parentId", parentId);
            } else {
                parentId = "0";
                sql = "select a.id value, a.name \"label\", a.parent_id parentId, '1' opt from sys_office a where a.del_flag = '0' and a.useable = '1' order by a.sort ";
            }
            List<LinkedHashMap> resultList = zformDao.findMapBySqlParm(sql, param);
            List<LinkedHashMap> tree = this.buildTree(resultList, parentId);
            return tree;
        } else if (TREE_TYPE_USER.equals(formNo)) {
            if (StringUtil.isNotEmpty(parentId)) {
                hasChildren = " ,(select case when exists(select 1 from sys_office where parent_id = a.id) then 1 when exists(select 1 from sys_user where parent_id = a.id) then 1 else 0 end from sys_dual) AS \"hasChildren\" ";
                sql = "select b.value, b.\"label\", b.parentId, b.opt, b.hasChildren from ( ";
                sql += "select a.id value, a.name \"label\", a.parent_id parentId, '0' opt, a.sort " + hasChildren + " from sys_office a where a.del_flag = '0' and a.useable = '1' and a.parent_id = #{param.parentId}";
                sql += " union select c.id value, c.name \"label\", c.parent_id parentId, '1' opt, c.sort, 0 AS \"hasChildren\" from sys_user c where c.del_flag = '0' and c.useable = '1' and (c.is_sys is null or c.is_sys != '1') and c.parent_id = #{param.parentId}";
                sql += ") b order by b.sort";
                param.put("parentId", parentId);
            } else {
                parentId = "0";
                sql = "select b.value, b.\"label\", b.parentId, b.opt from ( ";
                sql += "select a.id value, a.name \"label\", a.parent_id parentId, '0' opt, a.sort from sys_office a where a.del_flag = '0' and a.useable = '1'";
                sql += " union select c.id value, c.name \"label\", c.parent_id parentId, '1' opt, c.sort from sys_user c where c.del_flag = '0' and c.useable = '1' and (c.is_sys is null or c.is_sys != '1') ";
                sql += ") b order by b.sort";
            }
            List<LinkedHashMap> resultList = zformDao.findMapBySqlParm(sql, param);
            List<LinkedHashMap> tree = this.buildTree(resultList, parentId);
            return tree;
        } else {
            // 验证表名是否为合法SQL标识符，防止SQL注入
            SqlInjectionUtil.validateIdentifier(formNo);
            //树表，id, name, sort
            if (StringUtil.isNotEmpty(parentId)) {
                hasChildren = " ,(select case when exists(select 1 from " + formNo + " where parent_id = a.id) then 1 else 0 end from sys_dual) AS \"hasChildren\" ";
                sql = "select a.id value, a.name label, a.parent_id parentId, '1' opt " + hasChildren + " from " + formNo + " a where a.del_flag = '0' and a.parent_id = #{param.parentId} order by a.sort ";
                param.put("parentId", parentId);
            } else {
                parentId = "0";
                sql = "select a.id value, a.name label, a.parent_id parentId, '1' opt from " + formNo + " a where a.del_flag = '0' order by a.sort ";
            }
            List<LinkedHashMap> resultList = zformDao.findMapBySqlParm(sql, param);
            List<LinkedHashMap> tree = this.buildTree(resultList, parentId);
            return tree;
        }
    }

    private List<LinkedHashMap> buildTree(List<LinkedHashMap> nodes, Object parentId) {
        List<LinkedHashMap> tree = null;
        for (LinkedHashMap node : nodes) {
            Object nodeParentId = node.get("parentId");
            Object nodeHasChildren = node.get("hasChildren");
            if ((parentId == null && nodeParentId == null) || (parentId != null && parentId.equals(nodeParentId))) {
                LinkedHashMap treeNode = Maps.newLinkedHashMap();
                treeNode.put("value", node.get("value"));
                treeNode.put("label", node.get("label"));
                if (nodeHasChildren != null) {
                    treeNode.put("hasChildren", Global.YES.equals(StringUtil.getString(nodeHasChildren)));
                }
                List<LinkedHashMap> subTree = buildTree(nodes, node.get("value"));
                if (Global.NO.equals(StringUtil.getString(node.get("opt")))) {
                    //0 机构节点，查找人节点，查不到则禁用
                    if (subTree == null || subTree.size() == 0) {
                        treeNode.put("disabled", true);
                    } else {
                        treeNode.put("children", subTree);
                    }
                } else {
                    treeNode.put("children", subTree);
                }
                if (tree == null) tree = Lists.newArrayList();
                tree.add(treeNode);
            }
        }
        return tree;
    }

    public void replaceFilterDataValue(User currentUser, GridselectParam.FilterData filterData) {
        if (currentUser != null && filterData != null) {
            if (filterData.getChildren()!=null && filterData.getChildren().size()>0) {
                for (GridselectParam.FilterData child : filterData.getChildren()) {
                    this.replaceFilterDataValue(currentUser, child);
                }
            }else{
                String ownerCodeCompany = currentUser.getCompany().getCode();
                //{office}
                String ownerCodeOffice = currentUser.getOffice().getCode();
                String companyAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getCompany().getArea().getCode();
                String officeAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getOffice().getArea().getCode();
                List<Role> roleList = currentUser.getRoleList();
                String roles="";
                if(roleList!=null){
                    List<String> roleIdList = new LinkedList<>();
                    roleList.forEach(r-> roleIdList.add(r.getId()));
                    roles = String.join(",",roleIdList);
                }

                String expression = StringUtil.getString(filterData.getValue());
                if (expression.contains("{company}")
                        || expression.contains("{office}")
                        || expression.contains("{companyAreaCode}")
                        || expression.contains("{officeAreaCode}")
                        || expression.contains("{roles}")) {
                    expression = expression.replaceAll("\\{company\\}", ownerCodeCompany);
                    expression = expression.replaceAll("\\{office\\}", ownerCodeOffice);
                    expression = expression.replaceAll("\\{companyAreaCode\\}", companyAreaCode);
                    expression = expression.replaceAll("\\{officeAreaCode\\}", officeAreaCode);
                    expression = expression.replaceAll("\\{roles\\}", roles);
                    filterData.setValue(expression);
                }
                expression = StringUtil.getString(filterData.getValue2());
                if (expression.contains("{company}")
                        || expression.contains("{office}")
                        || expression.contains("{companyAreaCode}")
                        || expression.contains("{officeAreaCode}")
                        || expression.contains("{roles}")) {
                    expression = expression.replaceAll("\\{company\\}", ownerCodeCompany);
                    expression = expression.replaceAll("\\{office\\}", ownerCodeOffice);
                    expression = expression.replaceAll("\\{companyAreaCode\\}", companyAreaCode);
                    expression = expression.replaceAll("\\{officeAreaCode\\}", officeAreaCode);
                    expression = expression.replaceAll("\\{roles\\}", roles);
                    filterData.setValue2(expression);
                }
            }
        }
    }

    // 设置数据权限
    public void permissionDataSet(User currentUser,String tableName,QueryWrapper queryWrapper){
        if (currentUser == null){
            return;
        }
        //Data Permission
        //{company}
        String ownerCodeCompany = currentUser.getCompany().getCode();
        //{office}
        String ownerCodeOffice = currentUser.getOffice().getCode();
        String companyAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getCompany().getArea().getCode();
        String officeAreaCode = currentUser.getCompany().getArea() == null ? "" : currentUser.getOffice().getArea().getCode();

        List<Role> roleList = currentUser.getRoleList();
        String roles="";
        if(roleList!=null){
            List<String> roleIdList = new LinkedList<>();
            roleList.forEach(r-> roleIdList.add(r.getId()));
            roles = String.join(",",roleIdList);
        }
        for (Datapermission datapermission : currentUser.getDatapermissionList()) {
            if (datapermission.getMainTable().equalsIgnoreCase(tableName)) {
                String expression = datapermission.getExpression();
                expression = expression.replaceAll("\\{company\\}", ownerCodeCompany);
                expression = expression.replaceAll("\\{office\\}", ownerCodeOffice);
                expression = expression.replaceAll("\\{companyAreaCode\\}", companyAreaCode);
                expression = expression.replaceAll("\\{officeAreaCode\\}", officeAreaCode);
                expression = expression.replaceAll("\\{roles\\}", roles);
                expression = expression.replaceAll("\\{userId\\}", currentUser.getId());
                queryWrapper.apply(expression);
            }
        }
    }
    /**
     * 将前端提交的动态表单 JSON 转换为 Zform 对象。
     */
    public Zform getZformFromMap(JSONObject zformMap, String loginName) throws Exception {
        return this.getZformFromMap(zformMap, loginName, false);
    }
    /**
     * 将前端提交的动态表单 JSON 转换为 Zform 对象，查询场景会额外处理加密字段查询条件。
     */
    public Zform getZformFromMap(JSONObject zformMap, String loginName, Boolean isSearch) throws Exception {
        String formNo = zformMap.getString("formNo");
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        String isAroundBreak = zformMap.getString("isAroundBreak");
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(formNo);
        Consumer<QueryWrapper<Zform>> consumer = null;
        if (aroundService != null && StringUtil.isEmpty(isAroundBreak)) {
            aroundService.beforeGetZformFromMap(zformMap, loginName, false);
            aroundService.beforeGetZformFromMapSearch(zformMap, loginName, isSearch);
            //潜规则 1：如果有queryWrapperConsumer，就添加到queryWrapper中
            if (zformMap.containsKey("queryWrapperConsumer")) {
                consumer = (Consumer<QueryWrapper<Zform>>) zformMap.get("queryWrapperConsumer");
                zformMap.remove("queryWrapperConsumer");
            }
        }
        Zform zform = this.getZformFromMapAction(zformMap, loginName, false);
        if (consumer != null) {
            zform.getQueryWrapper().and(consumer);
        }
        zform.setZformMap(zformMap);
        // 兼容说明：仅在查询时对条件字段做加密查询处理。
        if (isSearch){
            // 根据查询条件是否需要将查询字段改为查询加密字段数据
            this.encryptData(zform, genTable);
        }
        return zform;
    }

    /**
     * 执行 JSON 到 Zform 的字段级映射，处理对象字段、日期范围、子表和查询包装器。
     */
    public Zform getZformFromMapAction(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        Zform zform = new Zform();
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        if (queryWrapper == null) {
            queryWrapper = new QueryWrapper();
        }
        String formNo = null;
        Set<String> it = zformMap.keySet();
        JSONObject zformMapFromReq = new JSONObject();
        cn.hutool.core.bean.BeanUtil.copyProperties(zformMap, zformMapFromReq);
        if (it.contains("formNo")) {
            formNo = zformMap.getString("formNo");
            zform.setFormNo(formNo);
            GenTable genTable = genTableService.getGenTableWithDefination(formNo);

            String value = null;
            JSONObject jsonValue = null;
            for (GenTableColumn column : genTable.getColumnList()) {
                if (it.contains(column.getName())) {
                    if (column.getJavaFieldId().contains(".") && false == Global.YES.equals(column.getSelectSimple())) {
                        Object v = zformMap.get(column.getName());
                        zformMap.remove(column.getName());
                        zformMapFromReq.remove(column.getName());
                        if (v instanceof Map) {
                            zformMap.put(column.getSimpleJavaField(), v);
                        } else {
                            jsonValue = new JSONObject();
                            jsonValue.put("id", v);
                            zformMap.put(column.getSimpleJavaField(), jsonValue);
                        }
                    } else {
                        value = zformMap.getString(column.getName());
                        zformMap.remove(column.getName());
                        zformMapFromReq.remove(column.getName());
                        if (value != null) {
                            zformMap.put(column.getJavaField(), value);
                        }
                    }
                } else {
                    //处理日期范围查询条件 e.g. beginReg_date endReg_date，换成beginD01 endD01
                    if (StringUtil.isNotEmpty(column.getDateType())) {
                        String beginName = "begin" + StringUtil.toUpperCaseFirstOne(column.getName());
                        String endName = "end" + StringUtil.toUpperCaseFirstOne(column.getName());
                        if (it.contains(beginName)) {
                            value = zformMap.getString(beginName);
                            zformMap.remove(beginName);
                            zformMapFromReq.remove(beginName);
                            zformMap.put("begin" + StringUtil.toUpperCaseFirstOne(column.getJavaField()), value);
                        }
                        if (it.contains(endName)) {
                            value = zformMap.getString(endName);
                            zformMap.remove(endName);
                            zformMapFromReq.remove(endName);
                            zformMap.put("end" + StringUtil.toUpperCaseFirstOne(column.getJavaField()), value);
                        }
                    }
                }
            }
            //特殊处理filterDataArr参数
            JSONArray filterDataArr = zformMap.getJSONArray("filterDataArr");
            if (filterDataArr != null) {
                List<GridselectParam.FilterData> filterList = JSONArray.parseArray(filterDataArr.toJSONString(), GridselectParam.FilterData.class);
                if (filterList != null && filterList.size() > 0) {
                    for (GridselectParam.FilterData filterData : filterList) {
                        replaceFilterDataValue(UserUtil.getCurrentUser(), filterData);
                        filterDataChild(queryWrapper, filterData);
                    }
                }
            }
            JSONObject queryParamType = zformMap.getJSONObject("queryParamType");
            if (queryParamType == null) {
                queryParamType = new JSONObject();
            }
            List<String> dateBetweenList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : zformMapFromReq.entrySet()) {
                String key = entry.getKey();
                if ("orderBy".equals(key) || "formNo".equals(key) || "pageParam".equals(key) || "queryParamType".equals(key) || "userTaskKey".equals(key) || "filterDataArr".equals(key)) {
                    continue;
                }
                // 已知限制：非主表字段的 like 查询仍依赖请求字段名动态解析。
                if (!StringUtil.isEmpty(entry.getValue())) {
                    SqlInjectionUtil.filterContent(key);
                    Object queryValue = entry.getValue();
                    if (queryValue instanceof Map) {
                        queryValue = ((Map) queryValue).get("id");
                        if (StringUtil.isEmpty(queryValue)) {
                            continue;
                        }
                    }
                    if (queryParamType.containsKey(key) && !StringUtil.isEmpty(queryParamType.getString(key)) && QueryTypeEnum.between.name().equals(queryParamType.getString(key))) {
                        String tempKey = key.replaceFirst("begin", "").replaceFirst("end", "");
                        //第一个字母改为小写
                        key = tempKey.substring(0, 1).toLowerCase() + tempKey.substring(1);
                        if (zformMapFromReq.containsKey("begin" + tempKey) && zformMapFromReq.containsKey("end" + tempKey) && !dateBetweenList.contains(key)) {
                            this.addFilter(queryWrapper, QueryTypeEnum.between, key, zformMapFromReq.get("begin" + tempKey), zformMapFromReq.get("end" + tempKey));
                            dateBetweenList.add(key);
                        }
                    } else if (queryParamType.containsKey(key) && !StringUtil.isEmpty(queryParamType.getString(key))) {
                        this.addFilter(queryWrapper, QueryTypeEnum.valueOf(queryParamType.getString(key)), key, queryValue, null);
                    } else {
                        this.addFilter(queryWrapper, QueryTypeEnum.eq, key, queryValue, null);
                    }
                }
            }

            //处理子表
            if (genTable.isHasChildren()) {
                int i = 0;
                for (GenTable childGenTable : genTable.getChildList()) {
                    i++;
                    if (!zformMap.containsKey(childGenTable.getName())) {
                        continue;
                    }
                    Object childrenObj = zformMap.get(childGenTable.getName());
                    if (childrenObj instanceof ArrayList) {
                        List children = zformMap.getObject(childGenTable.getName(), ArrayList.class);
                        List<Zform> childList = Lists.newArrayList();
                        for (int j = 0; j < children.size(); j++) {
                            JSONObject jsonObject = JSONHelper.toJSONObject(children.get(j));
                            jsonObject.put("formNo", childGenTable.getName());
                            childList.add(getZformFromMap(jsonObject, loginName));
                        }
                        if (i >= 10) {
                            zformMap.put("childrenList" + i, childList);
                        } else {
                            zformMap.put("childrenList0" + i, childList);
                        }
                    } else if (childrenObj instanceof JSONArray) {
                        JSONArray children = zformMap.getObject(childGenTable.getName(), JSONArray.class);
                        List<Zform> childList = Lists.newArrayList();
                        for (int j = 0; j < children.size(); j++) {
                            JSONObject jsonObject = children.getJSONObject(j);
                            jsonObject.put("formNo", childGenTable.getName());
                            childList.add(getZformFromMap(jsonObject, loginName));

                        }
                        zformMap.put("childrenList0" + i, childList);
                    }
                }
            }
            if (false == isChildren) {
                //处理树表，向下一级，当isChildren = true时，即本身就是下一级子表数据时，不再继续向下处理
                if (genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE) && it.contains(genTable.getName())) {
                    JSONArray children = zformMap.getObject(genTable.getName(), JSONArray.class);
                    if (children != null) {
                        List<Zform> childrenList = Lists.newArrayList();
                        for (int j = 0; j < children.size(); j++) {
                            JSONObject jsonObject = children.getJSONObject(j);
                            childrenList.add(getZformFromMap(jsonObject, loginName, true));
                        }
                        zformMap.put("children", childrenList);
                    }
                }
            }
        }
        //zform = JSON.toJavaObject(zformMap, Zform.class);
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.putDeserializer(Date.class, new MyDateDeserializer());
        zform = TypeUtils.cast(zformMap, Zform.class, parserConfig);
        if (StringUtil.isNotEmpty(StringUtil.getString(zformMap.get("procInsId")))) {
            zform.setProcInsId(StringUtil.getString(zformMap.get("procInsId")));
        }
        if (StringUtil.isNotEmpty(StringUtil.getString(zformMap.get("procDefKey")))) {
            zform.setProcDefKey(StringUtil.getString(zformMap.get("procDefKey")));
        }
        if (zformMap.get("act") != null) {
            zform.setAct(JSON.toJavaObject(zformMap.getJSONObject("act"), Act.class));
        }
        zform.setQueryWrapper(queryWrapper);
        return zform;
    }

}
