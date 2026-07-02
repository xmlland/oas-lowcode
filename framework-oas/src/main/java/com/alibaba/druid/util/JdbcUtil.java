package com.alibaba.druid.util;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: JDBC 批量执行工具类
 */
public class JdbcUtil extends JdbcUtils {
    private static final Log LOG = LogFactory.getLog(JdbcUtil.class);


    public static int executeBatch(Connection conn, String sql, List<List<Object>> parameters) throws SQLException {

        int updateCount;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParametersBatch(stmt, parameters);
            int[] ints = stmt.executeBatch();
            updateCount = Arrays.stream(ints).sum();
        } finally {
            close((Statement)stmt);
        }
        return updateCount;

    }

    private static void setParametersBatch(PreparedStatement stmt, List<List<Object>> parameters){
        parameters.forEach((list) -> {
            try {
                setParameters(stmt, list);
                stmt.addBatch();
            } catch (SQLException var3) {
                throw new RuntimeException(var3);
            }
        });
    }

    static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        int i = 0;

        for(int size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            // 根据 set类型 进行判断
            if (param instanceof Date){
                // java.util.Date 转 java.sql.Date 设置参数时才不会出错
                stmt.setObject(i + 1, new java.sql.Date(((Date) param).getTime()));
                continue;
            }
            stmt.setObject(i + 1, param);
        }
    }

}
