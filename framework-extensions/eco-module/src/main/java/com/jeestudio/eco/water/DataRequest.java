package com.jeestudio.eco.water;

import java.util.Map;

/**
 * @Description: 水环境评价输入数据
 */
public class DataRequest {
    private String sectionCode;  // 断面编码
    private String sectionType;  // 断面类型：HL河流，HK湖库，DXS地下水
    private String monitorDate;  // 监测日期
    private String itemCode;     // 指标编码
    private String itemValue;    // 监测结果
    private String szmb;         // 水质目标，支持数字1-5
    private String zbxz;          // 单指标的限值
    private String bdkczb;          // 是否为本底扣除指标，1为是，0或空为否  如果是本地扣除指标的话，该指标不参与水质类别、达标情况、超标因子等所有计算

    private String itemName;     // 指标名称（评价结果中的指标名称用这个）

    private String sectionName;     // 点位名称

    private String areaName;     // 行政区划

    private String riverName;     // 所在水体名称

    private Map<String,Object> extendData;  // 扩展数据

    private String itemCodeYs;     // 原始指标编码

    // Getters and Setters
    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public String getMonitorDate() {
        return monitorDate;
    }

    public void setMonitorDate(String monitorDate) {
        this.monitorDate = monitorDate;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getSzmb() {
        return szmb;
    }

    public void setSzmb(String szmb) {
        this.szmb = szmb;
    }

    public String getZbxz() {
        return zbxz;
    }

    public void setZbxz(String zbxz) {
        this.zbxz = zbxz;
    }

    public String getBdkczb() {
        return bdkczb;
    }

    public void setBdkczb(String bdkczb) {
        this.bdkczb = bdkczb;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getRiverName() {
        return riverName;
    }

    public void setRiverName(String riverName) {
        this.riverName = riverName;
    }

    public Map<String, Object> getExtendData() {
        return extendData;
    }

    public void setExtendData(Map<String, Object> extendData) {
        this.extendData = extendData;
    }

    public String getItemCodeYs() {
        return itemCodeYs;
    }

    public void setItemCodeYs(String itemCodeYs) {
        this.itemCodeYs = itemCodeYs;
    }
}
