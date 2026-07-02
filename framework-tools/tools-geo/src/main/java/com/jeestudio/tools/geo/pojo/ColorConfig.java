package com.jeestudio.tools.geo.pojo;

import cn.hutool.core.img.ColorUtil;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 颜色配置
 */
public class ColorConfig implements java.io.Serializable {

    private static final long serialVersionUID = -7548099785165467032L;

    // 默认透明度
    public static final int DEFAULT_OPACITY = 50;
    /*
     * 空气质量指数颜色数组
     **/
    public static final List<ColorLevel> AIR_AQI_COLOR_ARRAY = Arrays.asList(
            new ColorLevel("#00e400", 50.0),
            new ColorLevel("#ffff00", 100.0),
            new ColorLevel("#ff7e00", 150.0),
            new ColorLevel("#ff0000", 200.0),
            new ColorLevel("#99004c", 300.0),
            new ColorLevel("#7e0023", 400.0)
    );

    public static ColorConfig getAirAqiColorConfig() {
        ColorConfig colorConfig = new ColorConfig();
        colorConfig.setColorArray(AIR_AQI_COLOR_ARRAY);
        return colorConfig;
    }
    public static ColorConfig autoLevel() {
        return getAirAqiColorConfig();
    }


    /**
     * @Description: 根据数据生成等级分类，默认使用 AQI颜色等级
     * @param dataList 数据列表
     * @return java.awt.Color
     **/
    public static ColorConfig autoLevel(List<PointData> dataList) {
        return autoColorLevel(dataList
                ,AIR_AQI_COLOR_ARRAY.stream().map(ColorLevel::getColor).collect(Collectors.toList())
                ,DEFAULT_OPACITY
        );
    }
    /**
     * @Description: 根据数据生成等级分类，使用颜色列表区分等级
     * @param dataList 数据列表
     * @param colorList 颜色列表
     * @return java.awt.Color
     **/
    public static ColorConfig autoColorLevel(List<PointData> dataList, List<String> colorList) {
        return autoColorLevel(dataList,colorList,DEFAULT_OPACITY);
    }
    /**
     * 根据数据生成等级分类，使用颜色列表区分等级
     * @param dataList 数据列表
     * @param colorList 颜色列表
     * @param opacity 透明度
     * @return java.awt.Color
     */
    public static ColorConfig autoColorLevel(List<PointData> dataList, List<String> colorList, int opacity) {
        List<Double> collect = dataList.stream().map(PointData::getValue).sorted().collect(Collectors.toList());

        ColorConfig colorConfigs = new ColorConfig();
        colorConfigs.setOpacity(opacity);
        // 如果数据量大于 20，去除首尾
        int start = collect.size() > 20 ? 1 : 0;
        double min = collect.get(start);
        double step = 0;
        if (collect.size() > 2) {
            step = (
                    (BigDecimal.valueOf(collect.get( collect.size() - 1- start ))
                            .subtract(BigDecimal.valueOf(min)))
                            .divide(BigDecimal.valueOf(colorList.size()),10, RoundingMode.HALF_UP)
            ).doubleValue();
        }
        System.out.println("colorConfigs: step:"+step);
        List<ColorLevel> colorArray = new ArrayList<>();
        for (String s : colorList) {
            ColorLevel colorLevel = new ColorLevel(s, min);
            colorArray.add(colorLevel);
            min = (min + step);
            System.out.println("colorConfigs: color:" + colorLevel.getColor() + "，value：" + colorLevel.getValue());
        }
        colorConfigs.setColorArray(colorArray);

        return colorConfigs;
    }

    // 透明度 0- 100
    private int opacity;

    private List<ColorLevel> colorArray;

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public List<ColorLevel> getColorArray() {
        return colorArray;
    }

    public void setColorArray(List<ColorLevel> colorArray) {
        this.colorArray = colorArray;
    }

    /**
     * @Description: 根据数据值，获取对应值的颜色等级
     * @param val 数据值
     * @return java.lang.Double
     **/
    public double getLevel(Double val) {
        //根据颜色配置将值转换为对应的等级
        double level = 1;
        int lastSize = colorArray.size() - 1;
        for (int i = 0; i < lastSize; i++) {
            //当前级别的值
            double currentLevelValue = colorArray.get(i).getValue();
            //下一级别的值
            double nextLevelValue = colorArray.get(i + 1).getValue();
            if (val > currentLevelValue && val <= nextLevelValue) {
                level = i + 1;
//                // 如果值在当前级别和下一级别之间，则计算插值
//                double interpolation = (val - currentLevelValue) / (nextLevelValue - currentLevelValue);
//                level += interpolation;
                break;
            }
        }
        if (val >= colorArray.get(lastSize).getValue()) {
//            double currentLevelValue = colorArray.get(lastSize - 1).getValue();
//            //下一级别的值
//            double nextLevelValue = colorArray.get(lastSize).getValue();
//            return colorArray.size() + (val - nextLevelValue) / (nextLevelValue - currentLevelValue);
            return colorArray.size();
        }
        return level;
    }

    /**
     * @Description: 根据等级值，进行转换颜色，
     * @param levelVal 小数等级值
     * @return int
     **/
    public int getColor(Double levelVal) {
        if (levelVal <= 1) {
            //当val小于最小值时，返回最小值的颜色
            return hexStringToColor(colorArray.get(0).getColor(), opacity).getRGB();
        }else if (levelVal >= colorArray.size() - 1) {
            //当val大于最大值时，返回最大值的颜色
            return hexStringToColor(colorArray.get(colorArray.size() - 1).getColor(), opacity).getRGB();
        }else if (levelVal % 1 == 0) {
            //如果val等于某个值，则返回该值的颜色
            return hexStringToColor(colorArray.get(levelVal.intValue() - 1).getColor(), opacity).getRGB();
        }
        //当val不是整数时，根据val的值，计算出对应的颜色
        double decimal = levelVal % 1;
        int level = levelVal.intValue();
        int nextLevel = level + 1;
        Color color = hexStringToColor(colorArray.get(level - 1).getColor(), opacity);
        Color nextColor = hexStringToColor(colorArray.get(nextLevel - 1).getColor(), opacity);
        int red = (int) (color.getRed() + (nextColor.getRed() - color.getRed()) * decimal);
        int green = (int) (color.getGreen() + (nextColor.getGreen() - color.getGreen()) * decimal);
        int blue = (int) (color.getBlue() + (nextColor.getBlue() - color.getBlue()) * decimal);
        Color res = new Color(red, green, blue, opacity * 255 / 100);
        return res.getRGB();
    }

    /**
     * 根据颜色配置生成颜色
     *
     * @param hex     颜色配置
     * @param opacity 透明度 0- 100
     * @return java.awt.Color
     */
    public static Color hexStringToColor(String hex, int opacity) {
        Color color = ColorUtil.hexToColor(hex);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity * 255 / 100);
    }

    public static class ColorLevel {
        String color;
        Double value;

        public ColorLevel(String color, double value) {
            this.color = color;
            this.value = value;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }
}
