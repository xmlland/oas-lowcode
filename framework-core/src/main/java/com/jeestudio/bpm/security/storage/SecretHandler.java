package com.jeestudio.bpm.security.storage;

/**
 * @Description: 机密性保护处理类（数据存储加密）
 */
public abstract class SecretHandler {

    /**
     * 对数据进行加密
     * @param data 明文
     * @return
     */
    public abstract String encrypt(String data);

    /**
     * 对数据进行解密
     * @param data 密文
     * @return
     */
    public abstract String decrypt(String data);
}
