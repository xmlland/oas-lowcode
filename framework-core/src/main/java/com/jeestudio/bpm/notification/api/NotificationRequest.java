package com.jeestudio.bpm.notification.api;

import java.util.*;

/**
 * @Description: 统一消息通知请求
 */
public class NotificationRequest {

    /** 消息模板编码（可选，使用模板时填写） */
    private String templateCode;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 接收人 ID 列表 */
    private List<String> recipientIds = new ArrayList<>();
    /** 发送人 ID */
    private String senderId;
    /** 发送通道集合（为空时使用默认通道） */
    private Set<Channel> channels = new HashSet<>();
    /** 模板变量 */
    private Map<String, String> params = new HashMap<>();
    /** 关联菜单链接（可选） */
    private String menuHref;
    /** 关联菜单名称（可选） */
    private String menuName;
    /** 关联菜单英文名称（可选） */
    private String menuNameEn;
    /** 关联记录 ID（可选） */
    private String recordId;
    /** 所属公司代码（可选） */
    private String ownerCode;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 内部类
     */
    public static class Builder {
        private final NotificationRequest request = new NotificationRequest();

        public Builder templateCode(String templateCode) {
            request.templateCode = templateCode;
            return this;
        }

        public Builder title(String title) {
            request.title = title;
            return this;
        }

        public Builder content(String content) {
            request.content = content;
            return this;
        }

        public Builder recipientIds(List<String> recipientIds) {
            request.recipientIds = recipientIds;
            return this;
        }

        public Builder recipientId(String recipientId) {
            request.recipientIds.add(recipientId);
            return this;
        }

        public Builder senderId(String senderId) {
            request.senderId = senderId;
            return this;
        }

        public Builder channels(Set<Channel> channels) {
            request.channels = channels;
            return this;
        }

        public Builder channel(Channel channel) {
            request.channels.add(channel);
            return this;
        }

        public Builder params(Map<String, String> params) {
            request.params = params;
            return this;
        }

        public Builder param(String key, String value) {
            request.params.put(key, value);
            return this;
        }

        public Builder menuHref(String menuHref) {
            request.menuHref = menuHref;
            return this;
        }

        public Builder menuName(String menuName) {
            request.menuName = menuName;
            return this;
        }

        public Builder menuNameEn(String menuNameEn) {
            request.menuNameEn = menuNameEn;
            return this;
        }

        public Builder recordId(String recordId) {
            request.recordId = recordId;
            return this;
        }

        public Builder ownerCode(String ownerCode) {
            request.ownerCode = ownerCode;
            return this;
        }

        public NotificationRequest build() {
            return request;
        }
    }

    // Getter 和 Setter 方法

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    public void setChannels(Set<Channel> channels) {
        this.channels = channels;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getMenuHref() {
        return menuHref;
    }

    public void setMenuHref(String menuHref) {
        this.menuHref = menuHref;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuNameEn() {
        return menuNameEn;
    }

    public void setMenuNameEn(String menuNameEn) {
        this.menuNameEn = menuNameEn;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }
}
