package com.jeestudio.bpm.config.web;

import com.jeestudio.bpm.security.DecryptFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 过滤器配置
 */
@Configuration
public class FilterConfig {


    /**
     * 解密过滤器
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "project.transmittalEncryption", havingValue = "true")
    public FilterRegistrationBean<DecryptFilter> decryptFilter() {
        FilterRegistrationBean<DecryptFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DecryptFilter());
        registration.addUrlPatterns("/*"); // 设置Filter拦截的URL模式
        registration.setOrder(0); // 设置Filter的执行顺序 越小越先执行
        return registration;
    }
}
