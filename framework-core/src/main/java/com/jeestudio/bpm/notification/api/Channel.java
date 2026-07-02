package com.jeestudio.bpm.notification.api;

/**
 * @Description: 消息通道枚举
 */
public enum Channel {
    /** 站内信 */
    SITE_MSG,
    /** WebSocket 实时推送 */
    WEBSOCKET,
    /** 邮件 */
    EMAIL,
    /** 短信 */
    SMS
}
