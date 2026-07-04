package com.jeestudio.bpm.storage.config;

import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.provider.CosStorageProvider;
import com.jeestudio.bpm.storage.provider.LocalStorageProvider;
import com.jeestudio.bpm.storage.provider.MinioStorageProvider;
import com.jeestudio.bpm.storage.provider.OssStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 文件存储自动配置
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@ComponentScan("com.jeestudio.bpm.storage")
public class StorageAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StorageAutoConfiguration.class);

    /**
     * 注册 MinIO 存储提供商
     * 当 storage.minio.enabled=true 时启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "minioStorageProvider")
    public IStorageProvider minioStorageProvider(StorageProperties properties) {
        StorageProperties.MinioConfig minio = properties.getMinio();
        // 安全加固：校验MinIO凭据不能为空或使用默认弱凭据
        String accessKey = minio.getAccessKey();
        String secretKey = minio.getSecretKey();
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException(
                    "MinIO认证凭据未配置！请设置环境变量 MINIO_ACCESS_KEY 和 MINIO_SECRET_KEY");
        }
        if ("minioadmin".equals(accessKey) && "minioadmin".equals(secretKey)) {
            throw new IllegalStateException(
                    "MinIO使用了默认弱凭据 minioadmin/minioadmin，请更换为强随机凭据");
        }
        log.info("注册 MinIO 存储提供商, endpoint: {}", minio.getEndpoint());
        return new MinioStorageProvider(minio);
    }

    /**
     * 注册阿里云 OSS 存储提供商
     * 当 storage.oss.enabled=true 时启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "storage.oss", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "ossStorageProvider")
    public IStorageProvider ossStorageProvider(StorageProperties properties) {
        log.info("注册阿里云 OSS 存储提供商, endpoint: {}", properties.getOss().getEndpoint());
        return new OssStorageProvider(properties.getOss());
    }

    /**
     * 注册腾讯云 COS 存储提供商
     * 当 storage.cos.enabled=true 时启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "storage.cos", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "cosStorageProvider")
    public IStorageProvider cosStorageProvider(StorageProperties properties) {
        log.info("注册腾讯云 COS 存储提供商, region: {}", properties.getCos().getRegion());
        return new CosStorageProvider(properties.getCos());
    }

    /**
     * 注册本地文件系统存储提供商
     * 当 storage.local.enabled=true 时启用（默认启用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "storage.local", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "localStorageProvider")
    public IStorageProvider localStorageProvider(StorageProperties properties) {
        log.info("注册本地文件系统存储提供商, rootPath: {}", properties.getLocal().getRootPath());
        return new LocalStorageProvider(properties.getLocal());
    }
}
