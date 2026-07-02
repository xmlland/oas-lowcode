package com.jeestudio.bpm.notification.channel;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.notification.api.Channel;
import com.jeestudio.bpm.notification.api.NotificationRequest;
import com.jeestudio.bpm.webSocket.WebSocket;
import com.jeestudio.bpm.webSocket.constant.WebsocketConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: WebSocket消息通道
 */
@Component
public class WebSocketChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChannel.class);

    @Autowired(required = false)
    private WebSocket webSocket;

    @Override
    public Channel getType() {
        return Channel.WEBSOCKET;
    }

    @Override
    public void send(NotificationRequest request) {
        if (webSocket == null) {
            logger.warn("WebSocket bean is not available, skipping WebSocket notification");
            return;
        }

        // 构造 WebSocket 消息 JSON
        JSONObject msgJson = new JSONObject();
        msgJson.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
        msgJson.put(WebsocketConst.MSG_TXT, request.getTitle());
        msgJson.put(WebsocketConst.MSG_ID, "");
        
        // 添加额外信息便于前端处理
        if (request.getContent() != null) {
            msgJson.put("content", request.getContent());
        }
        if (request.getMenuHref() != null) {
            msgJson.put("menuHref", request.getMenuHref());
        }
        if (request.getRecordId() != null) {
            msgJson.put("recordId", request.getRecordId());
        }

        String message = msgJson.toJSONString();

        for (String recipientId : request.getRecipientIds()) {
            try {
                webSocket.sendMessage(recipientId, message);
                logger.debug("WebSocket message pushed to user {}: {}", recipientId, request.getTitle());
            } catch (Exception e) {
                logger.error("Failed to push WebSocket message to user {}: {}", recipientId, e.getMessage(), e);
            }
        }
    }
}
