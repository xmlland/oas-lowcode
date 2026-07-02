package com.jeestudio.bpm.storage.provider;

import com.jeestudio.bpm.storage.api.IStorageProvider;
import com.jeestudio.bpm.storage.config.StorageProperties;
import com.jeestudio.bpm.storage.exception.StorageException;
import com.jeestudio.bpm.utils.FileNameEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @Description: 本地文件存储服务商
 * 将文件保存到服务器本地目录，适用于开发环境和轻量部署。
 */
public class LocalStorageProvider implements IStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageProvider.class);

    private final StorageProperties.LocalConfig config;

    public LocalStorageProvider(StorageProperties.LocalConfig config) {
        this.config = config;
    }

    @Override
    public String getType() {
        return "local";
    }

    @Override
    public String upload(String bucket, String folder, String fileName, InputStream inputStream, String contentType) {
        // 安全加固：校验文件名和路径，防止路径穿越攻击
        validateFileName(fileName);
        validatePathComponent(folder);
        if (bucket != null && !bucket.isEmpty()) {
            validatePathComponent(bucket);
        }
        
        // 本地存储中 bucket 当作子目录使用
        String basePath = config.getRootPath();
        if (bucket != null && !bucket.isEmpty()) {
            basePath = basePath + File.separator + bucket;
        } else {
            basePath = basePath + config.getUploadFolder();
        }
        
        // 构建完整路径
        String fullPath = basePath + File.separator + folder + File.separator + fileName;
        fullPath = fullPath.replace("/", File.separator).replace("\\\\", File.separator);
        
        // 路径规范化校验，防止路径穿越
        fullPath = validateAndNormalizePath(config.getRootPath(), fullPath);
        
        File targetFile = new File(fullPath);
        
        try {
            // 确保父目录存在
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    throw new StorageException("无法创建目录: " + parentDir.getAbsolutePath());
                }
            }
            
            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
            }
            inputStream.close();
            
            log.info("本地文件上传成功, path: {}", targetFile.getAbsolutePath());
            
            // 返回相对路径（基于 bucket 或 uploadFolder）
            String relativePath;
            if (bucket != null && !bucket.isEmpty()) {
                relativePath = "/" + bucket + "/" + folder + "/" + fileName;
            } else {
                relativePath = config.getUploadFolder() + "/" + folder + "/" + fileName;
            }
            // 统一使用正斜杠
            return relativePath.replace("\\", "/").replaceAll("/+", "/");
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("本地文件上传失败, path: {}", targetFile.getAbsolutePath(), e);
            throw new StorageException("本地文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void download(String filePath, String displayName, HttpServletRequest request, HttpServletResponse response) {
        // 安全加固：校验路径，防止路径穿越攻击
        validatePathComponent(filePath);
        
        // 构建完整路径
        String fullPath = config.getRootPath() + File.separator + filePath;
        fullPath = fullPath.replace("/", File.separator).replace("\\\\", File.separator);
        
        // 路径规范化校验
        fullPath = validateAndNormalizePath(config.getRootPath(), fullPath);
        
        File file = new File(fullPath);
        
        if (!file.exists()) {
            log.error("本地文件不存在, path: {}", fullPath);
            throw new StorageException("文件不存在: " + filePath);
        }
        
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + FileNameEncoder.encode(request.getHeader("User-Agent"), displayName));
            
            try (FileInputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
                outputStream.flush();
            }
            
            log.info("本地文件下载成功, path: {}", fullPath);
        } catch (Exception e) {
            log.error("本地文件下载失败, path: {}", fullPath, e);
            throw new StorageException("本地文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String bucket, String filePath) {
        // 安全加固：校验路径，防止路径穿越攻击
        validatePathComponent(filePath);
        if (bucket != null && !bucket.isEmpty()) {
            validatePathComponent(bucket);
        }
        
        // 构建完整路径
        String basePath = config.getRootPath();
        if (bucket != null && !bucket.isEmpty()) {
            basePath = basePath + File.separator + bucket;
        }
        
        String fullPath = basePath + File.separator + filePath;
        fullPath = fullPath.replace("/", File.separator).replace("\\\\", File.separator);
        
        // 路径规范化校验
        fullPath = validateAndNormalizePath(config.getRootPath(), fullPath);
        
        File file = new File(fullPath);
        
        if (!file.exists()) {
            log.warn("本地文件不存在，无需删除, path: {}", fullPath);
            return true;
        }
        
        try {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("本地文件删除成功, path: {}", fullPath);
            } else {
                log.warn("本地文件删除失败, path: {}", fullPath);
            }
            return deleted;
        } catch (Exception e) {
            log.error("本地文件删除失败, path: {}", fullPath, e);
            return false;
        }
    }

    @Override
    public boolean exists(String bucket, String filePath) {
        // 安全加固：校验路径，防止路径穿越攻击
        validatePathComponent(filePath);
        if (bucket != null && !bucket.isEmpty()) {
            validatePathComponent(bucket);
        }
        
        // 构建完整路径
        String basePath = config.getRootPath();
        if (bucket != null && !bucket.isEmpty()) {
            basePath = basePath + File.separator + bucket;
        }
        
        String fullPath = basePath + File.separator + filePath;
        fullPath = fullPath.replace("/", File.separator).replace("\\\\", File.separator);
        
        // 路径规范化校验
        fullPath = validateAndNormalizePath(config.getRootPath(), fullPath);
        
        File file = new File(fullPath);
        boolean exists = file.exists() && file.isFile();
        log.debug("本地文件存在检查, path: {}, exists: {}", fullPath, exists);
        return exists;
    }

    @Override
    public String getPresignedUrl(String bucket, String filePath, int expirySeconds) {
        // 本地存储不支持预签名 URL，直接返回相对路径
        log.warn("本地存储不支持预签名URL，返回相对路径");
        
        String relativePath;
        if (bucket != null && !bucket.isEmpty()) {
            relativePath = "/" + bucket + "/" + filePath;
        } else {
            relativePath = config.getUploadFolder() + "/" + filePath;
        }
        // 统一使用正斜杠
        return relativePath.replace("\\", "/").replaceAll("/+", "/");
    }

    /**
     * 校验路径组件不包含路径穿越字符（如 .. 或 /）
     */
    private void validatePathComponent(String component) {
        if (component == null) {
            return;
        }
        if (component.contains("..") || component.contains(File.separator) || component.contains("/")) {
            log.error("检测到路径穿越攻击, component: {}", component);
            throw new StorageException("检测到非法的路径组件: " + component);
        }
    }

    /**
     * 校验文件名仅包含合法字符（字母、数字、中文、点、下划线、横线、空格、括号）
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new StorageException("文件名不能为空");
        }
        // 允许中文、字母、数字、点、下划线、横线、空格、小括号
        if (!fileName.matches("[a-zA-Z0-9._\\-\\u4e00-\\u9fa5 ()\\[\\]]+")) {
            log.error("文件名包含非法字符: {}", fileName);
            throw new StorageException("文件名包含非法字符: " + fileName);
        }
    }

    /**
     * 校验并规范化完整路径，确保路径在根目录范围内，防止路径穿越
     */
    private String validateAndNormalizePath(String rootPath, String fullPath) {
        try {
            Path rootDir = Paths.get(rootPath).toRealPath();
            Path resolvedPath = Paths.get(fullPath).normalize().toAbsolutePath();
            
            // 检查规范化后的路径是否仍在根目录子树内
            if (!resolvedPath.startsWith(rootDir)) {
                log.error("检测到路径穿越攻击, root: {}, resolved: {}", rootDir, resolvedPath);
                throw new StorageException("检测到非法的文件路径，路径不在允许的范围内");
            }
            return resolvedPath.toString();
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("路径校验失败, root: {}, fullPath: {}", rootPath, fullPath, e);
            throw new StorageException("文件路径校验失败: " + e.getMessage(), e);
        }
    }
}
