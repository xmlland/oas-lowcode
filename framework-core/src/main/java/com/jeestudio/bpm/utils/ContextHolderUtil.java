package com.jeestudio.bpm.utils;

/**
 * @Description: 请求上下文工具
 */
public class ContextHolderUtil {

    public static jakarta.servlet.http.HttpServletRequest getHttpServletRequest() {
        return ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
    }


    public static jakarta.servlet.http.HttpServletResponse getHttpServletResponse() {
        return ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getResponse();
    }
}
