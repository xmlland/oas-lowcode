package com.jeestudio.tools.base.enums;

/**
 * @Description: 导入校验枚举
 */
public enum ImportValidEnum {
    NOT_EMPTY("NOT_EMPTY"),
    ID_CARD_NUMBER("ID_CARD_NUMBER"),
    UNIQUE("UNIQUE");
    ImportValidEnum(String value) {
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
