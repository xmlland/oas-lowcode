package com.jeestudio.tools.security.utils;

import cn.hutool.core.convert.Convert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 文件校验工具
 */
public class FileValidUtil {
    /**
     * 上传contentType
     */
    private static List<String> contentTypes;

    /**
     * 允许上传类型
     */
    private static List<String> acceptFiles;

    static {
        init();
    }

    private static void init() {
        contentTypes = new ArrayList<>();
        acceptFiles = new ArrayList<>();
        contentTypes.add("application/msword");
        contentTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        contentTypes.add("application/vnd.ms-excel");
        contentTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        contentTypes.add("application/pdf");
        contentTypes.add("image/jpeg");
        contentTypes.add("image/png");
        contentTypes.add("text/plain");
        contentTypes.add("application/zip");
        contentTypes.add("video/mp4");
        contentTypes.add("video/webm");
        contentTypes.add("video/ogg");

        acceptFiles.add("doc");
        acceptFiles.add("docx");
        acceptFiles.add("xls");
        acceptFiles.add("xlsx");
        acceptFiles.add("pdf");
        acceptFiles.add("jpe");
        acceptFiles.add("jpeg");
        acceptFiles.add("jpg");
        acceptFiles.add("png");
        acceptFiles.add("txt");
        acceptFiles.add("zip");
        acceptFiles.add("rar");
        acceptFiles.add("mp4");
        acceptFiles.add("avi");
    }

    public static List<String> getContentTypes() {
        return contentTypes;
    }

    public static void setContentTypes(List<String> contentTypes) {
        FileValidUtil.contentTypes = contentTypes;
    }

    public static List<String> getAcceptFiles() {
        return acceptFiles;
    }

    public static void setAcceptFiles(List<String> acceptFiles) {
        FileValidUtil.acceptFiles = acceptFiles;
    }


    public static void addContentTypes(String contentType) {
        FileValidUtil.contentTypes.add(contentType);
    }


    public static void addAcceptFile(String acceptFile) {
        FileValidUtil.acceptFiles.add(acceptFile);
    }


    /**
     * 验证文件是否合法
     *
     * @param fileName    文件名  a.doc
     * @param contentType 上传类型 通过MultipartFile获取
     * @param bytes       文件内容 通过MultipartFile获取
     * @return
     */
    public static boolean validFile(String fileName, String contentType, byte[] bytes) {
        return validExtend(fileName) && validContentType(contentType) && validExtendByContent(bytes);
    }


    /**
     * 验证文件后缀
     *
     * @param fileName 文件名  a.doc
     * @return
     */
    public static boolean validExtend(String fileName) {
        String extend = Convert.toStr(fileName).toLowerCase();
        return acceptFiles.contains(extend);
    }

    /**
     * 验证上传类型
     *
     * @param contentType 上传类型 通过MultipartFile获取
     * @return
     */
    public static boolean validContentType(String contentType) {
        String content = Convert.toStr(contentType).toLowerCase();
        return contentTypes.contains(content);
    }


    /**
     * 验证文件内容
     *
     * @param bytes 文件内容 通过MultipartFile获取
     * @return
     */
    public static boolean validExtendByContent(byte[] bytes) {
        String fileType = FileTypeUtil.getFileTypeByStream(bytes);
        return acceptFiles.contains(fileType);
    }


    public static void main(String[] args) {


    }
}
