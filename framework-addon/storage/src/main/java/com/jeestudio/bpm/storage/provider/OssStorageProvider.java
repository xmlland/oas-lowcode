package com.jeestudio.bpm.storage.provider;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.config.StorageProperties;
import com.jeestudio.bpm.storage.exception.StorageException;
import com.jeestudio.bpm.utils.FileNameEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

/**
 * @Description: 阿里云OSS文件存储服务商
 * 基于阿里云 OSS SDK 实现文件存储操作。
 */
public class OssStorageProvider implements IStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(OssStorageProvider.class);

    private final StorageProperties.OssConfig config;

    public OssStorageProvider(StorageProperties.OssConfig config) {
        this.config = config;
    }

    /**
     * 创建 OSS 客户端
     */
    private OSSClient createOssClient() {
        return new OSSClient(
                config.getEndpoint(),
                config.getAccessKey(),
                config.getSecretKey()
        );
    }

    @Override
    public String getType() {
        return "oss";
    }

    @Override
    public String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        String key = folder + "/" + fileName;
        
        OSSClient ossClient = null;
        try {
            ossClient = createOssClient();
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentType != null && !contentType.isEmpty()) {
                metadata.setContentType(contentType);
            }
            
            ossClient.putObject(bucket, key, inputStream, metadata);
            inputStream.close();
            
            // 生成访问 URL
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 10000);
            URL url = ossClient.generatePresignedUrl(bucket, key, expiration);
            
            if (url == null) {
                throw new StorageException("OSS上传后生成URL失败");
            }
            
            // 返回去掉签名参数的公开 URL
            String ossUrl = url.toString();
            ossUrl = ossUrl.substring(0, ossUrl.indexOf("?")).replaceFirst("http:", "https:");
            
            log.info("OSS文件上传成功, bucket: {}, key: {}", bucket, key);
            return ossUrl;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS文件上传失败, bucket: {}, key: {}", bucket, key, e);
            throw new StorageException("OSS文件上传失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response) {
        String bucket = config.getBucket();
        String objectKey = filePath;
        
        // 从 URL 中提取 key（如果是完整 URL）
        if (filePath.contains("aliyuncs.com/")) {
            objectKey = filePath.substring(filePath.indexOf("aliyuncs.com/") + 13);
            if (objectKey.contains("?")) {
                objectKey = objectKey.substring(0, objectKey.indexOf("?"));
            }
        }
        
        OSSClient ossClient = null;
        try {
            ossClient = createOssClient();
            OSSObject ossObject = ossClient.getObject(bucket, objectKey);
            InputStream inputStream = ossObject.getObjectContent();
            
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + FileNameEncoder.encode(request.getHeader("User-Agent"), displayName));
            
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, n);
            }
            outputStream.flush();
            inputStream.close();
            
            log.info("OSS文件下载成功, bucket: {}, objectKey: {}", bucket, objectKey);
        } catch (Exception e) {
            log.error("OSS文件下载失败, bucket: {}, objectKey: {}", bucket, objectKey, e);
            throw new StorageException("OSS文件下载失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public boolean delete(String bucket, String filePath) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        String objectKey = filePath;
        // 从 URL 中提取 key
        if (filePath.contains("aliyuncs.com/")) {
            objectKey = filePath.substring(filePath.indexOf("aliyuncs.com/") + 13);
            if (objectKey.contains("?")) {
                objectKey = objectKey.substring(0, objectKey.indexOf("?"));
            }
        }
        
        OSSClient ossClient = null;
        try {
            ossClient = createOssClient();
            ossClient.deleteObject(bucket, objectKey);
            log.info("OSS文件删除成功, bucket: {}, objectKey: {}", bucket, objectKey);
            return true;
        } catch (Exception e) {
            log.error("OSS文件删除失败, bucket: {}, objectKey: {}", bucket, objectKey, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public boolean exists(String bucket, String filePath) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        OSSClient ossClient = null;
        try {
            ossClient = createOssClient();
            boolean exists = ossClient.doesObjectExist(bucket, filePath);
            log.debug("OSS检查文件存在, bucket: {}, filePath: {}, exists: {}", bucket, filePath, exists);
            return exists;
        } catch (Exception e) {
            log.error("OSS检查文件是否存在失败, bucket: {}, filePath: {}", bucket, filePath, e);
            throw new StorageException("OSS检查文件是否存在失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String filePath, int expirySeconds) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        OSSClient ossClient = null;
        try {
            ossClient = createOssClient();
            Date expiration = new Date(System.currentTimeMillis() + expirySeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(bucket, filePath, expiration);
            
            if (url == null) {
                throw new StorageException("生成OSS预签名URL失败");
            }
            
            String presignedUrl = url.toString();
            log.debug("生成OSS预签名URL成功, bucket: {}, filePath: {}, expiry: {}s", bucket, filePath, expirySeconds);
            return presignedUrl;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成OSS预签名URL失败, bucket: {}, filePath: {}", bucket, filePath, e);
            throw new StorageException("生成OSS预签名URL失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
