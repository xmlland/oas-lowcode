package com.jeestudio.bpm.utils;

import com.jeestudio.tools.base.utils.ConvertUtil;

/**
 * @Description: 字符串相似度比较工具
 */
public class CompareUtil {

    // 计算两个字符串的Levenshtein距离
    private static int calculateLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[str1.length()][str2.length()];
    }

    /**
     * 计算两个字符串的相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度 0-1
     */
    public static double calculateSimilarity(String str1, String str2) {
        str1 = ConvertUtil.getString(str1);
        str2 = ConvertUtil.getString(str2);
        if (str1.equals(str2)) {
            return 1.0;
        }
        int distance = calculateLevenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return 1.0 - (double) distance / maxLength;
    }

}
