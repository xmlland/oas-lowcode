package com.jeestudio.eco.soil.Enum;

/**
 * @Description: 农用地土壤污染风险管控标准 评价结果
 **/
/**
 * @Description: 土壤污染风险评价结果枚举
 */
public enum SoilEvaluationEnum {
    ExceedControlValue("超管制值"),
    BetweenScreeningValueAndControlValue("筛选值与管制值之间"),
    OTHER("-");
    private final String description;
    SoilEvaluationEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @Description: 根据指标值、筛选值、管制值 获取评价结果
     * @param itemValue 指标值
     * @param screeningValue 筛选值
     * @param controlValue 管制值
     * @return com.jeestudio.eco.soil.Enum.SoilAgriculturalEvaluation
     **/
    public static SoilEvaluationEnum getEvaluation(Double itemValue, Double screeningValue, Double controlValue) {
        SoilEvaluationEnum result = OTHER;
        if (itemValue == null) {
            return result;
        }

        if (controlValue != null && itemValue > controlValue) {
            result = ExceedControlValue;
        } else if (screeningValue != null && itemValue > screeningValue) {
            result = BetweenScreeningValueAndControlValue;
        }
        return result;
    }
}
