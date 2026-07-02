package com.jeestudio.bpm.storage.provider;

import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.config.StorageProperties;
import com.jeestudio.bpm.storage.exception.StorageException;
import com.jeestudio.bpm.utils.FileNameEncoder;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: MinIO文件存储服务商
 * 基于 MinIO SDK 实现文件上传、下载、删除和访问地址生成。
 */
public class MinioStorageProvider implements IStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageProvider.class);

    private final StorageProperties.MinioConfig config;
    private volatile MinioClient minioClient;

    public MinioStorageProvider(StorageProperties.MinioConfig config) {
        this.config = config;
    }

    /**
     * 获取或初始化 MinioClient
     */
    private MinioClient getMinioClient() {
        if (minioClient == null) {
            synchronized (this) {
                if (minioClient == null) {
                    minioClient = MinioClient.builder()
                            .endpoint(config.getEndpoint())
                            .credentials(config.getAccessKey(), config.getSecretKey())
                            .build();
                    log.info("MinIO客户端初始化成功，endpoint: {}", config.getEndpoint());
                }
            }
        }
        return minioClient;
    }

    @Override
    public String getType() {
        return "minio";
    }

    @Override
    public String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        String key = folder + "/" + fileName;
        
        try {
            // 自动推断 contentType
            if (contentType == null || contentType.isEmpty()) {
                contentType = inferContentType(fileName);
            }
            
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(inputStream, -1, 100 * 1048576) // 100MB 分块
                            .contentType(contentType)
                            .build());
            
            inputStream.close();
            log.info("MinIO文件上传成功, bucket: {}, key: {}", bucket, key);
            
            // 返回相对路径，用于私有对象存储
            return "/minio/" + bucket + "/" + key;
        } catch (Exception e) {
            log.error("MinIO文件上传失败, bucket: {}, key: {}", bucket, key, e);
            throw new StorageException("MinIO文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response) {
        String bucket = config.getBucket();
        String objectName = filePath;
        
        // 处理路径前缀
        if (filePath.startsWith("/minio/" + bucket + "/")) {
            objectName = filePath.replaceFirst("/minio/" + bucket + "/", "");
        } else if (filePath.startsWith("/minio/")) {
            // 路径可能包含不同的 bucket
            String[] parts = filePath.substring(7).split("/", 2);
            if (parts.length == 2) {
                bucket = parts[0];
                objectName = parts[1];
            }
        }

        try {
            InputStream inputStream = getMinioClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            
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
            log.info("MinIO文件下载成功, bucket: {}, objectName: {}", bucket, objectName);
        } catch (Exception e) {
            log.error("MinIO文件下载失败, bucket: {}, objectName: {}", bucket, objectName, e);
            throw new StorageException("MinIO文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String bucket, String filePath) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        // 处理路径前缀
        String objectName = filePath;
        if (filePath.startsWith("/minio/")) {
            String[] parts = filePath.substring(7).split("/", 2);
            if (parts.length == 2) {
                bucket = parts[0];
                objectName = parts[1];
            }
        }
        
        try {
            getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            log.info("MinIO文件删除成功, bucket: {}, objectName: {}", bucket, objectName);
            return true;
        } catch (Exception e) {
            log.error("MinIO文件删除失败, bucket: {}, objectName: {}", bucket, objectName, e);
            return false;
        }
    }

    @Override
    public boolean exists(String bucket, String filePath) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        // 处理数据库存储的 /minio/bucket/object 格式路径
        String objectName = filePath;
        if (filePath != null && filePath.startsWith("/minio/")) {
            String[] parts = filePath.substring(7).split("/", 2);
            if (parts.length == 2) {
                bucket = parts[0];
                objectName = parts[1];
            }
        }

        try {
            StatObjectResponse response = getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            return response != null;
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                return false;
            }
            log.error("MinIO检查文件是否存在失败, bucket: {}, objectName: {}", bucket, objectName, e);
            throw new StorageException("MinIO检查文件是否存在失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("MinIO检查文件是否存在失败, bucket: {}, objectName: {}", bucket, objectName, e);
            throw new StorageException("MinIO检查文件是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String filePath, int expirySeconds) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        // 处理数据库存储的 /minio/bucket/object 格式路径
        String objectName = filePath;
        if (filePath != null && filePath.startsWith("/minio/")) {
            String withoutPrefix = filePath.substring(7); // 去掉 "/minio/"
            String[] parts = withoutPrefix.split("/", 2);
            if (parts.length == 2) {
                bucket = parts[0];     // 从路径中提取 bucket
                objectName = parts[1]; // 提取真正的 object name
            }
        }

        try {
            // 推断 Content-Type
            String contentType = inferContentType(objectName);

            Map<String, String> extraQueryParams = new HashMap<>();
            extraQueryParams.put("response-content-disposition", "inline");
            extraQueryParams.put("response-content-type", contentType);

            String url = getMinioClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(expirySeconds)
                            .extraQueryParams(extraQueryParams)
                            .build());
            log.debug("生成MinIO预签名URL成功, bucket: {}, objectName: {}, expiry: {}s", bucket, objectName, expirySeconds);
            // 替换内部地址为公开地址（适配 Docker/nginx 代理场景）
            String publicEndpoint = config.getPublicEndpoint();
            if (publicEndpoint != null && !publicEndpoint.isEmpty()) {
                url = url.replace(config.getEndpoint(), publicEndpoint);
            }
            return url;
        } catch (Exception e) {
            log.error("生成MinIO预签名URL失败, bucket: {}, objectName: {}", bucket, objectName, e);
            throw new StorageException("生成MinIO预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文件名推断 ContentType
     */
    private String inferContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpeg") || lowerName.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".mpeg")) {
            return "audio/mpeg";
        } else if (lowerName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".json")) {
            return "application/json";
        } else if (lowerName.endsWith(".xml")) {
            return "application/xml";
        } else if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return "text/html";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }
}
