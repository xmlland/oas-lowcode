package com.jeestudio.bpm.security.pwd;

/**
 * @Description: 口令校验处理器抽象类
 */
public abstract class ValidateUserPasswordHandler {

    public enum ValidMode {
        DEFAULT, CUSTOM, MIX_DEFAULT_CUSTOM, MIX_CUSTOM_DEFAULT
    }

    /**
     * 校验模式
     * DEFAULT 为默认校验模式
     * CUSTOM 为自定义校验模式
     * MIX_DEFAULT_CUSTOM 先校验默认模式，再校验自定义模式
     * MIX_CUSTOM_DEFAULT 先校验自定义模式，再校验默认模式
     *
     * @return
     */
    public ValidMode validMode() {
        return ValidMode.DEFAULT;
    }

    /**
     * 校验密码
     * @param plainPassword 明文密码
     * @param password 密文密码
     * @param loginName 登录名
     * @param ip 登录IP
     * @return
     */
    public boolean validate(String plainPassword, String password, String loginName, String ip) {
        return true;
    }
}
