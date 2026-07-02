package com.jeestudio.bpm.service.secLog;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * @Description: 标准输出安全日志Appender
 */
public class StdOutSecLogAppender implements SecLogAppender {
    @Override
    public void accept(String threadName, String id, String account, long startTime, long endTime, long costTime, String requestURI, String content, String ip, String type, LogResult result, Map<String, String[]> requestParam) {

        System.out.println("StdOutSecLogAppender ============ start");
        System.out.println("threadName: " +threadName);
        System.out.println("account: " + account);
        System.out.println("startTime: " + DateUtil.format(new Date(startTime), "yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println("endTime: " + DateUtil.format(new Date(endTime), "yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.println("costTime: " + costTime);
        System.out.println("requestURI: " + requestURI);
        System.out.println("content: " + content);
        System.out.println("ip: " + ip);
        System.out.println("type: " + type);
        System.out.println("result: " + result);
        JSONObject requestPackage = new JSONObject();
        requestPackage.putAll(requestParam);
        System.out.println("requestParam: " + requestPackage);
        System.out.println("StdOutSecLogAppender ============ end");

    }
}
