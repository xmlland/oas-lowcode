package com.jeestudio.bpm.service.gen;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.*;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.gen.dialect.AbstractGenDialect;
import com.jeestudio.bpm.gen.dialect.DorisGenDialect;
import com.jeestudio.bpm.gen.enums.GenKey;
import com.jeestudio.bpm.mapper.base.gen.*;
import com.jeestudio.bpm.mapper.base.system.AreaDao;
import com.jeestudio.bpm.mapper.base.system.OfficeDao;
import com.jeestudio.bpm.service.ai.TransService;
import com.jeestudio.bpm.service.common.DatahouseService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.enums.ImportValidEnum;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.base.utils.JSONHelper;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.excel.ExcelImportUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Types;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 表单配置与代码生成服务
 */
@Service
@Slf4j
public class GenTableService {

    protected static final Logger logger = LoggerFactory.getLogger(GenTableService.class);

    @Value("${versionSchema}")
    protected String versionSchema;

    @Autowired
    private GenTableDao genTableDao;

    @Autowired
    private GenTableDeriveConfigDao genTableDeriveConfigDao;

    @Autowired
    private GenTableColumnDao genTableColumnDao;

    @Autowired
    private GenDataBaseDictDao genDataBaseDictDao;

    @Autowired
    OfficeDao officeDao;

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private GenSchemeDao genSchemeDao;

    @Autowired
    private TransService transService;

    @Lazy
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    DatahouseService datahouseService;

    @Autowired
    private DynamicRoutingDataSource dataSource;

    @Resource(name = "taskExecutor")
    private TaskExecutor taskExecutor;

    private static final List<String> SOURCE_SQL_DANGEROUS_KEYWORDS = Arrays.asList(
            " insert ", " update ", " delete ", " drop ", " alter ", " truncate ",
            " create ", " grant ", " revoke ", " execute ", " call ", " merge ",
            " replace ", " comment ", " vacuum ", " analyze "
    );

    /*@Value("${spring.datasource.dbType}")*/
    private String dbType=DbTypeUtil.getDbType();

    /**
     * 根据表单编号读取完整表单定义，包含子表、字段列表和动态 SQL 映射，并写入配置缓存。
     */
    public GenTable getGenTableWithDefination(String formNo) {
        Object cachedGenTable = cacheUtil.getHashCache(GenUtil.GENTABLE_CACHE, formNo);
        GenTable genTable = cachedGenTable == null ? null : JsonConvertUtil.gsonBuilder().fromJson((String) cachedGenTable, new TypeToken<GenTable>() {
        }.getType());
        if (genTable == null) {
            genTable = genTableDao.get(formNo);
            if (genTable != null) {
                attachDeriveConfig(genTable);
                GenTable theGenTable = new GenTable();
                theGenTable.setParentTable(genTable.getName());
                List<GenTable> childList = genTableDao.findList(theGenTable);
                genTable.setChildList(childList);
                GenTableColumn genTableColumn = new GenTableColumn();
                genTableColumn.setGenTable(new GenTable(genTable.getId()));
                List<GenTableColumn> genTableColumnList = this.genTableColumnDao.findList(genTableColumn);
                if (genTableColumnList.size() == 4) {
                    //多对多关系表，去掉id和delFlag
                    genTable.setColumnList(Lists.newArrayList());
                    for (GenTableColumn column : genTableColumnList) {
                        if (false == column.getName().equalsIgnoreCase("id") && false == column.getName().equalsIgnoreCase("del_flag")) {
                            genTable.getColumnList().add(column);
                        }
                    }
                } else {
                    genTable.setColumnList(genTableColumnList);
                }
                if (StringUtil.isEmpty(genTable.getSqlColumns())) {
                    GenUtil.buildSqlMapForDynamicTable(genTable, dbType);
                    this.save(genTable);
                }
                cacheUtil.setHashCache(GenUtil.GENTABLE_CACHE, formNo, JsonConvertUtil.objectToJson(genTable));
            }
        }
        if (genTable != null) {
            attachDeriveConfig(genTable);
        }
        return genTable;
    }

    /**
     * Get gentable by id
     *
     * @param id
     * @return gentable
     */
    public GenTable get(String id) {
        GenTable genTable = this.genTableDao.get(id);
        attachDeriveConfig(genTable);
        GenTableColumn genTableColumn = new GenTableColumn();
        genTableColumn.setGenTable(new GenTable(genTable.getId()));
        genTable.setColumnList(this.genTableColumnDao.findList(genTableColumn));
        return genTable;
    }

    /**
     * 解析视图或统计表来源 SQL 的返回字段。
     *
     * <p>这里只读取 JDBC 元数据，不读取真实业务数据。前端拿到字段草稿后，
     * 再由用户确认是否增量添加到表单设计器。</p>
     */
    public ResultJson parseSourceSqlFields(JSONObject reqBody) {
        String sourceSql = reqBody == null ? "" : ConvertUtil.getString(reqBody.get("sourceSql"));
        String normalizedSql = normalizeSourceSqlForMetadata(sourceSql);
        String metadataSql = buildSourceMetadataSql(normalizedSql);
        JSONArray fields = new JSONArray();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(metadataSql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            Set<String> usedNames = new HashSet<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = normalizeSourceFieldName(metaData.getColumnLabel(i), metaData.getColumnName(i), i);
                String uniqueName = buildUniqueSourceFieldName(columnName, usedNames);
                int jdbcType = metaData.getColumnType(i);
                JSONObject field = new JSONObject(true);
                field.put("name", uniqueName);
                field.put("rawName", columnName);
                field.put("label", inferSourceFieldLabel(uniqueName));
                field.put("jdbcTypeCode", jdbcType);
                field.put("jdbcType", inferJdbcType(metaData, i, jdbcType));
                field.put("javaType", inferJavaType(jdbcType));
                field.put("showType", inferShowType(uniqueName, jdbcType));
                field.put("queryType", inferQueryType(uniqueName, jdbcType));
                field.put("hidden", isSystemHiddenSourceField(uniqueName));
                field.put("dictCandidate", isDictionaryCandidate(uniqueName));
                fields.add(field);
            }
            if (fields.isEmpty()) {
                fields.addAll(parseSimpleSourceSqlFields(normalizedSql));
            }
        } catch (Exception e) {
            logger.error("Parse source sql fields failed: {}", ExceptionUtils.getStackTrace(e));
            return ResultJson.failed("来源SQL解析失败：" + e.getMessage());
        }
        return ResultJson.success("来源SQL解析成功").put("fields", fields);
    }

    private String normalizeSourceSqlForMetadata(String sourceSql) {
        String normalized = sourceSql == null ? "" : sourceSql.trim();
        if (StringUtil.isBlank(normalized)) {
            throw new BusinessException("来源SQL不能为空");
        }
        while (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        String lowerSql = normalized.toLowerCase(Locale.ROOT);
        String checkSql = " " + lowerSql.replaceAll("\\s+", " ") + " ";
        if (!checkSql.startsWith(" select ") && !checkSql.startsWith(" with ")) {
            throw new BusinessException("来源SQL只允许使用 select 或 with 查询语句");
        }
        if (checkSql.contains(";")) {
            throw new BusinessException("来源SQL不允许包含多条语句");
        }
        for (String keyword : SOURCE_SQL_DANGEROUS_KEYWORDS) {
            if (checkSql.contains(keyword)) {
                throw new BusinessException("来源SQL包含不允许的内容：" + keyword.trim());
            }
        }
        return normalized;
    }

    private String buildSourceMetadataSql(String normalizedSql) {
        return "SELECT * FROM (" + normalizedSql + ") source_sql_meta WHERE 1 = 0";
    }

    private JSONArray parseSimpleSourceSqlFields(String normalizedSql) {
        JSONArray fields = new JSONArray();
        String selectBody = extractTopLevelSelectBody(normalizedSql);
        if (StringUtil.isBlank(selectBody)) {
            return fields;
        }
        List<String> expressions = splitTopLevelSelectExpressions(selectBody);
        Set<String> usedNames = new HashSet<>();
        int index = 1;
        for (String expression : expressions) {
            String fieldName = normalizeSourceFieldName(inferFieldNameFromSelectExpression(expression, index), null, index);
            String uniqueName = buildUniqueSourceFieldName(fieldName, usedNames);
            JSONObject field = new JSONObject(true);
            field.put("name", uniqueName);
            field.put("rawName", fieldName);
            field.put("label", inferSourceFieldLabel(uniqueName));
            field.put("jdbcTypeCode", Types.VARCHAR);
            field.put("jdbcType", "varchar(255)");
            field.put("javaType", "String");
            field.put("showType", inferShowType(uniqueName, Types.VARCHAR));
            field.put("queryType", inferQueryType(uniqueName, Types.VARCHAR));
            field.put("hidden", isSystemHiddenSourceField(uniqueName));
            field.put("dictCandidate", isDictionaryCandidate(uniqueName));
            fields.add(field);
            index++;
        }
        return fields;
    }

    private String extractTopLevelSelectBody(String sql) {
        String lowerSql = sql.toLowerCase(Locale.ROOT);
        int selectIndex = findTopLevelKeyword(lowerSql, "select", 0);
        if (selectIndex < 0) {
            return "";
        }
        int fromIndex = findTopLevelKeyword(lowerSql, "from", selectIndex + 6);
        if (fromIndex < 0 || fromIndex <= selectIndex) {
            return "";
        }
        return sql.substring(selectIndex + 6, fromIndex);
    }

    private int findTopLevelKeyword(String lowerSql, String keyword, int start) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = start; i <= lowerSql.length() - keyword.length(); i++) {
            char ch = lowerSql.charAt(i);
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (ch == '(') {
                    depth++;
                } else if (ch == ')' && depth > 0) {
                    depth--;
                } else if (depth == 0 && lowerSql.startsWith(keyword, i) && isKeywordBoundary(lowerSql, i, keyword.length())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isKeywordBoundary(String text, int start, int length) {
        boolean left = start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1)) && text.charAt(start - 1) != '_';
        int end = start + length;
        boolean right = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end)) && text.charAt(end) != '_';
        return left && right;
    }

    private List<String> splitTopLevelSelectExpressions(String selectBody) {
        List<String> expressions = new ArrayList<>();
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int start = 0;
        for (int i = 0; i < selectBody.length(); i++) {
            char ch = selectBody.charAt(i);
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (ch == '(') {
                    depth++;
                } else if (ch == ')' && depth > 0) {
                    depth--;
                } else if (ch == ',' && depth == 0) {
                    String expression = selectBody.substring(start, i).trim();
                    if (StringUtil.isNotBlank(expression)) {
                        expressions.add(expression);
                    }
                    start = i + 1;
                }
            }
        }
        String expression = selectBody.substring(start).trim();
        if (StringUtil.isNotBlank(expression)) {
            expressions.add(expression);
        }
        return expressions;
    }

    private String inferFieldNameFromSelectExpression(String expression, int index) {
        if (StringUtil.isBlank(expression)) {
            return "field_" + index;
        }
        String trimmed = expression.trim();
        String alias = extractAliasAfterAs(trimmed);
        if (StringUtil.isNotBlank(alias)) {
            return alias;
        }
        String simpleName = extractSimpleColumnName(trimmed);
        if (StringUtil.isNotBlank(simpleName)) {
            return simpleName;
        }
        return "field_" + index;
    }

    private String extractAliasAfterAs(String expression) {
        String[] parts = expression.split("(?i)\\s+as\\s+");
        if (parts.length < 2) {
            return "";
        }
        return parts[parts.length - 1].trim();
    }

    private String extractSimpleColumnName(String expression) {
        String cleaned = expression.trim();
        if (cleaned.matches("(?i).+\\s+[A-Za-z_][A-Za-z0-9_]*$")) {
            String[] tokens = cleaned.split("\\s+");
            String possibleAlias = tokens[tokens.length - 1];
            if (!possibleAlias.contains(".") && !possibleAlias.contains("(") && !possibleAlias.contains(")")) {
                return possibleAlias;
            }
        }
        if (cleaned.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")) {
            int dotIndex = cleaned.lastIndexOf('.');
            return dotIndex >= 0 ? cleaned.substring(dotIndex + 1) : cleaned;
        }
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1) {
            return cleaned.substring(1, cleaned.length() - 1);
        }
        return "";
    }

    private String normalizeSourceFieldName(String columnLabel, String columnName, int index) {
        String name = StringUtil.isNotBlank(columnLabel) ? columnLabel : columnName;
        if (StringUtil.isBlank(name) || "?column?".equalsIgnoreCase(name)) {
            name = columnName;
        }
        name = name == null ? "" : name.trim();
        if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 1) {
            name = name.substring(1, name.length() - 1);
        }
        if (StringUtil.isBlank(name) || "?column?".equalsIgnoreCase(name)) {
            name = "field_" + index;
        }
        name = name.replaceAll("[^A-Za-z0-9_\\.]", "_");
        if (StringUtil.isBlank(name)) {
            name = "field_" + index;
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "field_" + name;
        }
        return name;
    }

    private String buildUniqueSourceFieldName(String name, Set<String> usedNames) {
        String baseName = StringUtil.isBlank(name) ? "field" : name;
        String candidate = baseName;
        int index = 2;
        while (usedNames.contains(candidate.toLowerCase(Locale.ROOT))) {
            candidate = baseName + "_" + index++;
        }
        usedNames.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    private String inferJdbcType(ResultSetMetaData metaData, int index, int jdbcType) throws Exception {
        String typeName = metaData.getColumnTypeName(index);
        int precision = metaData.getPrecision(index);
        int scale = metaData.getScale(index);
        String lowerTypeName = typeName == null ? "" : typeName.toLowerCase(Locale.ROOT);
        if (jdbcType == Types.VARCHAR || jdbcType == Types.CHAR || jdbcType == Types.NVARCHAR || jdbcType == Types.NCHAR) {
            int length = precision > 0 ? precision : 255;
            return "varchar(" + Math.min(length, 4000) + ")";
        }
        if (jdbcType == Types.LONGVARCHAR || jdbcType == Types.LONGNVARCHAR || jdbcType == Types.CLOB || jdbcType == Types.NCLOB) {
            return "text";
        }
        if (jdbcType == Types.INTEGER || jdbcType == Types.SMALLINT || jdbcType == Types.TINYINT) {
            return "integer";
        }
        if (jdbcType == Types.BIGINT) {
            return "bigint";
        }
        if (jdbcType == Types.FLOAT || jdbcType == Types.REAL || jdbcType == Types.DOUBLE) {
            return "double";
        }
        if (jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
            int p = precision > 0 ? precision : 18;
            int s = scale >= 0 ? scale : 4;
            return "decimal(" + p + "," + s + ")";
        }
        if (jdbcType == Types.DATE) {
            return "date";
        }
        if (jdbcType == Types.TIMESTAMP || jdbcType == Types.TIMESTAMP_WITH_TIMEZONE || jdbcType == Types.TIME || jdbcType == Types.TIME_WITH_TIMEZONE) {
            return "datetime";
        }
        if (jdbcType == Types.BOOLEAN || jdbcType == Types.BIT) {
            return "boolean";
        }
        return StringUtil.isNotBlank(lowerTypeName) ? lowerTypeName : "varchar(255)";
    }

    private String inferJavaType(int jdbcType) {
        if (jdbcType == Types.INTEGER || jdbcType == Types.SMALLINT || jdbcType == Types.TINYINT) {
            return "Integer";
        }
        if (jdbcType == Types.BIGINT) {
            return "Long";
        }
        if (jdbcType == Types.FLOAT || jdbcType == Types.REAL || jdbcType == Types.DOUBLE
                || jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
            return "java.math.BigDecimal";
        }
        if (jdbcType == Types.DATE || jdbcType == Types.TIMESTAMP || jdbcType == Types.TIMESTAMP_WITH_TIMEZONE
                || jdbcType == Types.TIME || jdbcType == Types.TIME_WITH_TIMEZONE) {
            return "java.util.Date";
        }
        if (jdbcType == Types.BOOLEAN || jdbcType == Types.BIT) {
            return "Boolean";
        }
        return "String";
    }

    private String inferShowType(String name, int jdbcType) {
        if (jdbcType == Types.DATE || jdbcType == Types.TIMESTAMP || jdbcType == Types.TIMESTAMP_WITH_TIMEZONE
                || jdbcType == Types.TIME || jdbcType == Types.TIME_WITH_TIMEZONE) {
            return "dateselect";
        }
        if (jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL || jdbcType == Types.FLOAT
                || jdbcType == Types.REAL || jdbcType == Types.DOUBLE) {
            return "input";
        }
        return "input";
    }

    private String inferQueryType(String name, int jdbcType) {
        if (jdbcType == Types.VARCHAR || jdbcType == Types.CHAR || jdbcType == Types.NVARCHAR
                || jdbcType == Types.NCHAR || jdbcType == Types.LONGVARCHAR || jdbcType == Types.LONGNVARCHAR) {
            return isDictionaryCandidate(name) ? "=" : "like";
        }
        return "=";
    }

    private String inferSourceFieldLabel(String name) {
        if (StringUtil.isBlank(name)) {
            return "字段";
        }
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("id", "ID");
        labelMap.put("name", "名称");
        labelMap.put("title", "标题");
        labelMap.put("status", "状态");
        labelMap.put("type", "类型");
        labelMap.put("category", "分类");
        labelMap.put("amount", "金额");
        labelMap.put("contract_no", "合同编号");
        labelMap.put("contract_name", "合同名称");
        labelMap.put("contract_type", "合同类型");
        labelMap.put("contract_status", "合同状态");
        labelMap.put("party_a", "甲方");
        labelMap.put("party_b", "乙方");
        labelMap.put("currency", "币种");
        labelMap.put("sign_date", "签订日期");
        labelMap.put("start_date", "开始日期");
        labelMap.put("end_date", "结束日期");
        labelMap.put("risk_level", "风险等级");
        labelMap.put("total", "合计");
        labelMap.put("count", "数量");
        labelMap.put("create_date", "创建时间");
        labelMap.put("update_date", "更新时间");
        labelMap.put("remarks", "备注");
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (labelMap.containsKey(lowerName)) {
            return labelMap.get(lowerName);
        }
        String chineseLabel = inferSourceFieldChineseLabel(lowerName);
        if (StringUtil.isNotBlank(chineseLabel)) {
            return chineseLabel;
        }
        String[] words = lowerName.replace(".", "_").split("_+");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (StringUtil.isBlank(word)) {
                continue;
            }
            label.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            if (word.length() > 1) {
                label.append(word.substring(1));
            }
            label.append(" ");
        }
        return StringUtil.isBlank(label.toString()) ? name : label.toString().trim();
    }

    private String inferSourceFieldChineseLabel(String lowerName) {
        if (StringUtil.isBlank(lowerName)) {
            return "";
        }
        Map<String, String> wordMap = new HashMap<>();
        wordMap.put("contract", "合同");
        wordMap.put("customer", "客户");
        wordMap.put("supplier", "供应商");
        wordMap.put("vendor", "供应商");
        wordMap.put("project", "项目");
        wordMap.put("order", "订单");
        wordMap.put("invoice", "发票");
        wordMap.put("payment", "付款");
        wordMap.put("user", "用户");
        wordMap.put("dept", "部门");
        wordMap.put("office", "机构");
        wordMap.put("party", "方");
        wordMap.put("no", "编号");
        wordMap.put("code", "编码");
        wordMap.put("name", "名称");
        wordMap.put("title", "标题");
        wordMap.put("type", "类型");
        wordMap.put("status", "状态");
        wordMap.put("category", "分类");
        wordMap.put("level", "等级");
        wordMap.put("currency", "币种");
        wordMap.put("amount", "金额");
        wordMap.put("price", "价格");
        wordMap.put("total", "合计");
        wordMap.put("count", "数量");
        wordMap.put("date", "日期");
        wordMap.put("time", "时间");
        wordMap.put("start", "开始");
        wordMap.put("end", "结束");
        wordMap.put("sign", "签订");
        wordMap.put("create", "创建");
        wordMap.put("update", "更新");
        wordMap.put("audit", "审核");
        wordMap.put("risk", "风险");
        wordMap.put("remark", "备注");
        wordMap.put("remarks", "备注");
        wordMap.put("desc", "描述");
        wordMap.put("description", "描述");
        String[] words = lowerName.replace(".", "_").split("_+");
        StringBuilder label = new StringBuilder();
        boolean hasChineseWord = false;
        for (String word : words) {
            if (StringUtil.isBlank(word)) {
                continue;
            }
            String chinese = wordMap.get(word);
            if (StringUtil.isBlank(chinese)) {
                return "";
            }
            label.append(chinese);
            hasChineseWord = true;
        }
        return hasChineseWord ? label.toString() : "";
    }

    private boolean isSystemHiddenSourceField(String name) {
        if (StringUtil.isBlank(name)) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        return "id".equals(lowerName) || "del_flag".equals(lowerName)
                || "create_by".equals(lowerName) || "create_date".equals(lowerName)
                || "update_by".equals(lowerName) || "update_date".equals(lowerName)
                || "remarks".equals(lowerName) || "owner_code".equals(lowerName);
    }

    private boolean isDictionaryCandidate(String name) {
        if (StringUtil.isBlank(name)) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        return lowerName.endsWith("_type") || lowerName.endsWith("_status")
                || lowerName.endsWith("_category") || lowerName.endsWith("_level")
                || lowerName.endsWith("_kind") || lowerName.endsWith("_code")
                || lowerName.contains("status") || lowerName.contains("type")
                || lowerName.contains("category") || lowerName.contains("currency");
    }

    private void attachDeriveConfig(GenTable genTable) {
        if (genTable == null || StringUtil.isBlank(genTable.getId())) {
            return;
        }
        GenTableDeriveConfig deriveConfig = genTableDeriveConfigDao.getByGenTableId(genTable.getId());
        if (deriveConfig == null) {
            deriveConfig = buildDeriveConfigFromLegacy(genTable);
        } else {
            fillLegacyFromDeriveConfig(genTable, deriveConfig);
        }
        genTable.setDeriveConfig(deriveConfig);
    }

    private void attachDeriveConfig(GenTableView genTableView) {
        if (genTableView == null || StringUtil.isBlank(genTableView.getId())) {
            return;
        }
        GenTableDeriveConfig deriveConfig = genTableDeriveConfigDao.getByGenTableId(genTableView.getId());
        if (deriveConfig == null) {
            deriveConfig = buildDeriveConfigFromLegacy(genTableView);
        } else {
            fillLegacyFromDeriveConfig(genTableView, deriveConfig);
        }
        genTableView.setDeriveConfig(deriveConfig);
    }

    private GenTableDeriveConfig buildDeriveConfigFromLegacy(GenTable genTable) {
        GenTableDeriveConfig deriveConfig = new GenTableDeriveConfig();
        if (genTable != null) {
            deriveConfig.setGenTableId(genTable.getId());
            deriveConfig.setUniqueKeys(genTable.getBlockChainParam2());
            deriveConfig.setPartitionKeys(genTable.getBlockChainParam3());
            deriveConfig.setBucketKeys(genTable.getBlockChainParam4());
            deriveConfig.setEditOpenMode(StringUtil.isNotBlank(genTable.getBlockChainParam5()) ? genTable.getBlockChainParam5() : "1");
            deriveConfig.setListDescription(genTable.getBlockChainParam6());
        }
        deriveConfig.setSourceMode(GenTableDeriveConfig.SOURCE_MODE_NORMAL);
        deriveConfig.setEnabled(Global.YES);
        return deriveConfig;
    }

    private GenTableDeriveConfig buildDeriveConfigFromLegacy(GenTableView genTableView) {
        GenTableDeriveConfig deriveConfig = new GenTableDeriveConfig();
        if (genTableView != null) {
            deriveConfig.setGenTableId(genTableView.getId());
            deriveConfig.setUniqueKeys(genTableView.getBlockChainParam2());
            deriveConfig.setPartitionKeys(genTableView.getBlockChainParam3());
            deriveConfig.setBucketKeys(genTableView.getBlockChainParam4());
            deriveConfig.setEditOpenMode(StringUtil.isNotBlank(genTableView.getBlockChainParam5()) ? genTableView.getBlockChainParam5() : "1");
            deriveConfig.setListDescription(genTableView.getBlockChainParam6());
        }
        deriveConfig.setSourceMode(GenTableDeriveConfig.SOURCE_MODE_NORMAL);
        deriveConfig.setEnabled(Global.YES);
        return deriveConfig;
    }

    private void fillLegacyFromDeriveConfig(GenTable genTable, GenTableDeriveConfig deriveConfig) {
        if (genTable == null || deriveConfig == null) {
            return;
        }
        genTable.setBlockChainParam2(deriveConfig.getUniqueKeys());
        genTable.setBlockChainParam3(deriveConfig.getPartitionKeys());
        genTable.setBlockChainParam4(deriveConfig.getBucketKeys());
        genTable.setBlockChainParam5(StringUtil.isNotBlank(deriveConfig.getEditOpenMode()) ? deriveConfig.getEditOpenMode() : "1");
        genTable.setBlockChainParam6(deriveConfig.getListDescription());
    }

    private void fillLegacyFromDeriveConfig(GenTableView genTableView, GenTableDeriveConfig deriveConfig) {
        if (genTableView == null || deriveConfig == null) {
            return;
        }
        genTableView.setBlockChainParam2(deriveConfig.getUniqueKeys());
        genTableView.setBlockChainParam3(deriveConfig.getPartitionKeys());
        genTableView.setBlockChainParam4(deriveConfig.getBucketKeys());
        genTableView.setBlockChainParam5(StringUtil.isNotBlank(deriveConfig.getEditOpenMode()) ? deriveConfig.getEditOpenMode() : "1");
        genTableView.setBlockChainParam6(deriveConfig.getListDescription());
    }

    private void normalizeDeriveConfig(GenTable genTable) {
        if (genTable == null) {
            return;
        }
        GenTableDeriveConfig deriveConfig = genTable.getDeriveConfig();
        if (deriveConfig == null) {
            deriveConfig = buildDeriveConfigFromLegacy(genTable);
            genTable.setDeriveConfig(deriveConfig);
        }
        deriveConfig.setGenTableId(genTable.getId());
        if (StringUtil.isBlank(deriveConfig.getSourceMode())) {
            deriveConfig.setSourceMode(GenTableDeriveConfig.SOURCE_MODE_NORMAL);
        }
        if (StringUtil.isBlank(deriveConfig.getEnabled())) {
            deriveConfig.setEnabled(Global.YES);
        }
        if (StringUtil.isBlank(deriveConfig.getEditOpenMode())) {
            deriveConfig.setEditOpenMode(StringUtil.isNotBlank(genTable.getBlockChainParam5()) ? genTable.getBlockChainParam5() : "1");
        }
        if (deriveConfig.getUniqueKeys() == null) {
            deriveConfig.setUniqueKeys(genTable.getBlockChainParam2());
        }
        if (deriveConfig.getPartitionKeys() == null) {
            deriveConfig.setPartitionKeys(genTable.getBlockChainParam3());
        }
        if (deriveConfig.getBucketKeys() == null) {
            deriveConfig.setBucketKeys(genTable.getBlockChainParam4());
        }
        if (deriveConfig.getListDescription() == null) {
            deriveConfig.setListDescription(genTable.getBlockChainParam6());
        }
        fillLegacyFromDeriveConfig(genTable, deriveConfig);
    }

    private void saveDeriveConfigIfPresent(GenTable genTable) {
        if (genTable == null || StringUtil.isBlank(genTable.getId())) {
            return;
        }
        normalizeDeriveConfig(genTable);
        GenTableDeriveConfig deriveConfig = genTable.getDeriveConfig();
        if (deriveConfig == null) {
            return;
        }
        GenTableDeriveConfig existing = genTableDeriveConfigDao.getByGenTableId(genTable.getId());
        if (existing == null) {
            deriveConfig.setId(null);
            deriveConfig.preInsert();
            genTableDeriveConfigDao.insert(deriveConfig);
        } else {
            deriveConfig.setId(existing.getId());
            deriveConfig.setLastRunTime(existing.getLastRunTime());
            deriveConfig.setLastRunStatus(existing.getLastRunStatus());
            deriveConfig.setLastRunMessage(existing.getLastRunMessage());
            deriveConfig.preUpdate();
            genTableDeriveConfigDao.update(deriveConfig);
        }
    }

    /**
     * Get gentable list by page
     *
     * @param page
     * @param genTable
     * @return gentable page
     */
    public Page<GenTable> findPage(Page<GenTable> page, GenTable genTable) {
        genTable.setPage(page);
        IPage<GenTable> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getPageNo(),page.getPageSize());
        genTable.getSqlMap().put("orderBy", genTable.getPage().getOrderBy());
        IPage<GenTable> pageList = this.genTableDao.findPageList(iPage, genTable, genTable.getSqlMap());
        page.setList(pageList.getRecords());
        page.setCount(pageList.getTotal());
        return page;
    }

    /**
     * Get gentable list
     *
     * @param genTable
     * @return gentable list
     */
    public List<GenTable> findList(GenTable genTable) {
        return this.genTableDao.findList(genTable);
    }

    /**
     * Get all gentable list
     */
    public List<GenTable> findAll() {
        return this.genTableDao.findAllList(new GenTable());
    }

    /**
     * Get gentable list from db
     */
    public List<GenTable> findTableListFormDb(GenTable genTable) {
        genTable.setDbName(dbType);
        return this.genDataBaseDictDao.findTableList(genTable);
    }

    /**
     * Get gentable list from db other schemas
     */
    public List<GenTable> findTableListFormDbSchema(GenTable genTable) {
        genTable.setDbName(dbType);
        return this.genDataBaseDictDao.findTableListSchema(genTable);
    }

    /**
     * Check table name exists
     */
    public boolean existTable(String tableName) {
        if (StringUtil.isBlank(tableName)) {
            return false;
        } else {
            return (1 == this.genTableDao.findCount(tableName));
        }
    }

    /**
     * Check table name exists from db
     */
    public boolean existTableNameInDB(String tableName) {
        if (StringUtil.isBlank(tableName)) {
            return false;
        } else {
            GenTable genTable = new GenTable();
            genTable.setName(tableName);
            genTable.setDbName(dbType);
            return (1 == this.genDataBaseDictDao.findTableList(genTable).size());
        }
    }

    /**
     * Get table from db
     */
    public GenTable getTableFormDb(GenTable genTable, String dbName) {
        String name = genTable.getName();
        if (StringUtil.isNotBlank(genTable.getName())) {
            genTable.setDbName(dbName);
            List<GenTable> list = genDataBaseDictDao.findTableList(genTable);
            if (list == null || list.size() == 0) {
                name = "schema." + genTable.getName();
                genTable.setName(name);
                list = genDataBaseDictDao.findTableListSchema(genTable);
            }
            if (list != null && list.size() > 0) {
                //If it is new, initialize the table properties
                if (StringUtil.isBlank(genTable.getId())) {
                    genTable = list.get(0);
                    //Set field description
                    if (StringUtil.isBlank(genTable.getComments())) {
                        genTable.setComments(genTable.getName());
                    }
                    genTable.setClassName(StringUtil.toCapitalizeCamelCase(genTable.getName()));
                }

                genTable.setDbName(dbName);
                //Add new column
                genTable.setName(name);
                List<GenTableColumn> columnList = genDataBaseDictDao.findTableColumnList(genTable);
                for (GenTableColumn column : columnList) {
                    boolean isExists = false;
                    for (GenTableColumn e : genTable.getColumnList()) {
                        if (e.getName().equals(column.getName())) {
                            isExists = true;
                        }
                    }
                    if (!isExists) {
                        genTable.getColumnList().add(column);
                    }
                }

                //Delete deleted columns
                for (GenTableColumn e : genTable.getColumnList()) {
                    boolean isExists = false;
                    for (GenTableColumn column : columnList) {
                        if (column.getName().equals(e.getName())) {
                            isExists = true;
                        }
                    }
                    if (!isExists) {
                        e.setDelFlag(GenTableColumn.DEL_FLAG_DELETE);
                    }
                }
                genTable.setDbName(dbName);
                //Get the primary key
                genTable.setPkList(genDataBaseDictDao.findTablePK(genTable));
                GenUtil.initColumnField(genTable);
            }
        }
        return genTable;
    }

    /**
     * Save gentable
     */
    @Transactional(readOnly = false)
    public void save(GenTable genTable) {
        boolean isSync = true;

        if (StringUtil.isBlank(genTable.getId())) {
            isSync = false;
        } else {
            GenTable oldTable = get(genTable.getId());
            if (oldTable.getColumnList().size() != genTable.getColumnList().size()
                    || (StringUtil.isNotBlank(oldTable.getName())
                    && false == oldTable.getName().equals(genTable.getName()))
                    || (StringUtil.isNotBlank(oldTable.getComments())
                    && false == oldTable.getComments().equals(genTable.getComments()))) {
                isSync = false;
            } else {
                for (GenTableColumn column : genTable.getColumnList()) {
                    GenTableColumn oldColumn = this.genTableColumnDao.get(column.getId());
                    if (oldColumn != null && (StringUtil.isBlank(oldColumn.getId())
                            || (StringUtil.isNotBlank(oldColumn.getName())
                            && false == oldColumn.getName().equals(column.getName()))
                            || (StringUtil.isNotBlank(oldColumn.getJdbcType())
                            && false == oldColumn.getJdbcType().equals(column.getJdbcType()))
                            || (StringUtil.isNotBlank(oldColumn.getIsPk())
                            && false == oldColumn.getIsPk().equals(column.getIsPk()))
                            || (StringUtil.isNotBlank(oldColumn.getComments())
                            && false == oldColumn.getComments().equals(column.getComments())))) {
                        isSync = false;
                    }
                }
            }
        }

        if (false == isSync) {
            genTable.setIsSync(Global.NO);
        }
        if (StringUtil.isBlank(genTable.getId())) {
            genTable.preInsert();
            this.genTableDao.insert(genTable);
        } else {
            genTable.preUpdate();
            this.genTableDao.update(genTable);
        }
        this.genTableColumnDao.deleteByGenTable(genTable);
        for (GenTableColumn column : genTable.getColumnList()) {
            if (StringUtil.isBlank(column.getName())) {
                continue;
            }
            column.setGenTable(genTable);
            column.setId(null);
            column.preInsert();
            this.genTableColumnDao.insert(column);
        }
        cacheUtil.deleteHashCache(GenUtil.GENTABLE_CACHE, genTable.getName());
    }

    /**
     * Save gentable sync flag
     */
    @Transactional(readOnly = false)
    public void syncSave(GenTable genTable) {
        genTable.setIsSync(Global.YES);
        this.genTableDao.update(genTable);
    }

    /**
     * Save gentable from db
     */
    @Transactional(readOnly = false)
    public void saveFromDB(GenTable genTable) {
        genTable.preInsert();
        this.genTableDao.insert(genTable);

        for (GenTableColumn column : genTable.getColumnList()) {
            column.setGenTable(genTable);
            column.setId(null);
            column.preInsert();
            this.genTableColumnDao.insert(column);
        }
    }

    /**
     * Delete gentable
     */
    @Transactional(readOnly = false)
    public void delete(GenTable genTable) {
        if (genTable != null && StringUtil.isNotBlank(genTable.getId())) {
            genTableDeriveConfigDao.deleteByGenTableId(genTable.getId());
        }
        this.genTableDao.delete(genTable);
        this.genTableColumnDao.deleteByGenTable(genTable);
        this.genTableDao.delete(genTable);
        this.genTableColumnDao.deleteByGenTable(genTable);
        List<GenTable> childList = this.genTableDao.getChildren(genTable.getName());
        for (GenTable child : childList) {
            child.setParentTable("");
            child.setParentTableFk("");
            child.preUpdate();
            genTableDao.update(child);
        }
    }

    /**
     * Build gentable by sql
     */
    @Transactional(readOnly = false)
    public void buildTable(String sql) {
        this.genTableDao.buildTable(sql);
    }

    /**
     * Copy gentable
     */
    @Transactional(readOnly = false)
    public void copy(GenTable genTable) {
        genTable.setIsSync(Global.NO);

        genTable.setId(IdGen.uuid());
        genTable.setName(genTable.getName() + "2");
        genTable.setClassName(genTable.getClassName());
        this.genTableDao.insert(genTable);

        for (GenTableColumn column : genTable.getColumnList()) {
            column.setGenTable(genTable);
            column.setId(IdGen.uuid());
            this.genTableColumnDao.insert(column);
        }
    }

    /**
     * Get gentable by name
     */
    public GenTable getByName(String name) {
        GenTable genTable = genTableDao.getByName(name);
        return genTable;
    }

    /**
     * Find gentable column list by page
     */
    public Page<GenTableColumn> findTableColumn(Page<GenTableColumn> page, GenTableColumn column) {
        column.setPage(page);
        GenTable gt = column.getGenTable();
        if (gt == null) {
            page.setList(null);
        } else {
            List<GenTableColumn> list = this.genTableColumnDao.findPageList(column);
            for (int i = 0; i < list.size(); i++) {
                GenTableColumn genTableColum = list.get(i);
                String showType = genTableColum.getShowType();
                String javaField = genTableColum.getJavaField();
                switch (showType) {
                    case "treeselectRedio":
                        String[] treeselectRedioIda = javaField.split("\\.");
                        String treeselectRedioId = javaField;
                        if (treeselectRedioIda.length >= 2) {
                            genTableColum.setJavaField(treeselectRedioIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(treeselectRedioId + "Id");
                        }
                        break;
                    case "treeselectCheck":
                        String[] treeselectCheckIda = javaField.split("\\.");
                        String treeselectCheckId = javaField;
                        if (treeselectCheckIda.length >= 2) {
                            genTableColum.setJavaField(treeselectCheckIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(treeselectCheckId + "Id");
                        }
                        break;
                    case "officeselectTree":
                        String[] officeselectTreeIda = javaField.split("\\.");
                        String officeselectTreeId = javaField;
                        if (officeselectTreeIda.length >= 2) {
                            genTableColum.setJavaField(officeselectTreeIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(officeselectTreeId + "Id");
                        }
                        break;
                    case "areaselect":
                        String[] areaselectIda = javaField.split("\\.");
                        String areaselectId = javaField;
                        if (areaselectIda.length >= 2) {
                            genTableColum.setJavaField(areaselectIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(areaselectId + "Id");
                        }
                        break;
                    case "treeselect":
                        String[] treeselectIda = javaField.split("\\.");
                        String treeselectId = javaField;
                        if (treeselectIda.length >= 2) {
                            genTableColum.setJavaField(treeselectIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(treeselectId + "Id");
                        }
                        break;
                    case "gridselect":
                        String[] gridselectIda = javaField.split("\\.");
                        String gridselectId = javaField;
                        if (gridselectIda.length >= 2) {
                            genTableColum.setJavaField(gridselectIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(gridselectId + "Id");
                        }
                        break;
                    case "fileupload":
                        String[] fileuploadIda = javaField.split("\\.");
                        String fileuploadId = javaField;
                        if (fileuploadIda.length >= 2) {
                            genTableColum.setJavaField(fileuploadIda[0] + "File");
                        } else {
                            genTableColum.setJavaField(fileuploadId + "File");
                        }
                        break;
                    case "fileuploadsec":
                        String[] fileuploadsecIda = javaField.split("\\.");
                        String fileuploadsecId = javaField;
                        if (fileuploadsecIda.length >= 2) {
                            genTableColum.setJavaField(fileuploadsecIda[0] + "File");
                        } else {
                            genTableColum.setJavaField(fileuploadsecId + "File");
                        }
                        break;
                    case "fileuploadpic":
                        String[] fileuploadpicIda = javaField.split("\\.");
                        String fileuploadpicId = javaField;
                        if (fileuploadpicIda.length >= 2) {
                            genTableColum.setJavaField(fileuploadpicIda[0] + "File");
                        } else {
                            genTableColum.setJavaField(fileuploadpicId + "File");
                        }
                        break;
                    case "userselect":
                        String[] userselectIda = javaField.split("\\.");
                        String userselectId = javaField;
                        if (userselectIda.length >= 2) {
                            genTableColum.setJavaField(userselectIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(userselectId + "Id");
                        }
                        break;
                    case "officeselect":
                        String[] officeselectIda = javaField.split("\\.");
                        String officeselectId = javaField;
                        if (officeselectIda.length >= 2) {
                            genTableColum.setJavaField(officeselectIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(officeselectId + "Id");
                        }
                        break;
                    case "userselectMulti":
                        String[] userselectMultiIda = javaField.split("\\.");
                        String userselectMultiId = javaField;
                        if (userselectMultiIda.length >= 2) {
                            genTableColum.setJavaField(userselectMultiIda[0] + "Id");
                        } else {
                            genTableColum.setJavaField(userselectMultiId + "Id");
                        }
                        break;
                }
                String jf = genTableColum.getJavaField();
                if (jf.indexOf(".") != -1) {
                    genTableColum.setJavaField(jf.split("\\.")[0]);
                }
            }
            page.setList(list);
        }
        return page;
    }

    /**
     * Find gentable column list
     */
    public List<GenTableColumn> findByColum(GenTableColumn column) {
        GenTable gt = column.getGenTable();
        if (gt == null) {
            return null;
        } else {
            List<GenTableColumn> list = this.genTableColumnDao.findPageList(column);
            return list;
        }
    }

    /**
     * Save edit form with Json
     */
    @Transactional(readOnly = false)
    public void saveEditForm(String json) {
        JSONArray array = JSONArray.parseArray(json);
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            genTableColumnDao.saveEditForm(String.valueOf(obj.get("id")),
                    String.valueOf(obj.get("showType") == null ? "" : obj.get("showType")),
                    String.valueOf(obj.get("isOneLine") == null ? "" : obj.get("isOneLine")),
                    String.valueOf(obj.get("isNull") == null ? "" : obj.get("isNull")),
                    String.valueOf(obj.get("comments") == null ? "" : obj.get("comments")),
                    String.valueOf(obj.get("javaField") == null ? "" : obj.get("javaField")),
                    String.valueOf(obj.get("maxLength") == null ? "" : obj.get("maxLength")),
                    String.valueOf(obj.get("minLength") == null ? "" : obj.get("minLength")),
                    String.valueOf(obj.get("min") == null ? "" : obj.get("min")),
                    String.valueOf(obj.get("max") == null ? "" : obj.get("max")),
                    String.valueOf(obj.get("friendlyJdbcType") == null ? "" : obj.get("friendlyJdbcType")),
                    String.valueOf(obj.get("javaType") == null ? "" : obj.get("javaType")),
                    String.valueOf(obj.get("jdbcType") == null ? "" : obj.get("jdbcType")),
                    String.valueOf(obj.get("queryType") == null ? "" : obj.get("queryType")),
                    Integer.valueOf(String.valueOf(obj.get("formSort"))),
                    String.valueOf(obj.get("validateType") == null ? "" : obj.get("validateType")),
                    String.valueOf(obj.get("dictType") == null ? "" : obj.get("dictType")),
                    Integer.valueOf(String.valueOf(obj.get("searchSort"))), String.valueOf(obj.get("isForm")),
                    String.valueOf(obj.get("isQuery")), String.valueOf(obj.get("isList")));
        }
    }

    /**
     * Save edit list
     */
    @Transactional(readOnly = false)
    public void saveEditList(String jsonList) {
        JSONArray array = JSONArray.parseArray(jsonList);
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            genTableColumnDao.saveEditList(String.valueOf(obj.get("id")), "1");
        }
    }

    /**
     * Save edit search with Json search
     */
    @Transactional(readOnly = false)
    public void saveEditSearch(String jsonSearch) {
        JSONArray array = JSONArray.parseArray(jsonSearch);
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            genTableColumnDao.saveEditSearch(String.valueOf(obj.get("id")), "1", i,
                    String.valueOf(obj.get("queryType") == null ? "" : obj.get("queryType")));
        }
    }

    /**
     * Find gentable column list by gentable id
     */
    public List<GenTableColumn> findGenTableColumnList(String genTableId) {
        List<GenTableColumn> list = genTableColumnDao.findGenTableColumnList(genTableId);
        return list;
    }

    /**
     * Update edit by gentable id
     */
    @Transactional(readOnly = false)
    public void updateEdit(String genTableId) {
        genTableColumnDao.updateEdit(genTableId, "0", "");
    }

    /**
     * Save sql for gentable
     */
    @Transactional(readOnly = false)
    public void saveSql(GenTable genTable) {
        genTableDao.saveSql(genTable.getId(),
                genTable.getSqlColumns(),
                genTable.getSqlColumnsFriendly(),
                genTable.getSqlColumnsFriendlyExt(),
                genTable.getSqlJoins(),
                genTable.getSqlJoinsExt(),
                genTable.getSqlInsert(),
                genTable.getSqlUpdate(),
                genTable.getSqlSort(),
                genTable.getExtSql02());
    }

    /**
     * 根据设计器 JSON 重建字段配置，处理主键、删除标记、父子表外键和流程字段等内置规则。
     */
    @Transactional(readOnly = false)
    public void saveJsons(GenTable genTable, boolean b) {
        genTableColumnDao.deleteByGenTableId(genTable.getId());
        GenTableColumn column = new GenTableColumn();
        if (b) {
            column = saveColumnId(column, genTable);
            this.genTableColumnDao.insert(column);

            column = saveColumnDelFlag(column, genTable);
            this.genTableColumnDao.insert(column);
        }
        JSONArray json = genTable.getJson();
        saveColumnPro(json, column, genTable);
        String parentTable = genTable.getParentTable();
        if (StringUtil.isNotEmpty(parentTable) && false == GenTable.TABLE_TYPE_RIGHTTABLE.equals(genTable.getTableType())) {
            saveColumnParentFk(json, column, genTable);
        }
        boolean bId = false;
        boolean bDel = false;
        for (int i = 0; i < json.size(); i++) {
            JSONObject obj = json.getJSONObject(i);
            String field = obj.getString("javaField");
            String str = "procInsId";
            if (str.equals(field)) {
                String cate = genTable.getProcessDefinitionCategory();
                if ("".equals(cate) == true || cate == null) {
                    continue;
                }
            }
            String fieldDef = obj.getString("javaField");
            String strDef = "procDefKey";
            if (strDef.equals(fieldDef)) {
                String cate = genTable.getProcessDefinitionCategory();
                if ("".equals(cate) == true || cate == null) {
                    continue;
                }
            }
            String pkColumnName = StringUtil.isNotBlank(genTable.getPkColumnName()) ? genTable.getPkColumnName() : "id";
            if (pkColumnName.equals(obj.getString("javaField"))) {
                column.setIsPk("1");
                bId = true;
            } else {
                column.setIsPk("0");
            }
            if ("delFlag".equals(obj.getString("javaField"))) {
                bDel = true;
            }
            if (!pkColumnName.equals(obj.getString("javaField")) && !"createBy.id".equals(obj.getString("javaField"))
                    && !"createDate".equals(obj.getString("javaField"))
                    && !"delFlag".equals(obj.getString("javaField"))) {
                column.setIsEdit("1");
            } else {
                column.setIsEdit("0");
            }
            String javaField = obj.getString("javaField");
            if (javaField.contains("users") && javaField.indexOf(".") != -1 && javaField.indexOf("|") != -1) {
                saveUsersName(json, genTable, javaField, obj.getString("name"));
            }
            if (obj.containsKey("blockChainParam1")) {
                column.setBlockChainParam1(obj.getString("blockChainParam1"));
            } else {
                column.setBlockChainParam1("0");
            }
            if (obj.containsKey("blockChainParam2")) {
                column.setBlockChainParam2(obj.getString("blockChainParam2"));
            } else {
                column.setBlockChainParam2("0");
            }
            if (obj.containsKey("blockChainParam3")) {
                column.setBlockChainParam3(obj.getString("blockChainParam3"));
            } else {
                column.setBlockChainParam3("0");
            }
            if (obj.containsKey("blockChainParam4")) {
                column.setBlockChainParam4(obj.getString("blockChainParam4"));
            } else {
                column.setBlockChainParam4("0");
            }
            if (obj.containsKey("blockChainParam5")) {
                column.setBlockChainParam5(obj.getString("blockChainParam5"));
            } else {
                column.setBlockChainParam5("0");
            }
            column.setGenTable(genTable);
            column.setId(null);
            column.setIsInsert("1");
            column.setShowType(obj.getString("showType"));
            column.setIsOneLine(String.valueOf(obj.getString("isOneLine") == null ? "0" : obj.getString("isOneLine")));
            column.setIsNull(String.valueOf(obj.getString("isNull") == null ? "0" : obj.getString("isNull")));
            column.setComments(obj.getString("comments"));
            column.setCommentsEn(obj.getString("comments_EN"));
            column.setDateType(obj.getString("dateType"));
            column.setJavaField(obj.getString("javaField"));
            if (obj.containsKey("maxLength") && StringUtil.isNotEmpty(obj.getString("maxLength"))) {
                column.setMaxLength(obj.getString("maxLength"));
            } else {
                if ((obj.getString("showType").equals("input") || obj.getString("showType").equals("textarea"))
                        && obj.getString("jdbcType").startsWith("varchar")) {
                    String jdbcType = obj.getString("jdbcType");
                    column.setMaxLength(jdbcType.substring(jdbcType.indexOf("(") + 1, jdbcType.length() - 1));
                } else {
                    column.setMaxLength("");
                }
            }
            if (obj.containsKey("minLength")) {
                column.setMinLength(obj.getString("minLength"));
            } else {
                column.setMinLength("");
            }
            if (obj.containsKey("minValue")) {
                column.setMinValue(obj.getString("minValue"));
            } else {
                column.setMinValue("");
            }
            if (obj.containsKey("maxValue")) {
                column.setMaxValue(obj.getString("maxValue"));
            } else {
                column.setMaxValue("");
            }
            column.setJavaType(obj.getString("javaType"));
            column.setJdbcType(obj.getString("jdbcType"));
            if (obj.containsKey("dictType")) {
                column.setDictType(obj.getString("dictType"));
            } else {
                column.setDictType("");
            }
            if (obj.containsKey("queryType")) {
                column.setQueryType(obj.getString("queryType"));
            } else {
                column.setQueryType("");
            }
            if (obj.containsKey("validateType")) {
                column.setValidateType(obj.getString("validateType"));
            } else {
                column.setValidateType("");
            }
            if (obj.containsKey("tableName")) {
                column.setTableName(obj.getString("tableName"));
            } else {
                column.setTableName("");
            }
            if (obj.containsKey("fieldLabels")) {
                column.setFieldLabels(obj.getString("fieldLabels"));
            } else {
                column.setFieldLabels("");
            }
            if (obj.containsKey("fieldKeys")) {
                column.setFieldKeys(obj.getString("fieldKeys"));
            } else {
                column.setFieldKeys("");
            }
            if (obj.containsKey("searchLabel")) {
                column.setSearchLabel(obj.getString("searchLabel"));
            } else {
                column.setSearchLabel("");
            }
            if (obj.containsKey("searchKey")) {
                column.setSearchKey(obj.getString("searchKey"));
            } else {
                column.setSearchKey("");
            }

            if (obj.containsKey("selectSimple")) {
                column.setSelectSimple(obj.getString("selectSimple"));
            } else {
                column.setSelectSimple("");
            }
            if (obj.containsKey("selectValuefield")) {
                column.setSelectValuefield(obj.getString("selectValuefield"));
            } else {
                column.setSelectValuefield("");
            }
            if (obj.containsKey("selectDsf")) {
                column.setSelectDsf(obj.getString("selectDsf"));
            } else {
                column.setSelectDsf("");
            }
            if (obj.containsKey("selectOrder")) {
                column.setSelectOrder(obj.getString("selectOrder"));
            } else {
                column.setSelectOrder("");
            }
            if (obj.containsKey("listConfig")) {
                column.setListConfig(obj.getString("listConfig"));
            } else {
                column.setListConfig("");
            }
            if (obj.containsKey("formItemConfig")) {
                column.setFormItemConfig(obj.getString("formItemConfig"));
            } else {
                column.setFormItemConfig("");
            }
            if (obj.containsKey("extendJs")) {
                column.setExtendJs(obj.getString("extendJs"));
            } else {
                column.setExtendJs("");
            }
            if (obj.containsKey("align")) {
                column.setAlign(obj.getString("align"));
            } else {
                column.setAlign("");
            }
            if (obj.containsKey("settings")) {
                column.setSettings(obj.getString("settings"));
            } else {
                column.setSettings("");
            }
            column.setIsForm(String.valueOf(obj.getString("isForm") == null ? "0" : obj.getString("isForm")));
            column.setIsQuery(String.valueOf(obj.getString("isQuery") == null ? "0" : obj.getString("isQuery")));
            column.setIsList(String.valueOf(obj.getString("isList") == null ? "0" : obj.getString("isList")));
            if ("".equals(obj.getString("formSort"))) {
                column.setFormSort(100);
            } else {
                String formSort = obj.getString("formSort");
                BigDecimal bigDecimal = new BigDecimal(formSort);
                DecimalFormat df = new DecimalFormat("0");
                formSort = df.format(bigDecimal);
                int num = formSort.indexOf(".");
                if (formSort != null) {
                    if (num != -1) {
                        column.setFormSort(Integer.valueOf(formSort.substring(0, num)));
                    } else {
                        column.setFormSort(Integer.valueOf(formSort));
                    }
                } else {
                    column.setFormSort(100);
                }
            }
            if ("".equals(obj.getString("searchSort"))) {
                column.setSearchSort(100);
            } else {
                String searchSort = obj.getString("searchSort");
                BigDecimal bigDecimal = new BigDecimal(searchSort);
                DecimalFormat df = new DecimalFormat("0");
                searchSort = df.format(bigDecimal);
                int num = searchSort.indexOf(".");
                if (searchSort != null) {
                    if (num != -1) {
                        column.setSearchSort((Integer.valueOf(searchSort.substring(0, num))));
                    } else {
                        column.setSearchSort(Integer.valueOf(searchSort));
                    }
                } else {
                    column.setSearchSort(100);
                }
            }
            column.setIsReadonly(String.valueOf(obj.getString("isReadonly") == null ? "0" : obj.getString("isReadonly")));
            column.setRemarks(obj.getString("remarks"));
            column.setDefaultValue(obj.getString("defaultValue"));
            String name = obj.getString("name");
            if (name.indexOf(".") != -1) {
                String[] names = name.split("\\.");
                name = names[0] + "_" + names[1].split("\\|")[0];
                column.setName(name);
            } else {
                column.setName(obj.getString("name"));
            }
            if ("".equals(obj.getString("listSort"))) {
                column.setListSort(100);
            } else {
                String listSort = obj.getString("listSort");
                BigDecimal bigDecimal = new BigDecimal(listSort);
                DecimalFormat df = new DecimalFormat("0");
                listSort = df.format(bigDecimal);
                int num = listSort.indexOf(".");
                if (listSort != null) {
                    if (num != -1) {
                        column.setListSort((Integer.valueOf(listSort.substring(0, num))));
                    } else {
                        column.setListSort(Integer.valueOf(listSort));
                    }
                } else {
                    column.setListSort(100);
                }
            }
            column.preInsert();
            this.genTableColumnDao.insert(column);
        }
        if (!b) {
            if (!bId) {
                column = saveColumnId(column, genTable);

                this.genTableColumnDao.insert(column);
            }
            if (!bDel) {
                column = saveColumnDelFlag(column, genTable);

                this.genTableColumnDao.insert(column);
            }
        }
    }

    /**
     * Save column parent fk
     */
    private void saveColumnParentFk(JSONArray json, GenTableColumn column, GenTable genTable) {
        boolean b = false;
        for (int i = 0; i < json.size(); i++) {
            JSONObject obj = json.getJSONObject(i);
            String field = obj.getString("javaField");
            String str = "parent.id";
            if (str.equals(field)) {
                b = true;
                break;
            }
        }
        if (!b) {
            column.setGenTable(genTable);
            column.setId(null);
            column.setIsInsert("1");
            column.setShowType("input");
            column.setIsOneLine("");
            column.setIsNull("0");
            column.setIsPk("0");
            column.setComments("FK");
            column.setJavaField("parent.id");
            column.setMaxLength("");
            column.setMinLength("");
            column.setMinValue("");
            column.setMaxValue("");
            column.setJavaType("String");
            column.setJdbcType("varchar(64)");
            column.setQueryType("=");
            column.setValidateType("");
            column.setIsForm("0");
            column.setIsQuery("0");
            column.setIsList("0");
            column.setFormSort(100);
            column.setSearchSort(100);
            column.setIsReadonly("0");
            column.setDefaultValue("");
            column.setName("parent_id");
            column.setListSort(100);
            column.preInsert();
            this.genTableColumnDao.insert(column);
        }

    }

    /**
     * Save column pro
     */
    private void saveColumnPro(JSONArray json, GenTableColumn column, GenTable genTable) {
        String cate = genTable.getProcessDefinitionCategory();
        boolean b = false;
        boolean c = false;
        for (int i = 0; i < json.size(); i++) {
            JSONObject obj = json.getJSONObject(i);
            String field = obj.getString("javaField");
            String str = "procInsId";
            if (str.equals(field)) {
                b = true;
                break;
            }
        }
        for (int i = 0; i < json.size(); i++) {
            JSONObject obj = json.getJSONObject(i);
            String field = obj.getString("javaField");
            String str = "procDefKey";
            if (str.equals(field)) {
                c = true;
                break;
            }
        }
        if (!b) {
            if ("".equals(cate) == false && cate != null) {
                column.setGenTable(genTable);
                column.setId(null);
                column.setIsInsert("1");
                column.setShowType("input");
                column.setIsOneLine("");
                column.setIsNull("0");
                column.setIsEdit("1");
                column.setIsPk("0");
                column.setComments("Process Instance ID");
                column.setJavaField("procInsId");
                column.setMaxLength("");
                column.setMinLength("");
                column.setMinValue("");
                column.setMaxValue("");
                column.setJavaType("String");
                column.setJdbcType("varchar(64)");
                column.setQueryType("=");
                column.setValidateType("");
                column.setIsForm("0");
                column.setIsQuery("0");
                column.setIsList("0");
                column.setFormSort(100);
                column.setSearchSort(100);
                column.setIsReadonly("0");
                column.setDefaultValue("");
                column.setName("proc_ins_id");
                column.setListSort(100);
                column.preInsert();
                this.genTableColumnDao.insert(column);
            }
        }
        if (!c) {
            if ("".equals(cate) == false && cate != null) {
                column.setGenTable(genTable);
                column.setId(null);
                column.setIsInsert("1");
                column.setShowType("input");
                column.setIsOneLine("");
                column.setIsNull("0");
                column.setIsEdit("1");
                column.setIsPk("0");
                column.setComments("Process Def Key");
                column.setJavaField("procDefKey");
                column.setMaxLength("");
                column.setMinLength("");
                column.setMinValue("");
                column.setMaxValue("");
                column.setJavaType("String");
                column.setJdbcType("varchar(64)");
                column.setQueryType("=");
                column.setValidateType("");
                column.setIsForm("0");
                column.setIsQuery("0");
                column.setIsList("0");
                column.setFormSort(100);
                column.setSearchSort(100);
                column.setIsReadonly("0");
                column.setDefaultValue("");
                column.setName("proc_def_key");
                column.setListSort(110);
                column.preInsert();
                this.genTableColumnDao.insert(column);
            }
        }
    }

    /**
     * Save users name
     */
    private void saveUsersName(JSONArray json, GenTable genTable, String javaField, String name) {
        javaField = javaField.split("\\.")[0];
        boolean b = false;
        for (int i = 0; i < json.size(); i++) {
            JSONObject obj = json.getJSONObject(i);
            String field = obj.getString("javaField");
            String str = javaField + "Name";
            if (str.equals(field)) {
                b = true;
                break;
            }
        }
        if (!b) {
            GenTableColumn column = new GenTableColumn();
            column.setGenTable(genTable);
            column.setId(null);
            column.setIsInsert("1");
            column.setShowType("input");
            column.setIsOneLine("");
            column.setIsNull("0");
            column.setIsEdit("1");
            column.setComments("Users name");
            column.setJavaField(javaField + "Name");
            column.setMaxLength("");
            column.setMinLength("");
            column.setMinValue("");
            column.setMaxValue("");
            column.setJavaType("String");
            column.setJdbcType("varchar(2000)");
            column.setQueryType("=");
            column.setValidateType("");
            column.setIsForm("0");
            column.setIsQuery("0");
            column.setIsList("0");
            column.setFormSort(100);
            column.setSearchSort(100);
            column.setIsReadonly("0");
            column.setDefaultValue("");
            column.setName(name + "_name");
            column.setListSort(100);
            column.preInsert();
            this.genTableColumnDao.insert(column);
        }
    }

    /**
     * Save column del flag
     */
    private GenTableColumn saveColumnDelFlag(GenTableColumn column, GenTable genTable) {
        column.setGenTable(genTable);
        column.setId(null);
        column.setIsInsert("1");
        column.setShowType("input");
        column.setIsOneLine("");
        column.setIsNull("0");
        column.setIsPk("0");
        column.setComments("DEL FLAG");
        column.setJavaField("delFlag");
        column.setMaxLength("");
        column.setMinLength("");
        column.setMinValue("");
        column.setMaxValue("");
        column.setJavaType("String");
        column.setJdbcType("varchar(64)");
        column.setQueryType("=");
        column.setValidateType("");
        column.setIsForm("0");
        column.setIsQuery("0");
        column.setIsList("0");
        column.setFormSort(100);
        column.setSearchSort(100);
        column.setIsReadonly("0");
        column.setDefaultValue("");
        column.setName("del_flag");
        column.setListSort(7);
        column.preInsert();
        return column;
    }

    /**
     * Save column id
     */
    private GenTableColumn saveColumnId(GenTableColumn column, GenTable genTable) {
        String pkColumnName = StringUtil.isNotBlank(genTable.getPkColumnName()) ? genTable.getPkColumnName() : "id";
        column.setGenTable(genTable);
        column.setId(null);
        column.setShowType("input");
        column.setIsInsert("1");
        column.setIsOneLine("");
        column.setIsNull("0");
        column.setIsPk("1");
        column.setComments("PK");
        column.setJavaField("id");
        column.setMaxLength("");
        column.setMinLength("");
        column.setMinValue("");
        column.setMaxValue("");
        column.setJavaType("String");
        column.setJdbcType("varchar(64)");
        column.setQueryType("=");
        column.setValidateType("");
        column.setIsForm("0");
        column.setIsQuery("0");
        column.setIsList("0");
        column.setFormSort(100);
        column.setSearchSort(100);
        column.setIsReadonly("0");
        column.setDefaultValue("");
        column.setName(pkColumnName);
        column.setListSort(6);
        column.preInsert();
        return column;
    }

    /**
     * Get gentable column list
     */
    public List<GenTableColumn> getListByGenTableId(String genTableId, String javaType) {
        List<GenTableColumn> list = genTableColumnDao.getListByGenTableId(genTableId, javaType);
        return list;
    }

    /**
     * 保存动态表单基础配置，并标记为未同步状态以等待后续建表或同步表结构。
     */
    @Transactional(readOnly = false)
    public void saveDynamic(GenTable genTable, int length) {
        genTable.setIsSync(Global.NO);
        if (StringUtil.isBlank(genTable.getId())) {
            genTable.preInsert();
            this.genTableDao.insert(genTable);
        } else {
            genTable.preUpdate();
            this.genTableDao.update(genTable);
        }
        cacheUtil.deleteHashCache(GenUtil.GENTABLE_CACHE, genTable.getName());
    }

    /**
     * 查询表单定义
     *
     * @param genTable
     * @return
     */
    public ResultJson getDefinition(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        GenTableView genTableView = genTableDao.getGengTableViewById(genTable.getId());

        List<GenTableColumnView> data = genTableColumnDao.getGenTableColumnViewByGenTableId(genTableView.getId());
        List<GenTableColumnView> dataDict = genTableColumnDao.getGenTableColumnViewByGenTableIdDict(genTableView.getId());
        for (GenTableColumnView genTableColumnView : dataDict) {
            List<DictGenView> dictGenViewList = dictDataService.getDictGenView(genTableColumnView.getDictType());
            genTableColumnView.setDictList(dictGenViewList);
        }
        //data.addAll(dataDict);
        resultJson.setCode(0);
        resultJson.setMsg("获取动态表单成功");
        resultJson.setMsg_en("Get dynamic form success");
        resultJson.put("genTable", genTableView);
        resultJson.put("data", data);
        resultJson.put("dataDict", dataDict);
        return resultJson;
    }

    /**
     * 查询表单设计器编辑数据，包含主表、子表、字段、字典项和多对多关系配置。
     */
    public ResultJson editForm(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isBlank(genTable.getId())&&StringUtil.isBlank(genTable.getName())) {
            resultJson.setCode(0);
            resultJson.setMsg("获取动态表单成功");
            resultJson.setMsg_en("Get dynamic form success");
            resultJson.put("genTable", new GenTableView());
            resultJson.put("data", new GenTableColumnView());
        } else {
            GenTableView genTableView = null;
            if (StringUtil.isNotBlank(genTable.getId())) {
                genTableView = genTableDao.getGengTableViewById(genTable.getId());
            } else {
                GenTable genTableCache = getGenTableWithDefination(genTable.getName());
                if (genTableCache != null) {
                    genTableView = genTableDao.getGengTableViewById(genTableCache.getId());
                }
            }
            if (genTableView != null) {
                attachDeriveConfig(genTableView);
                List<GenTableChildren> genTableChildrenList = genTableDao.getGenTableViewByParentTable(genTableView.getName());
                for (GenTableChildren genTableChildren : genTableChildrenList) {
                    List<GenTableColumnView> genTableColumnView = genTableColumnDao.getGenTableColumnViewByGenTableId(genTableChildren.getId());
                    List<GenTableColumnView> genTableColumnViewDict = genTableColumnDao.getGenTableColumnViewByGenTableIdDict(genTableChildren.getId());
                    for (GenTableColumnView genTableColumnView1 : genTableColumnViewDict) {
                        List<DictGenView> dictGenViewList = dictDataService.getDictGenView(genTableColumnView1.getDictType());
                        genTableColumnView1.setDictList(dictGenViewList);
                    }
                    genTableColumnView.addAll(genTableColumnViewDict);
                    genTableChildren.setData(genTableColumnView);
                }
                genTableView.setChildren(genTableChildrenList);
                List<GenTableColumnView> data = genTableColumnDao.getGenTableColumnViewByGenTableId(genTableView.getId());
                List<GenTableColumnView> dataDict = genTableColumnDao.getGenTableColumnViewByGenTableIdDict(genTableView.getId());
                for (GenTableColumnView genTableColumnView : dataDict) {
                    List<DictGenView> dictGenViewList = dictDataService.getDictGenView(genTableColumnView.getDictType());
                    genTableColumnView.setDictList(dictGenViewList);
                }
                data.addAll(dataDict);
                resultJson.setCode(0);
                resultJson.setMsg("获取动态表单成功");
                resultJson.setMsg_en("Get dynamic form success");
                resultJson.put("genTable", genTableView);
                resultJson.put("data", data);
                GenTable table = getGenTableWithDefination(genTableView.getName());
                List<GenTableExtRuleManyToMany> manyToMany = table.getExtRuleManyToMany();
                List<Map<String,String>> rows = new ArrayList<>();
                for (GenTableColumnView datum : data) {
                    Map<String, String> row = new HashMap<>();
                    row.put("name", datum.getName());
                    row.put("comments", datum.getComments());
                    row.put("isForm", Global.YES.equals(datum.getIsForm()) ? "10000" : "20000");
                    row.put("sort", datum.getFormSort());
                    rows.add(row);
                }
                rows.sort(Comparator.comparingDouble(o -> Double.parseDouble(o.get("isForm")) + Double.parseDouble(o.get("sort"))));
                for (GenTableExtRuleManyToMany many : manyToMany) {
                    Map<String, String> row = new HashMap<>();
                    row.put("name", many.getName());
                    row.put("comments", many.getLabel());
                    rows.add(row);
                }
                //在rows中返回所有的列（含多对多关系）
                resultJson.setRows(rows);
            } else {
                resultJson.setCode(0);
                resultJson.setMsg("获取动态表单失败");
                resultJson.setMsg_en("Get dynamic form fail");
                resultJson.put("genTable", new GenTableView());
                resultJson.put("data", new GenTableColumnView());
            }
        }
        return resultJson;
    }

    /**
     * 保存表单配置，是设计器正式保存的核心入口，可选择是否跳过字段 JSON 持久化。
     */
    @Transactional
    public ResultJson saveGenTable(GenTable genTable,boolean skipSaveJson) {
        ResultJson resultJson = new ResultJson();
        JSONArray array = genTable.getJson();
        boolean needToSave = true;
        if (StringUtil.isBlank(genTable.getId())) {
            //New genTable
            if (this.existTable(genTable.getName())) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("保存失败，表定义已存在。");
                resultJson.setMsg_en("Saving failed, table definition already exists.");
                resultJson.put("gentableId", "");
                needToSave = false;
            } else if (this.existTableNameInDB(genTable.getName())) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("保存失败，请从数据库导入表。");
                resultJson.setMsg_en("Saving failed, please import table from database.");
                resultJson.put("gentableId", "");
                needToSave = false;
            } else {
                //Save new genTable
                genTable.setIsNewRecord(true);
            }
        } else {
            //Update ,check oldName and name
            String oldName = genTable.getOldName();
            if (StringUtil.isNotEmpty(oldName) && false == oldName.equalsIgnoreCase(genTable.getName())) {
                if (this.existTable(genTable.getName())) {
                    resultJson.setCode(ResultJson.CODE_FAILED);
                    resultJson.setMsg("保存失败，表定义已存在。");
                    resultJson.setMsg_en("Saving failed, table definition already exists.");
                    resultJson.put("gentableId", "");
                    needToSave = false;
                }
            }
        }
        if (needToSave) {
            genTable.setIsRelease(Global.NO);
            genTable.setIsSync(Global.NO);
            genTable.setClassName("Zform");
            genTable.setFormType("dynamic");
            genTable.setIsBuildXform(Global.YES);
            if (StringUtil.isNotEmpty(genTable.getParentTable())) {
                if (StringUtil.isEmpty(genTable.getParentTableFk())) genTable.setParentTableFk("parent_id");
            }
            GenTable gentBeforeSave = new GenTable();
            if (StringUtil.isNotEmpty(genTable.getId())) {
                gentBeforeSave = this.get(genTable.getId());
            }
            //保存基本信息
            normalizeDeriveConfig(genTable);
            this.saveDynamic(genTable, array == null ? 0 : array.size());
            saveDeriveConfigIfPresent(genTable);
            if (!skipSaveJson){
                this.saveJsons(genTable, StringUtil.isBlank(genTable.getId()));
            }
            GenTable gent = this.get(genTable.getId());

            //多对多关系表，去掉id和delFlag
            //统计视图去掉id,delFlag
            List<GenTableColumn> genTableColumnList = gentBeforeSave.getColumnList();
            if (genTableColumnList.size() == 2 || genTableColumnList.size() == 4 || genTable.getName().toLowerCase().endsWith(GenUtil.SUM_VIEW)) {
                gent.setColumnList(Lists.newArrayList());
                for (GenTableColumn column : genTableColumnList) {
                    if (false == column.getName().equalsIgnoreCase("id") && false == column.getName().equalsIgnoreCase("del_flag")) {
                        gent.getColumnList().add(column);
                    }
                }
            }

            gent.setSqlColumns(gentBeforeSave.getSqlColumns());
            gent.setSqlColumnsFriendly(gentBeforeSave.getSqlColumnsFriendly());
            gent.setSqlJoins(gentBeforeSave.getSqlJoins());
            gent.setSqlSort(gentBeforeSave.getSqlSort());
            GenUtil.buildSqlMapForDynamicTable(gent, dbType);
            // datahouse 模块自动生成 extSql02（MySQL/Doris 语法查询列片段）
            if (GenTable.MODULE_DATAHOUSE.equals(gent.getModule())) {
                GenUtil.buildExtSql02ForDatahouse(gent);
            }
            this.saveSql(gent);
            this.saveDict(gent);
            if (StringUtil.isEmpty(genTable.getExtJs()) && Global.YES.equals(genTable.getIsMobile())) {
                gent.setExtJs(GenAppUtil.buildFormDefinition(gent, dictDataService.getDictList("app-form-group", false)));
                this.save(gent);
            }
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("保存动态表单成功。");
            resultJson.setMsg_en("Save dynamic success.");
            resultJson.put("gentableId", genTable.getId());
            resultJson.put("genTable", genTable);
            this.refreshGenTableCache(genTable.getName());
        }
        return resultJson;
    }

    /**
     * 创建包含所有字段的App表单定义
     * @param formId
     * @return
     */
    public String buildFormDefinitionForApp(String formId) {
        GenTable genTable = this.get(formId);
        genTable = this.getGenTableWithDefination(genTable.getName());
        return GenAppUtil.buildFormDefinition(genTable, dictDataService.getDictList("app-form-group",false), true);
    }

    @Transactional(readOnly = false)
    public void saveDict(GenTable genTable) {
        //genTable = this.getGenTableWithDefination(genTable.getName());
        String code = genTable.getName();
        String comments = genTable.getComments();
        String commentsEn = genTable.getCommentsEn();
        dictDataService.deleteCascade(code);

        Dict fieldParent = buildSimpleDict("page-form-field", "page-form-field", "0,");
        Dict cellParent = buildSimpleDict("page-table-cell", "page-table-cell", "0,");
        Dict fieldDict = buildChildDict(code, comments, commentsEn, fieldParent);
        Dict cellDict = buildChildDict(code, comments, commentsEn, cellParent);
        dictDataService.save(fieldDict);
        dictDataService.save(cellDict);

        for (GenTableColumn column : genTable.getColumnList()) {
            Dict parentDict = Global.YES.equals(column.getIsForm()) ? fieldDict
                    : (Global.YES.equals(column.getIsList()) || Global.YES.equals(column.getIsQuery())) ? cellDict
                    : null;
            if (parentDict != null) {
                dictDataService.save(buildChildDict(column.getName(), column.getComments(), column.getCommentsEn(), parentDict));
            }
        }
    }

    /**
     * Build a simple Dict with id, code, parentIds
     */
    private Dict buildSimpleDict(String id, String code, String parentIds) {
        Dict dict = new Dict();
        dict.setId(id);
        dict.setCode(code);
        dict.setParentIds(parentIds);
        return dict;
    }

    /**
     * Build a child Dict under the given parent
     */
    private Dict buildChildDict(String code, String name, String nameEn, Dict parent) {
        Dict dict = new Dict();
        dict.setCode(code);
        dict.setName(name);
        dict.setNameEn(nameEn);
        dict.setParent(parent);
        dict.setParentCode(parent.getCode());
        dict.setParentIds(parent.getParentIds() + parent.getId() + ",");
        return dict;
    }

    /**
     * 同步动态表单物理表结构：不存在时创建，存在时按字段差异增量同步。
     */
    @Transactional
    public ResultJson syncDynamicTable(GenTable genTable) {
        StringBuilder log = new StringBuilder();
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isEmpty(genTable.getId())) {
            resultJson.setCode(0);
            resultJson.setMsg("同步表失败");
            resultJson.setMsg_en("Sync table failed");
            return resultJson;
        } else {
            genTable = this.get(genTable.getId());

            if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
                // datahouse 模块：跳过 master 同步，只在 Doris 中重建
                AbstractGenDialect dorisDialect = GenUtil.getGenDialect("doris");
                String dropSql = dorisDialect.getDropTableSql(genTable.getName());
                datahouseService.buildTable(dropSql);
                String uniqueKey = StringUtil.isNotBlank(genTable.getBlockChainParam2())
                    ? genTable.getBlockChainParam2() : null;
                String partitionKey = StringUtil.isNotBlank(genTable.getBlockChainParam3())
                    ? genTable.getBlockChainParam3() : null;
                String distributedKey = StringUtil.isNotBlank(genTable.getBlockChainParam4())
                    ? genTable.getBlockChainParam4() : null;
                String[] createSqls = ((DorisGenDialect) dorisDialect).getCreateTableSql(
                    genTable.getName(), genTable.getComments(), genTable.getColumnList(), uniqueKey, partitionKey, distributedKey);
                for (String sql : createSqls) {
                    datahouseService.buildTable(sql);
                }
            } else {
                // 普通模块：在 master 中同步
                //从系统视图查询表，不存在则创建，存在则遍历字段，执行同步
                if (false == this.existTable(genTable.getName())) {
                    //表未创建
                    resultJson = this.syncDynamic(genTable);
                } else {
                    //表已创建
                    String sql = null;
                    genTable.setDbName(dbType);
                    List<GenTableColumn> columnListInDb = genDataBaseDictDao.findTableColumnList(genTable);

                    AbstractGenDialect genDialect = GenUtil.getGenDialect(dbType);

                    for (GenTableColumn column : genTable.getColumnList()) {
                        column = this.isNewColumn(column, columnListInDb, genDialect);
                        if (column != null && "isnew".equals(column.getRemarks())) {
                            //新字段
                            String[] addColumnSql = genDialect.getAddColumnSql(genTable.getName(), column);
                            for (String addColumn : addColumnSql) {
                                genTableDao.buildTable(addColumn);
                                log.append(addColumn + ";");
                            }
                        } else if (column != null) {
                            //字段类型有变化
                            String[] modifyColumnSql = genDialect.getModifyColumnSql(genTable.getName(), column);
                            for (String modifyColumn : modifyColumnSql) {
                                genTableDao.buildTable(modifyColumn);
                                log.append(modifyColumn + ";");
                            }
                        }
                    }
                }
            }

            this.syncSave(genTable);
            this.checkManyToMany(genTable);

            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("同步表成功。");
            resultJson.setMsg_en("Sync table success.");
            resultJson.put("gentableId", genTable.getId());
            resultJson.put("genTable", genTable);
            this.refreshGenTableCache(genTable.getName());
            logger.info(log.toString());
        }
        return resultJson;
    }

    /**
     * 批量同步动态表单及其版本表，通常用于配置批处理或数据库切换后的结构刷新。
     */
    @Transactional
    public void syncTables(List<GenTable> genTableList) {
        for (GenTable genTable: genTableList) {
            if (false == genTable.getName().contains("@view")) {
                syncDynamicTable(genTable);
            }
        }
        for (GenTable genTable: genTableList) {
            if (false == genTable.getName().contains("@view") && Global.YES.equals(genTable.getIsVersion())) {
                syncVersionTable(genTable);
            }
        }
    }

    /**
     * 数据库类型切换后重建表单 SQL 映射，并刷新主表及子表缓存。
     */
    @Transactional
    public void changeDb(List<GenTable> genTableList) throws Exception {
        String theName = "";
        try {
            for (GenTable genTable : genTableList) {
                theName = genTable.getName();
                //重构Sql
                genTable = this.getGenTableWithDefination(genTable.getName());
                GenUtil.buildSqlMapForDynamicTable(genTable, dbType);
                this.save(genTable);
                this.refreshGenTableCache(genTable.getName());
                List<GenTable> subList = genTable.getChildList();
                for (GenTable subGenTable : subList) {
                    theName = subGenTable.getName();
                    //重构Sql
                    subGenTable = this.getGenTableWithDefination(subGenTable.getName());
                    GenUtil.buildSqlMapForDynamicTable(subGenTable, dbType);
                    this.save(subGenTable);
                    this.refreshGenTableCache(subGenTable.getName());
                }
            }
        } catch (Exception err) {
            throw new Exception(theName + " " + err.getMessage());
        }
    }

    @Transactional
    public void syncVersionTable(GenTable genTable) {
        StringBuilder log = new StringBuilder();
        if (StringUtil.isNotEmpty(genTable.getId())) {
            genTable = this.get(genTable.getId());
            String genTableName = genTable.getName();
            String genTableNameWithSchema = this.getTableNameWithSchema(versionSchema, genTableName);
            //从系统视图查询表，不存在则创建，存在则遍历字段，执行同步
            if (false == this.existTableNameInDB(genTableNameWithSchema + "_v")
                    && false == this.existTableNameInDB(genTableNameWithSchema + "_V")) {
                //留痕表未创建
                this.syncDynamicVersion(genTable);
            } else {
                if (genTable.getColumnList().size() == 4) {
                    List<GenTableColumn> listNew = Lists.newArrayList();
                    for (GenTableColumn column : genTable.getColumnList()) {
                        if (false == "id".equalsIgnoreCase(column.getName()) && false == "del_flag".equalsIgnoreCase(column.getName())) {
                            column.setIsPk(Global.YES);
                            listNew.add(column);
                        }
                    }
                    genTable.setColumnList(listNew);
                }
                //留痕表已创建
                String sql = null;
                genTable.setDbName(dbType);
                genTable.setName(genTableNameWithSchema + "_v");
                List<GenTableColumn> columnListInDb = genDataBaseDictDao.findTableColumnList(genTable);
                AbstractGenDialect genDialect = GenUtil.getGenDialect(dbType);
                for (GenTableColumn column : genTable.getColumnList()) {
                    column = this.isNewColumn(column, columnListInDb, genDialect);
                    if (column != null && "isnew".equals(column.getRemarks())) {
                        //新字段
                        String[] addColumnSql = genDialect.getAddColumnSql(genTable.getName(), column);
                        for (String addColumn : addColumnSql) {
                            genTableDao.buildTable(addColumn);
                            log.append(addColumn + ";");
                        }
                    } else if (column != null) {
                        //字段类型有变化
                        String[] modifyColumnSql = genDialect.getModifyColumnSql(genTable.getName(), column);
                        for (String modifyColumn : modifyColumnSql) {
                            genTableDao.buildTable(modifyColumn);
                            log.append(modifyColumn + ";");
                        }
                    }
                }

            }
            genTable.setName(genTableName);
            this.syncSave(genTable);
            this.refreshGenTableCache(genTable.getName());
            logger.info(log.toString());
        }
    }

    /**
     * 检查字段是否需要创建
     *
     * @param column
     * @param columnListInDb
     * @return
     */
    protected GenTableColumn isNewColumn(GenTableColumn column, List<GenTableColumn> columnListInDb) {
        return isNewColumn(column, columnListInDb, null);
    }

    /**
     * 检查字段是否需要创建或修改类型。
     *
     * @param genDialect 当前库方言；非空时用语义等价比较 jdbcType（如 datetime≈timestamp）
     */
    protected GenTableColumn isNewColumn(GenTableColumn column, List<GenTableColumn> columnListInDb,
                                         AbstractGenDialect genDialect) {
        column.setRemarks("isnew");
        for (GenTableColumn columnInDb : columnListInDb) {
            if (columnInDb.getName().equalsIgnoreCase(column.getName())) {
                column.setRemarks("isnotnew");
                // 字段类型无变化（含方言语义等价：datetime vs timestamp、integer vs int 等）
                if (isJdbcTypeUnchanged(column.getJdbcType(), columnInDb.getJdbcType(), genDialect)) {
                    column = null;
                }
                break;
            }
        }
        return column;
    }

    /**
     * 元数据 jdbcType 与库中 jdbcType 是否视为未变化。
     */
    protected boolean isJdbcTypeUnchanged(String metaJdbcType, String dbJdbcType, AbstractGenDialect genDialect) {
        if (metaJdbcType == null && dbJdbcType == null) {
            return true;
        }
        if (metaJdbcType != null && metaJdbcType.equalsIgnoreCase(dbJdbcType)) {
            return true;
        }
        if (genDialect != null) {
            return genDialect.isJdbcTypeEquivalent(metaJdbcType, dbJdbcType);
        }
        return false;
    }

    private boolean buildManayToManyVersionTable(GenTable genTable) {
        boolean isManyToMany = false;
        if (genTable.getColumnList().size() == 4) {
            isManyToMany = true;
            List<GenTableColumn> listNew = Lists.newArrayList();
            for (GenTableColumn column : genTable.getColumnList()) {
                if (false == "id".equalsIgnoreCase(column.getName()) && false == "del_flag".equalsIgnoreCase(column.getName())) {
                    column.setIsPk(Global.YES);
                    listNew.add(column);
                }
            }
            genTable.setColumnList(listNew);
            this.syncDynamicVersionTable(genTable);
        }
        return isManyToMany;
    }

    private String getTableNameWithSchema(String versionSchema, String tableName) {
        //没有配置留痕库 直接返回
        if (StringUtil.isBlank(versionSchema)){
            return tableName;
        }
        if (tableName.indexOf(".") != -1) {
            tableName = tableName.substring(tableName.indexOf(".") + 1);
        }
        return (StringUtil.isBlank(versionSchema) ? "" : (versionSchema + ".")) + tableName;
    }


    private void syncDynamicVersionTable(GenTable genTable){
        String tableName = genTable.getName() + "_v";
        String tableNameWithSchema = this.getTableNameWithSchema(versionSchema, tableName);
        List<GenTableColumn> getTableColumnList = genTable.getColumnList();

        boolean vDateCreated = false;
        List<GenTableColumn> versionColumnList = Lists.newArrayList();
        for (GenTableColumn genTableColumn : getTableColumnList) {
            versionColumnList.add(genTableColumn);
            if (Global.YES.equals(genTableColumn.getIsPk()) && !vDateCreated){
                GenTableColumn vdate = new GenTableColumn();
                vdate.setIsPk(Global.YES);
                vdate.setName("vdate");
                vdate.setJdbcType("datetime");
                vdate.setComments("留痕时间");
                versionColumnList.add(vdate);
                vDateCreated = true;
            }
        }

        if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
            // datahouse 模块：只在 Doris 中创建留痕表
            AbstractGenDialect dorisDialect = GenUtil.getGenDialect("doris");
            String vTableName = genTable.getName() + "_v";
            String dropSql = dorisDialect.getDropTableSql(vTableName);
            datahouseService.buildTable(dropSql);
            String uniqueKey = StringUtil.isNotBlank(genTable.getBlockChainParam2())
                ? genTable.getBlockChainParam2() : null;
            String partitionKey = StringUtil.isNotBlank(genTable.getBlockChainParam3())
                ? genTable.getBlockChainParam3() : null;
            String distributedKey = StringUtil.isNotBlank(genTable.getBlockChainParam4())
                ? genTable.getBlockChainParam4() : null;
            String[] createSqls = ((DorisGenDialect) dorisDialect).getCreateTableSql(
                vTableName, genTable.getComments() + "留痕表", versionColumnList, uniqueKey, partitionKey, distributedKey);
            for (String sql : createSqls) {
                datahouseService.buildTable(sql);
            }
        } else {
            // 普通模块：在 master 中创建留痕表
            AbstractGenDialect genDialect = GenUtil.getGenDialect(dbType);
            try {
                this.buildTable(genDialect.getDropTableSql(tableNameWithSchema));
            } catch (Exception e) {
                logger.error("删除表失败", e);
            }
            String[] createTableSql = genDialect.getCreateTableSql(tableNameWithSchema, genTable.getComments()+"_留痕表", versionColumnList);
            for (String sql : createTableSql) {
                this.buildTable(sql);
            }
        }
    }

    /**
     * 创建留痕表
     * @param genTable
     * @return
     */
    @Transactional
    public ResultJson syncDynamicVersion(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isEmpty(genTable.getId())) {
            resultJson.setCode(0);
            resultJson.setMsg("创建留痕表失败");
            resultJson.setMsg_en("Sync version table failed");
            return resultJson;
        } else {
            genTable = this.get(genTable.getId());
            if (false == this.buildManayToManyVersionTable(genTable)) {
                this.syncDynamicVersionTable(genTable);
            }
            this.syncSave(genTable);
            resultJson.setCode(0);
            resultJson.setMsg("创建留痕表成功");
            resultJson.setMsg_en("Sync version table success");
            return resultJson;
        }

    }
    @Value("${spring.profiles.active}")
    String active;
    /**
     * 重建动态表单物理表，生产环境默认禁止执行，避免误删正式数据表。
     */
    @Transactional
    public ResultJson syncDynamic(GenTable genTable) {
        if ("prod".equals(active)){
            ResultJson resultJson = new ResultJson();
            resultJson.setCode(0);
            resultJson.setMsg("prod不允许重建表");
            resultJson.setMsg_en("Sync table failed");
            return resultJson;
        }
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isEmpty(genTable.getId())) {
            resultJson.setCode(0);
            resultJson.setMsg("同步数据库失败");
            resultJson.setMsg_en("Sync table failed");
            return resultJson;
        } else {
            genTable = this.get(genTable.getId());
            String tableName = genTable.getName();
            List<GenTableColumn> getTableColumnList = genTable.getColumnList();

            if (GenTable.MODULE_DATAHOUSE.equals(genTable.getModule())) {
                // datahouse 模块：只在 Doris 中重建
                AbstractGenDialect dorisDialect = GenUtil.getGenDialect("doris");
                try {
                    datahouseService.buildTable(dorisDialect.getDropTableSql(tableName));
                } catch (Exception e) {
                    logger.error("Doris删除表失败", e);
                }
                String uniqueKey = StringUtil.isNotBlank(genTable.getBlockChainParam2())
                    ? genTable.getBlockChainParam2() : null;
                String partitionKey = StringUtil.isNotBlank(genTable.getBlockChainParam3())
                    ? genTable.getBlockChainParam3() : null;
                String distributedKey = StringUtil.isNotBlank(genTable.getBlockChainParam4())
                    ? genTable.getBlockChainParam4() : null;
                String[] createSqls = ((DorisGenDialect) dorisDialect).getCreateTableSql(
                    tableName, genTable.getComments(), getTableColumnList, uniqueKey, partitionKey, distributedKey);
                for (String sql : createSqls) {
                    datahouseService.buildTable(sql);
                }
            } else {
                // 普通模块：在 master 中重建
                AbstractGenDialect genDialect = GenUtil.getGenDialect(dbType);
                try {
                    this.buildTable(genDialect.getDropTableSql(tableName));
                } catch (Exception e) {
                    logger.error("删除表失败", e);
                }
                String[] createTableSql = genDialect.getCreateTableSql(tableName, genTable.getComments(), getTableColumnList);
                for (String sql : createTableSql) {
                    this.buildTable(sql);
                }
            }

            this.syncSave(genTable);
            this.checkManyToMany(genTable);

            resultJson.setCode(0);
            resultJson.setMsg("同步数据库成功");
            resultJson.setMsg_en("Sync table success");
            return resultJson;
        }

    }

    //检查过多多关系表定义，不存在时创建
    private void checkManyToMany(GenTable genTable) {
        Integer tableSort = genTable.getTableSort();
        if (tableSort == null) {
            tableSort = 0;
        }
        for(GenTableExtRuleManyToMany manyToMany : genTable.getExtRuleManyToMany()) {
            GenTable relGenTable = this.getGenTableWithDefination(manyToMany.getRelTable());
            tableSort++;
            if (relGenTable == null) {
                //不存在，创建，从sys_user_role复制后再修改
                this.copyManyToMany(tableSort,manyToMany,genTable.getModule());
            }
        }
    }

    private void copyManyToMany(Integer tableSort,GenTableExtRuleManyToMany manyToMany,String module) {
        GenTable genTable = this.getGenTableWithDefination("sys_user_role");
        if (genTable != null) {
            //复制关系表定义
            genTable.setIsSync(Global.NO);

            genTable.setId(IdGen.uuid());
            genTable.setModule(module);
            genTable.setTableSort(tableSort);
            genTable.setName(manyToMany.getRelTable());
            genTable.setClassName(StringUtil.toCamelCase(manyToMany.getRelTable()));
            genTable.setComments(manyToMany.getLabel());
            genTable.setCommentsEn(transService.getTransResultString(manyToMany.getLabel(), "auto", "en"));
            this.genTableDao.insert(genTable);

            /*
            "label":"角色成员",
            "relTable":"sys_user_role",
            "relColumn":"role_id",
            "relManyColumn":"user_id",
             */
            int i = 0;
            List<GenTableColumn> newColumns = new ArrayList<>();
            for (GenTableColumn column : genTable.getColumnList()) {
                if ("id".equals(column.getName()) || "del_flag".equals(column.getName())) {
                    continue;
                }
                column.setGenTable(genTable);
                column.setId(IdGen.uuid());
                if (i++ == 0) {
                    column.setName(manyToMany.getRelColumn());
                    column.setComments(manyToMany.getRelColumn());
                    column.setCommentsEn(manyToMany.getRelColumn());
                    column.setIsPk(Global.YES);
                } else {
                    column.setName(manyToMany.getRelManyColumn());
                    column.setComments(manyToMany.getRelManyColumn());
                    column.setCommentsEn(manyToMany.getRelManyColumn());
                    column.setIsPk(Global.YES);
                }
                newColumns.add(column);
                this.genTableColumnDao.insert(column);
            }
            genTable.setColumnList(newColumns);
            //重建表
            this.syncDynamic(genTable);
            this.saveGenTable(genTable,true);
        }
    }

    @Transactional
    public ResultJson removeDynamic(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isNotBlank(genTable.getId())) {
            genTable = get(genTable.getId());
        }
        this.delete(genTable);
        genSchemeDao.delete(genSchemeDao.findUniqueByProperty("gen_table_id", genTable.getId()));
        resultJson.setMsg_en("Remove gentable success");
        resultJson.setMsg("移除成功");
        resultJson.setCode(0);
        return resultJson;
    }

    @Transactional
    public ResultJson deleteDynamic(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isNotBlank(genTable.getId())) {
            genTable = get(genTable.getId());
        }
        this.delete(genTable);
        genSchemeDao.delete(genSchemeDao.findUniqueByProperty("gen_table_id", genTable.getId()));
        StringBuffer sql = new StringBuffer();
        if ("mysql".equals(dbType) || "postgresql".equals(dbType)) {
            sql.append("drop table if exists " + genTable.getName() + " ;");
        } else if ("oracle".equals(dbType) || "dm".equals(dbType)||"kingbase".equals(dbType))
            sql.append("DROP TABLE " + genTable.getName());
        else if (("mssql".equals(dbType)) || ("sqlserver".equals(dbType))) {
            sql.append("if exists (select * from sysobjects where id = object_id(N'["
                    + genTable.getName()
                    + "]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)  drop table ["
                    + genTable.getName() + "]");
        }
        try {
            this.buildTable(sql.toString());
            resultJson.setMsg_en("Delete gentable success");
            resultJson.setMsg("删除业务表记录和数据库表成功");
            resultJson.setCode(0);
        } catch (Exception localException) {
            resultJson.setMsg_en("Table does not exists, remove gentable success");
            resultJson.setMsg("数据库表不存在，删除业务表记录成功");
            resultJson.setCode(0);
        }
        return resultJson;
    }

    @Transactional
    public ResultJson copyDynamic(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isNotBlank(genTable.getId())) {
            genTable = get(genTable.getId());
        }
        this.copy(genTable);
        resultJson.setMsg_en("Copy gentable success");
        resultJson.setMsg("复制动态表单成功");
        resultJson.setCode(0);
        return resultJson;
    }

    @Transactional
    public ResultJson importListDynamic() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(0);
        resultJson.setMsg_en("Get table success");
        resultJson.setMsg("获取数据表成功");
        resultJson.put("tableList", this.findTableListFormDb(new GenTable()));
        return resultJson;
    }

    @Transactional
    public ResultJson importListDynamicSchema() {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(0);
        resultJson.setMsg_en("Get table success");
        resultJson.setMsg("获取数据表成功");
        resultJson.put("tableList", this.findTableListFormDbSchema(new GenTable()));
        return resultJson;
    }

    @Transactional
    public ResultJson importDynamic(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (!StringUtil.isBlank(genTable.getName())) {
            if (this.existTable(genTable.getName())) {
                resultJson.setCode(0);
                resultJson.setMsg_en("Table exists");
                resultJson.setMsg("表已经添加！");
                return resultJson;
            }
            (genTable = this.getTableFormDb(genTable, dbType))
                    .setTableType("0");
            genTable.setIsBuildXform(Global.YES);
            //save
            genTable.setClassName("Zform");
            genTable.setFormType("dynamic");
            genTable.setTableType("0");
            genTable.setIsRelease(Global.NO);
            genTable.setIsSync(Global.NO);
            genTable.preInsert();
            if (StringUtil.isNotEmpty(genTable.getComments())) {
                genTable.setCommentsEn(transService.getTransResultString(genTable.getComments(), "auto", "en"));
            }
            this.genTableDao.insert(genTable);
            for (GenTableColumn column : genTable.getColumnList()) {
                if (StringUtil.isNotEmpty(column.getComments())) {
                    column.setCommentsEn(transService.getTransResultString(column.getComments(), "auto", "en"));
                }
                column.setGenTable(genTable);
                column.setId(null);
                column.preInsert();
                this.genTableColumnDao.insert(column);
            }
            resultJson.setCode(0);
            resultJson.setMsg_en("Import table success");
            resultJson.setMsg("数据库导入表单成功！");
        }
        return resultJson;
    }

    @Transactional
    public ResultJson importDynamicSchema(GenTable genTable) {
        ResultJson resultJson = new ResultJson();
        if (!StringUtil.isBlank(genTable.getName())) {
            if (this.existTable(genTable.getName())) {
                resultJson.setCode(0);
                resultJson.setMsg_en("Table exists");
                resultJson.setMsg("表已经添加！");
                return resultJson;
            }
            (genTable = this.getTableFormDb(genTable, dbType))
                    .setTableType("0");
            genTable.setIsBuildXform(Global.YES);
            //save
            genTable.setClassName("Zform");
            genTable.setFormType("dynamic");
            genTable.setTableType("0");
            genTable.setIsRelease(Global.NO);
            genTable.setIsSync(Global.NO);
            genTable.preInsert();
            if (StringUtil.isNotEmpty(genTable.getComments())) {
                genTable.setCommentsEn(transService.getTransResultString(genTable.getComments(), "auto", "en"));
            }
            this.genTableDao.insert(genTable);
            for (GenTableColumn column : genTable.getColumnList()) {
                if (StringUtil.isNotEmpty(column.getComments())) {
                    column.setCommentsEn(transService.getTransResultString(column.getComments(), "auto", "en"));
                }
                column.setGenTable(genTable);
                column.setId(null);
                column.preInsert();
                this.genTableColumnDao.insert(column);
            }
            resultJson.setCode(0);
            resultJson.setMsg_en("Import table success");
            resultJson.setMsg("数据库导入表单成功！");
        }
        return resultJson;
    }

    @Transactional
    public ResultJson buildViewDynamic(GenScheme genScheme) {
        ResultJson resultJson = new ResultJson();
        if (StringUtil.isBlank(genScheme.getPackageName()))
            genScheme.setPackageName("com.gt_plus.modules");
        GenScheme oldGenScheme;
        if ((oldGenScheme = genSchemeDao.findUniqueByProperty(
                "gen_table_id", genScheme.getGenTable().getId())) != null) {
            genScheme = oldGenScheme;
        }
        genScheme.setGenTable(get(genScheme.getGenTable().getId()));
        resultJson.setCode(0);
        resultJson.setMsg("获取发布信息成功");
        resultJson.setMsg_en("Get build message success");
        resultJson.put("genScheme", genScheme);
        return resultJson;
    }

    /**
     * 发布动态表单，生成前端/后端运行所需配置，并递归发布子表。
     */
    @Transactional
    public ResultJson buildDynamic(GenScheme genScheme, String currentUserName) throws Exception {
        ResultJson resultJson = new ResultJson();
        /*GenTable genTable = this.get(genScheme.getGenTable().getId());
        genTable.setIsRelease(Global.YES);
        this.save(genTable);
        if (StringUtil.isBlank(genScheme.getId())) {
            genScheme.setIsNewRecord(true);
            genScheme.preInsert();
            this.genSchemeDao.insert(genScheme);
        } else {
            genScheme.preUpdate();
            this.genSchemeDao.update(genScheme);
        }
        String result = this.xgenerateCode(genScheme, currentUserName);*/
        this.releaseTables(genScheme, currentUserName);

        resultJson.setCode(0);
        resultJson.setMsg("发布成功");
        resultJson.setMsg_en("Build Success");
        return resultJson;
    }

    private void releaseTables(GenScheme genScheme, String currentUserName) throws Exception {
        GenTable genTable = this.get(genScheme.getGenTable().getId());
        genTable.setIsRelease(Global.YES);
        this.save(genTable);

        if (StringUtil.isEmpty(genScheme.getRemarks())) {
            if (StringUtil.isBlank(genScheme.getId())) {
                genScheme.setIsNewRecord(true);
                genScheme.preInsert();
                this.genSchemeDao.insert(genScheme);
            } else {
                genScheme.preUpdate();
                this.genSchemeDao.update(genScheme);
            }
        }

        //refreshGenTableCache(genTable.getName());
        genTable = this.getGenTableWithDefination(genTable.getName());

        this.xgenerateCode(genScheme, currentUserName);
        //级联发布子表
        for (GenTable child : genTable.getChildList()) {
            GenScheme genSchemeChild = new GenScheme();
            genSchemeChild.setCategory("dynamic");
            genSchemeChild.setFunctionNameSimple(child.getComments());
            genSchemeChild.setFunctionName(child.getComments());
            genSchemeChild.setGenTable(child);
            genSchemeChild.setRemarks("isChild");
            this.releaseTables(genSchemeChild, currentUserName);
        }
    }

    private String xgenerateCode(GenScheme genScheme, String currentUserName) throws Exception {

        StringBuilder result = new StringBuilder();
        String timePath = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        GenTable genTable = genTableDao.get(genScheme.getGenTable().getId());
        genScheme.setGenTable(genTable);
        genTable.setColumnList(genTableColumnDao.findList(new GenTableColumn(
                new GenTable(genTable.getId()))));
        GenConfig config = GenUtil.getConfig();

        boolean isMobile = Global.YES.equals(genTable.getIsMobile()) ? true : false;
        List<GenTemplate> templateList = GenUtil.getTemplateList(config, genScheme.getGenTable().getFormType(), false, isMobile, this.isGenJava(currentUserName));
        if (genTable.getChildList() == null || genTable.getChildList().size() == 0) {
            if (false == genTable.getTableType().equals(GenTable.TABLE_TYPE_TREE)) {
                GenTable parentTable = new GenTable();
                parentTable.setParentTable(genTable.getName());
                List<GenTable> childList = genTableDao.findList(parentTable);
                for (GenTable subTable : childList) {
                    GenTableColumn genTableColumn = new GenTableColumn();
                    genTableColumn.setGenTable(new GenTable(subTable.getId()));
                    subTable.setColumnList(this.genTableColumnDao.findList(genTableColumn));
                }
                genTable.setChildList(childList);
            }
        }
        genScheme.setGenTable(genTable);
        Map<String, Object> model = GenUtil.getDataModel(genScheme, dbType);

        String projectPath = null;
        if (Boolean.parseBoolean(this.getSettingValueByKey(GenKey.VUE.getValue(), currentUserName))) {
            // 代码生成，允许前端页面生成到项目目录
            projectPath = this.getProjectVueFolderPath(currentUserName);
            Path vueModulePath = Paths.get(projectPath + "/views/" + genTable.getModule() + "/" + genTable.getName());
            if (false == Files.exists(vueModulePath)) {
                // 目录不存在，创建多级目录
                Files.createDirectories(vueModulePath);
            } else {
                // 目录已经存在，判断文件是否存在
                if (Files.notExists(Paths.get(projectPath + "/views/" + genTable.getModule() + "/" + genTable.getName() + "/list.vue"))
                        && Files.notExists(Paths.get(projectPath + "/views/" + genTable.getModule() + "/" + genTable.getName() + "/form.vue"))) {
                    // 文件不存在，直接发布到项目目录中
                } else {
                    // 文件已经存在，要发布到系统设置gen.key定义的目录中
                    projectPath = this.getGenFolder(currentUserName);
                }
            }
        } else {
            projectPath = this.getGenFolder(currentUserName);
        }

        for (GenTemplate tpl : templateList) {
            result.append(GenUtil.xgenerateToFile(false,
                    genScheme.getGenTable().getFormType(),
                    tpl,
                    genScheme,
                    genScheme.getReplaceFile(),
                    timePath,
                    projectPath,
                    this.getGenKey(currentUserName),
                    this.getGenUrl(currentUserName),
                    dbType));
        }
        return result.toString();
    }

    /**
     * 刷新表单定义缓存；formNo 为空时重建全部表单缓存。
     */
    @Async
    public void refreshGenTableCache(String formNo) {
        if (StringUtil.isNotEmpty(formNo)) {
            cacheUtil.deleteLikeHashCache(GenUtil.GENTABLE_CACHE, formNo);
            this.getGenTableWithDefination(formNo);
        } else {
            Set<String> hashKeySet = cacheUtil.getHashKeys(GenUtil.GENTABLE_CACHE);
            for (String hashKey : hashKeySet) {
                cacheUtil.deleteLikeHashCache(GenUtil.GENTABLE_CACHE, hashKey);
            }
            List<GenTable> list = this.findAll();
            for (GenTable genTable : list) {
                this.getGenTableWithDefination(genTable.getName());
            }
        }
    }

    /**
     * 获取或自动生成表单导入模板文件组，支持按区域和当前数据生成模板内容。
     */
    @Transactional(readOnly = false)
    public String getImportTemplateFileGroupIdByFormNo(String formNo, String areaId, String fileRoot, boolean importTemplateWithData, String parentId, JSONObject zformMap, String fileNameUrl) {
        String groupId = "";
        GenTable genTable = this.getGenTableWithDefination(formNo);
        if (StringUtil.isNotEmpty(genTable.getImportTemplateFile())) {

            groupId = genTable.getImportTemplateFile();
            List<SysFile> sysFiles = sysFileService.getFileListByGroupId(groupId);
            if (sysFiles != null && sysFiles.size() > 0) {
                SysFile sysFile = sysFiles.get(0);
                String fileName = sysFile.getName();
                if (!ConvertUtil.getString(fileName).equals("/"+genTable.getComments() + "导入模板.xlsx")){
                    logger.info("表单" + formNo + "已经存在导入模板，使用上传的模板");
                    return groupId;
                }
                logger.info("表单" + formNo + "已经存在自动生成的模板，重新生成");
            }
        }
        List<ExcelField> excelImportFieldList = getExcelImportFieldList(genTable, areaId);
        String fileName = genTable.getComments() + "导入模板.xlsx";
        String sheetName = genTable.getComments();
        if(StrUtil.isNotEmpty(fileNameUrl)){
            fileName = fileNameUrl;
            sheetName = fileNameUrl.replace(".xlsx","");
        }

        if (importTemplateWithData) {
            try {
                String currentLoginName = UserUtil.getCurrentLoginName();
                zformMap.put("formNo", formNo);
                Zform zform = zformService.getZformFromMap(zformMap, currentLoginName);
                Page<Zform> zformPage = new Page<>(1, Integer.MAX_VALUE);
                if (zformMap.containsKey("orderBy")){
                    zformPage.setOrderBy(zformMap.getString("orderBy"));
                }
                Page<Zform> page = zformService.findPageMap(zformPage,
                        zform,
                        "path",
                        currentLoginName,
                        genTable,
                        "",
                        parentId, null);
                List<Zform> zformDataList = new ArrayList<>(page.getMap().size());
                for (LinkedHashMap map : page.getMap()) {
                    JSONObject jsonObject = JSONHelper.toJSONObject(map);
                    jsonObject.put("formNo", formNo);
                    zformDataList.add(zformService.getZformFromMap(jsonObject, currentLoginName));
                }
                page.setList(zformDataList);
                List<Map<String, Object>> maps = zformService.exportDataMap(page, zform, genTable, currentLoginName);
                createTemplateNew(excelImportFieldList, sheetName, fileRoot, fileName, maps);
            } catch (Exception e) {
                logger.error("导入模板生成失败,{}", ExceptionUtil.stacktraceToString(e));
                throw new BusinessException(e.getMessage());
            }
        } else {
            createTemplateNew(excelImportFieldList, sheetName, fileRoot, fileName);
        }
        groupId = saveSysFile(fileRoot, fileName);
        GenTableView theGenTable = new GenTableView();
        theGenTable.setId(genTable.getId());
        //不更新表单的导入模板 未设置导入模板的情况下，每次都是用最新的模板
        //theGenTable.setImportTemplateFile(groupId);
        genTableDao.saveImport(theGenTable);
        this.refreshGenTableCache(formNo);
        return groupId;
    }

    @Transactional(readOnly = false)
    public String getExportFilePathByFormNo(String formNo, String fileRoot) {
        String filePath;
        //优先判断资源文件是否存在模板
        filePath = fileRoot + "/doc/" + formNo + "_export.xlsx";
        if (false == new File(filePath).exists()) {
            try {
                URL url = this.getClass().getResource("/tpl/" + formNo + "_export.xlsx");
                if (url != null) {
                    filePath = url.getPath();
                }
            } catch (Exception err) {
                logger.warn("读取导出模板资源失败", err);
            }
        }
        if (StringUtil.isEmpty(filePath)) {
            GenTable genTable = this.getGenTableWithDefination(formNo);
            if (StringUtil.isEmpty(genTable.getExportTemplateFile())) {
                //create export template file by isExport in genTableColumn
                List<String> columnList = Lists.newArrayList();
                if (StringUtil.isNotEmpty(genTable.getExportList())) {
                    String[] exportList = genTable.getExportList().split(",");
                    for (int i = 0; i < exportList.length; i++) {
                        for (GenTableColumn column : genTable.getColumnList()) {
                            if (column.getJavaField().equals(exportList[i])) {
                                columnList.add(column.getComments());
                                break;
                            }
                        }
                    }
                } else {
                    for (GenTableColumn column : genTable.getColumnList()) {
                        if (Global.YES.equals(column.getIsForm())) {
                            columnList.add(column.getComments());
                        }
                    }
                }
                String fileName = genTable.getName() + "_export.xlsx";
                createTemplate(columnList, fileRoot, fileName);
                String groupId = saveSysFile(fileRoot, fileName);
                GenTableView theGenTable = new GenTableView();
                theGenTable.setId(genTable.getId());
                theGenTable.setExportTemplateFile(groupId);
                genTableDao.saveExport(theGenTable);
                this.refreshGenTableCache(formNo);
                filePath = fileRoot + "/" + fileName;
            } else {
                filePath = sysFileService.getFirstFilePathByGroupId(genTable.getExportTemplateFile(), fileRoot);
                if (false == new File(filePath).exists()) {
                    //Create template file
                    List<String> columnList = Lists.newArrayList();
                    if (StringUtil.isNotEmpty(genTable.getExportList())) {
                        String[] exportList = genTable.getExportList().split(",");
                        for (int i = 0; i < exportList.length; i++) {
                            for (GenTableColumn column : genTable.getColumnList()) {
                                if (column.getJavaField().equals(exportList[i])) {
                                    columnList.add(column.getComments());
                                    break;
                                }
                            }
                        }
                    } else {
                        for (GenTableColumn column : genTable.getColumnList()) {
                            if (Global.YES.equals(column.getIsForm())) {
                                columnList.add(column.getComments());
                            }
                        }
                    }
                    String fileName = genTable.getName() + "_export.xlsx";
                    createTemplate(columnList, fileRoot, fileName);
                }
            }
        }
        return filePath;
    }

    public String saveSysFile(String fileRoot, String fileName) {
        SysFile sysFile = new SysFile();
        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        sysFile.setGroupId(groupId);
        sysFile.setName(fileName.contains("/") ? fileName.substring(fileName.lastIndexOf("/")).replace("/","") : fileName);
        sysFile.setExt(fileName.substring(fileName.lastIndexOf(".")).replace(".",""));
        File file = new File(fileRoot + "/" + fileName);
        long size = file.length();
        String sizeStr;
        if (size / 1024 < 1024) {
            sizeStr = "(" + new DecimalFormat("0.0").format(size / 1024D) + "K)";
        } else {
            sizeStr = "(" + new DecimalFormat("0.0").format(size / 1024D / 1024D) + "M)";
        }
        sysFile.setSize(sizeStr);
        sysFile.setPath("/" + fileName);
        sysFile.setUploadTime(new Date());
        sysFile.setType("FILE");
        sysFile.setSort(1);
        sysFile.setSecFlag(Global.NO);
        sysFile.setVisitCount(0);
        sysFile.setToPdf(Global.NO);
        sysFileService.save(sysFile);
        return groupId;
    }

    @Autowired
    @Lazy
    ZformService zformService;

    @Autowired
    AreaDao areaDao;


    private List<String> getFullAreaName(TagTree tagTree, List<TagTree> list) {
        List<String> res = new ArrayList<>();
        res.add(tagTree.getName());
        if (StringUtil.isNotEmpty(tagTree.getParentId()) && !"1".equals(tagTree.getParentId())) {
            for (TagTree tree : list) {
                if (tree.getId().equals(tagTree.getParentId())) {
                    List<String> strings = getFullAreaName(tree, list);
                    res.addAll(strings);
                }
            }
        }
        return res;
    }

    private List<String> getFullName(Map<String, Object> target, List<LinkedHashMap> list) {
        List<String> res = new ArrayList<>();
        res.add(ConvertUtil.getString(target.get("name")));
        Object parent = target.get("parent");
        if (parent != null) {
            if (parent instanceof Map) {
                Map<String, Object> parentMap = (Map<String, Object>) parent;
                String pid = oConvertUtils.getString(parentMap.get("id"));
                if (StringUtil.isNotEmpty(pid)) {
                    for (Map<String, Object> tree : list) {
                        if (oConvertUtils.getString(tree.get("id")).equals(pid)) {
                            List<String> strings = getFullName(tree, list);
                            res.addAll(strings);
                        }
                    }
                }
            }

        }
        return res;
    }

    public List<ExcelField> getExcelExportFieldList(GenTable genTable) {
        List<GenTableColumn> columnList = new ArrayList<>();
        if (StringUtil.isNotEmpty(genTable.getExportList())) {
            String[] importList = genTable.getExportList().split(",");
            for (int i = 0; i < importList.length; i++) {
                for (GenTableColumn column : genTable.getColumnList()) {
                    if (column.getJavaField().equals(importList[i])) {
                        columnList.add(column);
                        break;
                    }
                }
            }
        } else {
            for (GenTableColumn column : genTable.getColumnList()) {
                if (Global.YES.equals(column.getIsForm())) {
                    columnList.add(column);
                }
            }
        }
        List<ExcelField> excelFieldList = new ArrayList<>();
        for (GenTableColumn genTableColumn : columnList) {
            ExcelField excelField = new ExcelField();
            excelField.setFieldValue(genTableColumn.getSimpleJavaField());
            excelField.setFieldTitle(genTableColumn.getComments());
            excelFieldList.add(excelField);
        }
        return excelFieldList;
    }

    /**
     * 是否显示行政区域 参考UArea里面的判断逻辑
     * @param areaId 行政区域ID
     * @param showRank 显示等级 1:省级 2:市级 3:区县
     * @return
     */
    private boolean isAreaShow(String areaId, String areaName, String showRank) {
        if ("3".equals(showRank)) {
            //仅显示区县
            return !areaId.endsWith("00") && !"1".equals(areaId);
        }else if ("2".equals(showRank)) {
            //根据地区代码筛选出所有的市级以上单位（包括市级），不包括4个直辖市的所有区
            return (areaId.endsWith("00") && !"县".equals(areaName) && !"市辖区".equals(areaName));
        } else if ("1".equals(showRank)) {
            //城市等级为1，筛选出所有省级地区
            //根据地区代码筛选出所有的省级以上单位（包括省级），包括4个直辖市
            return areaId.endsWith("0000");
        }
        return true;
    }

    /**
     * 获取导入字段 zry 2023-03-10 17:04:36
     *
     * @param genTable
     * @return
     */
    public List<ExcelField> getExcelImportFieldList(GenTable genTable, String areaId) {
        // 后续优化：支持自定义导入模板样式
        // 后续优化：行政区按用户或配置条件过滤
        // 后续优化：缓存导入字段元数据
        // 后续优化：支持树选择字段下拉导入
        List<GenTableColumn> columnList = new ArrayList<>();
        if (StringUtil.isNotEmpty(genTable.getImportList())) {
            String[] importList = genTable.getImportList().split(",");
            for (int i = 0; i < importList.length; i++) {
                for (GenTableColumn column : genTable.getColumnList()) {
                    if (column.getJavaField().equals(importList[i])) {
                        columnList.add(column);
                        break;
                    }
                }
            }
        } else {
            for (GenTableColumn column : genTable.getColumnList()) {
                if (Global.YES.equals(column.getIsForm())) {
                    columnList.add(column);
                }
            }
        }
        List<ExcelField> excelFieldList = new ArrayList<>();
        for (GenTableColumn genTableColumn : columnList) {
            ExcelField excelField = new ExcelField();
            excelField.setFieldValue(genTableColumn.getSimpleJavaField());
            excelField.setFieldTitle(genTableColumn.getComments());
            String dictType = genTableColumn.getDictType();

            if ("0".equals(genTableColumn.getIsNull())) {
                List<ImportValidEnum> importValidEnums = new ArrayList<>();
                importValidEnums.add(ImportValidEnum.NOT_EMPTY);
                excelField.setImportValidEnums(importValidEnums.toArray(new ImportValidEnum[0]));
            }
            if (genTableColumn.getJavaField().startsWith("d")) {
                //是日期
                excelField.setDate(true);
            }
            if (StringUtil.isNotBlank(dictType)) {
                List<DictResult> dictResults = dictDataService.getDictList(dictType, false);
                LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                for (DictResult dictResult : dictResults) {
                    dictMap.put(dictResult.getMember(), dictResult.getMemberName());
                }
                excelField.setDataMap(dictMap);
            }
            if ("select".equals(genTableColumn.getShowType())&&Global.NO.equals(genTableColumn.getSelectSimple())&&
                    StringUtil.isNotEmpty(genTableColumn.getTableName())
            ){
                //下拉数据来自其他表
                GridselectParam gridselectParam = new GridselectParam();
                gridselectParam.setPageParam(new PageParam());
                gridselectParam.getPageParam().setPageNo(1);
                gridselectParam.getPageParam().setPageSize(9999);
                if (StringUtil.isNotEmpty(genTableColumn.getSelectOrder())){
                    gridselectParam.getPageParam().setOrderBy(genTableColumn.getSelectOrder());
                }
                gridselectParam.setTableName(genTableColumn.getTableName());
                Page<Zform> zformPage = zformService.gridselectDataMapList(gridselectParam);
                List<LinkedHashMap> mapList = zformPage.getMap();
                LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                for (LinkedHashMap map : mapList) {
                    String key = genTableColumn.getSelectValuefield();
                    String value = genTableColumn.getSearchKey();
                    dictMap.put(Convert.toStr(map.get(key)), Convert.toStr(map.get(value)));
                }
                excelField.setDataMap(dictMap);
            }
            if ("officeselectTree".equals(genTableColumn.getShowType())) {
                //机构选择
                List<Office> officeList = officeDao.findAllList(new Office());
                LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                for (Office office : officeList) {
                    dictMap.put(office.getName(), office.getName());
                }
                excelField.setDataMap(dictMap);
            }
            if ("areaselect".equals(genTableColumn.getShowType())) {
                List<TagTree> areaTagTreeAll;
                // 使用表单配置的根区域。
                String rootAreaId = genTableColumn.getGenTableColumnFormItemConfig().getRootAreaId();
                // 行政区显示等级。
                String showRank = genTableColumn.getGenTableColumnFormItemConfig().getShowRank();
                String freeChoice = genTableColumn.getGenTableColumnFormItemConfig().getFreeChoice();
                if ("1".equals(freeChoice) || "true".equals(freeChoice)) {
                    // 自由选择时不限制行政区等级。
                    showRank = "";
                }
                if (StringUtil.isNotBlank(rootAreaId)) {
                    areaId = rootAreaId;
                }
                if (StringUtil.isNotBlank(areaId)) {
                    areaTagTreeAll = areaDao.findAreaSubTree(areaId);
                } else {
                    areaTagTreeAll = areaDao.findAreaTagTreeAll();
                }
                LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                for (TagTree tagTree : areaTagTreeAll) {
                    List<String> stringList = getFullAreaName(tagTree, areaTagTreeAll);
                    Collections.reverse(stringList);
                    if (isAreaShow(tagTree.getId(), tagTree.getName(), showRank)) {
                        //根据表单配置的行政区等级 觉得该行政区是否显示 2024-07-30
                        dictMap.put(tagTree.getId(), tagTree.getId() + "|" + StrUtil.join("", stringList));
                    }
                }
                excelField.setDataMap(dictMap);
            }
            if ("parentId".equals(genTableColumn.getShowType())) {
                if (GenTable.TABLE_TYPE_RIGHTTABLE.equals(genTable.getTableType())){
                    List<LinkedHashMap> treeList = zformService.findMapList(genTable.getParentTable(), new QueryWrapper());
                    List<LinkedHashMap> children = treeList.stream().filter(k -> Global.NO.equals(oConvertUtils.getString(k.get("hasChildren")))).collect(Collectors.toList());
                    LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                    for (LinkedHashMap map : children) {
                        List<String> stringList = getFullName(map, treeList);
                        Collections.reverse(stringList);
                        dictMap.put(oConvertUtils.getString(map.get("id")), StrUtil.join("|", stringList));
                    }
                    excelField.setDataMap(dictMap);
                }
            }
            if ("gridselect".equals(genTableColumn.getShowType()) || (("parentId".equals(genTableColumn.getShowType()) || "parent_code".equals(genTableColumn.getName())) && "sys_dictionary".equals(genTable.getName()))) {
                String fieldKeys = genTableColumn.getFieldKeys();
                String dsfPlus = "";
                GridselectParam gridselectParam = new GridselectParam();
                gridselectParam.setPageParam(new PageParam());
                gridselectParam.getPageParam().setPageNo(1);
                gridselectParam.getPageParam().setPageSize(99999);
                if ("parentId".equals(genTableColumn.getShowType()) || "parent_code".equals(genTableColumn.getName())) {
                    fieldKeys = "name";
                    genTableColumn.setTableName(genTable.getName());
                    genTableColumn.setSearchKey("name");
                    List<GridselectParam.FilterData> filterDataList = new ArrayList<>();
                    filterDataList.add(new GridselectParam.FilterData("a.parent_code", "data-params", "eq"));
                    gridselectParam.setFilterList(filterDataList);


                }
                if (StringUtil.isNotEmpty(fieldKeys)) {
                    String[] keys = fieldKeys.split(",");

                    gridselectParam.setTableName(genTableColumn.getTableName());
                    gridselectParam.setSearchKey(genTableColumn.getSearchKey());
                    gridselectParam.setSearchValue("");
                    String formItemConfig = genTableColumn.getFormItemConfig();
                    if (StringUtil.isNotEmpty(formItemConfig)){
                        JSONObject formItemConfigObj = JSONObject.parseObject(formItemConfig);
                        if (formItemConfigObj.containsKey("formControlProps")){
                            JSONObject formControlProps = formItemConfigObj.getJSONObject("formControlProps");
                            if (formControlProps.containsKey("filterData")){
                                JSONArray filterData = formControlProps.getJSONArray("filterData");
                                List<GridselectParam.FilterData> list = JSONHelper.toList(filterData.toString(), GridselectParam.FilterData.class);
                                gridselectParam.setFilterList(list);
                            }
                            if (formControlProps.containsKey("columns")){
                                JSONArray columns = formControlProps.getJSONArray("columns");
                                keys = columns.stream().map(k -> ((JSONObject) k).getString("dataIndex")).toArray(String[]::new);
                            }
                        }
                    }
                    Page<Zform> zformPage = zformService.gridselectDataMapList(gridselectParam);
                    List<LinkedHashMap> mapList = zformPage.getMap();
                    zformService.appendDictName(mapList,gridselectParam.getTableName());
                    LinkedHashMap<Object, Object> dictMap = new LinkedHashMap<>();
                    for (LinkedHashMap map : mapList) {
                        List<String> labels = new ArrayList<>();
                        for (String key : keys) {
                            if (StringUtil.isNotEmpty(key)) {
                                String toStr = Convert.toStr(map.get(key));
                                if (StringUtil.isNotEmpty(toStr)) {
                                    labels.add(toStr);
                                }
                            }
                        }
                        String keyStr = "id";
                        if ("sys_dictionary".equals(genTable.getName()) && "parent_code".equals(genTableColumn.getName())) {
                            keyStr = "code";
                        }
                        if (labels.size() > 0 && map.containsKey(keyStr)) {
                            dictMap.put(Convert.toStr(map.get(keyStr)), StrUtil.join("|", labels));
                        }

                    }
                    excelField.setDataMap(dictMap);
                }

            }
            excelFieldList.add(excelField);
        }
        AroundServiceI aroundService = AroundUtil.getAroundServiceI(genTable);
        if (aroundService != null) {
            return aroundService.afterGetExcelImportFieldList(excelFieldList, genTable, areaId);
        }
        return excelFieldList;
    }

    /**
     * 创建带字典下拉项的导入模板。
     *
     * @param excelImportFieldList 导入字段列表
     * @param tableComment 表说明
     * @param outPath 输出路径
     * @param outName 输出文件名
     */
    private void createTemplateNew(List<ExcelField> excelImportFieldList, String tableComment, String outPath, String outName) {

        Workbook workbook = ExcelImportUtil.exportTemplate(tableComment, excelImportFieldList);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outPath + "/" + outName);
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                } catch (IOException e) {
                    logger.warn("刷新文件输出流失败", e);
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输出流失败", e);
                }
            }

        }

    }

    private void createTemplateNew(List<ExcelField> excelImportFieldList, String tableComment, String outPath, String outName, List<Map<String, Object>> list ) {

        Workbook workbook = ExcelImportUtil.exportTemplate(tableComment, excelImportFieldList);
        Sheet sheet = workbook.getSheetAt(0);
        int i = 1;
        CellStyle leftStyle = com.jeestudio.tools.excel.ExcelUtil.getLeftStyle(workbook);
        CellStyle centerStyle =com.jeestudio.tools.excel.ExcelUtil.getCenterStyle(workbook);
        CellStyle rightStyle = com.jeestudio.tools.excel.ExcelUtil.getRightStyle(workbook);
        for (Map<String, Object> map : list) {
            // Row 行,Cell 方格 , Row 和 Cell 都是从0开始计数的
            // 创建一行，在页sheet上
            Row row1 = sheet.createRow(i);


            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(map));
            // 在row行上创建一个方格
            int j = 0;
            for (ExcelField excelField : excelImportFieldList) {
                Cell cell = row1.createCell(j);
                Object fieldValue = jsonObject.get(excelField.getFieldValue());
                if (excelField.getDataMap() != null) {
                    if (excelField.isDictMultiple()) {
                        String toStr = Convert.toStr(fieldValue);
                        if (toStr == null) {
                            cell.setCellValue(toStr);
                        } else {
                            List<Object> res = new ArrayList<>();
                            String[] split = toStr.split(excelField.getDictSeparator());
                            for (String s : split) {
                                Object orDefault = excelField.getDataMap().getOrDefault(s, null);
                                if (orDefault != null) {
                                    res.add(orDefault);
                                }
                            }
                            cell.setCellValue(StringUtils.join(res, excelField.getDictSeparator()));
                        }
                    } else {
                        String strValue = Convert.toStr(fieldValue);//boolean类型翻译bug 2022-11-28 16:59:21
                        Object orDefault = excelField.getDataMap().getOrDefault(strValue, strValue);
                        if (orDefault != null && StrUtil.isNotEmpty(orDefault.toString())) {
                            cell.setCellValue(Convert.toStr(orDefault));
                        } else {
                            cell.setCellValue(Convert.toStr(strValue));
                        }
                    }
                } else {
                    cell.setCellValue(Convert.toStr(fieldValue));
                }
                String align = excelField.getAlignEnum().getValue();
                if (com.jeestudio.tools.excel.ExcelUtil.LEFT.equals(align)) {
                    cell.setCellStyle(leftStyle);
                } else if (com.jeestudio.tools.excel.ExcelUtil.CENTER.equals(align)) {
                    cell.setCellStyle(centerStyle);
                } else if (com.jeestudio.tools.excel.ExcelUtil.RIGHT.equals(align)) {
                    cell.setCellStyle(rightStyle);
                } else {
                    cell.setCellStyle(com.jeestudio.tools.excel.ExcelUtil.getContentStyle(workbook));
                }

                j++;
            }
            i++;
        }
        for (short index = 0; index < excelImportFieldList.size() ; index++) {
            sheet.autoSizeColumn(index); //调整第一列宽度
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outPath + "/" + outName);
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                } catch (IOException e) {
                    logger.warn("刷新文件输出流失败", e);
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输出流失败", e);
                }
            }

        }

    }

    private void createTemplate(List<String> columnList, String outPath, String outName) {
        String inPath = (new ApplicationHome(getClass())).getSource().getParentFile().toString();
        String inName = "/classes/templates/modules/excel/template_blank.xlsx";
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(inPath + inName);
            XSSFWorkbook xssfWorkbook = createExcel(columnList, fileInputStream);
            fileOutputStream = new FileOutputStream(outPath + "/" + outName);
            xssfWorkbook.write(fileOutputStream);
        } catch (Exception e) {
            logger.error("创建Excel模板失败", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                } catch (IOException e) {
                    logger.warn("刷新文件输出流失败", e);
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输出流失败", e);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输入流失败", e);
                }
            }
        }
    }

    private XSSFWorkbook createExcel(List<String> columnList, FileInputStream fileInputStream) throws IOException {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFSheet sheet0 = xssfWorkbook.getSheetAt(0);
        XSSFRow row0 = sheet0.createRow(0);
        for (int i = 0; i < columnList.size(); i++) {
            String value = columnList.get(i);
            XSSFCell cell = row0.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(value);
        }
        return xssfWorkbook;
    }

    /**
     * 保存导入导出字段与模板文件配置，并按表单字段顺序规范化字段列表。
     */
    @Transactional(readOnly = false)
    public void saveImportAndExport(GenTableView genTable) {
        if (genTable.getName().indexOf("-") != -1) {
            genTable.setName(genTable.getName().replaceFirst("-", "."));
        }
        if (StringUtil.isNotEmpty(genTable.getImportTemplateFile())) {
            LinkedHashMap<String, Object> map = sysFileService.getFileList(genTable.getImportTemplateFile());
            if (((List<SysFile>) map.get("files")).size() == 0) {
                genTable.setImportTemplateFile("");
            }
        }
        if (StringUtil.isNotEmpty(genTable.getExportTemplateFile())) {
            LinkedHashMap<String, Object> map = sysFileService.getFileList(genTable.getExportTemplateFile());
            if (((List<SysFile>) map.get("files")).size() == 0) {
                genTable.setExportTemplateFile("");
            }
        }
        //sort list
        GenTable theGenTable = this.getGenTableWithDefination(genTable.getName());
        String sortedImportList = "";
        String sortedExportList = "";
        String importList = "," + (genTable.getImportList() == null ? "" : genTable.getImportList()) + ",";
        String exportList = "," + (genTable.getExportList() == null ? "" : genTable.getExportList()) + ",";
        boolean noImportList = true;
        boolean noExportList = true;
        if (StringUtil.isNotEmpty(genTable.getImportList())) {
            noImportList = false;
        }
        if (StringUtil.isNotEmpty(genTable.getExportList())) {
            noExportList = false;
        }
        for (GenTableColumn column : theGenTable.getColumnList()) {
            if (importList.indexOf("," + column.getJavaField() + ",") != -1
                    || noImportList && Global.YES.equals(column.getIsForm())) {
                sortedImportList += "," + column.getJavaField();
            }
            if (exportList.indexOf("," + column.getJavaField() + ",") != -1
                    || noExportList && Global.YES.equals(column.getIsForm())) {
                sortedExportList += "," + column.getJavaField();
            }
        }
        if (sortedImportList.indexOf(",") == 0) sortedImportList = sortedImportList.substring(1);
        if (sortedExportList.indexOf(",") == 0) sortedExportList = sortedExportList.substring(1);

        genTable.setImportList(sortedImportList);
        genTable.setExportList(sortedExportList);
        genTableDao.saveImportAndExport(genTable);
        refreshGenTableCache(genTable.getName());
    }

    /**
     * 根据字段中文描述匹配历史字段定义，用于 AI/设计器创建字段时复用已有类型经验。
     */
    public GenTableColumnView getByComments(String comments, String mainTableModule) {
        List<GenTableColumnView> genTableColumnViewList = genTableColumnDao.getByComments(comments, mainTableModule);
        GenTableColumnView genTableColumnView = null;
        if (genTableColumnViewList.size() > 0) {
            genTableColumnView = genTableColumnViewList.get(0);
        }
        if (genTableColumnView != null) {
            String javaField = genTableColumnView.getJavaField();
            if (javaField.startsWith("users")) {
                javaField = "users";
            } else if (javaField.startsWith("user")) {
                javaField = "user";
            } else if (javaField.startsWith("office")) {
                javaField = "office";
            } else if (javaField.startsWith("area")) {
                javaField = "area";
            } else if (javaField.startsWith("sort")) {
                javaField = "sort";
            } else if (javaField.startsWith("c")) {
                javaField = "c";
            } else if (javaField.startsWith("d")) {
                javaField = "d";
            } else if (javaField.startsWith("s")) {
                javaField = "s";
            } else if (javaField.startsWith("g")) {
                javaField = "g";
            } else if (javaField.startsWith("t")) {
                javaField = "t";
            }
            genTableColumnView.setJavaField(javaField);
        }
        return genTableColumnView;
    }

    /**
     * 取消锁定sql
     * @param id
     */
    public void unlockSql(String id) {
        genTableDao.unlockSql(id);
    }

    /**
     * 根据系统字典项生成 Java 枚举源码文件，供代码生成或二次开发使用。
     */
    public ResultJson genEnum(JSONObject reqBody, String loginName) throws Exception {
        String pCode = reqBody.getString("code");
        String pName = reqBody.getString("name");
        String pid = reqBody.getString("id");

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("a.parent_id", pid);

        List<LinkedHashMap> sys_dictionaryMapList = zformService.findMapList("sys_dictionary", queryWrapper);

        for (LinkedHashMap sys_dictionary : sys_dictionaryMapList) {
            String code = (String)sys_dictionary.get("code");
            // 判断 code 是否全是英文字符
            if (code != null && code.matches("^[a-zA-Z]+$")) {
                // code 全部是英文字符
                sys_dictionary.put("enumCode",code);
            } else {
                // code 不是全部英文字符
                String name = (String)sys_dictionary.get("name");
                sys_dictionary.put("enumCode", getPinyinInitials(name));
            }

        }

        if (sys_dictionaryMapList == null || sys_dictionaryMapList.isEmpty()) {
            return ResultJson.failed("未找到对应的字典项");
        }

        // 初始化 Velocity
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(properties);

        // 创建 Velocity 上下文
        VelocityContext context = new VelocityContext();

        context.put("author", loginName);
        context.put("description", pName);
        context.put("time", new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()));
        context.put("enumName", toEnumName(pCode));  // pCode 作为枚举类名
        context.put("enumList", sys_dictionaryMapList);

        // 读取 Velocity 模板
        StringWriter stringWriter = new StringWriter();
        Template template = Velocity.getTemplate("/vms/genEnum.vm", "UTF-8");
        template.merge(context, stringWriter);

        // 生成 Java 文件
        String fileName = toEnumName(pCode) + ".java";
        String filePath = this.getGenFolder(loginName) + "/enum/" + fileName;

        // 创建目录路径
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        // 如果目录不存在，则创建
        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) {
                log.info("目录创建成功: " + parentDir.getAbsolutePath());
            } else {
                log.error("目录创建失败: " + parentDir.getAbsolutePath());
                return ResultJson.failed("目录创建失败");
            }
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(stringWriter.toString());
        } catch (Exception e) {
            log.error("" + ExceptionUtils.getStackTrace(e));
            return ResultJson.failed("生成枚举类失败：" + e.getMessage());
        }

        return ResultJson.success("成功生成枚举类: " + filePath);
    }
    /**
     * 将 pCode 转换为枚举类名
     */
    private String toEnumName(String pCode) {
        String[] parts = pCode.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return sb.append("Enum").toString();
    }

    public static String getPinyinInitials(String input) {
        StringBuilder initials = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) { // 判断是否是中文字符
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null) {
                    initials.append(pinyinArray[0].charAt(0)); // 取拼音首字母
                }
            }else if (Character.isLetter(c)) { // 只保留英文字符
                initials.append(c);
            }
        }
        return initials.toString().toUpperCase(); // 转为大写拼音首字母
    }

    private String getGenFolder(String loginName) throws Exception {
        String genFolder = this.getSettingValueByKey(GenKey.FOLDER.getValue(), loginName);
        return genFolder;
    }

    private String getProjectVueFolderPath(String loginName) {
        String projectRoot = System.getProperty("user.dir");
        projectRoot = projectRoot.replaceAll("\\\\","/");
        projectRoot += GenKey.VUE_SOURCE_FOLDER.getValue();
        return projectRoot;
    }

    private String getGenUrl(String loginName) throws Exception {
        return this.getSettingValueByKey(GenKey.URL.getValue(), loginName);
    }

    private String getGenKey(String loginName) throws Exception {
        return this.getSettingValueByKey(GenKey.KEY.getValue(), loginName);
    }

    private Boolean isGenJava(String loginName) throws Exception {
        return Boolean.parseBoolean(this.getSettingValueByKey(GenKey.JAVA.getValue(), loginName));
    }

    /**
     * 根据前缀查询系统设置项
     */
    private JSONObject getSettingMap(String prefix, String loginName) throws Exception {
        List<LinkedHashMap> settingList = this.getSysSettingList(prefix, loginName);
        JSONObject jsonObject = new JSONObject();
        for (LinkedHashMap theMap: settingList) {
            if (theMap.get("key_") != null) {
                jsonObject.put(StringUtil.getString(theMap.get("key_")), theMap.get("value_"));
            } else if (theMap.get("key_".toUpperCase(Locale.ROOT)) != null) {
                jsonObject.put(StringUtil.getString(theMap.get("key_".toUpperCase(Locale.ROOT))), theMap.get("value_".toUpperCase(Locale.ROOT)));
            }
        }
        return jsonObject;
    }

    private List<LinkedHashMap> getSysSettingList(String prefix, String loginName) throws Exception {
        JSONObject zformMap = new JSONObject();
        zformMap.put("formNo", "sys_setting");
        Zform zform = zformService.getZformFromMap(zformMap, loginName);
        if (StringUtil.isNotEmpty(prefix)) {
            zform.getQueryWrapper().likeRight("a.key_", prefix);
        }
        GenTable genTable = this.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), Integer.MAX_VALUE, zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPageMap(page,
                zform,
                "",
                loginName,
                genTable,
                "",
                "",
                "");
        List<LinkedHashMap> list = page.getMap();
        return list;
    }

    /**
     * 读取系统设置值，支持按当前登录用户解析运行时配置。
     */
    public String getSettingValueByKey(String key, String loginName) throws Exception {
        JSONObject settingMap = this.getSettingMap(key, loginName);
        if (settingMap.containsKey(key)) {
            return settingMap.getString(key);
        } else {
            return null;
        }
    }

    /**
     * 获取 AI 服务地址配置。
     */
    public String getAIUri(String loginName) throws Exception {
        return getSettingValueByKey(GenKey.AI_URI.getValue(), loginName);
    }

    /**
     * 获取 AI 服务密钥配置，具体解析逻辑由调用方统一处理。
     */
    public String getAIApiKey(String loginName) throws Exception {
        return getSettingValueByKey(GenKey.AI_API_KEY.getValue(), loginName);
    }

    /**
     * 获取 AI 模型配置，未配置时返回默认模型。
     */
    public String getAIModel(String loginName) throws Exception {
        String model = getSettingValueByKey(GenKey.AI_MODEL.getValue(), loginName);
        return (model != null && !model.isEmpty()) ? model : "qwen-plus";
    }
}
