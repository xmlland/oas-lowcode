package com.jeestudio.bpm.service.gen;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableDeriveConfig;
import com.jeestudio.bpm.mapper.base.gen.GenTableDao;
import com.jeestudio.bpm.mapper.base.gen.GenTableDeriveConfigDao;
import com.jeestudio.bpm.utils.DbTypeUtil;
import com.jeestudio.bpm.utils.IdGen;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统计表派生数据同步服务。
 *
 * <p>表单配置只负责保存来源 SQL、唯一键、同步策略和调度表达式；本服务负责把来源 SQL
 * 的结果写入统计表物理表，并回写最近一次执行状态。</p>
 */
@Service
@EnableScheduling
@Slf4j
public class GenTableDeriveSyncService {

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAILED = "failed";
    private static final String STRATEGY_REPLACE = "replace";
    private static final String STRATEGY_UPSERT = "upsert";
    private static final String STRATEGY_APPEND = "append";
    private static final String STRATEGY_MANUAL = "manual";
    private static final int MAX_STATUS_MESSAGE_LENGTH = 900;

    private static final List<String> SOURCE_SQL_DANGEROUS_KEYWORDS = Arrays.asList(
            " insert ", " update ", " delete ", " drop ", " alter ", " truncate ",
            " create ", " grant ", " revoke ", " execute ", " call ", " merge ",
            " replace ", " comment ", " vacuum ", " analyze "
    );

    private final Set<String> runningConfigIds = ConcurrentHashMap.newKeySet();

    private final String dbType = DbTypeUtil.getDbType();

    @Autowired
    private GenTableDao genTableDao;

    @Autowired
    private GenTableDeriveConfigDao genTableDeriveConfigDao;

    @Autowired
    private DynamicRoutingDataSource dataSource;

    public ResultJson syncSummaryNow(JSONObject reqBody) {
        String genTableId = reqBody == null ? "" : reqBody.getString("genTableId");
        if (StringUtil.isBlank(genTableId) && reqBody != null) {
            genTableId = reqBody.getString("id");
        }
        if (StringUtil.isBlank(genTableId)) {
            return ResultJson.failed("请选择要同步的统计表配置");
        }
        try {
            SyncResult result = syncSummaryByGenTableId(genTableId, false);
            return ResultJson.success("统计表同步完成，新增 " + result.insertedRows + " 条，更新 " + result.updatedRows + " 条，删除 " + result.deletedRows + " 条")
                    .put("insertedRows", result.insertedRows)
                    .put("updatedRows", result.updatedRows)
                    .put("deletedRows", result.deletedRows)
                    .put("sourceRows", result.sourceRows)
                    .put("deriveConfig", result.deriveConfig);
        } catch (Exception e) {
            log.error("Sync derived summary table failed: {}", ExceptionUtils.getStackTrace(e));
            return ResultJson.failed("统计表同步失败：" + e.getMessage());
        }
    }

    public SyncResult syncSummaryByGenTableId(String genTableId, boolean scheduled) {
        GenTable genTable = genTableDao.get(genTableId);
        if (genTable == null) {
            throw new BusinessException("表单配置不存在：" + genTableId);
        }
        GenTableDeriveConfig config = genTableDeriveConfigDao.getByGenTableId(genTableId);
        if (config == null) {
            throw new BusinessException("未找到数据来源配置，请先保存表单配置");
        }
        return syncSummary(genTable, config, scheduled);
    }

    @Scheduled(fixedDelay = 60000)
    public void runScheduledSummarySync() {
        List<GenTableDeriveConfig> configs = genTableDeriveConfigDao.findEnabledAsyncSummaryList();
        if (configs == null || configs.isEmpty()) {
            return;
        }
        for (GenTableDeriveConfig config : configs) {
            if (!isCronDue(config)) {
                continue;
            }
            if (STRATEGY_MANUAL.equalsIgnoreCase(config.getSyncStrategy())) {
                continue;
            }
            try {
                syncSummaryByGenTableId(config.getGenTableId(), true);
            } catch (Exception e) {
                log.error("Scheduled derived summary sync failed, genTableId={}: {}", config.getGenTableId(), ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private boolean isCronDue(GenTableDeriveConfig config) {
        if (config == null || StringUtil.isBlank(config.getScheduleCron())) {
            return false;
        }
        try {
            CronExpression cronExpression = CronExpression.parse(config.getScheduleCron().trim());
            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime now = ZonedDateTime.now(zone);
            ZonedDateTime base = config.getLastRunTime() == null
                    ? now.minusSeconds(61)
                    : ZonedDateTime.ofInstant(config.getLastRunTime().toInstant(), zone);
            ZonedDateTime next = cronExpression.next(base);
            return next != null && !next.isAfter(now);
        } catch (Exception e) {
            updateRunStatus(config, STATUS_FAILED, "调度Cron解析失败：" + e.getMessage());
            return false;
        }
    }

    private SyncResult syncSummary(GenTable genTable, GenTableDeriveConfig config, boolean scheduled) {
        if (!GenTableDeriveConfig.SOURCE_MODE_ASYNC_SUMMARY.equals(config.getSourceMode())) {
            throw new BusinessException("当前表单不是统计表模式，不能执行统计同步");
        }
        if (StringUtil.isBlank(config.getSourceSql())) {
            throw new BusinessException("来源SQL不能为空");
        }
        if (StringUtil.isBlank(genTable.getName())) {
            throw new BusinessException("目标表名不能为空");
        }
        if (StringUtil.isNotBlank(config.getId()) && !runningConfigIds.add(config.getId())) {
            throw new BusinessException("该统计表正在同步中，请稍后再试");
        }

        SyncResult result = new SyncResult();
        result.deriveConfig = config;
        try {
            String strategy = normalizeSyncStrategy(config.getSyncStrategy());
            if (STRATEGY_MANUAL.equals(strategy)) {
                strategy = StringUtil.isBlank(config.getUniqueKeys()) ? STRATEGY_APPEND : STRATEGY_UPSERT;
            }
            String sourceSql = normalizeSourceSql(config.getSourceSql());
            TargetTable targetTable = parseTargetTable(genTable.getName());
            try (Connection conn = dataSource.getConnection()) {
                boolean oldAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try {
                    LinkedHashMap<String, TargetColumn> targetColumns = loadTargetColumns(conn, targetTable);
                    List<LinkedHashMap<String, Object>> rows = querySourceRows(conn, sourceSql, targetColumns);
                    result.sourceRows = rows.size();
                    List<String> uniqueKeys = parseUniqueKeys(config.getUniqueKeys(), targetColumns, STRATEGY_UPSERT.equals(strategy));

                    if (STRATEGY_REPLACE.equals(strategy)) {
                        result.deletedRows = deleteTargetRows(conn, targetTable);
                        result.insertedRows = insertRows(conn, targetTable, targetColumns, rows, false);
                    } else if (STRATEGY_APPEND.equals(strategy) || STRATEGY_MANUAL.equals(strategy)) {
                        result.insertedRows = insertRows(conn, targetTable, targetColumns, rows, false);
                    } else {
                        for (LinkedHashMap<String, Object> row : rows) {
                            if (updateRow(conn, targetTable, targetColumns, row, uniqueKeys) > 0) {
                                result.updatedRows++;
                            } else {
                                insertRows(conn, targetTable, targetColumns, Collections.singletonList(row), true);
                                result.insertedRows++;
                            }
                        }
                    }
                    conn.commit();
                    conn.setAutoCommit(oldAutoCommit);
                } catch (Exception e) {
                    conn.rollback();
                    conn.setAutoCommit(oldAutoCommit);
                    throw e;
                }
            }
            updateRunStatus(config, STATUS_SUCCESS, "同步完成，来源 " + result.sourceRows + " 条，新增 " + result.insertedRows + " 条，更新 " + result.updatedRows + " 条，删除 " + result.deletedRows + " 条");
            result.deriveConfig = genTableDeriveConfigDao.getByGenTableId(genTable.getId());
            return result;
        } catch (Exception e) {
            updateRunStatus(config, STATUS_FAILED, e.getMessage());
            throw new BusinessException(e.getMessage());
        } finally {
            if (StringUtil.isNotBlank(config.getId())) {
                runningConfigIds.remove(config.getId());
            }
        }
    }

    private String normalizeSyncStrategy(String syncStrategy) {
        String strategy = StringUtil.isBlank(syncStrategy) ? STRATEGY_UPSERT : syncStrategy.trim().toLowerCase(Locale.ROOT);
        if (!STRATEGY_REPLACE.equals(strategy) && !STRATEGY_UPSERT.equals(strategy)
                && !STRATEGY_APPEND.equals(strategy) && !STRATEGY_MANUAL.equals(strategy)) {
            throw new BusinessException("不支持的同步策略：" + syncStrategy);
        }
        return strategy;
    }

    private String normalizeSourceSql(String sourceSql) {
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

    private TargetTable parseTargetTable(String tableName) {
        String normalized = tableName == null ? "" : tableName.trim();
        if (StringUtil.isBlank(normalized)) {
            throw new BusinessException("目标表名不能为空");
        }
        if (normalized.toLowerCase(Locale.ROOT).endsWith("@view")) {
            throw new BusinessException("统计表必须是物理表，不能使用 @view 后缀");
        }
        String[] parts = normalized.split("\\.");
        if (parts.length > 2) {
            throw new BusinessException("目标表名格式不正确：" + tableName);
        }
        for (String part : parts) {
            if (!part.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                throw new BusinessException("目标表名包含非法字符：" + tableName);
            }
        }
        TargetTable targetTable = new TargetTable();
        targetTable.schema = parts.length == 2 ? parts[0] : "";
        targetTable.name = parts.length == 2 ? parts[1] : parts[0];
        return targetTable;
    }

    private LinkedHashMap<String, TargetColumn> loadTargetColumns(Connection conn, TargetTable targetTable) throws SQLException {
        LinkedHashMap<String, TargetColumn> columns = new LinkedHashMap<>();
        String sql = "SELECT * FROM " + targetTable.expression() + " WHERE 1 = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                TargetColumn column = new TargetColumn();
                column.name = normalizeTargetColumnName(metaData.getColumnName(i));
                column.jdbcType = metaData.getColumnType(i);
                columns.put(column.name.toLowerCase(Locale.ROOT), column);
            }
        }
        if (columns.isEmpty()) {
            throw new BusinessException("目标统计表不存在或没有可写字段：" + targetTable.rawName());
        }
        return columns;
    }

    private List<LinkedHashMap<String, Object>> querySourceRows(Connection conn, String sourceSql, LinkedHashMap<String, TargetColumn> targetColumns) throws SQLException {
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sourceSql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String sourceName = normalizeSourceColumnName(metaData.getColumnLabel(i), metaData.getColumnName(i));
                    TargetColumn targetColumn = targetColumns.get(sourceName.toLowerCase(Locale.ROOT));
                    if (targetColumn == null) {
                        continue;
                    }
                    row.put(targetColumn.name, convertValue(rs.getObject(i), targetColumn.jdbcType));
                }
                applyDefaultColumns(row, targetColumns);
                rows.add(row);
            }
        }
        return rows;
    }

    private void applyDefaultColumns(LinkedHashMap<String, Object> row, LinkedHashMap<String, TargetColumn> targetColumns) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        putDefault(row, targetColumns, "id", IdGen.uuid());
        putDefault(row, targetColumns, "del_flag", "0");
        putDefault(row, targetColumns, "create_date", now);
        putDefault(row, targetColumns, "update_date", now);
        putDefault(row, targetColumns, "create_by", UserUtil.getCurrentUserId());
        putDefault(row, targetColumns, "update_by", UserUtil.getCurrentUserId());
    }

    private void putDefault(LinkedHashMap<String, Object> row, LinkedHashMap<String, TargetColumn> targetColumns, String columnName, Object value) {
        TargetColumn targetColumn = targetColumns.get(columnName.toLowerCase(Locale.ROOT));
        if (targetColumn != null && (!row.containsKey(targetColumn.name) || row.get(targetColumn.name) == null)) {
            row.put(targetColumn.name, convertValue(value, targetColumn.jdbcType));
        }
    }

    private List<String> parseUniqueKeys(String uniqueKeys, LinkedHashMap<String, TargetColumn> targetColumns, boolean required) {
        if (StringUtil.isBlank(uniqueKeys)) {
            if (required) {
                throw new BusinessException("按唯一键更新时，唯一键不能为空");
            }
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (String item : uniqueKeys.split(",")) {
            String key = normalizeSourceColumnName(item, item);
            if (StringUtil.isBlank(key)) {
                continue;
            }
            TargetColumn targetColumn = targetColumns.get(key.toLowerCase(Locale.ROOT));
            if (targetColumn == null) {
                throw new BusinessException("唯一键字段不存在于目标表：" + key);
            }
            if (used.add(targetColumn.name.toLowerCase(Locale.ROOT))) {
                keys.add(targetColumn.name);
            }
        }
        if (required && keys.isEmpty()) {
            throw new BusinessException("按唯一键更新时，唯一键不能为空");
        }
        return keys;
    }

    private int deleteTargetRows(Connection conn, TargetTable targetTable) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + targetTable.expression())) {
            return ps.executeUpdate();
        }
    }

    private int insertRows(Connection conn, TargetTable targetTable, LinkedHashMap<String, TargetColumn> targetColumns,
                           List<LinkedHashMap<String, Object>> rows, boolean singleUpsertInsert) throws SQLException {
        int count = 0;
        for (LinkedHashMap<String, Object> row : rows) {
            LinkedHashSet<String> columns = new LinkedHashSet<>(row.keySet());
            if (columns.isEmpty()) {
                continue;
            }
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(targetTable.expression()).append(" (");
            sql.append(columns.stream().map(this::quoteIdentifier).collect(java.util.stream.Collectors.joining(", ")));
            sql.append(") VALUES (");
            sql.append(columns.stream().map(item -> "?").collect(java.util.stream.Collectors.joining(", ")));
            sql.append(")");
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int index = 1;
                for (String column : columns) {
                    setPreparedValue(ps, index++, row.get(column), targetColumns.get(column.toLowerCase(Locale.ROOT)));
                }
                count += ps.executeUpdate();
            }
            if (singleUpsertInsert) {
                break;
            }
        }
        return count;
    }

    private int updateRow(Connection conn, TargetTable targetTable, LinkedHashMap<String, TargetColumn> targetColumns,
                          LinkedHashMap<String, Object> row, List<String> uniqueKeys) throws SQLException {
        for (String uniqueKey : uniqueKeys) {
            if (!row.containsKey(uniqueKey) || row.get(uniqueKey) == null) {
                throw new BusinessException("来源SQL结果中唯一键不能为空：" + uniqueKey);
            }
        }
        List<String> updateColumns = row.keySet().stream()
                .filter(column -> uniqueKeys.stream().noneMatch(key -> key.equalsIgnoreCase(column)))
                .filter(column -> !"id".equalsIgnoreCase(column))
                .filter(column -> !"create_by".equalsIgnoreCase(column))
                .filter(column -> !"create_date".equalsIgnoreCase(column))
                .collect(java.util.stream.Collectors.toList());
        if (updateColumns.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(targetTable.expression()).append(" SET ");
        sql.append(updateColumns.stream().map(column -> quoteIdentifier(column) + " = ?").collect(java.util.stream.Collectors.joining(", ")));
        sql.append(" WHERE ");
        sql.append(uniqueKeys.stream().map(column -> quoteIdentifier(column) + " = ?").collect(java.util.stream.Collectors.joining(" AND ")));
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String column : updateColumns) {
                setPreparedValue(ps, index++, row.get(column), targetColumns.get(column.toLowerCase(Locale.ROOT)));
            }
            for (String column : uniqueKeys) {
                setPreparedValue(ps, index++, row.get(column), targetColumns.get(column.toLowerCase(Locale.ROOT)));
            }
            return ps.executeUpdate();
        }
    }

    private void setPreparedValue(PreparedStatement ps, int index, Object value, TargetColumn targetColumn) throws SQLException {
        if (targetColumn == null || value == null) {
            ps.setObject(index, value);
        } else {
            ps.setObject(index, convertValue(value, targetColumn.jdbcType));
        }
    }

    private Object convertValue(Object value, int jdbcType) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String strValue)) {
            return value;
        }
        String str = strValue.trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            if (jdbcType == Types.DATE) {
                return Date.valueOf(LocalDate.parse(str.substring(0, Math.min(10, str.length()))));
            }
            if (jdbcType == Types.TIME || jdbcType == Types.TIME_WITH_TIMEZONE) {
                return Time.valueOf(LocalTime.parse(str.substring(0, Math.min(8, str.length()))));
            }
            if (jdbcType == Types.TIMESTAMP || jdbcType == Types.TIMESTAMP_WITH_TIMEZONE) {
                String normalized = str.length() == 10 ? str + " 00:00:00" : str.replace("T", " ");
                return Timestamp.valueOf(LocalDateTime.parse(normalized.substring(0, Math.min(19, normalized.length())).replace(" ", "T")));
            }
            if (jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
                return new BigDecimal(str);
            }
            if (jdbcType == Types.INTEGER || jdbcType == Types.SMALLINT || jdbcType == Types.TINYINT) {
                return Integer.valueOf(str);
            }
            if (jdbcType == Types.BIGINT) {
                return Long.valueOf(str);
            }
            if (jdbcType == Types.DOUBLE || jdbcType == Types.FLOAT || jdbcType == Types.REAL) {
                return Double.valueOf(str);
            }
        } catch (Exception ignored) {
            return value;
        }
        return value;
    }

    private String normalizeSourceColumnName(String label, String fallback) {
        String name = StringUtil.isNotBlank(label) ? label : fallback;
        if (name == null) {
            return "";
        }
        name = name.trim();
        if ((name.startsWith("\"") && name.endsWith("\"")) || (name.startsWith("`") && name.endsWith("`"))) {
            name = name.substring(1, name.length() - 1);
        }
        if ("createBy.id".equals(name)) {
            return "create_by";
        }
        if ("updateBy.id".equals(name)) {
            return "update_by";
        }
        name = name.replace(".", "_");
        return camelToSnake(name);
    }

    private String normalizeTargetColumnName(String name) {
        return name == null ? "" : name.trim();
    }

    private String camelToSnake(String value) {
        if (StringUtil.isBlank(value) || value.indexOf('_') >= 0) {
            return value;
        }
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    private String quoteIdentifier(String identifier) {
        if (StringUtil.isBlank(identifier) || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new BusinessException("字段名包含非法字符：" + identifier);
        }
        String quote = identifierQuote();
        return quote + identifier + quote;
    }

    private String identifierQuote() {
        String type = dbType == null ? "" : dbType.toLowerCase(Locale.ROOT);
        return type.contains("mysql") || type.contains("doris") ? "`" : "\"";
    }

    private void updateRunStatus(GenTableDeriveConfig config, String status, String message) {
        if (config == null || StringUtil.isBlank(config.getId())) {
            return;
        }
        config.setLastRunTime(new java.util.Date());
        config.setLastRunStatus(status);
        config.setLastRunMessage(limitStatusMessage(message));
        config.preUpdate();
        genTableDeriveConfigDao.updateRunStatus(config);
    }

    private String limitStatusMessage(String message) {
        String text = message == null ? "" : message;
        return text.length() > MAX_STATUS_MESSAGE_LENGTH ? text.substring(0, MAX_STATUS_MESSAGE_LENGTH) : text;
    }

    public static class SyncResult {
        public int sourceRows;
        public int insertedRows;
        public int updatedRows;
        public int deletedRows;
        public GenTableDeriveConfig deriveConfig;
    }

    private class TargetTable {
        private String schema;
        private String name;

        private String expression() {
            return StringUtil.isBlank(schema) ? quoteIdentifier(name) : quoteIdentifier(schema) + "." + quoteIdentifier(name);
        }

        private String rawName() {
            return StringUtil.isBlank(schema) ? name : schema + "." + name;
        }
    }

    private static class TargetColumn {
        private String name;
        private int jdbcType;
    }
}
