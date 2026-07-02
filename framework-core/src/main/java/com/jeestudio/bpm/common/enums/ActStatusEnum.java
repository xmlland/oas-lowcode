package com.jeestudio.bpm.common.enums;

/**
 * @Description: 工作流状态枚举
 */
public enum ActStatusEnum {
    /** 终止 */
    TERMINATE("00"),
    /** 结束 */
    END("99");
    private String code;

    ActStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
