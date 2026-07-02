package com.jeestudio.tools.security.enums;

/**
 * @Description: 密码字符类型枚举
 */
public enum PasswordCharTypeEnum {

    upperCase("大写字母"),
    lowerCase("小写字母"),
    number("数字"),
    character("特殊字符");


    private String typeName;

    PasswordCharTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
