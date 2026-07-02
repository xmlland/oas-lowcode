package com.jeestudio.tools.security.utils.security;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.jeestudio.tools.security.pojo.RSAEncryptResult;
import com.jeestudio.tools.security.pojo.RSAKey;

import java.security.KeyPair;

/**
 * @Description: RSA非对称加密工具
 */
public class RSAUtil {

    /**
     * 生成公钥/私钥 名词有歧义 弃用
     *
     * @return
     * @see #generateKey()
     */
    @Deprecated
    public static RSAKey generatePublicKey() {
        return generateKey();
    }

    /**
     * 生成公钥/私钥
     *
     * @return
     */
    public static RSAKey generateKey() {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA");
        RSA rsa = new RSA(keyPair.getPrivate(), keyPair.getPublic());
        return new RSAKey(rsa.getPrivateKeyBase64(), rsa.getPublicKeyBase64());
    }

    /**
     * 使用公钥加密数据
     *
     * @param publicKey  公钥
     * @param encryptStr 待加密字符串
     * @return
     */
    public static String encrypt(String publicKey, String encryptStr) {
        RSA rsa = new RSA(null, publicKey);
        return rsa.encryptHex(StrUtil.bytes(encryptStr, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
    }

    /**
     * 使用公钥加密数据
     *
     * @param rsaKey     公钥/私钥对
     * @param encryptStr 待加密字符串
     * @return
     */
    public static String encrypt(RSAKey rsaKey, String encryptStr) {
        RSA rsa = new RSA(rsaKey.getPrivateKey(), rsaKey.getPublicKey());
        return rsa.encryptHex(StrUtil.bytes(encryptStr, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
    }

    /**
     * 使用公钥加密数据
     *
     * @param encryptStr 待加密字符串
     * @return
     */
    public static RSAEncryptResult encrypt(String encryptStr) {
        RSAKey rsaKey = generateKey();
        RSA rsa = new RSA(rsaKey.getPrivateKey(), rsaKey.getPublicKey());
        String encryptHex = rsa.encryptHex(StrUtil.bytes(encryptStr, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return new RSAEncryptResult(encryptHex, rsa.getPrivateKeyBase64(), rsa.getPublicKeyBase64());
    }

    public static String decrypt(String privateKey, String decryptStr) {
        RSA rsa = new RSA(privateKey, null);
        return rsa.decryptStr(decryptStr, KeyType.PrivateKey);
    }

    public static void main(String[] args) {
        RSAKey rsaKey = generateKey();
        System.out.println(rsaKey.getPublicKey());
        System.out.println(rsaKey.getPrivateKey());
        String encrypt = encrypt(rsaKey, "锄禾日当午");


        System.out.println(encrypt);


        System.out.println(decrypt(rsaKey.getPrivateKey(),
                encrypt));

    }
}
