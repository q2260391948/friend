package com.my.xiaozhang.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChineseTokenizer {
    public static void main(String[] args) {
        List<String> strings = Stream.of("Java编程", "Python编程", "算法学习", "数据结构").collect(Collectors.toList());
        List<String> result = tokenize(strings);
        System.out.println("Tokens: " + result);
    }
    private static List<String> tokenize(List<String> strings) {
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
}
