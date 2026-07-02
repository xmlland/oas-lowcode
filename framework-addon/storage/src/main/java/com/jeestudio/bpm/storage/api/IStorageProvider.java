package com.jeestudio.bpm.storage.api;

import java.io.InputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description: 文件存储服务商接口
 * 定义本地存储、MinIO、OSS、COS 等实现需要提供的统一文件操作能力。
 */
public interface IStorageProvider {

    /**
     * 获取提供商类型标识
     *
     * @return 类型标识，如 "minio", "oss", "cos", "local"
     */
    String getType();

    /**
     * 上传文件
     *
     * @param bucket      存储桶名称
     * @param folder      文件夹路径（支持日期格式如 yyyy/MM/dd）
     * @param fileName    文件名
     * @param inputStream 文件流
     * @param contentType MIME 类型
     * @return 可访问的文件 URL
     */
    String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType);

    /**
     * 下载文件到 HTTP 响应
     *
     * @param filePath    文件路径
     * @param displayName 下载时显示的文件名
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     */
    void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response);

    /**
     * 删除文件
     *
     * @param bucket   存储桶名称
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean delete(String bucket, String filePath);

    /**
     * 判断文件是否存在
     *
     * @param bucket   存储桶名称
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String bucket, String filePath);

    /**
     * 获取预签名访问 URL
     *
     * @param bucket        存储桶名称
     * @param filePath      文件路径
     * @param expirySeconds 过期时间（秒）
     * @return 预签名 URL
     */
    String getPresignedUrl(String bucket, String filePath, int expirySeconds);
}
