package com.jeestudio.bpm.modules.oas.enums;

import com.alibaba.fastjson.annotation.JSONCreator;

/**
 * @author admin
 * @Description: 打卡记录-考勤状态
 * @time 2026年03月08日 09:56:50
 */
public enum OaAttendanceRecordAttendanceStatusEnum {

    ZC("0", "正常"),    CD("1", "迟到"),    ZT("2", "早退"),    CDZT("3", "迟到+早退"),    QK("4", "缺卡"),    KG("5", "旷工"),    XX("6", "休息"),    QJ("7", "请假"),    CC("8", "出差")    ;

    private String code;
    private String name;

    OaAttendanceRecordAttendanceStatusEnum(String code, String name) {
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
    public static OaAttendanceRecordAttendanceStatusEnum getByCode(String code) {
        for (OaAttendanceRecordAttendanceStatusEnum value : OaAttendanceRecordAttendanceStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
