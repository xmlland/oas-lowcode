package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @author admin
 * @Description: 打卡记录-上班打卡方式
 * @time 2026年05月24日 14:04:49
 */
public enum OaAttendanceRecordClockInTypeEnum {

    GPS("1", "GPS"),
    WIFI("2", "WiFi"),
    SD("3", "手动")
    ;

    private String code;
    private String name;

    OaAttendanceRecordClockInTypeEnum(String code, String name) {
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
    public static OaAttendanceRecordClockInTypeEnum getByCode(String code) {
        for (OaAttendanceRecordClockInTypeEnum value : OaAttendanceRecordClockInTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
