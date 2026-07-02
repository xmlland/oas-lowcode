package com.jeestudio.bpm.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * @Description: 配置文件工具
 */
public class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    public static String getProperty(String classPath, String key) {
        String value = "";
        Properties properties = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classPath);
        try {
            properties.load(in);
            value = properties.getProperty(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception err) {
                    logger.error(ExceptionUtils.getStackTrace(err));
                }
            }
        }
        return value;
    }
}
