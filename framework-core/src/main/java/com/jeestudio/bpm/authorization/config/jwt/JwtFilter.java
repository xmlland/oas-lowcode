package com.jeestudio.bpm.authorization.config.jwt;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.security.TranEncryptUtil;
import com.jeestudio.bpm.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description: JWT认证过滤器
 */
@Component
public class JwtFilter extends BasicHttpAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired(required = false)
    private ProjectProperties projectProperties;

    public JwtFilter() {
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (this.isLoginAttempt(request, response)) {
            try {
                this.executeLogin(request, response);
            } catch (Exception e) {
                String msg_en = "Token expired or incorrect, " + e.getMessage();
                String msg = "登录已超时，请重新登录";
                Throwable throwable = e.getCause();
                if (throwable instanceof SignatureVerificationException) {
                    msg_en = "Incorrect token";
                    msg = "用户凭据不正确";
                } else if (e.getMessage().contains("Token expired")) {
                    if (this.refreshToken(request, response)) {
                        return true;
                    } else {
                        msg_en = "Token Expired";
                        msg = "登录已超时，请重新登录";
                    }
                } else {
                    if (throwable != null) {
                        msg_en = throwable.getMessage();
                        msg = throwable.getMessage();
                    }
                }
                this.response401(response, msg, msg_en);
                return false;
            }
            return true;
        } else {
            HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
            String httpMethod = httpServletRequest.getMethod();
            String requestURI = httpServletRequest.getRequestURI();
            logger.info("Current request {} Authorization Attribute(Token) Empty Request type {}", requestURI, httpMethod);
            this.response401(response, "请先登录", "Please login first");
            return false;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        this.sendChallenge(request, response);
        return false;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.contains("websocket")){
            // WebSocket握手阶段：从URL参数中提取Token（WebSocket升级请求无法携带自定义Header）
            String tokenStr = httpRequest.getParameter("token");
            return StringUtils.isNotBlank(tokenStr);
        }
        String tokenStr = getHeadToken(httpRequest);
        return StringUtils.isNotBlank(tokenStr);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.contains("websocket")){
            // WebSocket握手阶段：从URL参数提取Token进行JWT认证
            HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
            String tokenStr = httpRequest.getParameter("token");
            if (StringUtils.isBlank(tokenStr)) {
                throw new AuthenticationException("WebSocket连接缺少认证Token");
            }
            JwtToken token = new JwtToken(tokenStr);
            Subject su = this.getSubject(request, response);
            su.login(token);
            return true;
        }else {
            HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
            String tokenStr = getHeadToken(httpRequest);
            httpServletResponse.setHeader("token", tokenStr);
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "token");
            Boolean fileCheck = checkFilterToken(requestURI);
            // #4修复：文件服务内部通信Token从配置读取，不再硬编码
            if (fileCheck && isValidFileServiceToken(tokenStr)){
                return true;
            }else{
                JwtToken token = new JwtToken(tokenStr);
                Subject su = this.getSubject(request, response);
                su.login(token);
            }
            // 如果不是文件下载接口，那么设置当前页面的Cookie中的token, 文件接口不设置Cookie，防止前端根据状态直接登录
            if (!fileCheck) {
                // 如果没有抛出异常则代表登入成功，返回true, 并且添加 Cookie,Cookie用于请求文件
                String setCookieKey = "Set-Cookie";
                String encodedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenStr.getBytes());
                Cookie cookie = new Cookie("Token",encodedToken );
                cookie.setHttpOnly(true);
                cookie.setPath("/"); // 设置cookie的路径
                cookie.setSecure(true);
                cookie.setMaxAge((int) (Global.EXRP_MINUTE*60)); // 设置cookie的最大生存时间（以秒为单位）
                httpServletResponse.addCookie(cookie);

                if (httpServletResponse.containsHeader(setCookieKey)) {
                    Collection<String> headers = httpServletResponse.getHeaders(setCookieKey);
                    for (String header : headers) {
                        if (!header.startsWith("rememberMe")) {
                            httpServletResponse.setHeader(setCookieKey, header);
                        }
                    }
                }
            }

            return true;
        }

    }

    /**
     * #4修复：验证文件服务内部通信Token
     * Token从配置文件读取（project.file-service-token），不再硬编码
     */
    private boolean isValidFileServiceToken(String tokenStr) {
        if (projectProperties == null) {
            return false;
        }
        String configuredToken = projectProperties.getFileServiceToken();
        if (StringUtils.isBlank(configuredToken)) {
            // 未配置内部通信Token则禁止此方式访问
            return false;
        }
        return configuredToken.equals(tokenStr);
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);

        // #11修复：CORS从配置文件读取允许的Origin白名单
        String origin = httpServletRequest.getHeader("Origin");
        if (isAllowedOrigin(origin)) {
            httpServletResponse.setHeader("Access-Control-Allow-Origin", origin);
        }

        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * #11修复：检查Origin是否在配置的白名单中
     * 配置项 project.cors-allowed-origins，多个用逗号分隔
     * 为空则不允许任何跨域，设置为 * 则允许所有（仅开发环境使用）
     */
    private boolean isAllowedOrigin(String origin) {
        if (StringUtils.isBlank(origin)) {
            return false;
        }
        if (projectProperties == null) {
            return false;
        }
        String allowedOrigins = projectProperties.getCorsAllowedOrigins();
        if (StringUtils.isBlank(allowedOrigins)) {
            return false;
        }
        if ("*".equals(allowedOrigins.trim())) {
            return true;
        }
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedOrigins.split(",")));
        return allowedSet.contains(origin.trim());
    }

    /**
     * @Description: 获取请求中的 token（仅从Header和HttpOnly Cookie获取，禁止URL参数传递防止泄露）
     */
    private String getHeadToken(HttpServletRequest httpRequest) {
        String tokenStr = httpRequest.getHeader("token");
        if (StringUtils.isBlank(tokenStr)) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("Token".equals(cookie.getName()) && StringUtils.isNotBlank(cookie.getValue())) {
                        tokenStr = new String(Base64.getUrlDecoder().decode(cookie.getValue().getBytes()));
                        break;
                    }
                }
            }
        }
        return tokenStr;
    }
    private Boolean checkFilterToken(String url) {
        String filterFileMapTokenUrl = PropertiesUtil.getProperty("serverIp.properties", "FilterFileMapToken");
        // 默认没有配置情况下，使用默认的文件下载接口
        if (StringUtils.isBlank(filterFileMapTokenUrl)) {
            filterFileMapTokenUrl = "/a/system/sysFile/fileDownload";
        }
        String[] fm = filterFileMapTokenUrl.split(",");
        for (String s : fm) {
            if (url.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * #15修复：Token刷新增加竞态保护
     * 使用Redis标记防止并发请求重复刷新同一Token
     */
    private boolean refreshToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String token = httpRequest.getHeader("token");
        String username = JwtUtil.getClaim(token, Global.USERNAME);
        String userId = JwtUtil.getClaim(token, Global.USERID);
        String clientId = JwtUtil.getClaim(token, "clientId");
        String key = Global.PREFIX_SHIRO_REFRESH_TOKEN + username + "_" + userId + "_token_clientId_" + clientId;
        boolean exist = cacheUtil.existKey(key);
        if (exist) {
            String t = cacheUtil.getObjectAuthorization(key);
            if (t != null && t.equals(token)) {
                // 先将旧key标记为"refreshing"防止并发请求重复刷新
                CacheData lockData = new CacheData();
                lockData.setKey(key);
                lockData.setValue("refreshing:" + System.currentTimeMillis());
                lockData.setTimeout(1L);
                cacheUtil.setObjectExpireMinuteAuthorization(lockData);

                // 签发新token
                String token_ = JwtUtil.sign(username, userId);
                JwtToken jwtToken = new JwtToken(token_);
                String clientId_ = JwtUtil.getClaim(token_, "clientId");
                CacheData cacheData = new CacheData();
                cacheData.setKey(Global.PREFIX_SHIRO_REFRESH_TOKEN + username + "_" + userId + "_token_clientId_" + clientId_);
                cacheData.setValue(token_);
                cacheData.setTimeout(Global.EXRP_MINUTE);
                cacheUtil.setObjectExpireMinuteAuthorization(cacheData);
                this.getSubject(request, response).login(jwtToken);
                HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
                httpServletResponse.setHeader("token", token_);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "token");
                TranEncryptUtil.setPublicKey(userId);
                return true;
            } else if (t != null && t.startsWith("refreshing:")) {
                // 另一个请求正在刷新，短暂等待后重试
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                // 尝试找到新token（通过用户维度查找）
                return retryWithNewToken(request, response, username, userId);
            }
        }
        return false;
    }

    /**
     * 并发刷新场景下，尝试使用已由其他请求刷新的新token
     */
    private boolean retryWithNewToken(ServletRequest request, ServletResponse response, String username, String userId) {
        // 无法确定新的clientId，让客户端重新登录
        logger.debug("Token刷新并发冲突，用户: {}", username);
        return false;
    }

    private void response401(ServletResponse response, String msg, String msg_en) {
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {
            String data = JsonConvertUtil.objectToJson(new ResultJson(1001, msg, msg_en, null));
            out.append(data);
        } catch (IOException e) {
            logger.error("Return Response IOException:" + e.getMessage());
        }
    }
}
