package com.jeestudio.bpm.security;

import com.jeestudio.bpm.utils.ContextHolderUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 请求参数解密过滤器
 */

public class DecryptFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = ContextHolderUtil.getHttpServletRequest();
        if (TranEncryptUtil.isSkip()){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
            Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
            //将请求中已存在的参数解密后赋值到newParams中
            Map<String, String[]> newParams = new HashMap<>(parameterMap);
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] value = entry.getValue();
                for (int i = 0; i < value.length; i++) {
                    value[i] = TranEncryptUtil.decryptRequestData(value[i]);
                }
                newParams.put(entry.getKey(), value);
            }
            servletRequest = new CustomParameterRequestWrapper(httpServletRequest, newParams);

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
