package com.jeestudio.tools.test.gui;

import com.jeestudio.tools.base.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * //TODO description
 * 2022年07月12日 14:00:00
 *
 * @author U-002
 */
public class ExcelTable3 {
    @Excel(title = "点位编码")
    private String pointCode;  //点位编码
    @Excel(title = "点位类型")
    private String pointType;  //点位类型
    @Excel(title = "是否超标",dictSingleSheet = true)
    private Boolean overStandard;//是否超标

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public Boolean getOverStandard() {
        return overStandard;
    }

    public void setOverStandard(Boolean overStandard) {
        this.overStandard = overStandard;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pointCode", pointCode)
                .append("pointType", pointType)
                .append("overStandard", overStandard)
                .toString();
    }
}
