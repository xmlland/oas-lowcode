package com.jeestudio.bpm.storage.manager;

import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.api.StorageManager;
import com.jeestudio.bpm.storage.config.StorageProperties;
import com.jeestudio.bpm.storage.exception.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 默认文件存储管理器
 * 根据配置路由到本地、MinIO、OSS、COS 等具体存储服务商。
 */
@Service
public class DefaultStorageManager implements StorageManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultStorageManager.class);

    private final StorageProperties storageProperties;

    private final Map<String, IStorageProvider> providerMap = new ConcurrentHashMap<>();

    @Autowired
    public DefaultStorageManager(StorageProperties storageProperties,
                                  @Autowired(required = false) List<IStorageProvider> providers) {
        this.storageProperties = storageProperties;

        // 注册所有可用的存储提供商
        if (providers != null && !providers.isEmpty()) {
            for (IStorageProvider provider : providers) {
                providerMap.put(provider.getType(), provider);
                logger.info("注册存储提供商: {}", provider.getType());
            }
        } else {
            logger.warn("未发现任何存储提供商实现，请确保至少配置了一种存储后端");
        }
    }

    @Override
    public String upload(String folder, String fileName, InputStream inputStream, String contentType) {
        IStorageProvider provider = getDefaultProvider();
        String bucket = getDefaultBucket(provider.getType());
        String datePath = formatDatePath();
        String fullFolder = folder + datePath;
        return provider.upload(bucket, fullFolder, fileName, inputStream, contentType);
    }

    @Override
    public String upload(String providerType, String bucket, String folder, String fileName,
                         InputStream inputStream, String contentType) {
        IStorageProvider provider = getProvider(providerType);
        return provider.upload(bucket, folder, fileName, inputStream, contentType);
    }

    @Override
    public void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response) {
        IStorageProvider provider = getDefaultProvider();
        provider.download(filePath, displayName, request, response);
    }

    @Override
    public boolean delete(String filePath) {
        IStorageProvider provider = getDefaultProvider();
        String bucket = getDefaultBucket(provider.getType());
        return provider.delete(bucket, filePath);
    }

    @Override
    public IStorageProvider getProvider(String type) {
        IStorageProvider provider = providerMap.get(type);
        if (provider == null) {
            throw new StorageException("未找到存储提供商: " + type + "，可用的提供商: " + providerMap.keySet());
        }
        return provider;
    }

    @Override
    public IStorageProvider getDefaultProvider() {
        String defaultType = storageProperties.getDefaultProvider();
        IStorageProvider provider = providerMap.get(defaultType);
        if (provider == null) {
            if (providerMap.isEmpty()) {
                throw new StorageException("未配置任何存储提供商，请检查配置或确保至少有一个 IStorageProvider 实现被注册");
            }
            // 回退到第一个可用的提供商
            provider = providerMap.values().iterator().next();
            logger.warn("默认存储提供商 '{}' 不可用，回退使用: {}", defaultType, provider.getType());
        }
        return provider;
    }

    /**
     * 格式化日期路径
     */
    private String formatDatePath() {
        String format = storageProperties.getUploadPathFormat();
        if (format == null || format.isEmpty()) {
            return "";
        }
        // 移除首尾的斜杠，避免路径拼接问题
        format = format.replaceAll("^/+|/+$", "");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    /**
     * 获取默认存储桶
     */
    private String getDefaultBucket(String providerType) {
        switch (providerType) {
            case "minio":
                return storageProperties.getMinio().getBucket();
            case "oss":
                return storageProperties.getOss().getBucket();
            case "cos":
                return storageProperties.getCos().getBucket();
            case "local":
            default:
                return storageProperties.getLocal().getUploadFolder();
        }
    }
}
