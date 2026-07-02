package com.jeestudio.bpm.notification.service;

import com.jeestudio.bpm.notification.channel.MessageChannel;
import com.jeestudio.bpm.notification.api.Channel;
import com.jeestudio.bpm.notification.api.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 统一消息通知服务
 */
@Service
public class NotificationService implements com.jeestudio.bpm.notification.api.NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final Map<Channel, MessageChannel> channelMap;

    /** 默认通道配置，逗号分隔，如 "SITE_MSG,WEBSOCKET" */
    @Value("${notification.default-channels:SITE_MSG,WEBSOCKET}")
    private String defaultChannels;

    @Autowired
    public NotificationService(@Autowired(required = false) List<MessageChannel> channels) {
        if (channels != null) {
            this.channelMap = channels.stream()
                .collect(Collectors.toMap(MessageChannel::getType, c -> c));
        } else {
            this.channelMap = new HashMap<>();
        }
        logger.info("NotificationService initialized with channels: {}", this.channelMap.keySet());
    }

    /**
     * 发送消息到指定通道
     * 如果 request.channels 为空，则使用默认通道配置
     *
     * @param request 消息通知请求
     */
    @Override
    public void send(NotificationRequest request) {
        Set<Channel> targetChannels = request.getChannels();
        if (targetChannels == null || targetChannels.isEmpty()) {
            targetChannels = parseDefaultChannels();
        }

        for (Channel channel : targetChannels) {
            MessageChannel handler = channelMap.get(channel);
            if (handler != null) {
                try {
                    handler.send(request);
                    logger.debug("Message sent via channel {}: title={}", channel, request.getTitle());
                } catch (Exception e) {
                    logger.error("Failed to send message via channel {}: {}", channel, e.getMessage(), e);
                }
            } else {
                logger.warn("No handler registered for channel: {}", channel);
            }
        }
    }

    /**
     * 解析默认通道配置
     *
     * @return 默认通道集合
     */
    private Set<Channel> parseDefaultChannels() {
        Set<Channel> result = new HashSet<>();
        if (defaultChannels != null && !defaultChannels.isBlank()) {
            for (String ch : defaultChannels.split(",")) {
                try {
                    result.add(Channel.valueOf(ch.trim()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown default channel: {}", ch.trim());
                }
            }
        }
        return result;
    }
}
