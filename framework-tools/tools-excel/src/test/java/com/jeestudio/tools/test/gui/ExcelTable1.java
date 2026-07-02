package com.jeestudio.tools.test.gui;

import com.jeestudio.tools.base.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * //TODO description
 * 2022年07月12日 13:59:00
 *
 * @author U-002
 */
@Excel(title = "重点监管单位调查信息", skipImportValid = true)
public class ExcelTable1 {
    @Excel(title = "行政区",dictSingleSheet = true)
    private String areaCode;

    @Excel(title = "重点监管单位名称")
    private String enterName;  //重点监管单位名称
    @Excel(title = "统一社会信用代码")
    private String creditCode;  //统一社会信用代码
    @Excel(title = "重点监管单位所在地地址")
    private String enterAddress;  //重点监管单位所在地地址

    @Excel(title = "正门经度")
    private String gateLng;  //正门经度
    @Excel(title = "正门纬度")
    private String gateLat;  //正门纬度
    @Excel
    private List<ExcelTable2> children;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getEnterName() {
        return enterName;
    }

    public void setEnterName(String enterName) {
        this.enterName = enterName;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }

    public String getEnterAddress() {
        return enterAddress;
    }

    public void setEnterAddress(String enterAddress) {
        this.enterAddress = enterAddress;
    }

    public String getGateLng() {
        return gateLng;
    }

    public void setGateLng(String gateLng) {
        this.gateLng = gateLng;
    }

    public String getGateLat() {
        return gateLat;
    }

    public void setGateLat(String gateLat) {
        this.gateLat = gateLat;
    }

    public List<ExcelTable2> getChildren() {
        return children;
    }

    public void setChildren(List<ExcelTable2> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("areaCode", areaCode)
                .append("enterName", enterName)
                .append("creditCode", creditCode)
                .append("enterAddress", enterAddress)
                .append("gateLng", gateLng)
                .append("gateLat", gateLat)
                .append("children", children)
                .toString();
    }
}
