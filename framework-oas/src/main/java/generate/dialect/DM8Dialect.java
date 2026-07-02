package generate.dialect;

import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import generate.pojo.DbColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 达梦数据库更新SQL生成方言
 */
public class DM8Dialect extends DialectI {



    @Override
    public List<DbColumn> selectColumns(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from (select ");
        sql.append("     (OWNER || '.' || TABLE_NAME) AS table_name, ");
        sql.append("     COLUMN_NAME AS column_name, ");
        sql.append("     COLUMN_ID AS column_sort ");
        sql.append(" from ALL_TAB_COLS) t");
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
