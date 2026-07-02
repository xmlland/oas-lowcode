package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @Description: 考勤记录上班打卡状态枚举
 */
public enum OaAttendanceRecordClockInStatusEnum {

    WDK("0", "未打卡"),
    ZC("1", "正常"),
    CD("2", "迟到"),
    BK("3", "补卡")
    ;

    private String code;
    private String name;

    OaAttendanceRecordClockInStatusEnum(String code, String name) {
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
    public static OaAttendanceRecordClockInStatusEnum getByCode(String code) {
        for (OaAttendanceRecordClockInStatusEnum value : OaAttendanceRecordClockInStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
