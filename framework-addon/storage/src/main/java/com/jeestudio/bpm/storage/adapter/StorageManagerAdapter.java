package com.jeestudio.bpm.storage.adapter;

import com.jeestudio.bpm.storage.IFileStorageAdapter;
import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.api.StorageManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @Description: 文件存储管理适配器
 */
@Component
public class StorageManagerAdapter implements IFileStorageAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StorageManagerAdapter.class);

    private final StorageManager storageManager;

    public StorageManagerAdapter(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @Override
    public String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType) {
        if (bucket != null) {
            // 使用默认 provider 类型 + 指定 bucket
            IStorageProvider provider = storageManager.getDefaultProvider();
            return storageManager.upload(provider.getType(), bucket, folder, fileName, inputStream, contentType);
        }
        // 使用默认 provider + 默认 bucket
        return storageManager.upload(folder, fileName, inputStream, contentType);
    }

    @Override
    public void download(String bucket, String filePath, String displayName,
                         HttpServletRequest request, HttpServletResponse response) {
        // StorageManager.download 内部使用默认 provider 处理
        storageManager.download(filePath, displayName, request, response);
    }

    @Override
    public boolean delete(String bucket, String filePath) {
        return storageManager.delete(filePath);
    }

    @Override
    public String getPresignedUrl(String bucket, String filePath, int expirySeconds) {
        IStorageProvider provider = storageManager.getDefaultProvider();
        return provider.getPresignedUrl(bucket, filePath, expirySeconds);
    }

    @Override
    public boolean exists(String bucket, String filePath) {
        IStorageProvider provider = storageManager.getDefaultProvider();
        return provider.exists(bucket, filePath);
    }
}
