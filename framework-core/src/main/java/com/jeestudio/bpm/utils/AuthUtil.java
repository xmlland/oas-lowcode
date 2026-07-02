package com.jeestudio.bpm.utils;

import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @Description: 动态口令工具
 */
public class AuthUtil {
    protected static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long TIME_STEP = 300; // 300 seconds
    public static final String SECRET_KEY = "G7h2Jk9LmA";
    public static final String SYSTEM_SETTING_KEY = "auth_key"; //系统设置中的key

    public static String generateCode(String secretKey, long timestamp) {
        long timeIndex = timestamp / TIME_STEP;
        byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(Long.toString(timeIndex).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 8); // 取前6位
        } catch (Exception e) {
            logger.warn("AuthUtil generate code wrong:" + e.getMessage());
            return null;
        }
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()_+";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String key = generateRandomString(20);
        logger.info("key:" + key);

        String secretKey = "Ha1KLYjHJdhNQh7vMzaz";
        String code = generateCode(secretKey, System.currentTimeMillis());
        logger.info("code is:" + code);
    }
}
