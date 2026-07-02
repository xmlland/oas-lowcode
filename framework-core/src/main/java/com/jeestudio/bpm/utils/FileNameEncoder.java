package com.jeestudio.bpm.utils;

import jakarta.mail.internet.MimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Description: 文件名编码工具类
 * 处理不同浏览器下载文件时的文件名编码
 */
public class FileNameEncoder {

    private static final Logger log = LoggerFactory.getLogger(FileNameEncoder.class);

    /**
     * 根据浏览器 User-Agent 编码文件名
     * @param userAgent 浏览器 User-Agent
     * @param fileName 原始文件名
     * @return 编码后的文件名
     */
    public static String encode(String userAgent, String fileName) {
        userAgent = (userAgent == null ? "" : userAgent.toLowerCase());

        String rtn = new String();
        try {
            String new_filename = URLEncoder.encode(fileName, "UTF8");
            new_filename = new_filename.replaceAll("\\+", " ");
            if (userAgent != null) {
                userAgent = userAgent.toLowerCase();
                //IE
                if (userAgent.indexOf("msie") != -1) {
                    rtn = new_filename;
                }
                //Opera filename*
                else if (userAgent.indexOf("opera") != -1) {
                    rtn = "filename*=UTF-8''" + new_filename;
                }
                //Safari
                else if (userAgent.indexOf("safari") != -1) {
                    rtn = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
                }
                //Chrome
                else if (userAgent.indexOf("applewebkit") != -1) {
                    new_filename = MimeUtility.encodeText(fileName, "UTF8", "B");
                    rtn = new_filename;
                }
                //FireFox
                else if (userAgent.indexOf("firefox") != -1) {
                    rtn = "\"" + new String(fileName.getBytes("UTF-8"), "ISO8859-1") + "\"";
                } else {
                    rtn = new_filename;
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("获取下载文件名称错误", e);
        }
        return rtn;
    }
}
