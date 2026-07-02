package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.gen.enums.DateTypeEnum;
import com.jeestudio.bpm.utils.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: SQL Server数据库代码生成方言
 */
public class MssqlGenDialect extends AbstractGenDialect {
    @Override
    public String formatSelectColumn(String tableAlias, String columnName, DateTypeEnum dateTypeEnum) {
        switch (dateTypeEnum) {
            case YEAR:
                return "CONVERT(VARCHAR(4), " + tableAlias + "." + columnName + ", 23)";
            case YEAR_MONTH:
                return "CONVERT(VARCHAR(7), " + tableAlias + "." + columnName + ", 23)";
            case DATE:
                return "CONVERT(VARCHAR(100), " + tableAlias + "." + columnName + ", 23)";
            case HOUR:
                return "CONVERT(VARCHAR(13), " + tableAlias + "." + columnName + ", 20)";
            case MINUTE:
                return "CONVERT(VARCHAR(16), " + tableAlias + "." + columnName + ", 20)";
            default:
                return "CONVERT(VARCHAR(100), " + tableAlias + "." + columnName + ", 20)";
        }

    }

    @Override
    public String getDropTableSql(String tableName) {
        return StrUtil.format("if exists (select * from sysobjects where id = object_id(N'[{}]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)  drop table [{}]", tableName, tableName);
    }

    @Override
    public String getJdbcType(GenTableColumn genTableColumn) {
        String jdbcType = genTableColumn.getJdbcType();
        if (jdbcType.equalsIgnoreCase("integer")) {
            jdbcType = "int";
        } else if (jdbcType.equalsIgnoreCase("datetime")) {
            jdbcType = "datetime";
        } else if (jdbcType.contains("nvarchar(")) {
            jdbcType = jdbcType;
        } else if (jdbcType.contains("varchar(")) {
            jdbcType = jdbcType.replace("varchar", "nvarchar");
        } else if (jdbcType.equalsIgnoreCase("double")) {
            jdbcType = "decimal(22,4)";
        } else if (jdbcType.equalsIgnoreCase("longblob")) {
            jdbcType = "varbinary(MAX)";
        } else if (jdbcType.equalsIgnoreCase("longtext")) {
            jdbcType = "nvarchar(max)";
        } else if (jdbcType.contains("tinyint")) {
            jdbcType = "bit";
        }
        return jdbcType;
    }

    @Override
    public String[] getCreateTableSql(String tableName, String tableComment, List<GenTableColumn> genTableColumnList) {
        List<String> sqlList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append(StrUtil.format("CREATE TABLE {} (", tableName));

        StringBuilder pk = new StringBuilder();

        for (GenTableColumn column : genTableColumnList) {
            String jdbcType = this.getJdbcType(column);

            sql.append(StrUtil.format(" {} {},", column.getName(), jdbcType));

            if (Global.YES.equals(column.getIsPk())) {
                pk.append(column.getName()).append(",");
            }

            sqlList.add(this.getCommentSql(tableName, column));
        }

        if (pk.length() > 0) {
            pk.deleteCharAt(pk.length() - 1);
        }

        if (pk.length() > 0) {
            sql.append(StrUtil.format("PRIMARY KEY ({})", pk));
        } else {
            sql.deleteCharAt(sql.length() - 1);
        }

        sql.append(")");

        sqlList.add(0, sql.toString());

        sqlList.add(StrUtil.format(
                "IF EXISTS (SELECT 1 FROM ::fn_listextendedproperty('MS_Description', 'SCHEMA', N'dbo', 'TABLE', N'{}', NULL, NULL)) " +
                        "EXEC sp_updateextendedproperty 'MS_Description', N'{}', 'SCHEMA', N'dbo', 'TABLE', N'{}' " +
                        "ELSE " +
                        "EXEC sp_addextendedproperty 'MS_Description', N'{}', 'SCHEMA', N'dbo', 'TABLE', N'{}'",
                tableName, tableComment, tableName, tableComment, tableName
        ));

        return sqlList.toArray(new String[0]);
    }

    private String getCommentSql(String tableName, GenTableColumn column) {
        return StrUtil.format("IF ((SELECT COUNT(*) FROM ::fn_listextendedproperty('MS_Description', 'SCHEMA', N'dbo', 'TABLE', N'{}', 'COLUMN', N'{}')) > 0)   EXEC sp_updateextendedproperty 'MS_Description', N'{}', 'SCHEMA', N'dbo', 'TABLE', N'{}', 'COLUMN', N'{}' ELSE   EXEC sp_addextendedproperty 'MS_Description', N'{}', 'SCHEMA', N'dbo', 'TABLE', N'{}', 'COLUMN', N'{}'", tableName, column.getName(), column.getComments(), tableName, column.getName(), column.getComments(), tableName, column.getName());

    }

    @Override
    public String[] getAddColumnSql(String tableName, GenTableColumn genTableColumn) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(StrUtil.format("alter table {} add {} {} null", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
        sqlList.add(this.getCommentSql(tableName, genTableColumn));
        return sqlList.toArray(new String[0]);
    }

    @Override
    public String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn) {
        List<String> sqlList = new ArrayList<>();
        if (Global.NO.equals(genTableColumn.getIsPk())){
            //sqlserver主键不修改
            sqlList.add(StrUtil.format("alter table {} alter column {} {}", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
            sqlList.add(this.getCommentSql(tableName, genTableColumn));
        }
        return sqlList.toArray(new String[0]);
    }
}
