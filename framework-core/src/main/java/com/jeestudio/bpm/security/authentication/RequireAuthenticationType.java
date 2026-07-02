package com.jeestudio.bpm.security.authentication;

import lombok.Getter;

/**
 * @Description: 二次鉴权类型枚举
 */
@Getter
public enum RequireAuthenticationType {
    NONE("none","自定义"),
    PASSWORD("password","密码"),
    SMS("sms","短信"),
    LOG("log","日志");

    private String code;
    private String name;

    RequireAuthenticationType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
