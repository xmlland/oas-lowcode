package com.jeestudio.bpm.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.codec.Base64Decoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: AES加密工具类
 * 使用AES/CBC/PKCS5Padding模式，密钥从外部配置注入
 */
public class Aes {

    private static final Logger logger = LoggerFactory.getLogger(Aes.class);

    private static volatile String KEY = null;

    /**
     * 新的安全加密模式：AES/CBC/PKCS5Padding
     */
    private static final String ALGORITHMSTR_CBC = "AES/CBC/PKCS5Padding";

    /**
     * 旧的不安全模式，仅用于解密旧数据兼容
     */
    private static final String ALGORITHMSTR_ECB = "AES/ECB/PKCS5Padding";

    private static final int IV_LENGTH = 16;

    /**
     * 从配置初始化密钥
     * @param configKey AES密钥，不能为空或以"please-change"开头
     * @throws IllegalStateException 如果密钥未配置或无效
     */
    public static void init(String configKey) {
        if (configKey == null || configKey.isEmpty()) {
            logger.error("AES密钥未配置，请在配置文件中设置 project.login-encrypt-key 或环境变量 LOGIN_ENCRYPT_KEY");
            throw new IllegalStateException("AES密钥未配置，请设置 project.login-encrypt-key 或环境变量 LOGIN_ENCRYPT_KEY");
        }
        if (configKey.startsWith("please-change")) {
            logger.error("AES密钥使用了占位符值，请在配置文件中设置真实的 project.login-encrypt-key 或环境变量 LOGIN_ENCRYPT_KEY");
            throw new IllegalStateException("AES密钥使用了占位符值，请设置真实的密钥");
        }
        KEY = configKey;
        logger.info("AES密钥初始化成功");
    }

    public static String aesDecrypt(String encrypt) {
        if (KEY == null) {
            throw new IllegalStateException("AES密钥未初始化，请确保已调用 Aes.init() 方法");
        }
        try {
            return aesDecrypt(encrypt, KEY);
        } catch (Exception e) {
            logger.error("AES解密失败", e);
            return "";
        }
    }

    public static String aesEncrypt(String content) {
        if (KEY == null) {
            throw new IllegalStateException("AES密钥未初始化，请确保已调用 Aes.init() 方法");
        }
        try {
            return aesEncrypt(content, KEY);
        } catch (Exception e) {
            logger.error("AES加密失败", e);
            return "";
        }
    }

    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    public static byte[] base64Decode(String base64Code) throws Exception {
        return StringUtils.isEmpty(base64Code) ? null : Base64Decoder.decode(base64Code);
    }

    /**
     * AES/CBC加密：生成随机IV并拼接在密文前面
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHMSTR_CBC);
        SecretKeySpec keySpec = new SecretKeySpec(normalizeKey(encryptKey), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(content.getBytes("utf-8"));

        // IV + 密文
        byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);
        return result;
    }

    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * AES解密：优先尝试CBC模式（新格式），失败后降级尝试ECB（兼容旧数据）
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        // 先尝试CBC模式（新格式：前16字节为IV）
        if (encryptBytes.length > IV_LENGTH) {
            try {
                byte[] iv = new byte[IV_LENGTH];
                byte[] ciphertext = new byte[encryptBytes.length - IV_LENGTH];
                System.arraycopy(encryptBytes, 0, iv, 0, IV_LENGTH);
                System.arraycopy(encryptBytes, IV_LENGTH, ciphertext, 0, ciphertext.length);

                Cipher cipher = Cipher.getInstance(ALGORITHMSTR_CBC);
                SecretKeySpec keySpec = new SecretKeySpec(normalizeKey(decryptKey), "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                return new String(cipher.doFinal(ciphertext), "utf-8");
            } catch (Exception e) {
                // CBC解密失败，降级尝试ECB（兼容旧数据）
                logger.debug("CBC解密失败，尝试ECB兼容模式");
            }
        }

        // ECB降级兼容（用于解密旧的ECB格式数据）
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR_ECB);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(normalizeKey(decryptKey), "AES"));
        return new String(cipher.doFinal(encryptBytes), "utf-8");
    }

    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
    }

    /**
     * 将密钥归一化为16字节（128位AES）
     */
    private static byte[] normalizeKey(String key) {
        byte[] keyBytes = key.getBytes();
        byte[] result = new byte[16];
        System.arraycopy(keyBytes, 0, result, 0, Math.min(keyBytes.length, 16));
        return result;
    }
}
