package generate.dialect;

import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import generate.pojo.DbColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: GBase数据库更新SQL生成方言
 */
public class GBaseDialect extends DialectI {
    @Override
    public List<DbColumn> selectColumns(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select t.tabname table_name, c.colname column_name, c.colno column_sort    ");
        sql.append(" from systables t,    ");
        sql.append("         syscolumns c    ");
        sql.append(" WHERE t.tabid = c.tabid    ");
        sql.append(" and t.tabname = '" + SqlInjectionUtil.validateSqlIdentifier(tableName) + "'    ");
        sql.append(" ORDER BY c.colno    ");


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
