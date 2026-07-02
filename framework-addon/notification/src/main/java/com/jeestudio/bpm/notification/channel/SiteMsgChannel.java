package com.jeestudio.bpm.notification.channel;

import com.jeestudio.bpm.common.entity.system.SysMsg;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.notification.api.Channel;
import com.jeestudio.bpm.notification.api.NotificationRequest;
import com.jeestudio.bpm.service.system.SysMsgService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * @Description: 站内消息通道
 */
@Component
public class SiteMsgChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(SiteMsgChannel.class);

    /** 未删除标志 */
    private static final String DEL_FLAG_NORMAL = "0";
    /** 默认消息类型 */
    private static final String DEFAULT_TYPE = "default";

    @Autowired
    private SysMsgService sysMsgService;

    @Override
    public Channel getType() {
        return Channel.SITE_MSG;
    }

    @Override
    public void send(NotificationRequest request) {
        Date now = new Date();
        User sender = null;
        if (request.getSenderId() != null && !request.getSenderId().isEmpty()) {
            sender = new User(request.getSenderId());
        }

        // 获取当前登录用户，用于审计字段
        User currentUser = UserUtil.getCurrentUser();
        User auditUser = currentUser != null ? currentUser : sender;

        for (String recipientId : request.getRecipientIds()) {
            try {
                SysMsg sysMsg = new SysMsg();
                sysMsg.setId(UUID.randomUUID().toString().replace("-", ""));
                sysMsg.setTitle(request.getTitle());
                sysMsg.setContent(request.getContent());
                sysMsg.setTypes(DEFAULT_TYPE);
                sysMsg.setSender(sender);
                sysMsg.setRecipient(new User(recipientId));
                sysMsg.setSendTime(now);
                sysMsg.setStatus(Global.YES);
                sysMsg.setOwnerCode(request.getOwnerCode());
                sysMsg.setCreateBy(auditUser);
                sysMsg.setCreateDate(now);
                sysMsg.setUpdateBy(auditUser);
                sysMsg.setUpdateDate(now);
                sysMsg.setDelFlag(DEL_FLAG_NORMAL);

                // 关联菜单信息（可选）
                if (request.getMenuHref() != null) {
                    sysMsg.setMenuHref(request.getMenuHref());
                }
                if (request.getMenuName() != null) {
                    sysMsg.setMenuName(request.getMenuName());
                }
                if (request.getMenuNameEn() != null) {
                    sysMsg.setMenuName_EN(request.getMenuNameEn());
                }
                if (request.getRecordId() != null) {
                    sysMsg.setRecord(request.getRecordId());
                }

                sysMsgService.save(sysMsg);
                logger.debug("Site message saved for user {}: {}", recipientId, request.getTitle());
            } catch (Exception e) {
                logger.error("Failed to save site message for user {}: {}", recipientId, e.getMessage(), e);
            }
        }
    }
}
