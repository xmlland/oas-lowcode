package com.jeestudio.bpm.gen.enums;

/**
 * @Description: 日期格式类型枚举
 */
public enum DateTypeEnum {

    YEAR("yyyy"),
    YEAR_MONTH("yyyy-MM"),
    DATE("yyyy-MM-dd"),
    HOUR("yyyy-MM-dd HH"),
    MINUTE("yyyy-MM-dd HH:mm"),
    TIME("yyyy-MM-dd HH:mm:ss");
    private String value;

    DateTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //根据value获取enum
    public static DateTypeEnum getEnum(String value) {
        for (DateTypeEnum dateTypeEnum : DateTypeEnum.values()) {
            if (dateTypeEnum.getValue().equals(value)) {
                return dateTypeEnum;
            }
        }
        return null;
    }
}
