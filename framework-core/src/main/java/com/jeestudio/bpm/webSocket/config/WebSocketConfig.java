package com.jeestudio.bpm.webSocket.config;

import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Description: WebSocket配置
 */
@Configuration
public class WebSocketConfig {

    /**
     * 显式注册Tomcat WebSocket初始化器，确保ServerContainer可用
     */
    @Bean
    public TomcatContextCustomizer wsInitializer() {
        return context -> context.addServletContainerInitializer(new WsSci(), null);
    }

    /**
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
