package com.jeestudio.eco.soil;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.eco.soil.entity.SoilEvaluate;
import com.jeestudio.eco.soil.entity.SoilItemEvaluate;
import com.jeestudio.eco.soil.entity.SoilItemValue;
import com.jeestudio.eco.water.entity.MonitorValueEntity;
import com.jeestudio.eco.soil.Enum.SoilUseType;

import java.util.ArrayList;
import java.util.List;
/**
 * @Description: 土壤质量评价工具
 */

public class SoilQualityAssessor {

    /**
     * @Description: 评价 土壤单指标结果根据 soilUseTypeStr 土地利用类型 区分 农用地和建设用地
     * @param soilUseTypeStr 土地利用类型
     * @param itemField 指标名称
     * @param phValueStr ph值
     * @param itemValueStr 指标值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilItemEvaluate soilItemEvaluate(String soilUseTypeStr, String phValueStr, String itemField, String itemValueStr) {
        Double phValue = new MonitorValueEntity(phValueStr).getValueDouble();
        Double itemValue = new MonitorValueEntity(itemValueStr).getValueDouble();
        SoilUseType soilUseType = SoilUseType.fromString(soilUseTypeStr);
        if (soilUseType == null) {
            throw new RuntimeException("未知土地利用类型: " + soilUseTypeStr);
        }
        // 如果是 建设用地 则调用建设用地评价方法
        if (soilUseType.isConstructionLand()){
            return soilConstructionlItemEvaluate(soilUseTypeStr,itemField,itemValueStr);
        }
        return soilAgriculturalItemEvaluate(soilUseTypeStr, phValue, itemField, itemValue);
    }

    /**
     * @Description: 评价土壤多指标结果
     * @param soilUseTypeStr 土地利用类型
     * @param phValueStr ph值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilEvaluate soilEvaluate(String soilUseTypeStr, String phValueStr, List<SoilItemValue> soilItemValueList) {
        Double phValue = new MonitorValueEntity(phValueStr).getValueDouble();
        return soilEvaluate(soilUseTypeStr,phValue,soilItemValueList);
    }

    /**
     * @Description: 评价农用地土壤多指标数据结果
     * @param soilUseTypeStr 土地利用类型
     * @param soilItemValueList 指标值列表
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilEvaluate soilEvaluate(String soilUseTypeStr, Double phValue, List<SoilItemValue> soilItemValueList) {
        SoilUseType soilUseType = SoilUseType.fromString(soilUseTypeStr);
        if (soilUseType == null) {
            throw new RuntimeException("未知土地利用类型: " + soilUseTypeStr);
        }
        // 如果是 建设用地 则调用建设用地评价方法
        if (soilUseType.isConstructionLand()){
            return soilConstructionlEvaluate(soilUseTypeStr,soilItemValueList);
        }
        return soilAgriculturalEvaluate(soilUseTypeStr, phValue, soilItemValueList);
    }


    /**
     * @Description: 评价农用地土壤单指标结果
     * @param soilUseType 土地利用类型
     * @param itemField 指标名称
     * @param phValueStr ph值
     * @param itemValueStr 指标值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilItemEvaluate soilAgriculturalItemEvaluate(String soilUseType,String phValueStr, String itemField, String itemValueStr) {
        SoilItemEvaluate evaluate = new SoilItemEvaluate();
        evaluate.evaluateAgriculturaItem(itemField,itemValueStr,phValueStr,soilUseType);
        return evaluate;
    }
    /**
     * @Description: 评价农用地土壤单指标结果
     * @param soilUseType 土地利用类型
     * @param itemField 指标名称
     * @param phValue ph值
     * @param itemValue 指标值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilItemEvaluate soilAgriculturalItemEvaluate(String soilUseType, Double phValue,String itemField, Double itemValue) {
//        if (phValue == null || itemValue == null) {
//            throw new RuntimeException("当前指标" +itemField+"数据："+itemValue+",或ph指标数据："+phValue+"，为空。");
//        }

        SoilItemEvaluate evaluate = new SoilItemEvaluate();
        evaluate.evaluateAgriculturaItem(itemField,itemValue,phValue,soilUseType);
        return evaluate;
    }
    /**
     * @Description: 评价农用地土壤多指标数据结果
     * @param soilUseType 土地利用类型
     * @param phValueStr ph值
     * @param soilItemValueList 指标值列表
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilEvaluate soilAgriculturalEvaluate(String soilUseType, String phValueStr, List<SoilItemValue> soilItemValueList) {
        List<SoilItemEvaluate> evaluateList = new ArrayList<>();
        soilItemValueList.forEach(soilItemValue -> {
            SoilItemEvaluate soilItemEvaluate = soilAgriculturalItemEvaluate(soilUseType, phValueStr, soilItemValue.getItemName(), soilItemValue.getItemValueStr());
            evaluateList.add(soilItemEvaluate);
        });
        SoilEvaluate soilEvaluate = new SoilEvaluate();
        soilEvaluate.setEvaluationData(soilUseType,phValueStr,evaluateList);
        return soilEvaluate;
    }

    /**
     * @Description: 评价农用地土壤多指标数据结果
     * @param soilUseType 土地利用类型
     * @param soilItemValueList 指标值列表
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilEvaluate soilAgriculturalEvaluate(String soilUseType, Double phValue, List<SoilItemValue> soilItemValueList) {
        List<SoilItemEvaluate> evaluateList = new ArrayList<>();
        soilItemValueList.forEach(soilItemValue -> {
            SoilItemEvaluate soilItemEvaluate = soilAgriculturalItemEvaluate(soilUseType, phValue, soilItemValue.getItemName(), soilItemValue.getItemValue());
            evaluateList.add(soilItemEvaluate);
        });
        SoilEvaluate soilEvaluate = new SoilEvaluate();
        soilEvaluate.setEvaluationData(soilUseType,phValue,evaluateList);
        return soilEvaluate;
    }

    /**
     * @Description: 评价建设用地土壤单指标结果
     * @param soilUseType 土地利用类型
     * @param itemField 指标名称
     * @param itemValueStr 指标值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilItemEvaluate soilConstructionlItemEvaluate(String soilUseType,String itemField, String itemValueStr) {
        Double itemValue = new MonitorValueEntity(itemValueStr).getValueDouble();
        return soilConstructionlItemEvaluate(soilUseType, itemField, itemValue);
    }

    /**
     * @Description: 评价农用地土壤单指标结果
     * @param soilUseType 土地利用类型
     * @param itemField 指标名称
     * @param itemValue 指标值
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilItemEvaluate soilConstructionlItemEvaluate(String soilUseType, String itemField, Double itemValue) {
//        if (itemValue == null) {
//            throw new RuntimeException("当前指标" +itemField+"数据："+itemValue+",为空。");
//        }

        SoilItemEvaluate evaluate = new SoilItemEvaluate();
        evaluate.evaluateConstructionlItem(itemField,itemValue,soilUseType);
        return evaluate;
    }

    /**
     * @Description: 评价农用地土壤多指标数据结果
     * @param soilUseType 土地利用类型
     * @param soilItemValueList 指标值列表
     * @return com.jeestudio.eco.soil.entity.SoilItemEvaluate
     **/
    public static SoilEvaluate soilConstructionlEvaluate(String soilUseType, List<SoilItemValue> soilItemValueList) {
        List<SoilItemEvaluate> evaluateList = new ArrayList<>();
        soilItemValueList.forEach(soilItemValue -> {
            SoilItemEvaluate soilItemEvaluate = soilConstructionlItemEvaluate(soilUseType, soilItemValue.getItemName(), soilItemValue.getItemValue());
            evaluateList.add(soilItemEvaluate);
        });
        SoilEvaluate soilEvaluate = new SoilEvaluate();
        soilEvaluate.setEvaluationData(soilUseType,evaluateList);
        return soilEvaluate;
    }

    public static void main(String[] args) {
        SoilItemEvaluate soilItemEvaluate = soilAgriculturalItemEvaluate("水田", "7", "镉", "34");

        System.out.println(JSONObject.toJSONString(soilItemEvaluate));
        List<SoilItemValue> soilItemValueList = new ArrayList<SoilItemValue>(){{
            add(new SoilItemValue("镉","34"));
            add(new SoilItemValue("汞","0.043"));
            add(new SoilItemValue("砷","7.22"));
            add(new SoilItemValue("铅","14.9"));
            add(new SoilItemValue("铬","53.0"));
            add(new SoilItemValue("铜","26.0"));
            add(new SoilItemValue("锌","50.0"));
            add(new SoilItemValue("镍","16.0"));
            add(new SoilItemValue("六六六总量","0"));
            add(new SoilItemValue("滴滴涕总量","0"));
            add(new SoilItemValue("苯并a芘","0.1L"));
        }};
        SoilEvaluate soilEvaluate = soilAgriculturalEvaluate("耕地-旱地", 8.86, soilItemValueList);
        System.out.println(JSONObject.toJSONString(soilEvaluate));
    }

}
