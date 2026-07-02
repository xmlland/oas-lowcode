package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: PostgreSQL数据库代码生成方言
 */
public class PostgreGenDialect extends OracleGenDialect {

    @Override
    public String getJdbcType(GenTableColumn genTableColumn) {
        String jdbcType = genTableColumn.getJdbcType();
        if (jdbcType.contains("integer")) {
            jdbcType = "int";
        } else if (jdbcType.contains("datetime")) {
            jdbcType = "timestamp";
        } else if (jdbcType.contains("nvarchar(")) {
            jdbcType = jdbcType.replace("nvarchar", "varchar");
        } else if (jdbcType.equalsIgnoreCase("double")) {
            jdbcType = "numeric(22,4)";
        } else if (jdbcType.equalsIgnoreCase("longblob")) {
            jdbcType = "bytea";
        } else if (jdbcType.equalsIgnoreCase("longtext")) {
            jdbcType = "text";
        } else if (jdbcType.contains("tinyint")) {
            jdbcType = "boolean";
        } else if (jdbcType.contains("decimal")) {
            jdbcType = jdbcType.replace("decimal", "numeric");
        }
        return jdbcType;
    }


    @Override
    public String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(StrUtil.format("alter table {} alter {} type {}", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
        sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, genTableColumn.getName(), genTableColumn.getComments()));
        return sqlList.toArray(new String[0]);
    }

}
