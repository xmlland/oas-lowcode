package com.jeestudio.bpm.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * @Description: 虚拟线程配置类
 * 为 Tomcat 启用虚拟线程执行器，替代传统的线程池
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * 配置 Tomcat 使用虚拟线程处理请求
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
