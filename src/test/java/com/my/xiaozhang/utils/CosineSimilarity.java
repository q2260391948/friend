package com.my.xiaozhang.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class CosineSimilarity {
    public static double cosineSimilarity(List<String> list1, List<String> list2) {
        Map<String, Integer> vector1 = new HashMap();
        list1.forEach(p -> {
            System.out.println(p);
            if (!vector1.containsKey(p)) {
                vector1.put(p, 1);
            } else {
                Integer value = vector1.get(p);
                System.out.println(value);
                vector1.put(p, value++);
            }
        });
        Map<String, Integer> vector2 = new HashMap();
        list2.forEach(p -> {
            System.out.println(p);
            if (!vector2.containsKey(p)) {
                vector2.put(p, 1);
            } else {
                Integer value = vector2.get(p);
                System.out.println(value);
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

    public static void main(String[] args) {
        // 示例：计算两个简单文本的余弦相似度
        List<String> list1 = Stream.of("jAVA", "python", "go", "c++", "编程", "go").collect(toList());
        List<String> list2 = Stream.of("python", "go", "c", "编程").collect(toList());
        double similarity = cosineSimilarity(list1, list2);
        System.out.println("Cosine Similarity: " + similarity);
    }
}
