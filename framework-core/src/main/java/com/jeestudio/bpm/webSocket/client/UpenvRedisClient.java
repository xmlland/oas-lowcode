package com.jeestudio.bpm.webSocket.client;


import com.jeestudio.bpm.webSocket.BaseMap;
import com.jeestudio.bpm.webSocket.constant.GlobalConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;

/**
 * @Description: Redis客户端
 */
@Configuration
public class UpenvRedisClient {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 发送消息
     *
     * @param handlerName
     * @param params
     */
    public void sendMessage(String handlerName, BaseMap params) {
        params.put(GlobalConstants.HANDLER_NAME, handlerName);
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, params);
    }


}
