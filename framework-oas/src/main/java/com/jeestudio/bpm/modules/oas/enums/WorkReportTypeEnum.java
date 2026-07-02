package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @Description: 工作汇报类型枚举
 */
public enum WorkReportTypeEnum {

    DAILY("daily", "日报"),    WEEKLY("weekly", "周报"),    MONTHLY("monthly", "月报")    ;

    private String code;
    private String name;

    WorkReportTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JSONCreator
    public static WorkReportTypeEnum getByCode(String code) {
        for (WorkReportTypeEnum value : WorkReportTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
