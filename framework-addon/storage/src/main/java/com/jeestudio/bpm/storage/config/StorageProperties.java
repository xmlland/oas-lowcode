package com.jeestudio.bpm.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: 文件存储配置属性
 */
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 默认存储提供商类型
     */
    private String defaultProvider = "local";

    /**
     * 上传路径日期格式
     */
    private String uploadPathFormat = "/yyyy/MM/dd/";

    /**
     * MinIO 配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云 OSS 配置
     */
    private OssConfig oss = new OssConfig();

    /**
     * 腾讯云 COS 配置
     */
    private CosConfig cos = new CosConfig();

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public String getUploadPathFormat() {
        return uploadPathFormat;
    }

    public void setUploadPathFormat(String uploadPathFormat) {
        this.uploadPathFormat = uploadPathFormat;
    }

    public MinioConfig getMinio() {
        return minio;
    }

    public void setMinio(MinioConfig minio) {
        this.minio = minio;
    }

    public OssConfig getOss() {
        return oss;
    }

    public void setOss(OssConfig oss) {
        this.oss = oss;
    }

    public CosConfig getCos() {
        return cos;
    }

    public void setCos(CosConfig cos) {
        this.cos = cos;
    }

    public LocalConfig getLocal() {
        return local;
    }

    public void setLocal(LocalConfig local) {
        this.local = local;
    }

    /**
     * MinIO 配置
     */
    public static class MinioConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 服务端点
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 默认存储桶
         */
        private String bucket;

        /**
         * 缩略图存储桶
         */
        private String thumbBucket = "thumb";

        /**
         * 对外公开的 MinIO 访问地址（用于生成浏览器可访问的预签名 URL）
         * 为空时使用 endpoint 原值（适用于浏览器可直接访问 MinIO 的场景）
         * Docker 部署时通常设为 /minio（通过 nginx 反向代理访问）
         */
        private String publicEndpoint;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getThumbBucket() {
            return thumbBucket;
        }

        public void setThumbBucket(String thumbBucket) {
            this.thumbBucket = thumbBucket;
        }

        public String getPublicEndpoint() {
            return publicEndpoint;
        }

        public void setPublicEndpoint(String publicEndpoint) {
            this.publicEndpoint = publicEndpoint;
        }
    }

    /**
     * 阿里云 OSS 配置
     */
    public static class OssConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 服务端点
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 默认存储桶
         */
        private String bucket;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    /**
     * 腾讯云 COS 配置
     */
    public static class CosConfig {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 应用 ID
         */
        private String appId;

        /**
         * 密钥 ID
         */
        private String secretId;

        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 存储桶
         */
        private String bucket;

        /**
         * 区域
         */
        private String region;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSecretId() {
            return secretId;
        }

        public void setSecretId(String secretId) {
            this.secretId = secretId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }
    }

    /**
     * 本地存储配置
     */
    public static class LocalConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 根路径
         */
        private String rootPath = "./files";

        /**
         * 上传文件夹
         */
        private String uploadFolder = "/userfiles";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRootPath() {
            return rootPath;
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }

        public String getUploadFolder() {
            return uploadFolder;
        }

        public void setUploadFolder(String uploadFolder) {
            this.uploadFolder = uploadFolder;
        }
    }
}
