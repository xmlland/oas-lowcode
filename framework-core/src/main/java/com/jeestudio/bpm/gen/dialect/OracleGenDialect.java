package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.gen.enums.DateTypeEnum;
import com.jeestudio.bpm.utils.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: Oracle数据库代码生成方言
 */
public class OracleGenDialect extends AbstractGenDialect {
    @Override
    public String formatSelectColumn(String tableAlias, String columnName, DateTypeEnum dateTypeEnum) {
        String format;
        switch (dateTypeEnum) {
            case YEAR:
                format = "YYYY";
                break;
            case YEAR_MONTH:
                format = "YYYY-MM";
                break;
            case DATE:
                format = "YYYY-MM-DD";
                break;
            case HOUR:
                format = "YYYY-MM-DD HH24";
                break;
            case MINUTE:
                format = "YYYY-MM-DD HH24:mi";
                break;
            default:
                format = "YYYY-MM-DD HH24:mi:ss";
                break;
        }
        return "TO_CHAR(" +
                tableAlias + "." + columnName +
                ",'" + format + "')";
    }

    @Override
    public String getJdbcType(GenTableColumn genTableColumn) {
        String jdbcType = genTableColumn.getJdbcType();
        if (jdbcType.equalsIgnoreCase("integer") || jdbcType.equalsIgnoreCase("int")) {
            jdbcType = "number(10,0)";
        } else if (jdbcType.equalsIgnoreCase("datetime")) {
            jdbcType = "TIMESTAMP(0)";
        } else if (jdbcType.contains("nvarchar(")) {
            jdbcType = jdbcType.replace("nvarchar", "nvarchar2");
        } else if (jdbcType.contains("varchar(")) {
            jdbcType = jdbcType.replace("varchar", "nvarchar2");
        } else if (jdbcType.equalsIgnoreCase("double")) {
            jdbcType = "float(24)";
        } else if (jdbcType.equalsIgnoreCase("longblob")) {
            jdbcType = "blob";
        } else if (jdbcType.equalsIgnoreCase("longtext")) {
            jdbcType = "nclob";
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
        sql.setLength(sql.length() - 1);
        sqlList.add(0, sql.append(")").toString());

        sqlList.add(StrUtil.format("comment on table {} is  '{}'", tableName, tableComment));

        sqlList.add(StrUtil.format("alter table {} add primary key ({})", tableName, pk.substring(0, pk.length() - 1)));
        return sqlList.toArray(new String[0]);
    }

    @Override
    public String[] getAddColumnSql(String tableName, GenTableColumn genTableColumn) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(StrUtil.format("alter table {} add {} {}", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
        sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, genTableColumn.getName(), genTableColumn.getComments()));
        return sqlList.toArray(new String[0]);
    }

    @Override
    public String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(StrUtil.format("alter table {} modify {} {}", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
        sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, genTableColumn.getName(), genTableColumn.getComments()));
        return sqlList.toArray(new String[0]);
    }
}
