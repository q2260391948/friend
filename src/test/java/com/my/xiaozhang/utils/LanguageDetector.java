package com.my.xiaozhang.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageDetector {
    public static void main(String[] args) {
        List<String> strings =  Stream.of("hello world", "你好世界", "hello 你好", "世界 world").collect(Collectors.toList());
        for (String str : strings) {
            String pattern = "[\u4e00-\u9fa5]+"; // 中文字符的正则表达式
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(str);
            if (m.find()) { // 如果字符串中包含中文字符
                pattern = "[a-zA-Z]+"; // 英文字符的正则表达式
                p = Pattern.compile(pattern);
                m = p.matcher(str);
                if (m.find()) { // 如果字符串中包含英文字符
                    System.out.println(str + " 包含中英文混合");
                } else { // 如果字符串中不包含英文字符
                    System.out.println(str + " 包含纯中文");
                }
            } else { // 如果字符串中不包含中文字符
                pattern = "[a-zA-Z]+"; // 英文字符的正则表达式
                p = Pattern.compile(pattern);
                m = p.matcher(str);
                if (m.matches()) { // 如果字符串只包含英文字符
                    System.out.println(str + " 包含纯英文");
                } else { // 如果字符串包含英文字符和其他字符（如数字、符号等）
                    System.out.println(str + " 包含英文和其他字符");
                }
            }
        }
    }
}
