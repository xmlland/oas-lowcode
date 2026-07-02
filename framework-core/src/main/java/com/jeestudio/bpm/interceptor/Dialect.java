package com.jeestudio.bpm.interceptor;

/**
 * @Description: 数据库分页方言接口
 */
public interface Dialect {
    /**
     * Does the database support paging the current paging query method
     * If the database does not support it, Database Paging will not be performed
     * @return true：Supports the current paging query method
     */
    boolean supportsLimit();

    /**
     * Convert SQL to paging SQL and call paging SQL respectively
     * @param sql    SQL statement
     * @param offset Start number
     * @param limit  How many records are displayed per page
     * @return SQL of paging query
     */
    String getLimitString(String sql, int offset, int limit);
}
