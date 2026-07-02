package com.jeestudio.bpm.authorization.config;

import com.jeestudio.bpm.authorization.CacheManager.RedisCacheManager;
import com.jeestudio.bpm.authorization.config.jwt.JwtFilter;
import com.jeestudio.bpm.authorization.config.realm.UserRealm;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.utils.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.InvalidRequestFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description: Shiro安全配置
 */
@Configuration
public class ShiroConfig {

    @Autowired
    private ProjectProperties projectProperties;

    /**
     * Security Manager
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager() {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        //close Session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        defaultWebSecurityManager.setSubjectDAO(subjectDAO);
        //Cache
        defaultWebSecurityManager.setCacheManager(getRedisCacheManager());
        //Realm
        defaultWebSecurityManager.setRealm(userRealm());
        defaultWebSecurityManager.setSessionManager(sessionManager());
        return defaultWebSecurityManager;
    }

    @Bean
    public RedisCacheManager getRedisCacheManager() {
        return new RedisCacheManager();
    }

    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        Map<String, Filter> filterMap = new HashMap<>(16);
        filterMap.put("authc", new JwtFilter());
        filterMap.put("invalidRequest", invalidRequestFilter());
        factoryBean.setFilters(filterMap);
        factoryBean.setSecurityManager(securityManager);
        String loginUrl = PropertiesUtil.getProperty("serverIp.properties", "loginUrl");
        factoryBean.setLoginUrl(loginUrl);
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>(16);
        filterChainDefinitionMap.put(loginUrl, "anon");
        String filterMapsAnon = PropertiesUtil.getProperty("serverIp.properties", "FilterMapAnon");
        String[] fa = filterMapsAnon.split(",");
        for (String string : fa) {
            filterChainDefinitionMap.put(string, "anon");
        }
        String filterMaps = PropertiesUtil.getProperty("serverIp.properties", "FilterMapJwt");
        String[] fm = filterMaps.split(",");
        for (String s : fm) {
            filterChainDefinitionMap.put(s, "authc");
        }
        String logout = PropertiesUtil.getProperty("serverIp.properties", "logout");
        filterChainDefinitionMap.put(logout, "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        // region actuator安全加固：health端点可匿名（供K8s健康检查），其他端点需认证
        filterChainDefinitionMap.put("/actuator/health", "anon");
        filterChainDefinitionMap.put("/actuator/**", "authc");
        // endregion
        // region websocket安全加固：WebSocket握手需JWT认证（Token通过URL参数传递，WebSocket URL不会出现在Referer中）
        filterChainDefinitionMap.put("/websocket/**", "authc");
        // endregion


        factoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return factoryBean;
    }

    private InvalidRequestFilter invalidRequestFilter() {
        InvalidRequestFilter invalidRequestFilter = new InvalidRequestFilter();
        // 安全加固：启用分号拦截，防止路径参数注入攻击（如 /path;jsessionid=xxx）
        invalidRequestFilter.setBlockSemicolon(true);
        return invalidRequestFilter;
    }

    @Bean
    public UserRealm userRealm() {
        return new UserRealm();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Lazy DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * 安全加固：CORS配置统一从 ProjectProperties 读取，与 JwtFilter 的 isAllowedOrigin 保持一致
     * origin 白名单通过配置 project.cors-allowed-origins 设置，多个用逗号分隔
     * 未配置时默认允许所有（兼容已有部署），生产环境建议配置具体的域名白名单
     */
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String allowedOrigins = projectProperties.getCorsAllowedOrigins();
        if (StringUtils.isBlank(allowedOrigins) || "*".equals(allowedOrigins.trim())) {
            // 未配置或配置为 * 时，允许所有 origin（兼容已有部署，生产环境建议配置具体域名）
            corsConfiguration.addAllowedOriginPattern("*");
        } else {
            Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedOrigins.split(",")));
            for (String origin : allowedSet) {
                corsConfiguration.addAllowedOriginPattern(origin.trim());
            }
        }
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }
}
