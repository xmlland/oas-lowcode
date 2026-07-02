package com.jeestudio.bpm.config.web;

import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @Description: 全局异常处理器
 * 用于统一处理Controller层未捕获的异常，返回友好的错误信息
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    /**
     * 异常计数器 - 按异常类型统计
     */
    private static final ConcurrentHashMap<String, AtomicLong> exceptionCounters = new ConcurrentHashMap<>();

    /**
     * 记录异常计数
     */
    private void countException(String exceptionType) {
        exceptionCounters.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取异常统计数据
     */
    public static Map<String, Long> getExceptionStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        exceptionCounters.forEach((key, value) -> stats.put(key, value.get()));
        return stats;
    }

    /**
     * 重置异常统计（用于测试或定期重置）
     */
    public static void resetExceptionStats() {
        exceptionCounters.clear();
    }

    /**
     * 处理业务异常 - 返回业务错误消息给前端
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public ResultJson handleBusinessException(BusinessException e) {
        countException("BusinessException");
        log.warn("Business exception: {}", e.getMessage());
        return ResultJson.failed(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoHandlerFoundException.class)
    public ResultJson handlerNoFoundException(Exception e) {
        countException("NoHandlerFoundException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("路径不存在，请检查路径是否正确");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DuplicateKeyException.class)
    public ResultJson handleDuplicateKeyException(DuplicateKeyException e) {
        countException("DuplicateKeyException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("数据库中已存在该记录");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
    public ResultJson handleAuthorizationException(AuthorizationException e) {
        countException("AuthorizationException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("权限不足，请联系管理员授权");
    }

    /**
     * 处理参数校验异常
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultJson handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        countException("MethodArgumentNotValidException");
        log.warn("Parameter validation failed: {}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResultJson.failed("参数校验失败: " + errorMsg);
    }

    /**
     * 处理数据访问异常（Spring DAO层异常）
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(DataAccessException.class)
    public ResultJson handleDataAccessException(DataAccessException e) {
        countException("DataAccessException");
        log.error("Data access exception: {}", e.getMessage(), e);
        return ResultJson.failed("数据库操作失败");
    }

    /**
     * 兜底异常处理 - 系统异常不暴露细节
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResultJson handleException(Exception e) {
        countException("Exception");
        log.error(e.getMessage(), e);
        return ResultJson.failed("系统异常，请联系管理员");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultJson handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        countException("MaxUploadSizeExceededException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("文件大小超出限制, 请压缩或降低文件质量! ");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    public ResultJson handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        countException("DataIntegrityViolationException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("字段太长,超出数据库字段的长度");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(PoolException.class)
    public ResultJson handlePoolException(PoolException e) {
        countException("PoolException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("缓存连接异常!");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(SQLException.class)
    public ResultJson handleSQLException(SQLException e) {
        countException("SQLException");
        log.error(e.getMessage(), e);
        return ResultJson.failed("数据库异常!");
    }

}
