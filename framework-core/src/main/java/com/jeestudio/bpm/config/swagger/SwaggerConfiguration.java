package com.jeestudio.bpm.config.swagger;

import com.jeestudio.bpm.config.ProjectProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: OpenAPI接口文档配置
 */
@Configuration
@ConditionalOnProperty(name = "project.swagger-enable", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {
    @Autowired
    ProjectProperties projectProperties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OAS 接口文档")
                        .description("OAS 办公自动化系统接口文档")
                        .version("1.0"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("bpm")
                .packagesToScan("com.jeestudio.bpm.controller")
                .build();
    }
}
