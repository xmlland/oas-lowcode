package com.jeestudio.tools.test.gui;

import com.jeestudio.tools.base.annotation.Dict;
import com.jeestudio.tools.base.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * //TODO description
 * 2022年07月12日 13:59:00
 *
 * @author U-002
 */
@Excel(skipImportValid = true)
public class ExcelTable2 {

    @Excel(title = "历史企业名称")
    private String historicalEnterName;//历史企业名称
    @Excel(title = "是否曾开展过调查监测工作")
    private Boolean hasInvestigation;//是否曾开展过调查监测工作
    @Excel(title = "调查监测工作")
    @Dict(dictCode = "investigation", multiple = true, separator = ";")
    private String investigation;//调查监测工作
    @Excel(title = "其他调查监测工作")
    private String otherInvestigation;//其他调查监测工作
    @Excel(title = "历史调查监测时间")
    private String investigationTime;//历史调查监测时间
    @Excel
    private List<ExcelTable3> children;

    public String getHistoricalEnterName() {
        return historicalEnterName;
    }

    public void setHistoricalEnterName(String historicalEnterName) {
        this.historicalEnterName = historicalEnterName;
    }

    public Boolean getHasInvestigation() {
        return hasInvestigation;
    }

    public void setHasInvestigation(Boolean hasInvestigation) {
        this.hasInvestigation = hasInvestigation;
    }

    public String getInvestigation() {
        return investigation;
    }

    public void setInvestigation(String investigation) {
        this.investigation = investigation;
    }

    public String getOtherInvestigation() {
        return otherInvestigation;
    }

    public void setOtherInvestigation(String otherInvestigation) {
        this.otherInvestigation = otherInvestigation;
    }

    public String getInvestigationTime() {
        return investigationTime;
    }

    public void setInvestigationTime(String investigationTime) {
        this.investigationTime = investigationTime;
    }

    public List<ExcelTable3> getChildren() {
        return children;
    }

    public void setChildren(List<ExcelTable3> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("historicalEnterName", historicalEnterName)
                .append("hasInvestigation", hasInvestigation)
                .append("investigation", investigation)
                .append("otherInvestigation", otherInvestigation)
                .append("investigationTime", investigationTime)
                .append("children", children)
                .toString();
    }
}
