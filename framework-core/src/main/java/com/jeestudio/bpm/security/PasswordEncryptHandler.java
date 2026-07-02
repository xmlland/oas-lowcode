package com.jeestudio.bpm.security;

/**
 * @Description: 密码加密处理器抽象类
 */
public abstract class PasswordEncryptHandler {

    /**
     * 对密码进行加密
     *
     * @param password 明文密码
     * @param encryptPassword  数据库存储的加密后的密码
     * @param loginName 登录名
     * @return 加密后的密码
     */
    public abstract String encrypt(String password,String encryptPassword,String loginName);
}
