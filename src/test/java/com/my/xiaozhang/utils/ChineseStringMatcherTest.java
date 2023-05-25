package com.my.xiaozhang.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ChineseStringMatcherTest {

    @Test
    public void testMatch() {
        String s1 = "今天天气不错，适合出门散步";
        String s2 = "今天的天气真好啊，一定要出门走走";
        String s3 = "今天心情很不错，想要出去放松一下";
        String s4 = "今天真的太棒了，心情好极了";
        String s5 = "今天有点阴沉，不太想出门";
        String s6 = "明天天气好像会更好，期待";
        String s7 = "今天天气真好啊";
    }

        // 编辑距离算法，计算两个字符串的相似度得分
        public  int editDistance(String s1, String s2) {
            int len1 = s1.length();
            int len2 = s2.length();

            // 构建二维数组存储编辑距离
            int[][] dp = new int[len1 + 1][len2 + 1];

            // 初始化第一行和第一列
            for (int i = 0; i <= len1; i++) {
                dp[i][0] = i;
            }
            for (int j = 0; j <= len2; j++) {
                dp[0][j] = j;
            }

            // 计算编辑距离
            for (int i = 1; i <= len1; i++) {
                for (int j = 1; j <= len2; j++) {
                    if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        int insert = dp[i][j - 1] + 1;
                        int delete = dp[i - 1][j] + 1;
                        int replace = dp[i - 1][j - 1] + 1;
                        dp[i][j] = Math.min(Math.min(insert, delete), replace);
                    }
                }
            }

            // 返回相似度得分
            return dp[len1][len2];
        }

        // 分词，将中文字符串转化为词语列表
        public List<String> tokenize(String str) {
            List<String> tokens = new ArrayList<>();
            String regex = "[\u4e00-\u9fa5]+"; // 匹配中文
            String[] words = str.split(regex);
            for (String word : words) {
                if (!word.isEmpty()) {
                    tokens.add(word);
                }
            }
            return tokens;
        }

}
