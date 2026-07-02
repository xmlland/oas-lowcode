package com.jeestudio.tools.base.utils;

/**
 * @Description: 随机数工具
 */
public class RandomUtil extends cn.hutool.core.util.RandomUtil {

    /**
     * 获取随机数
     * @param max 最大值
     * @param min 最小值
     * @param count 个数
     * @return
     */
    public static int[] generateNumber(int max,int min,int count){
        int[] randomNumber=new int [count];
        for (int i=0;i<count;i++) {
            randomNumber[i]= (int) (min + Math.random() * (max - min + 1));
        }
        return randomNumber;
    }
}
