package com.jeestudio.bpm.notification.channel;

import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.mapper.base.system.UserDao;
import com.jeestudio.bpm.notification.api.Channel;
import com.jeestudio.bpm.notification.api.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/**
 * @Description: 邮件消息通道
 */
@Component
@ConditionalOnProperty(name = "notification.email.enabled", havingValue = "true")
public class EmailChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserDao userDao;

    @Value("${notification.email.from:${spring.mail.username:noreply@example.com}}")
    private String fromAddress;

    @Override
    public Channel getType() {
        return Channel.EMAIL;
    }

    @Override
    public void send(NotificationRequest request) {
        if (request.getRecipientIds() == null || request.getRecipientIds().isEmpty()) {
            logger.warn("No recipients specified for email notification");
            return;
        }

        for (String recipientId : request.getRecipientIds()) {
            try {
                String email = getUserEmail(recipientId);
                if (email == null || email.isBlank()) {
                    logger.warn("User {} has no email configured, skipping email notification", recipientId);
                    continue;
                }

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(email);
                helper.setSubject(request.getTitle());
                helper.setText(request.getContent(), true);

                mailSender.send(mimeMessage);
                logger.debug("Email sent to {} for user {}", email, recipientId);
            } catch (Exception e) {
                logger.error("Failed to send email to user {}: {}", recipientId, e.getMessage(), e);
            }
        }
    }

    /**
     * 根据用户 ID 查询用户邮箱地址
     *
     * @param userId 用户 ID
     * @return 用户邮箱，如果用户不存在或无邮箱则返回 null
     */
    private String getUserEmail(String userId) {
        try {
            User user = userDao.get(userId);
            if (user != null) {
                return user.getEmail();
            }
            logger.warn("User not found: {}", userId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to query user email for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}
