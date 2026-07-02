package com.jeestudio.bpm.service.secLog;

import java.util.Map;

/**
 * @Description: 安全日志Appender接口
 */
public interface SecLogAppender {

    enum LogResult {
        SUCCESS, FAIL
    }

    void accept(String threadName, String id, String account, long startTime, long endTime, long costTime, String requestURI, String content, String ip, String type, LogResult result, Map<String, String[]> requestParam);
}
