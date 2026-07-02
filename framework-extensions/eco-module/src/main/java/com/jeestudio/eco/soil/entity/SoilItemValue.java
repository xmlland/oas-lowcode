package com.jeestudio.eco.soil.entity;

import com.jeestudio.eco.water.DataUtil;

import java.math.BigDecimal;

/**
 * @Description: 土壤单指标评价数据
 */
public class SoilItemValue {
    // 指标名称-中文
    private String itemName;
    // 指标名称-值
    private String itemValueStr;
    private Double itemValue;

    public SoilItemValue() {
    }

    public SoilItemValue(String itemName) {
        this.itemName = itemName;
    }

    public SoilItemValue(String itemName, String itemValueStr) {
        this.itemName = itemName;
        setItemValueStr(itemValueStr);
    }
    public SoilItemValue(String itemName, double itemValue) {
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.itemValueStr = String.valueOf(itemValue);
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemValueStr() {
        if (itemValueStr == null && itemValue != null) {
            itemValueStr = new BigDecimal(itemValue).toString();
        }
        return itemValueStr;
    }

    public void setItemValueStr(String itemValueStr) {
        this.itemValueStr = itemValueStr;
    }

    public Double getItemValue() {
        if (itemValue == null && itemValueStr!= null) {
            itemValue = DataUtil.getMonitorValueDouble(itemValueStr);
        }
        return itemValue;
    }

    public void setItemValue(Double itemValue) {
        this.itemValue = itemValue;
    }
}
