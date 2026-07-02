package com.jeestudio.tools.base.enums;

/**
 * @Description: 对齐方式枚举
 */
public enum AlignEnum {
    LEFT("LEFT"),
    CENTER("CENTER"),
    RIGHT("RIGHT");

    AlignEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
