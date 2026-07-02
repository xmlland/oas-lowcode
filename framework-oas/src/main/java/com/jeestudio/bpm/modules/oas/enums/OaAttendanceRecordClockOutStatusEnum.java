package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @author admin
 * @Description: 打卡记录-下班状态
 * @time 2026年03月08日 09:53:33
 */
public enum OaAttendanceRecordClockOutStatusEnum {

    WDK("0", "未打卡"),    ZC("1", "正常"),    ZT("2", "早退"),    BK("3", "补卡")    ;

    private String code;
    private String name;

    OaAttendanceRecordClockOutStatusEnum(String code, String name) {
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
    public static OaAttendanceRecordClockOutStatusEnum getByCode(String code) {
        for (OaAttendanceRecordClockOutStatusEnum value : OaAttendanceRecordClockOutStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
