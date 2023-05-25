package com.my.xiaozhang.utils;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.my.xiaozhang.constant.UserConstant.MapConstant;


/**
 * @author:22603
 * @Date:2023/4/18 15:58
 */
public class AllUtils {
    /**
     * 判断字符串类型
     *
     * @param strings
     * @return
     */
    public static int getStrType(List<String> strings) {
        int num1 = 0, num2 = 0, num3 = 0, num4 = 0;
        for (String str : strings) {
            String pattern = "[\u4e00-\u9fa5]+"; // 中文字符的正则表达式
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(str);
            if (m.find()) { // 如果字符串中包含中文字符
                pattern = "[a-zA-Z]+"; // 英文字符的正则表达式
                p = Pattern.compile(pattern);
                m = p.matcher(str);
                if (m.find()) { // 如果字符串中包含英文字符
//                    return "中英文混合";
                    num1++;
                } else { // 如果字符串中不包含英文字符
//                    return "纯中文";
                    num2++;
                }
            } else { // 如果字符串中不包含中文字符
                pattern = "[a-zA-Z]+"; // 英文字符的正则表达式
                p = Pattern.compile(pattern);
                m = p.matcher(str);
                if (m.matches()) { // 如果字符串只包含英文字符
//                    return "纯英文";
                    num3++;
                } else { // 如果字符串包含英文字符和其他字符（如数字、符号等）
//                    return "英文和其他字符";
                    num4++;
                }
            }
        }
        if (num1 != 0) {
            return 0;
        } else if (num1 == 0 && num2 != 0) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Jacquard 相似度匹配
     *
     * @param list1
     * @param list2
     * @return
     */
    public static double calculateJaccardSimilarity(List<String> list1, List<String> list2) {
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    /**
     * 将字符串集合中的中文剔除
     *
     * @param strings
     * @return
     */
    public static List<String> tokenize(List<String> strings) {
        List<String> result = new ArrayList<>();
        for (String string : strings) {
            String regex = "[\u4e00-\u9fa5]+"; // 匹配中文字符
            String[] words = string.split(regex);
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.add(word);
                }
            }
        }
        return result;
    }

    /**
     * 余弦相似度算法
     *
     * @param list1
     * @param list2
     * @return
     */
    public static double cosineSimilarity(List<String> list1, List<String> list2) {
        Map<String, Integer> vector1 = new HashMap();
        list1.forEach(p -> {

            if (!vector1.containsKey(p)) {
                vector1.put(p, 1);
            } else {
                Integer value = vector1.get(p);

                vector1.put(p, value++);
            }
        });
        Map<String, Integer> vector2 = new HashMap();
        list2.forEach(p -> {

            if (!vector2.containsKey(p)) {
                vector2.put(p, 1);
            } else {
                Integer value = vector2.get(p);

                vector2.put(p, value++);
            }
        });
        // 计算两个向量的内积
        double dotProduct = 0;
        for (String term : vector1.keySet()) {
            if (vector2.containsKey(term)) {
                dotProduct += vector1.get(term) * vector2.get(term);
            }
        }
        // 计算每个向量的范数
        double norm1 = 0;
        for (int value : vector1.values()) {
            norm1 += Math.pow(value, 2);
        }
        norm1 = Math.sqrt(norm1);

        double norm2 = 0;
        for (int value : vector2.values()) {
            norm2 += Math.pow(value, 2);
        }
        norm2 = Math.sqrt(norm2);

        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) {
            return 0;
        } else {
            return dotProduct / (norm1 * norm2);
        }
    }

    /**
     * 编辑距离
     * @return
     */
    public static int calculateEditDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }


    private static RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http")));


    public static List<String> analyzeText(String text) throws IOException {
        String analyzer="ik_max_word";
        AnalyzeRequest request = AnalyzeRequest.withGlobalAnalyzer(analyzer, text);
        AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
        List<String> tokens = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : response.getTokens()) {
            tokens.add(token.getTerm());
        }
        return tokens;
    }

    /**
     * 提取中文字符
     */
    public static String collectChineseChars(List<String> list) {
        StringBuilder chineseChars = new StringBuilder();

        for (String str : list) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c >= '\u4E00' && c <= '\u9FFF') {
                    chineseChars.append(c);
                }
            }
        }
        String chineseString = chineseChars.toString();
        return chineseString;
    }

}
