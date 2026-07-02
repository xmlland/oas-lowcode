package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.utils.Global;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: Doris数据库代码生成方言
 */
public class DorisGenDialect extends MysqlGenDialect {

    @Override
    public String[] getCreateTableSql(String tableName, String tableComment,
                                       List<GenTableColumn> genTableColumnList) {
        return getCreateTableSql(tableName, tableComment, genTableColumnList, null);
    }

    /**
     * 建表 SQL，支持外部指定唯一键、分区键和分桶键
     * @param uniqueKey      唯一键列名（UNIQUE KEY），null 则自动检测 isPk 列，无 PK 列默认 "id"
     * @param partitionKey   分区键列名（PARTITION BY RANGE），null 则不生成分区语句
     * @param distributedKey 分桶键列名（DISTRIBUTED BY HASH），null 则 fallback 到 uniqueKey
     */
    public String[] getCreateTableSql(String tableName, String tableComment,
                                       List<GenTableColumn> genTableColumnList,
                                       String uniqueKey, String partitionKey, String distributedKey) {
        StringBuilder sql = new StringBuilder();
        sql.append(StrUtil.format("CREATE TABLE {} (\n", tableName));

        // 使用 uniqueKey 控制列排序（Doris 要求 UNIQUE KEY 列必须在列定义的最前面）
        List<GenTableColumn> orderedColumns = genTableColumnList;
        Set<String> keySet = new LinkedHashSet<>();
        String effectiveUniqueKey = StrUtil.isNotBlank(uniqueKey) ? uniqueKey : partitionKey;
        if (StrUtil.isNotBlank(effectiveUniqueKey)) {
            for (String kn : effectiveUniqueKey.split(",")) {
                keySet.add(kn.trim());
            }
            orderedColumns = new ArrayList<>(genTableColumnList.size());
            // 先放唯一键列（按用户指定的顺序）
            for (String keyName : keySet) {
                for (GenTableColumn column : genTableColumnList) {
                    if (keyName.equals(column.getName())) {
                        orderedColumns.add(column);
                        break;
                    }
                }
            }
            // 再放其余列（保持原顺序，排除已添加的唯一键列）
            for (GenTableColumn column : genTableColumnList) {
                if (!keySet.contains(column.getName())) {
                    orderedColumns.add(column);
                }
            }
        }

        StringBuilder pk = new StringBuilder();
        for (GenTableColumn column : orderedColumns) {
            sql.append(StrUtil.format("  {} {} COMMENT '{}',\n",
                column.getName(), getJdbcType(column), column.getComments()));
            if (Global.YES.equals(column.getIsPk())) {
                pk.append(column.getName()).append(",");
            }
        }
        // 去除最后一个字段的逗号和换行
        int lastComma = sql.lastIndexOf(",");
        if (lastComma > 0) {
            sql.deleteCharAt(lastComma);
        }
        sql.append(")");

        // Doris 表模型：UNIQUE KEY
        String keyColumns;
        if (StrUtil.isNotBlank(uniqueKey)) {
            keyColumns = uniqueKey;
        } else if (StrUtil.isNotBlank(partitionKey)) {
            keyColumns = partitionKey;
        } else {
            keyColumns = pk.length() > 0 ? pk.substring(0, pk.length() - 1) : "id";
        }
        sql.append(StrUtil.format("\nUNIQUE KEY({})", keyColumns));

        // 表注释
        sql.append(StrUtil.format("\nCOMMENT '{}'", tableComment));

        // 分区属性：PARTITION BY RANGE
        if (StrUtil.isNotBlank(partitionKey)) {
            sql.append(StrUtil.format("\nPARTITION BY RANGE({}) ()", partitionKey));
        }

        // 分布式属性：DISTRIBUTED BY HASH
        String distributedColumns = keyColumns;
        if (StrUtil.isNotBlank(distributedKey)) {
            // 校验 distributedKey 是否为 uniqueKey 的子集
            Set<String> uniqueKeySet = new LinkedHashSet<>();
            for (String kn : keyColumns.split(",")) {
                uniqueKeySet.add(kn.trim());
            }
            boolean valid = true;
            for (String dn : distributedKey.split(",")) {
                if (!uniqueKeySet.contains(dn.trim())) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                distributedColumns = distributedKey;
            }
        }
        sql.append(StrUtil.format("\nDISTRIBUTED BY HASH({}) BUCKETS 8", distributedColumns));

        // 副本数（单节点开发环境用 1）
        sql.append("\nPROPERTIES(\"replication_num\" = \"1\")");

        return new String[]{sql.toString()};
    }

    /**
     * 兼容旧版调用：只传 partitionKey 时，同时作为 uniqueKey 使用
     */
    public String[] getCreateTableSql(String tableName, String tableComment,
                                       List<GenTableColumn> genTableColumnList, String partitionKey) {
        return getCreateTableSql(tableName, tableComment, genTableColumnList, partitionKey, null, null);
    }

    @Override
    public String getJdbcType(GenTableColumn column) {
        String jdbcType = column.getJdbcType();
        if (jdbcType == null) return "VARCHAR(255)";
        // PostgreSQL serial/int4 转 INT
        if (jdbcType.equalsIgnoreCase("serial") || jdbcType.equalsIgnoreCase("int4")) {
            return "INT";
        }
        // PostgreSQL int8/bigserial 转 BIGINT
        if (jdbcType.equalsIgnoreCase("int8") || jdbcType.equalsIgnoreCase("bigserial")) {
            return "BIGINT";
        }
        // PostgreSQL text/longtext 转 STRING
        if (jdbcType.equalsIgnoreCase("text") || jdbcType.equalsIgnoreCase("longtext")) {
            return "STRING";
        }
        // PostgreSQL timestamp/timestamptz 转 DATETIME
        if (jdbcType.toLowerCase().startsWith("timestamp")) {
            return "DATETIME";
        }
        // PostgreSQL bool 转 BOOLEAN
        if (jdbcType.equalsIgnoreCase("bool") || jdbcType.equalsIgnoreCase("boolean")) {
            return "BOOLEAN";
        }
        // numeric/decimal 保留
        if (jdbcType.toLowerCase().startsWith("numeric") || jdbcType.toLowerCase().startsWith("decimal")) {
            return jdbcType.toUpperCase();
        }
        return jdbcType.toUpperCase();
    }
}
