package com.jeestudio.bpm.gen.dialect;

import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.gen.enums.DateTypeEnum;

import java.util.List;

/**
 * @Description: 代码生成数据库方言抽象基类
 */
public abstract class AbstractGenDialect {


    /**
     * 格式化查询SQL中的日期字段
     *
     * @param tableAlias   表别名
     * @param columnName   查询字段
     * @param dateTypeEnum 日期类型
     * @return 处理后的查询字段
     */
    public abstract String formatSelectColumn(String tableAlias, String columnName, DateTypeEnum dateTypeEnum);


    public String getDropTableSql(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }


    public String getJdbcType(GenTableColumn genTableColumn) {
        return genTableColumn.getJdbcType();
    }

    /**
     * 归一化 jdbc 类型字符串，用于同步表时比较「元数据类型」与「库中类型」是否等价。
     * 避免 datetime vs timestamp、integer vs int 等同义写法触发无意义 ALTER。
     */
    public String normalizeJdbcTypeForCompare(String jdbcType) {
        if (jdbcType == null) {
            return "";
        }
        String t = jdbcType.trim().toLowerCase();
        // 去掉长度/精度：varchar(64) -> varchar, numeric(18,2) -> numeric
        int paren = t.indexOf('(');
        if (paren > 0) {
            t = t.substring(0, paren);
        }
        t = t.trim();
        if (t.isEmpty()) {
            return "";
        }
        // 日期时间族
        if ("datetime".equals(t) || "timestamp".equals(t)
                || "timestamptz".equals(t)
                || "timestamp without time zone".equals(t)
                || "timestamp with time zone".equals(t)) {
            return "timestamp";
        }
        if ("date".equals(t)) {
            return "date";
        }
        // 整型族
        if ("int".equals(t) || "integer".equals(t) || "int4".equals(t) || "serial".equals(t)) {
            return "int";
        }
        if ("bigint".equals(t) || "int8".equals(t) || "bigserial".equals(t)) {
            return "bigint";
        }
        if ("smallint".equals(t) || "int2".equals(t)) {
            return "smallint";
        }
        // 小数族
        if ("decimal".equals(t) || "numeric".equals(t) || "number".equals(t)) {
            return "numeric";
        }
        if ("double".equals(t) || "float8".equals(t) || "double precision".equals(t)) {
            return "double";
        }
        if ("float".equals(t) || "float4".equals(t) || "real".equals(t)) {
            return "float";
        }
        // 文本族
        if ("varchar".equals(t) || "nvarchar".equals(t) || "character varying".equals(t)) {
            return "varchar";
        }
        if ("char".equals(t) || "character".equals(t) || "bpchar".equals(t)) {
            return "char";
        }
        if ("text".equals(t) || "longtext".equals(t) || "clob".equals(t)) {
            return "text";
        }
        // 布尔
        if ("boolean".equals(t) || "bool".equals(t) || "tinyint".equals(t)) {
            return "boolean";
        }
        // 二进制
        if ("bytea".equals(t) || "blob".equals(t) || "longblob".equals(t)) {
            return "bytea";
        }
        return t;
    }

    /**
     * 判断设计器/元数据中的 jdbcType 与库中 jdbcType 是否语义等价（当前方言视角）。
     * <p>
     * 库侧通常是 information_schema 读出的物理类型（如 timestamp、int4）；
     * 元数据侧是设计器写法（如 datetime、integer）。两侧分别：
     * 1）走本方言 getJdbcType 映射；2）再 normalize 去长度、合并同义族。
     */
    public boolean isJdbcTypeEquivalent(String metaJdbcType, String dbJdbcType) {
        if (metaJdbcType == null && dbJdbcType == null) {
            return true;
        }
        if (metaJdbcType != null && metaJdbcType.equalsIgnoreCase(dbJdbcType)) {
            return true;
        }
        GenTableColumn metaCol = new GenTableColumn();
        metaCol.setJdbcType(metaJdbcType == null ? "" : metaJdbcType);
        GenTableColumn dbCol = new GenTableColumn();
        dbCol.setJdbcType(dbJdbcType == null ? "" : dbJdbcType);

        String metaNorm = normalizeJdbcTypeForCompare(getJdbcType(metaCol));
        String dbNorm = normalizeJdbcTypeForCompare(getJdbcType(dbCol));
        if (metaNorm.equals(dbNorm)) {
            return true;
        }
        // 库类型可能已是物理名，getJdbcType 不再改写时，用原始串再比一次
        return metaNorm.equals(normalizeJdbcTypeForCompare(dbJdbcType));
    }

    public abstract String[] getCreateTableSql(String tableName, String tableComment, List<GenTableColumn> genTableColumnList);

    public abstract String[] getAddColumnSql(String tableName, GenTableColumn genTableColumn);

    public abstract String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn);
}
