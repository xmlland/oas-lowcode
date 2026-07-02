package com.jeestudio.bpm.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Description: 接口耗时监控AOP切面
 * 拦截所有 RestController 方法，记录请求耗时并输出日志
 *
 * @author System
 */
@Component
@Aspect
@Slf4j
@Order(1)
public class ControllerMetricsAspect {

    /**
     * 慢接口阈值（毫秒）
     */
    private static final long SLOW_API_THRESHOLD_MS = 3000;

    /**
     * 切点：拦截所有 @RestController 类中的 public 方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {
    }

    @Around("restControllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String method = "UNKNOWN";
        String path = "UNKNOWN";

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            path = request.getRequestURI();
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // 正常请求日志
            if (duration >= SLOW_API_THRESHOLD_MS) {
                log.warn("[SLOW API] {} {} - {}ms", method, path, duration);
            } else {
                log.debug("[API] {} {} - {}ms", method, path, duration);
            }

            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            // 异常请求日志
            log.warn("[API ERROR] {} {} - {}ms - {}", method, path, duration, throwable.getClass().getSimpleName());
            throw throwable;
        }
    }
}
