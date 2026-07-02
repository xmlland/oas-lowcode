package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.utils.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: GBase数据库代码生成方言
 */
public class GBaseGenDialect extends OracleGenDialect {
    @Override
    public String getJdbcType(GenTableColumn genTableColumn) {
        String jdbcType = genTableColumn.getJdbcType();
        if (jdbcType.equalsIgnoreCase("integer") || jdbcType.equalsIgnoreCase("int")) {
            jdbcType = "integer";
        } else if (jdbcType.equalsIgnoreCase("datetime")) {
            jdbcType = "DATETIME YEAR TO FRACTION(5)";
        } else if (jdbcType.contains("nvarchar(")) {
            jdbcType = jdbcType.replace("nvarchar", "varchar");
        } else if (jdbcType.equalsIgnoreCase("double")) {
            jdbcType = "float";
        } else if (jdbcType.equalsIgnoreCase("longblob")) {
            jdbcType = "blob";
        } else if (jdbcType.equalsIgnoreCase("longtext")) {
            jdbcType = "text";
        } else if (jdbcType.contains("tinyint")) {
            jdbcType = "smallint";
        } else if (jdbcType.contains("decimal")) {
            jdbcType = jdbcType.replace("decimal", "decimal");
        }
        return jdbcType;
    }

    @Override
    public String[] getCreateTableSql(String tableName, String tableComment, List<GenTableColumn> genTableColumnList) {
        List<String> sqlList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append(StrUtil.format("create table {} (", tableName));
        StringBuilder pk = new StringBuilder();
        for (GenTableColumn column : genTableColumnList) {
            sql.append(StrUtil.format(" {} {} ,", column.getName(), getJdbcType(column)));
            if (Global.YES.equals(column.getIsPk())) {
                pk.append(column.getName()).append(",");
            }
            sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, column.getName(), column.getComments()));
        }
        sql.append(StrUtil.format("primary key ({}) CONSTRAINT {} )", pk.substring(0, pk.length() - 1), tableName));
        sqlList.add(0, sql.toString());
        sqlList.add(StrUtil.format("comment on table {} is  '{}'", tableName, tableComment));
        return sqlList.toArray(new String[0]);
    }
}
