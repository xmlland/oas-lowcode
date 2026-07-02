package com.jeestudio.bpm.config.web;

import com.jeestudio.bpm.BpmApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @Description: WAR部署启动初始化器
 */
public class ApplicationServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // 这里放入springboot的启动类
        return application.sources(BpmApplication.class);
    }
}
