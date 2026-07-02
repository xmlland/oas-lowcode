package com.jeestudio.bpm.gen.dialect;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: GaussDB数据库代码生成方言
 */
public class GaussdbGenDialect extends PostgreGenDialect {

    @Override
    public String[] getModifyColumnSql(String tableName, GenTableColumn genTableColumn) {
        if ("id".equalsIgnoreCase(genTableColumn.getName())) {
            //分布式数据库id列不能修改
            return new String[0];
        }else{
            List<String> sqlList = new ArrayList<>();
            sqlList.add(StrUtil.format("alter table {} alter {} type {}", tableName, genTableColumn.getName(), this.getJdbcType(genTableColumn)));
            sqlList.add(StrUtil.format("comment on column {}.{} is  '{}'", tableName, genTableColumn.getName(), genTableColumn.getComments()));
            return sqlList.toArray(new String[0]);
        }
    }
}
