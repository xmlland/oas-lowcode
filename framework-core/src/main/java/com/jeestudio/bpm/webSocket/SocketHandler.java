package com.jeestudio.bpm.webSocket;

import cn.hutool.core.util.ObjectUtil;
import com.jeestudio.bpm.webSocket.constant.CommonSendStatus;
import com.jeestudio.bpm.webSocket.listener.RedisListerer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: WebSocket消息处理器（Redis发布订阅）
 */
@Slf4j
@Component("socketHandler")
public class SocketHandler implements RedisListerer {

    @Autowired
    private WebSocket webSocket;

    @Override
    public void onMessage(BaseMap map) {
        log.info("【SocketHandler消息】Redis Listerer:" + map.toString());

        String userId = map.get("userId");
        String message = map.get("message");
        if (ObjectUtil.isNotEmpty(userId)) {
            webSocket.pushMessage(userId, message);
            //app端消息推送
            webSocket.pushMessage(userId+ CommonSendStatus.APP_SESSION_SUFFIX, message);
        } else {
            webSocket.pushMessage(message);
        }

    }
}