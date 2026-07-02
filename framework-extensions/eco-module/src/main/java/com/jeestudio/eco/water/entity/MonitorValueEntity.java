package com.jeestudio.eco.water.entity;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 监测值处理实体
 */
public class MonitorValueEntity {
    // 监测项名称
    private String itemName;
    // 监测项代码
    private String itemCode;
    // 监测值(字符串)
    private String valueStr;
    // 监测值是否可以转换成数值
    private boolean isToDouble = false;
    // 监测值(数值)
    private Double valueDouble;
    // 监测值(数值)
    private BigDecimal valueBigDecimal;
    // 是否超出出检测限
    private boolean detectionLimit = false;

    public MonitorValueEntity() {
    }

    public MonitorValueEntity(String itemName, String itemCode, String valueStr) {
        this.itemName = itemName;
        this.itemCode = itemCode;
        setValueStr(valueStr);
    }

    public MonitorValueEntity(String valueStr) {
        setValueStr(valueStr);
    }
    public MonitorValueEntity(Double valueStr) {
        setValue(valueStr);
    }
    public MonitorValueEntity(BigDecimal valueStr) {
        setValue(valueStr);
    }

    /**
     * 监测项名称
     **/
    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * 监测项编码
     **/
    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    /**
     * 指标值(字符串)
     **/
    public String getValueStr() {
        return this.valueStr;
    }

    public void setValueStr(String valueStr) {
        if (StringUtils.isNotEmpty( valueStr )){
            valueStr=valueStr.replaceAll("\\s+","");
        }
        this.isToDouble = checkVal(valueStr);
        this.valueStr = valueStr;
    }

    /**
     * 是否超出出检测限
     **/
    public boolean isDetectionLimit() {
        return detectionLimit;
    }

    public void setDetectionLimit(boolean detectionLimit) {
        this.detectionLimit = detectionLimit;
    }

    /**
     * @Description: 直接设置数据，将不进行检出限相关处理，仅设置数据
     * @param value 数据值
     **/
    public void setValue(Double value) {
        if (value != null) {
            this.isToDouble = true;
            this.valueDouble = value;
            this.valueBigDecimal = BigDecimal.valueOf(value);
            this.valueStr = this.valueBigDecimal.toPlainString();
        }else {
            this.isToDouble = false;
            this.valueDouble = null;
            this.valueBigDecimal = null;
            this.valueStr = null;
        }
    }
    /**
     * @Description: 直接设置数据，将不进行检出限相关处理，仅设置数据
     * @param value 数据值
     **/
    public void setValue(BigDecimal value) {
        if (value != null) {
            this.isToDouble = true;
            this.valueBigDecimal = value;
            this.valueDouble = value.doubleValue();
            this.valueStr = this.valueBigDecimal.toPlainString();
        }else {
            this.isToDouble = false;
            this.valueDouble = null;
            this.valueBigDecimal = null;
            this.valueStr = null;
        }
    }

    /**
     * 将监测值处理成正确的数值
     * 监测结果：
     * 不参评（未检出、-1、空字符、null），
     * 按检出限处理（不等于-1的其他负值、结尾是小写字母l、结尾是大写字母L、开头是＜、开头是<）、
     * 如果传入的监测结果是“有”或“无”，处理成1或0
     * 特殊，若数据负值，判定为低于检出限,数据转换成绝对值，进行一半处理
     * @param valueStr 监测结果
     * @return 当前监测值是否可以转换成数值
     */
    private boolean checkVal(String valueStr) {
        this.detectionLimit = false;
        this.valueDouble = null;
        this.valueBigDecimal = null;
        try {
            if (StringUtils.isNotEmpty(valueStr) ) {
                if("有".equals(valueStr)||"无".equals(valueStr)) {
                    this.valueBigDecimal = new BigDecimal("有".equals(valueStr)?"1.0":"0.0");
                    this.valueDouble = this.valueBigDecimal.doubleValue();
                    return true;
                }
                String tempItemValue = valueStr.replaceAll("\\s+","");
                //  按检出限处理（不等于-1的其他负值、结尾是小写字母l、结尾是大写字母L、开头是＜、开头是<）、
                if (tempItemValue.endsWith("l") || tempItemValue.endsWith("L")
                        || tempItemValue.startsWith("<") || tempItemValue.startsWith("＜")) {
                    this.detectionLimit = true;
                    tempItemValue= tempItemValue.replaceAll("l","")
                            .replaceAll("L","")
                            .replaceAll("<","")
                            .replaceAll("＜","");
                    if(tempItemValue.startsWith("-")){
                        return false;
                    }
                }
                this.valueBigDecimal = new BigDecimal(tempItemValue);
                // 内蒙  负值 判定为低于检出限
                if (this.valueBigDecimal.compareTo(BigDecimal.ZERO) < 0) {
                    this.detectionLimit = true;
                    this.valueBigDecimal = this.valueBigDecimal.abs();
                }
                // 如果是 超出检出限 则进行 一半
                if (this.detectionLimit){
                    this.valueBigDecimal = this.valueBigDecimal.divide(BigDecimal.valueOf(2), 20, RoundingMode.HALF_UP);
                }
                this.valueDouble = this.valueBigDecimal.doubleValue();
                return true;
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isToDouble() {
        return this.isToDouble;
    }

    public Double getValueDouble() {
        return this.valueDouble;
    }
    public BigDecimal getValueBigDecimal() {
        return this.valueBigDecimal;
    }


}
