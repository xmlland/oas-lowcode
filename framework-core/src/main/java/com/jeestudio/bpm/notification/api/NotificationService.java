package com.jeestudio.bpm.notification.api;

/**
 * @Description: 统一消息通知服务接口
 * 由 notification 模块实现
 */
public interface NotificationService {

    /**
     * 发送消息到指定通道
     * 如果 request.channels 为空，则使用默认通道配置
     *
     * @param request 消息通知请求
     */
    void send(NotificationRequest request);
}
