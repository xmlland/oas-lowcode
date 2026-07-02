package com.jeestudio.bpm.security.enums;

/**
 * @Description: 二次鉴权常量枚举
 */
public enum Auth2Enum {
    AUTH2_PREFIX("auth2_"),
    AUTH2_OPEN("auth2_open"),
    AUTH2_ROLES("auth2_roles"),
    AUTH2_USERS("auth2_users");
    private String value;

    Auth2Enum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //根据value获取enum
    public static Auth2Enum getEnum(String value) {
        for (Auth2Enum auth2Enum : Auth2Enum.values()) {
            if (auth2Enum.getValue().equals(value)) {
                return auth2Enum;
            }
        }
        return null;
    }
}
