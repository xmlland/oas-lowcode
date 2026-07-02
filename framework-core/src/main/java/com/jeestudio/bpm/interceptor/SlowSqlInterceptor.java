package com.jeestudio.bpm.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @Description: 慢 SQL 监控拦截器，记录超过阈值的 SQL 执行日志
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

    /**
     * 慢SQL阈值（毫秒），默认1000ms
     */
    private static final long SLOW_SQL_THRESHOLD_MS = 1000L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return invocation.proceed();
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            logSqlExecution(invocation, costTime);
        }
    }

    /**
     * 记录SQL执行日志
     */
    private void logSqlExecution(Invocation invocation, long costTime) {
        try {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            
            // 获取BoundSql
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            
            // 格式化SQL（去除多余空白）
            String formattedSql = formatSql(sql);
            
            // 获取参数信息
            String parameterInfo = getParameterInfo(boundSql);
            
            if (costTime >= SLOW_SQL_THRESHOLD_MS) {
                log.warn("[SLOW SQL] {}ms - {} | Parameters: {}", costTime, formattedSql, parameterInfo);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("[SQL] {}ms - {} | Parameters: {}", costTime, formattedSql, parameterInfo);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to log SQL execution: {}", e.getMessage());
        }
    }

    /**
     * 格式化SQL语句，去除多余空白
     */
    private String formatSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        return sql.replaceAll("[\\s]+", " ").trim();
    }

    /**
     * 获取参数信息
     */
    private String getParameterInfo(BoundSql boundSql) {
        try {
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            
            if (parameterObject == null) {
                return "null";
            }
            
            if (parameterMappings == null || parameterMappings.isEmpty()) {
                return parameterObject.toString();
            }
            
            // 简化输出：直接返回参数对象的字符串表示
            // 对于复杂对象，截断过长的输出
            String paramStr = parameterObject.toString();
            if (paramStr.length() > 500) {
                return paramStr.substring(0, 500) + "...(truncated)";
            }
            return paramStr;
        } catch (Exception e) {
            return "unable to get parameters";
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置文件设置属性，当前使用默认值
    }
}
