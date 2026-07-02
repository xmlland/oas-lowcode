package com.jeestudio.bpm.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @Description: 数学计算工具
 **/
public class MathUtil {

    public static String doubleToString(Double d, int n){
        // 不足两位小数补0
        String f = "0.";
        for (int i = 0; i < n; i++) {
            f += "0";
        }
        f += "#";
        DecimalFormat decimalFormat = new DecimalFormat(f);
        String strVal = decimalFormat.format(new BigDecimal(d).setScale(n, BigDecimal.ROUND_HALF_UP).doubleValue());
        return strVal;
    }

    public static Double stringToDouble(String d, int n){
        // 不足两位小数补0
        String f = "0.";
        for (int i = 0; i < n; i++) {
            f += "0";
        }
        f += "#";
        DecimalFormat decimalFormat = new DecimalFormat(f);
        String strVal = decimalFormat.format(new BigDecimal(d).setScale(n, BigDecimal.ROUND_HALF_UP).doubleValue());
        return Double.parseDouble(strVal);
    }

    /**
     * 将数据已百分之（按照指定精度）输出
     * @param arr 数组
     * @param sum 总数
     * @param idx 索引
     * @param precision 精度
     * @return
     */
    public static double getPercentValue(int[] arr,double sum,int idx,int precision){
        if((arr.length-1) < idx){
            return 0;
        }
        //求和
        if(sum <= 0){
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i];
            }
        }
        //10的2次幂是100，用于计算精度。
        double digits = Math.pow(10,precision);
        //扩大比例100
        double[] votesPerQuota = new double[arr.length];
        for(int i = 0; i < arr.length; i++){
            double val = arr[i] / sum * digits * 100;
            votesPerQuota[i] = val;
        }
        //总数,扩大比例意味的总数要扩大
        double targetSeats = digits * 100;
        //再向下取值，组成数组
        double[] seats = new double[arr.length];
        for(int i = 0; i < votesPerQuota.length; i++){
            seats[i] = Math.floor(votesPerQuota[i]);
        }
        //再新计算合计，用于判断与总数量是否相同,相同则占比会100%
        double currentSum = 0;
        for (int i = 0; i < seats.length; i++) {
            currentSum += seats[i];
        }
        //余数部分的数组:原先数组减去向下取值的数组,得到余数部分的数组
        double[] remainder = new double[arr.length];
        for(int i = 0; i < seats.length; i++){
            remainder[i] = votesPerQuota[i] - seats[i];
        }
        while(currentSum < targetSeats){
            double max = 0;
            int maxId = 0;
            int len = 0;
            for(int i = 0;i < remainder.length;++i){
                if(remainder[i] > max){
                    max = remainder[i];
                    maxId = i;
                }
            }
            //对最大项余额加1
            ++seats[maxId];
            //已经增加最大余数加1,则下次判断就可以不需要再判断这个余额数。
            remainder[maxId] = 0;
            //总的也要加1,为了判断是否总数是否相同,跳出循环。
            ++currentSum;
        }
        // 这时候的seats就会总数占比会100%
        return seats[idx] / digits;
    }

    public static String add(String num1, String num2) {
        // 将输入的字符串转换为BigDecimal类型
        BigDecimal bd1 = new BigDecimal(num1);
        BigDecimal bd2 = new BigDecimal(num2);

        // 获取输入参数的小数位数
        int scale1 = bd1.scale();
        int scale2 = bd2.scale();
        int maxScale = Math.max(scale1, scale2);

        // 将两个数相加，并设置结果的小数位数为最大精度
        BigDecimal result = bd1.add(bd2).setScale(maxScale, BigDecimal.ROUND_HALF_UP);

        // 将结果转换为字符串并返回
        return result.toString();
    }

    public static String subtract(String num1, String num2) {
        // 将输入的字符串转换为BigDecimal对象
        BigDecimal bd1 = new BigDecimal(num1);
        BigDecimal bd2 = new BigDecimal(num2);

        // 获取输入数字的小数点位数
        int scale1 = getScale(num1);
        int scale2 = getScale(num2);

        // 确定结果的小数点位数（取输入数字的最大位数）
        int resultScale = Math.max(scale1, scale2);

        // 执行相减操作
        BigDecimal result = bd1.subtract(bd2);

        // 设置结果的小数点位数
        result = result.setScale(resultScale, RoundingMode.HALF_UP);

        // 将结果转换为字符串并返回
        return result.toString();
    }

    // 获取数字的小数点位数
    private static int getScale(String num) {
        int decimalIndex = num.indexOf(".");
        if (decimalIndex != -1) {
            return num.length() - decimalIndex - 1;
        }
        return 0;
    }
}
