package com.jeestudio.framework.wordexport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @Description: Word 导出服务启动类
 */
@SpringBootApplication
public class WordExportApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WordExportApplication.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(WordExportApplication.class, args);
    }

}
