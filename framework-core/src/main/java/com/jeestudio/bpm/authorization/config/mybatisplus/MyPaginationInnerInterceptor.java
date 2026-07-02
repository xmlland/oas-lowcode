package com.jeestudio.bpm.authorization.config.mybatisplus;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;

import java.sql.Connection;

/**
 * @Description: MyBatis-Plus分页拦截器
 */
public class MyPaginationInnerInterceptor extends PaginationInnerInterceptor {
    DynamicRoutingDataSource dataSource;

    public MyPaginationInnerInterceptor(DynamicRoutingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDbTypeStr() {
        ItemDataSource itemDataSource = (ItemDataSource) dataSource.determineDataSource();
        DruidDataSource druidDataSource = (DruidDataSource) itemDataSource.getRealDataSource();
        return druidDataSource.getDbType();
    }

    @Override
    protected IDialect findIDialect(Executor executor) {
        String dbTypeStr = getDbTypeStr();
        if ("kingbase".equals(dbTypeStr)) {
            return DialectFactory.getDialect(DbType.KINGBASE_ES);
        }
        // 兼容说明：南大通用 8c、华为 GaussDB 使用 PostgreSQL 方言。
        if ("gaussdb".equals(dbTypeStr)) {
            return DialectFactory.getDialect(DbType.POSTGRE_SQL);
        }
        if (dbTypeStr != null) {
            if ("sqlserver".equals(dbTypeStr)) {
                dbTypeStr = "sqlserver2005";
            }
            return DialectFactory.getDialect(DbType.getDbType(dbTypeStr));
        } else {
            return DialectFactory.getDialect(JdbcUtils.getDbType(executor));
        }
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        super.beforePrepare(sh, connection, transactionTimeout);
    }

    @Override
    public String autoCountSql(IPage<?> page, String sql) {
        return "SELECT COUNT(*) AS total FROM (" + sql + ") t_count";
    }
}
