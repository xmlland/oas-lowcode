package com.jeestudio.tools.security.utils.security;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import com.jeestudio.tools.security.pojo.SM4EncryptResult;

import java.nio.charset.StandardCharsets;

/**
 * @Description: SM4对称加密工具类
 * <p>
 * 安全加固：新增CBC模式方法，旧ECB方法已弃用（保留兼容性但不推荐使用）
 * CBC模式使用随机IV（16字节），格式为 IV(32hex) + 密文(hex)
 * </p>
 */
public class SM4Util {

    private static final String ALGORITHM_CBC = "SM4/CBC/PKCS7Padding";
    private static final String ALGORITHM_ECB = "SM4/ECB/PKCS5Padding";
    private static final int IV_LENGTH = 16; // SM4 IV长度16字节

    /**
     * 生成密钥
     * @return
     */
    public static String generateKey(){
        return IdUtil.fastSimpleUUID().substring(0,16);
    }

    /**
     * 生成随机IV（16字节）
     */
    private static byte[] generateIv() {
        return SecureUtil.generateKey("SM4").getEncoded();
    }

    // ==================== CBC 模式（推荐使用） ====================

    /**
     * SM4 CBC模式加密
     * @param key 密钥（16字节）
     * @param encryptStr 明文
     * @return IV(32hex) + 密文(hex)
     */
    public static String encryptCbc(String key, String encryptStr) {
        validKey(key);
        byte[] iv = generateIv();
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_CBC,
                key.getBytes(StandardCharsets.UTF_8));
        sm4.setIv(iv);
        String ivHex = cn.hutool.core.util.HexUtil.encodeHexStr(iv);
        String cipherHex = sm4.encryptHex(encryptStr);
        return ivHex + cipherHex;
    }

    /**
     * SM4 CBC模式加密（自动生成密钥）
     * @param encryptStr 明文
     * @return 加密结果（包含密文和密钥），密文格式为 IV(32hex) + 密文(hex)
     */
    public static SM4EncryptResult encryptCbc(String encryptStr) {
        String key = generateKey();
        byte[] iv = generateIv();
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_CBC,
                key.getBytes(StandardCharsets.UTF_8));
        sm4.setIv(iv);
        String ivHex = cn.hutool.core.util.HexUtil.encodeHexStr(iv);
        String cipherHex = sm4.encryptHex(encryptStr);
        return new SM4EncryptResult(ivHex + cipherHex, key);
    }

    /**
     * SM4 CBC模式解密
     * @param key 密钥（16字节）
     * @param decryptStr 密文（IV(32hex) + 密文(hex) 格式）
     * @return 明文
     */
    public static String decryptCbc(String key, String decryptStr) {
        validKey(key);
        if (decryptStr == null || decryptStr.length() <= IV_LENGTH * 2) {
            throw new IllegalArgumentException("CBC密文格式错误，长度不足");
        }
        String ivHex = decryptStr.substring(0, IV_LENGTH * 2);
        String cipherHex = decryptStr.substring(IV_LENGTH * 2);
        byte[] iv = cn.hutool.core.util.HexUtil.decodeHex(ivHex);
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_CBC,
                key.getBytes(StandardCharsets.UTF_8));
        sm4.setIv(iv);
        return sm4.decryptStr(cipherHex);
    }

    // ==================== ECB 模式（已弃用，仅保留兼容性） ====================

    /**
     * 加密（ECB模式）
     * @deprecated ECB模式不安全（相同明文产生相同密文），请使用 {@link #encryptCbc(String, String)}
     */
    @Deprecated
    public static String encrypt(String key,String encryptStr){
        validKey(key);
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_ECB,key.getBytes(StandardCharsets.UTF_8));
        return sm4.encryptHex(encryptStr);
    }

    /**
     * 加密（ECB模式，自动生成密钥）
     * @deprecated ECB模式不安全，请使用 {@link #encryptCbc(String)}
     */
    @Deprecated
    public static SM4EncryptResult encrypt(String encryptStr){
        String key = generateKey();
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_ECB,key.getBytes(StandardCharsets.UTF_8));
        return new SM4EncryptResult(sm4.encryptHex(encryptStr),key);
    }

    /**
     * 解密（ECB模式）
     * @deprecated ECB模式不安全，请使用 {@link #decryptCbc(String, String)}
     */
    @Deprecated
    public static String decrypt(String key,String decryptStr){
        validKey(key);
        SymmetricCrypto sm4 = new SymmetricCrypto(ALGORITHM_ECB,key.getBytes(StandardCharsets.UTF_8));
        return sm4.decryptStr(decryptStr);
    }

    /**
     * 验证密钥
     * @param key
     */
    private static void validKey(String key){
        Assert.notBlank(key);
    }

    public static void main(String[] args) {

        String text = "黄河";
        String key = SM4Util.generateKey();
        String encrypt = SM4Util.encrypt(key, text);
        System.out.println(key);
        System.out.println(encrypt);
        System.out.println(SM4Util.decrypt(key, encrypt));
    }
}
