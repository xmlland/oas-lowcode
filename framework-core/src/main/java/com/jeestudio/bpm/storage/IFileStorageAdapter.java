package com.jeestudio.bpm.storage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @Description: 文件存储适配器接口
 * <p>
 * framework-core 定义接口，storage 模块提供实现。
 * 通过 @Autowired(required=false) 注入，解决循环依赖问题。
 * </p>
 */
public interface IFileStorageAdapter {

    /**
     * 上传文件
     *
     * @param bucket      存储桶（可为null，使用默认）
     * @param folder      目录
     * @param fileName    文件名
     * @param inputStream 文件流
     * @param contentType 内容类型（可为null，自动推断）
     * @return 文件路径
     */
    String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType);

    /**
     * 下载文件
     *
     * @param bucket      存储桶
     * @param filePath    文件路径
     * @param displayName 下载显示名
     * @param request     HTTP请求
     * @param response    HTTP响应
     */
    void download(String bucket, String filePath, String displayName, HttpServletRequest request, HttpServletResponse response);

    /**
     * 删除文件
     *
     * @param bucket   存储桶
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean delete(String bucket, String filePath);

    /**
     * 获取预签名URL（内联预览）
     *
     * @param bucket        存储桶
     * @param filePath      文件路径
     * @param expirySeconds 过期时间（秒）
     * @return 预签名URL
     */
    String getPresignedUrl(String bucket, String filePath, int expirySeconds);

    /**
     * 检查文件是否存在
     *
     * @param bucket   存储桶
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String bucket, String filePath);
}
