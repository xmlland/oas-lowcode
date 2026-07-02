package com.jeestudio.eco.soil.Enum;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 土壤土地利用类型枚举
 */
public enum SoilUseType {
    PaddyField("水田"),
    Orchard("果园"),
    Other("其他"),
    ForestLand("林地"),
    CultivatedLandPaddyField("耕地-水田"),
    CultivatedDryLand("耕地-旱地"),
    GardenPlotOrchard("园地-果园"),
    GardenPlotTeaGarden("园地-茶园"),
    GrasslandNaturalPasture("草地-天然牧草地"),
    GrasslandArtificialGrassland("草地-人工牧草地"),
    AgriculturalLand("农用地"),
    UnusedLand("未利用地"),
    TheFirstTypeOfLandUse("第一类用地"),
    TheSecondTypeOfLandUse("第二类用地"),
    ConstructionLand("建设用地");

    private final String soilUseTypeName;
    SoilUseType(String soilUseTypeName) {
        this.soilUseTypeName = soilUseTypeName;
    }
    // 根据名称获取枚举
    public static SoilUseType fromString(String soilUseTypeName) {
        if (soilUseTypeName == null) {
            return null;
        }
        soilUseTypeName = StringUtils.trimToEmpty(soilUseTypeName);
        for (SoilUseType item : values()) {
            if (item.soilUseTypeName.equals(soilUseTypeName)) {
                return item;
            }
        }
        return null;
    }
    // 获取是否为水田
    public boolean isPaddyField() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return this.soilUseTypeName.contains("水田");
    }
    // 获取是否为果园
    public boolean isOrchard() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return this.soilUseTypeName.contains("果园");
    }
    // 获取是否是否为农用地，非建设用地、未利用地即为农用地、
    public boolean isCultivatedLand() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return !isConstructionLand() && !isUnusedLand();
    }
    // 获取是否是否为农用地、未利用地 ,非建设用地即为农用地、未利用地
    public boolean isCultivatedLandOrUnusedLand() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return !isConstructionLand();
    }
    // 获取是否是否为建设用地
    public boolean isConstructionLand() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return this.soilUseTypeName.contains("第一类用地")
                || this.soilUseTypeName.contains("第二类用地")
                || this.soilUseTypeName.contains("建设用地");
    }
    // 获取是否是否为未利用地
    public boolean isUnusedLand() {
        if (StringUtils.isEmpty(this.soilUseTypeName)){
            return false;
        }
        return this.soilUseTypeName.contains("未利用地");
    }
}
