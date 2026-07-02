package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.gen.enums.DateTypeEnum;
import com.jeestudio.bpm.utils.Global;

import java.util.List;

/**
 * @Description: MySQL数据库代码生成方言
 */
public class MysqlGenDialect extends AbstractGenDialect {

    @Override
    public String formatSelectColumn(String tableAlias, String columnName, DateTypeEnum dateTypeEnum) {
        // 当 dateTypeEnum 为 null 时，设置默认值为 DATE
        if (dateTypeEnum == null) {
            dateTypeEnum = DateTypeEnum.DATE;
        }

        String format;
        switch (dateTypeEnum) {
            case YEAR:
                format = "%Y";
                break;
            case YEAR_MONTH:
                format = "%Y-%m";
                break;
            case DATE:
                format = "%Y-%m-%d";
                break;
            case HOUR:
                format = "%Y-%m-%d %H";
                break;
            case MINUTE:
                format = "%Y-%m-%d %H:%i";
                break;
            default:
                format = "%Y-%m-%d %H:%i:%s";
                break;
        }
        return "DATE_FORMAT(" +
                tableAlias + "." + columnName +
                ",'" + format + "')";
    }

    @Override
    public String[] getCreateTableSql(String tableName, String tableComment, List<GenTableColumn> genTableColumnList) {
        StringBuilder sql = new StringBuilder();
        sql.append(StrUtil.format("create table {} (", tableName));
        StringBuilder pk = new StringBuilder();
        for (GenTableColumn column : genTableColumnList) {
            sql.append(StrUtil.format(" {} {} comment '{}',", column.getName(), column.getJdbcType(), column.getComments()));
            if (Global.YES.equals(column.getIsPk())) {
                pk.append(column.getName()).append(",");
            }
        }
        sql.append(StrUtil.format("primary key ({})", pk.substring(0, pk.length() - 1)));
        sql.append(StrUtil.format(") comment '{}'", tableComment));
        return new String[]{sql.toString()};
    }

    @Override
    public String[] getAddColumnSql(String tableName, GenTableColumn genTableColumn) {
        return new String[]{StrUtil.format("ALTER TABLE {} ADD {} {} COMMENT '{}'", tableName, genTableColumn.getName(), genTableColumn.getJdbcType(), genTableColumn.getComments())};
    }

    @Override
    public String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn) {
        return new String[]{StrUtil.format("ALTER TABLE {} MODIFY {} {} COMMENT '{}'", tableName, genTableColumn.getName(), genTableColumn.getJdbcType(), genTableColumn.getComments())};
    }


}
