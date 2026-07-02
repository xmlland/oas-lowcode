package com.jeestudio.bpm.notification.channel;

import com.jeestudio.bpm.notification.api.Channel;
import com.jeestudio.bpm.notification.api.NotificationRequest;

/**
 * @Description: 消息通道接口
 * 站内信、WebSocket、邮件等通知通道通过该接口统一接入。
 */
public interface MessageChannel {

    /**
     * 获取通道类型
     *
     * @return 通道类型枚举
     */
    Channel getType();

    /**
     * 通过此通道发送消息
     *
     * @param request 消息通知请求
     */
    void send(NotificationRequest request);
}
