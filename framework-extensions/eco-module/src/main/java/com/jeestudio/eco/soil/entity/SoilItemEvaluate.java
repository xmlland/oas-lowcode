package com.jeestudio.eco.soil.entity;

import com.jeestudio.eco.water.DataUtil;
import com.jeestudio.eco.soil.Enum.SoilEvaluationEnum;
import com.jeestudio.eco.soil.Enum.SoilAgriculturalItem;
import com.jeestudio.eco.soil.Enum.SoilConstructionlItem;
import com.jeestudio.eco.soil.Enum.SoilUseType;

import java.math.BigDecimal;

/**
 * @Description: 土壤单指标评价结果
 */
public class SoilItemEvaluate {
    //  农用地评价指标 标准
    private SoilAgriculturalItem soilAgriculturalItem;
    //  建设用地评价指标 标准
    private SoilConstructionlItem soilConstructionlItem;
    // 指标名称-标准中
    private String itemName;
    // 传入的指标名称
    private String itemNameOriginal;
    private Double itemValue;
    private String itemValueOriginal;
    // 筛选值
    private Double screeningValue;
    // 管制值
    private Double controlValue;
    // 评价结果
    private SoilEvaluationEnum evaluation;
    // 评价结果-字符串
    private String evaluationStr;
    // ph值-字符串
    private String phValue;

    /**
     * @Description: 评价农用地土壤单指标结果
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @param phValue ph值
     * @param soilUseTypeStr 土地利用类型
     * @return
     **/
    public void evaluateAgriculturaItem(String itemField, String itemValue, String phValue, String soilUseTypeStr) {
        this.phValue = phValue;
        this.itemValueOriginal = itemValue;
        evaluateAgriculturaItem(itemField,  DataUtil.getMonitorValueDouble(itemValue), DataUtil.getMonitorValueDouble(phValue), soilUseTypeStr);
    }
    public void evaluateAgriculturaItem(String itemField, Double itemValue, String phValue, String soilUseTypeStr) {
        this.phValue = phValue;
        evaluateAgriculturaItem(itemField, itemValue, DataUtil.getMonitorValueDouble(phValue), soilUseTypeStr);
    }
    /**
     * @Description: 评价农用地土壤单指标结果
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @param phValue ph值
     * @param soilUseTypeStr 土地利用类型
     * @return
     **/
    public void evaluateAgriculturaItem(String itemField, Double itemValue, Double phValue, String soilUseTypeStr) {
        SoilUseType soilUseType = SoilUseType.fromString(soilUseTypeStr);
        evaluateAgriculturaItem(itemField, itemValue, phValue, soilUseType);
    }
    /**
     * @Description: 评价农用地土壤单指标结果
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @param phValue ph值
     * @param soilUseType 土地利用类型
     * @return void
     **/
    public void evaluateAgriculturaItem(String itemField, Double itemValue, Double phValue, SoilUseType soilUseType) {
        this.soilAgriculturalItem = SoilAgriculturalItem.fromString(itemField);
        if (soilAgriculturalItem == null) {
            return;
        }
        this.screeningValue = soilAgriculturalItem.getScreeningValue(phValue, soilUseType);
        this.controlValue = soilAgriculturalItem.getControlValue(phValue);
        this.evaluation = SoilEvaluationEnum.getEvaluation(itemValue, screeningValue, controlValue);
        this.evaluationStr = evaluation.getDescription();
        this.itemName = soilAgriculturalItem.getItemName();
        this.itemNameOriginal = itemField;
        this.itemValue = itemValue;
        if (phValue!=null) {
            this.phValue = new BigDecimal(phValue).toString();
        }
    }

    /**
     * @Description: 评价建设用地土壤单指标结果
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @param soilUseTypeStr 土地利用类型
     * @return void
     **/
    public void evaluateConstructionlItem(String itemField, Double itemValue, String soilUseTypeStr) {
        SoilUseType soilUseType = SoilUseType.fromString(soilUseTypeStr);
        evaluateConstructionlItem(itemField, itemValue, soilUseType);
    }

    /**
     * @Description: 评价建设用地土壤单指标结果
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @param soilUseType 土地利用类型
     * @return void
     **/
    public void evaluateConstructionlItem(String itemField, Double itemValue, SoilUseType soilUseType) {
        this.soilConstructionlItem = SoilConstructionlItem.fromString(itemField);
        if (soilConstructionlItem == null) {
            return;
        }
        this.screeningValue = soilConstructionlItem.getScreeningValue(soilUseType);
        this.controlValue = soilConstructionlItem.getControlValue(soilUseType);
        this.evaluation = SoilEvaluationEnum.getEvaluation(itemValue, screeningValue, controlValue);
        this.evaluationStr = evaluation.getDescription();
        this.itemName = soilConstructionlItem.getItemName();
        this.itemNameOriginal = itemField;
    }

    public SoilAgriculturalItem getSoilAgriculturalItem() {
        return soilAgriculturalItem;
    }

    public void setSoilAgriculturalItem(SoilAgriculturalItem soilAgriculturalItem) {
        this.soilAgriculturalItem = soilAgriculturalItem;
        if (soilAgriculturalItem!=null){
            this.itemName = soilAgriculturalItem.getItemName();
        }
    }

    public SoilConstructionlItem getSoilConstructionlItem() {
        return soilConstructionlItem;
    }

    public void setSoilConstructionlItem(SoilConstructionlItem soilConstructionlItem) {
        this.soilConstructionlItem = soilConstructionlItem;
        if (soilConstructionlItem!=null){
            this.itemName = soilConstructionlItem.getItemName();
        }
    }

    public Double getScreeningValue() {
        return screeningValue;
    }

    public void setScreeningValue(Double screeningValue) {
        this.screeningValue = screeningValue;
    }

    public Double getControlValue() {
        return controlValue;
    }

    public void setControlValue(Double controlValue) {
        this.controlValue = controlValue;
    }

    public SoilEvaluationEnum getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(SoilEvaluationEnum evaluation) {
        this.evaluation = evaluation;
        if (evaluation!=null){
            this.evaluationStr = evaluation.getDescription();
        }
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemNameOriginal() {
        return itemNameOriginal;
    }

    public String getEvaluationStr() {
        return evaluationStr;
    }

    public String getPhValue() {
        return phValue;
    }

    public Double getItemValue() {
        return itemValue;
    }

    public String getItemValueOriginal() {
        if (itemValueOriginal==null && itemValue!=null) {
            itemValueOriginal = new BigDecimal(itemValue).toString();
        }
        return itemValueOriginal;
    }
}
