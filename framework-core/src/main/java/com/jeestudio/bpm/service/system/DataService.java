package com.jeestudio.bpm.service.system;

import cn.hutool.core.lang.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * @Description: 雪花ID生成服务
 */
@Service
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Value("${workId}")
    private String workId;

    @Value("${dataCenterId}")
    private String dataCenterId;

    private Snowflake snowflake;

    @PostConstruct
    private void initSnowflake() {
        long l1;
        long l2;
        try {
            l1 = Long.parseLong(workId);
        } catch (NumberFormatException e) {
            l1 = 1L;
            logger.warn("解析workId失败，使用默认值1", e);
        }
        try {
            l2 = Long.parseLong(dataCenterId);
        } catch (NumberFormatException e) {
            l2 = 1L;
            logger.warn("解析dataCenterId失败，使用默认值1", e);
        }
        snowflake = new Snowflake(l1, l2);
    }

    public String nextId() {
        return snowflake.nextIdStr();
    }
}
