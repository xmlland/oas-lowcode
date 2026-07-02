package generate.dialect;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import generate.pojo.DbColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @Description: 数据库更新SQL生成方言基类
 */
public abstract class DialectI {

    private static final Logger logger = LoggerFactory.getLogger(DialectI.class);

    private static String url, username, password;

    public abstract List<DbColumn> selectColumns(String tableName);

    private String getDbTypeStr() {
        com.alibaba.druid.DbType dbTypeRaw = com.alibaba.druid.util.JdbcUtils.getDbTypeRaw(url, null);
        return dbTypeRaw.name();
    }

    private IDialect findIDialect() {
        String dbTypeStr = getDbTypeStr();
        if ("kingbase".equals(dbTypeStr)) {
            return DialectFactory.getDialect(DbType.KINGBASE_ES);
        }
        // 兼容说明：南大通用 8c、华为 GaussDB 使用 PostgreSQL 方言。
        if ("gaussdb".equals(dbTypeStr)) {
            return DialectFactory.getDialect(DbType.POSTGRE_SQL);
        }
        if ("sqlserver".equals(dbTypeStr)) {
            dbTypeStr = "sqlserver2005";
        }
        return DialectFactory.getDialect(DbType.getDbType(dbTypeStr));
    }

    public List<Supplier<List<Map<String, Object>>>> selectData(String tableName, String charter) {
        List<Supplier<List<Map<String, Object>>>> supplierList = new ArrayList<>();

        //查询总数
        //SELECT COUNT(1) FROM tableName
        String countSql = StrUtil.format("SELECT COUNT(1) count_res FROM {}{}{}", charter, tableName, charter);
        //查询数据
        List<Map<String, Object>> select = select(countSql);
        long total = 0;
        if (select.size() > 0) {
            total = (long) select.get(0).get("count_res");
        }
        if (total > 0) {
            String selectSql = "SELECT * FROM " + charter + tableName + charter;
            IDialect iDialect = findIDialect();

            int pageSize = 50000;
            //计算分页
            int pageCount = PageUtil.totalPage(total, pageSize);
            for (int i = 1; i <= pageCount; i++) {
                int offset = (i - 1) * pageSize;
                DialectModel dialectModel = iDialect.buildPaginationSql(selectSql, offset, pageSize);
                String dialectSql = dialectModel.getDialectSql();
                //判断dialectSql中？的数量
                int count = dialectSql.length() - dialectSql.replaceAll("\\?", "").length();
                if (count == 1) {
                    dialectSql = dialectSql.replace("?", String.valueOf(pageSize));
                } else if (count == 2) {
                    dialectSql = dialectSql.replaceFirst("\\?", String.valueOf(pageSize));
                    dialectSql = dialectSql.replaceFirst("\\?", String.valueOf(offset));
                }

                String finalDialectSql = dialectSql;
                supplierList.add(() -> {
                    Console.log("==========>{}分页查询：{}", Thread.currentThread().getName(), finalDialectSql);
                    return select(finalDialectSql);
                });
            }

        }
        return supplierList;
    }

    public void init(String url, String username, String password) {
        DialectI.url = url;
        DialectI.username = username;
        DialectI.password = password;
        String driver = "";
        if (url.contains("kingbase8")) {
            driver = "com.kingbase8.Driver";
        }
        if (url.contains("mysql")) {
            driver = "com.mysql.cj.jdbc.Driver";
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error("加载数据库驱动失败", e);
        }
    }

    public void init(String url, String username, String password, String driver) {
        DialectI.url = url;
        DialectI.username = username;
        DialectI.password = password;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error("加载数据库驱动失败", e);
        }
    }

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("获取数据库连接失败", e);
        }
        return null;
    }


    public List<Map<String, Object>> select(String sql) {
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> map = new java.util.HashMap<>();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    map.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            logger.error("执行SQL查询失败", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.warn("关闭数据库资源失败", e);
            }
        }
        return list;
    }
}
