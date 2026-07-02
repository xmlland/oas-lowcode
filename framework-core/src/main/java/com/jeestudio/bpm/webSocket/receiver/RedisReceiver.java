package com.jeestudio.bpm.webSocket.receiver;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jeestudio.bpm.webSocket.BaseMap;
import com.jeestudio.bpm.webSocket.constant.GlobalConstants;
import com.jeestudio.bpm.webSocket.listener.RedisListerer;
import lombok.Data;

import org.springframework.stereotype.Component;


/**
 * @Description: Redis消息接收器
 */
@Component
@Data
public class RedisReceiver {


    /**
     * 接受消息并调用业务逻辑处理器
     *
     * @param params
     */
    public void onMessage(BaseMap params) {
        Object handlerName = params.get(GlobalConstants.HANDLER_NAME);
//        RedisListerer messageListener = SpringContextHolder.getHandler(handlerName.toString(), RedisListerer.class);
        RedisListerer messageListener = SpringUtil.getBean(handlerName.toString(), RedisListerer.class);
        if (ObjectUtil.isNotEmpty(messageListener)) {
            messageListener.onMessage(params);
        }
    }

}
