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

    public abstract String[] getCreateTableSql(String tableName, String tableComment, List<GenTableColumn> genTableColumnList);

    public abstract String[] getAddColumnSql(String tableName, GenTableColumn genTableColumn);

    public abstract String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn);
}
