package generate.dialect;

import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import generate.pojo.DbColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 人大金仓数据库更新SQL生成方言
 */
public class Kingbase8Dialect extends DialectI {



    @Override
    public List<DbColumn> selectColumns(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * ");
        sql.append("         FROM (SELECT concat(case when b.nspname = 'common' then '' else concat(b.nspname, '.') end, a.relname) \"table_name\",  ");
        sql.append("         c.attname                                                                                 \"column_name\",  ");
        sql.append("         c.attnum AS                                                                               \"column_sort\"  ");
        sql.append(" from sys_class a  ");
        sql.append(" left join sys_namespace b on a.relnamespace = b.oid  ");
        sql.append(" left join sys_attribute c on c.attrelid = a.oid  ");
        sql.append(" where b.nspname is not null  ");
        sql.append(" and c.attnum > 0) t  ");
        sql.append(" where t.table_name = '" + SqlInjectionUtil.validateSqlIdentifier(tableName) + "'  ");
        sql.append(" order by column_sort  ");
        List<Map<String, Object>> select = select(sql.toString());
        List<DbColumn> dbColumns = new ArrayList<>();
        select.forEach(map -> {
            DbColumn dbColumn = new DbColumn();
            dbColumn.setTable_name(ConvertUtil.getString(map.get("table_name")));
            dbColumn.setColumn_name(ConvertUtil.getString(map.get("column_name")));
            dbColumn.setColumn_sort(ConvertUtil.getInteger(map.get("column_sort")));
            dbColumns.add(dbColumn);
        });
        return dbColumns;
    }
}
