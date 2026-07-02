package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @Description: 工作汇报状态枚举
 */
public enum WorkReportStatusEnum {

    DRAFT("0", "草稿"),    SUBMITTED("1", "已提交")    ;

    private String code;
    private String name;

    WorkReportStatusEnum(String code, String name) {
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
    public static WorkReportStatusEnum getByCode(String code) {
        for (WorkReportStatusEnum value : WorkReportStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
