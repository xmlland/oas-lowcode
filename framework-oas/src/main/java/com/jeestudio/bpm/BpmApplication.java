package com.jeestudio.bpm;

import com.jeestudio.bpm.feign.WordExportFeignClient;
import com.jeestudio.bpm.feign.WordUploadFeignClient;
import com.jeestudio.bpm.utils.SpringContextHolder;
import com.jeestudio.tools.dict.DictHandler;
import com.jeestudio.tools.excel.ExcelUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Description: 后端应用启动类
 */
@EnableFeignClients(clients = {WordExportFeignClient.class, WordUploadFeignClient.class})
@EnableAsync
@EnableTransactionManagement
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        GroovyTemplateAutoConfiguration.class,
        org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.class,
        org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration.class})
@EnableCaching
public class BpmApplication {

    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext applicationContext = SpringApplication.run(BpmApplication.class, args);
        SpringContextHolder springContextHolder = new SpringContextHolder();
        springContextHolder.setApplicationContext(applicationContext);
        Environment env = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        ExcelUtil.init(applicationContext.getBean(DictHandler.class));
        System.out.println("\n----------------------------------------------------------\n\t" +
                "Application is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui/index.html\n\t" +
                "----------------------------------------------------------");
    }
}
