package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.utils.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 达梦数据库代码生成方言
 */
public class DmGenDialect extends OracleGenDialect {
    @Override
    public String getJdbcType(GenTableColumn genTableColumn) {
        String jdbcType = genTableColumn.getJdbcType();
        if (jdbcType.equalsIgnoreCase("integer") || jdbcType.equalsIgnoreCase("int")) {
            jdbcType = "number(10,0)";
        } else if (jdbcType.equalsIgnoreCase("datetime")) {
            jdbcType = "TIMESTAMP(0)";
        } else if (jdbcType.contains("nvarchar(")) {
            jdbcType = jdbcType.replace("nvarchar", "varchar2");
        } else if (jdbcType.contains("varchar(")) {
            jdbcType = jdbcType.replace("varchar", "varchar2").replace(")", " char)");
        } else if (jdbcType.equalsIgnoreCase("double")) {
            jdbcType = "number(22,4)";
        } else if (jdbcType.equalsIgnoreCase("longblob")) {
            jdbcType = "blob";
        } else if (jdbcType.equalsIgnoreCase("longtext")) {
            jdbcType = "text";
        } else if (jdbcType.contains("tinyint")) {
            jdbcType = "number(1,0)";
        } else if (jdbcType.contains("decimal")) {
            jdbcType = jdbcType.replace("decimal", "number");
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
            String jdbcType = this.getJdbcType(column);
            sql.append(StrUtil.format(" {} {} ,", column.getName(), jdbcType));
            if (Global.YES.equals(column.getIsPk())) {
                pk.append(column.getName()).append(",");
            }
            sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, column.getName(), column.getComments()));
        }

        if (sql.length() > 0 && sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }

        sqlList.add(0, sql.append(")").toString());

        sqlList.add(StrUtil.format("comment on table {} is  '{}'", tableName, tableComment));

        if (pk.length() > 0 && pk.charAt(pk.length() - 1) == ',') {
            pk.deleteCharAt(pk.length() - 1);
        }

        sqlList.add(StrUtil.format("alter table {} add constraint PK_{}_{} primary key ({})", tableName, tableName, pk, pk));
        return sqlList.toArray(new String[0]);
    }
}
