package com.jeestudio.eco.soil.entity;

import com.jeestudio.eco.water.DataUtil;
import com.jeestudio.eco.soil.Enum.SoilEvaluationEnum;
import com.jeestudio.eco.soil.Enum.SoilUseType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 土壤多指标评价结果
 */
public class SoilEvaluate {
    // 土地利用类型
    private SoilUseType soilUseType;
    // 土地利用类型-中文
    private String soilUseTypeStr;
    private String phValueStr;
    private Double phValue;
    // 总体评价
    private SoilEvaluationEnum evaluation;
    // 总体中文评价结果
    private String evaluationStr;
    // 指标列表 评价情况
    private List<SoilItemEvaluate> soilItemEvaluateList;
    // 超管制值 指标列表
    private List<SoilItemValue> exceedControlValueItem;
    // 筛选值与管制值之间 指标列表
    private List<SoilItemValue> BetweenScreeningValueAndControlValueItems;

    // 土地利用类型枚举
    public SoilUseType getSoilUseType() {
        return soilUseType;
    }

    public void setSoilUseType(SoilUseType soilUseType) {
        this.soilUseType = soilUseType;
    }

    // 土地利用类型字符串
    public String getSoilUseTypeStr() {
        return soilUseTypeStr;
    }

    public void setSoilUseTypeStr(String soilUseTypeStr) {
        this.soilUseTypeStr = soilUseTypeStr;
        if (soilUseTypeStr != null) {
            this.soilUseType = SoilUseType.fromString(soilUseTypeStr);
        }
    }

    // 总体评价
    public SoilEvaluationEnum getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(SoilEvaluationEnum evaluation) {
        this.evaluation = evaluation;
    }

    // 总体评价字符串
    public String getEvaluationStr() {
        if (evaluationStr == null && evaluation!= null) {
            evaluationStr = evaluation.getDescription();
        }
        return evaluationStr;
    }

    public void setEvaluationStr(String evaluationStr) {
        this.evaluationStr = evaluationStr;
    }

    // 综合评价指标，指标评价情况列表
    public List<SoilItemEvaluate> getSoilItemEvaluateList() {
        return soilItemEvaluateList;
    }

    public void setSoilItemEvaluateList(List<SoilItemEvaluate> soilItemEvaluateList) {
        this.soilItemEvaluateList = soilItemEvaluateList;
    }

    // 综合评价超管制值 指标列表
    public List<SoilItemValue> getExceedControlValueItem() {
        return exceedControlValueItem;
    }

    public void setExceedControlValueItem(List<SoilItemValue> exceedControlValueItem) {
        this.exceedControlValueItem = exceedControlValueItem;
    }

    // 综合评价筛选值与管制值之间 指标列表
    public List<SoilItemValue> getBetweenScreeningValueAndControlValueItems() {
        return BetweenScreeningValueAndControlValueItems;
    }

    public void setBetweenScreeningValueAndControlValueItems(List<SoilItemValue> betweenScreeningValueAndControlValueItems) {
        BetweenScreeningValueAndControlValueItems = betweenScreeningValueAndControlValueItems;
    }

    // ph值原始字符串
    public String getPhValueStr() {
        if (phValueStr == null && phValue!= null) {
            phValueStr = new BigDecimal(phValue).toString();
        }
        return phValueStr;
    }

    public void setPhValueStr(String phValueStr) {
        this.phValueStr = phValueStr;
    }

    // ph值
    public Double getPhValue() {
        return phValue;
    }

    public void setPhValue(Double phValue) {
        this.phValue = phValue;
    }

    /**
     * @Description: 综合评价数据
     * @param soilUseType 土地利用类型字符串
     * @param phValue ph值
     * @param evaluateList 指标评价情况列表
     * @return void
     **/
    public void setEvaluationData(String soilUseType, String phValue, List<SoilItemEvaluate> evaluateList) {
        this.setPhValueStr(phValue);
        this.setPhValue(DataUtil.getMonitorValueDouble(phValue));
        this.setEvaluationData(soilUseType, evaluateList);
    }
    /**
     * @Description: 综合评价数据
     * @param soilUseType 土地利用类型字符串
     * @param phValue ph值
     * @param evaluateList 指标评价情况列表
     * @return void
     **/
    public void setEvaluationData(String soilUseType, Double phValue, List<SoilItemEvaluate> evaluateList) {
        this.setPhValue(phValue);
        this.setEvaluationData(soilUseType, evaluateList);
    }
    /**
     * @Description: 综合评价数据
     * @param soilUseType 土地利用类型-枚举
     * @param evaluateList 指标评价情况列表
     * @return void
     **/
    public void setEvaluationData(String soilUseType, List<SoilItemEvaluate> evaluateList) {
        this.setSoilItemEvaluateList(evaluateList);
        this.setSoilUseTypeStr(soilUseType);
        this.setEvaluation(SoilEvaluationEnum.OTHER);

        if (evaluateList != null) {
            for (SoilItemEvaluate evaluate : evaluateList) {
                if (evaluate.getEvaluation() == SoilEvaluationEnum.ExceedControlValue) {
                    this.setEvaluation(SoilEvaluationEnum.ExceedControlValue);
                    if (this.exceedControlValueItem == null) {
                        this.exceedControlValueItem = new ArrayList<>();;
                    }
                    this.exceedControlValueItem.add(new SoilItemValue(evaluate.getItemName()));
                }else if (evaluate.getEvaluation() == SoilEvaluationEnum.BetweenScreeningValueAndControlValue) {
                    // 如果已经为超管制值，不再设置为筛选值与管制值之间
                    if (this.evaluation != SoilEvaluationEnum.ExceedControlValue) {
                        this.setEvaluation(SoilEvaluationEnum.BetweenScreeningValueAndControlValue);
                    }

                    if (this.BetweenScreeningValueAndControlValueItems == null) {
                        this.BetweenScreeningValueAndControlValueItems = new ArrayList<>();
                    }
                    this.BetweenScreeningValueAndControlValueItems.add(new SoilItemValue(evaluate.getItemName()));
                }
            }
        }

    }
}
