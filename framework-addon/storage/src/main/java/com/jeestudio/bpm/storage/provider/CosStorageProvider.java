package com.jeestudio.bpm.storage.provider;

import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.config.StorageProperties;
import com.jeestudio.bpm.storage.exception.StorageException;
import com.jeestudio.bpm.utils.FileNameEncoder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

/**
 * @Description: 腾讯云COS文件存储服务商
 * 基于腾讯云 COS SDK 实现文件存储操作。
 */
public class CosStorageProvider implements IStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(CosStorageProvider.class);

    private final StorageProperties.CosConfig config;

    public CosStorageProvider(StorageProperties.CosConfig config) {
        this.config = config;
    }

    /**
     * 创建 COS 客户端
     */
    private COSClient createCosClient() {
        COSCredentials credentials = new BasicCOSCredentials(config.getSecretId(), config.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(config.getRegion()));
        return new COSClient(credentials, clientConfig);
    }

    @Override
    public String getType() {
        return "cos";
    }

    @Override
    public String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        String key = folder + "/" + fileName;
        
        COSClient cosClient = null;
        try {
            cosClient = createCosClient();
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentType != null && !contentType.isEmpty()) {
                metadata.setContentType(contentType);
            }
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata);
            cosClient.putObject(putObjectRequest);
            inputStream.close();
            
            // 生成访问 URL
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 10000);
            URL url = cosClient.generatePresignedUrl(bucket, key, expiration);
            
            if (url == null) {
                throw new StorageException("COS上传后生成URL失败");
            }
            
            // 返回去掉签名参数的公开 URL
            String cosUrl = url.toString();
            cosUrl = cosUrl.substring(0, cosUrl.indexOf("?")).replaceFirst("http:", "https:");
            
            log.info("COS文件上传成功, bucket: {}, key: {}", bucket, key);
            return cosUrl;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("COS文件上传失败, bucket: {}, key: {}", bucket, key, e);
            throw new StorageException("COS文件上传失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response) {
        String bucket = config.getBucket();
        String objectKey = filePath;
        
        // 从 URL 中提取 key（如果是完整 URL）
        if (filePath.contains("myqcloud.com/")) {
            objectKey = filePath.substring(filePath.indexOf("myqcloud.com/") + 13);
            if (objectKey.contains("?")) {
                objectKey = objectKey.substring(0, objectKey.indexOf("?"));
            }
        }
        
        COSClient cosClient = null;
        try {
            cosClient = createCosClient();
            COSObject cosObject = cosClient.getObject(bucket, objectKey);
            InputStream inputStream = cosObject.getObjectContent();
            
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
            
            log.info("COS文件下载成功, bucket: {}, objectKey: {}", bucket, objectKey);
        } catch (Exception e) {
            log.error("COS文件下载失败, bucket: {}, objectKey: {}", bucket, objectKey, e);
            throw new StorageException("COS文件下载失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
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
        if (filePath.contains("myqcloud.com/")) {
            objectKey = filePath.substring(filePath.indexOf("myqcloud.com/") + 13);
            if (objectKey.contains("?")) {
                objectKey = objectKey.substring(0, objectKey.indexOf("?"));
            }
        }
        
        COSClient cosClient = null;
        try {
            cosClient = createCosClient();
            cosClient.deleteObject(bucket, objectKey);
            log.info("COS文件删除成功, bucket: {}, objectKey: {}", bucket, objectKey);
            return true;
        } catch (Exception e) {
            log.error("COS文件删除失败, bucket: {}, objectKey: {}", bucket, objectKey, e);
            return false;
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public boolean exists(String bucket, String filePath) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        COSClient cosClient = null;
        try {
            cosClient = createCosClient();
            boolean exists = cosClient.doesObjectExist(bucket, filePath);
            log.debug("COS检查文件存在, bucket: {}, filePath: {}, exists: {}", bucket, filePath, exists);
            return exists;
        } catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            log.error("COS检查文件是否存在失败, bucket: {}, filePath: {}", bucket, filePath, e);
            throw new StorageException("COS检查文件是否存在失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("COS检查文件是否存在失败, bucket: {}, filePath: {}", bucket, filePath, e);
            throw new StorageException("COS检查文件是否存在失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String filePath, int expirySeconds) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = config.getBucket();
        }
        
        COSClient cosClient = null;
        try {
            cosClient = createCosClient();
            Date expiration = new Date(System.currentTimeMillis() + expirySeconds * 1000L);
            URL url = cosClient.generatePresignedUrl(bucket, filePath, expiration);
            
            if (url == null) {
                throw new StorageException("生成COS预签名URL失败");
            }
            
            String presignedUrl = url.toString();
            log.debug("生成COS预签名URL成功, bucket: {}, filePath: {}, expiry: {}s", bucket, filePath, expirySeconds);
            return presignedUrl;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成COS预签名URL失败, bucket: {}, filePath: {}", bucket, filePath, e);
            throw new StorageException("生成COS预签名URL失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }
}
