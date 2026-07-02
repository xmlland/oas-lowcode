package com.jeestudio.eco.water;

import java.util.Map;

/**
 * @Description: 水环境污染因子评价结果
 */
public class FactorResult {
    private String itemCode;  // 指标编码
    private String itemName;  // 指标名称
    private String itemValue;  // 监测结果
    private String itemSzlb;  // 指标水质（1-6）
    private String targetValue;  // 目标值
    private String cbbs;  // 超标倍数

    private String itemCodeYs;     // 原始指标编码

    private String bcp;  // 1代表该指标不参与综合水质、定类因子等评价

    private Map<String,Object> extendData;  // 扩展数据

    // Getters and Setters
    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getItemSzlb() {
        return itemSzlb;
    }

    public void setItemSzlb(String itemSzlb) {
        this.itemSzlb = itemSzlb;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getCbbs() {
        return cbbs;
    }

    public void setCbbs(String cbbs) {
        this.cbbs = cbbs;
    }

    public String getItemCodeYs() {
        return itemCodeYs;
    }

    public void setItemCodeYs(String itemCodeYs) {
        this.itemCodeYs = itemCodeYs;
    }

    public String getBcp() {
        return bcp;
    }

    public void setBcp(String bcp) {
        this.bcp = bcp;
    }

    public Map<String, Object> getExtendData() {
        return extendData;
    }

    public void setExtendData(Map<String, Object> extendData) {
        this.extendData = extendData;
    }
}
