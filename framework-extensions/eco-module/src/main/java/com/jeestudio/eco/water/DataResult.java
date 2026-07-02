package com.jeestudio.eco.water;

import java.util.List;
import java.util.Map;

/**
 * @Description: 水环境评价结果数据
 */
public class DataResult {
    private String sectionCode;  // 断面编码
    private String sectionType;  // 断面类型:HL、HK、DXS
    private String monitorDate;  // 监测日期
    private String szlb;  // 水质类别数值（1-6）
    private String szmb;  // 水质目标，支持数字1-5

    private List<FactorResult> wryz;  // 污染因子明细（超Ⅲ类）
    private String dbqk;  // 达标情况
    private List<FactorResult> cbyz;  // 超标因子明细
    private List<FactorResult> wrw;  // 污染物明细
    private List<FactorResult> dlyz;  // 定类因子

    private List<FactorResult> zjsjc;  // 重金属检出
    private List<FactorResult> yjwjc;  // 有机物检出
    private List<FactorResult> jcxcb;  // 检出限超标

    private String sdgx;  // 三氮关系：总氮＞氨氮+硝酸盐氮+亚硝酸盐氮
    private String sygx;  // 三氧关系：化学需氧量＞高锰酸盐指数 和 化学需氧量＞五日生化需氧量

    private String sectionName;     // 点位名称

    private String areaName;     // 行政区划

    private String riverName;     // 所在水体名称

    private Map<String,Object> extendData;  // 扩展数据

    private String itemCodeYs;     // 原始指标编码

    private List<FactorResult> bdkczb;  // 本底扣除指标

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

    public String getSzlb() {
        return szlb;
    }

    public void setSzlb(String szlb) {
        this.szlb = szlb;
    }

    public String getSzmb() {
        return szmb;
    }

    public void setSzmb(String szmb) {
        this.szmb = szmb;
    }

    public List<FactorResult> getWryz() {
        return wryz;
    }

    public void setWryz(List<FactorResult> wryz) {
        this.wryz = wryz;
    }

    public String getDbqk() {
        return dbqk;
    }

    public void setDbqk(String dbqk) {
        this.dbqk = dbqk;
    }

    public List<FactorResult> getCbyz() {
        return cbyz;
    }

    public void setCbyz(List<FactorResult> cbyz) {
        this.cbyz = cbyz;
    }

    public List<FactorResult> getWrw() {
        return wrw;
    }

    public void setWrw(List<FactorResult> wrw) {
        this.wrw = wrw;
    }

    public List<FactorResult> getDlyz() {
        return dlyz;
    }

    public void setDlyz(List<FactorResult> dlyz) {
        this.dlyz = dlyz;
    }

    public List<FactorResult> getZjsjc() {
        return zjsjc;
    }

    public void setZjsjc(List<FactorResult> zjsjc) {
        this.zjsjc = zjsjc;
    }

    public List<FactorResult> getYjwjc() {
        return yjwjc;
    }

    public void setYjwjc(List<FactorResult> yjwjc) {
        this.yjwjc = yjwjc;
    }

    public List<FactorResult> getJcxcb() {
        return jcxcb;
    }

    public void setJcxcb(List<FactorResult> jcxcb) {
        this.jcxcb = jcxcb;
    }

    public String getSdgx() {
        return sdgx;
    }

    public void setSdgx(String sdgx) {
        this.sdgx = sdgx;
    }

    public String getSygx() {
        return sygx;
    }

    public void setSygx(String sygx) {
        this.sygx = sygx;
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

    public List<FactorResult> getBdkczb() {
        return bdkczb;
    }

    public void setBdkczb(List<FactorResult> bdkczb) {
        this.bdkczb = bdkczb;
    }
}
