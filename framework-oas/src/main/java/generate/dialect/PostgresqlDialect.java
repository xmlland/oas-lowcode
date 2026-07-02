package generate.dialect;

import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import generate.pojo.DbColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: PostgreSQL数据库更新SQL生成方言
 */
public class PostgresqlDialect extends DialectI {


    @Override
    public List<DbColumn> selectColumns(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("  SELECT table_name table_name, column_name column_name, ordinal_position column_sort    ");
        sql.append("  FROM information_schema.columns t                  ");
        sql.append("  WHERE table_schema = current_schema()         ");
        sql.append("  and  t.table_name = '" + SqlInjectionUtil.validateSqlIdentifier(tableName) + "'     ");
        sql.append("  ORDER BY t.ordinal_position;           ");
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
