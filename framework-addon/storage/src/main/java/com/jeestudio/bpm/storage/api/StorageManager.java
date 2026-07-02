package com.jeestudio.bpm.storage.api;

import java.io.InputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description: 文件存储管理接口
 * 面向业务层屏蔽不同存储服务商的上传、下载、删除等差异。
 */
public interface StorageManager {

    /**
     * 使用默认存储后端上传
     *
     * @param folder      文件夹路径
     * @param fileName    文件名
     * @param inputStream 文件流
     * @param contentType MIME 类型
     * @return 可访问的文件 URL
     */
    String upload(String folder, String fileName, InputStream inputStream, String contentType);

    /**
     * 使用指定存储后端上传
     *
     * @param providerType 存储提供商类型
     * @param bucket       存储桶名称
     * @param folder       文件夹路径
     * @param fileName     文件名
     * @param inputStream  文件流
     * @param contentType  MIME 类型
     * @return 可访问的文件 URL
     */
    String upload(String providerType, String bucket, String folder, String fileName, InputStream inputStream, String contentType);

    /**
     * 下载文件
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
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean delete(String filePath);

    /**
     * 获取指定类型的 Provider
     *
     * @param type 提供商类型
     * @return 存储提供商实例
     */
    IStorageProvider getProvider(String type);

    /**
     * 获取默认 Provider
     *
     * @return 默认存储提供商实例
     */
    IStorageProvider getDefaultProvider();
}
