package com.jeestudio.bpm.config.web;

import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.security.PasswordEncryptHandler;
import com.jeestudio.bpm.security.pwd.ValidateUserPasswordHandler;
import com.jeestudio.bpm.security.storage.IntegrityHandler;
import com.jeestudio.bpm.security.storage.SecretHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 安全过滤器配置
 */
@Configuration
public class SecurityConfig {

    @Autowired
    ProjectProperties projectProperties;

    @Bean
    PasswordEncryptHandler passwordEncryptHandler() throws InstantiationException, IllegalAccessException {
        return projectProperties.getPasswordEncryptHandler().newInstance();
    }

    @Bean
    SecretHandler secretHandler() throws InstantiationException, IllegalAccessException {
        return projectProperties.getSecretHandler().newInstance();
    }

    @Bean
    IntegrityHandler integrityHandler() throws InstantiationException, IllegalAccessException {
        return projectProperties.getIntegrityHandler().newInstance();
    }

    @Bean
    ValidateUserPasswordHandler validateUserPasswordHandler() throws InstantiationException, IllegalAccessException {
        return projectProperties.getCustomValidateUserPasswordHandler().newInstance();
    }
}
