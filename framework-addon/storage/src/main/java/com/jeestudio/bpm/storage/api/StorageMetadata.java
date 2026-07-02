package com.jeestudio.bpm.storage.api;

/**
 * @Description: 文件存储元数据
 */
public class StorageMetadata {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private long size;

    /**
     * 可访问的 URL
     */
    private String url;

    /**
     * 存储桶名称
     */
    private String bucket;

    /**
     * 文件路径
     */
    private String path;

    public StorageMetadata() {
    }

    public StorageMetadata(String fileName, String contentType, long size, String url, String bucket, String path) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.url = url;
        this.bucket = bucket;
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "StorageMetadata{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", bucket='" + bucket + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
