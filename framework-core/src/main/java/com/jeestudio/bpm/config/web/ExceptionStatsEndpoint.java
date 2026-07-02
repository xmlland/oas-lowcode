package com.jeestudio.bpm.config.web;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 异常统计端点
 * 通过 /actuator/exceptionstats 访问
 *
 * @author Jason
 */
@Component
@Endpoint(id = "exceptionstats")
public class ExceptionStatsEndpoint {

    /**
     * 获取异常统计数据
     * GET /actuator/exception-stats
     */
    @ReadOperation
    public Map<String, Object> exceptionStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Long> stats = ExceptionHandler.getExceptionStats();
        
        // 计算总异常数
        long totalCount = stats.values().stream().mapToLong(Long::longValue).sum();
        
        result.put("totalExceptions", totalCount);
        result.put("exceptionsByType", stats);
        
        return result;
    }

    /**
     * 重置异常统计
     * POST /actuator/exception-stats
     */
    @WriteOperation
    public Map<String, String> resetStats() {
        ExceptionHandler.resetExceptionStats();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("message", "Exception stats have been reset");
        return result;
    }
}
