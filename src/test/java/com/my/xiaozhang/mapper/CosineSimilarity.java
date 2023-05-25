package com.my.xiaozhang.mapper;

import java.util.Arrays;

public class CosineSimilarity {
    public static double cosineSimilarity(String text1, String text2) {
        // 分词并去重
        String[] words1 = text1.split(" ");
        String[] words2 = text2.split(" ");
        String[] words = Arrays.stream(words1).distinct().toArray(String[]::new);
        words = Arrays.copyOf(words, words.length + words2.length);
        for (String word : words2) {
            if (!Arrays.asList(words1).contains(word)) {
                words[words.length - words2.length + Arrays.asList(words2).indexOf(word)] = word;
            }
        }

        // 构建向量
        int[] vector1 = new int[words.length];
        int[] vector2 = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            vector1[i] = countOccurrences(text1, words[i]);
            vector2[i] = countOccurrences(text2, words[i]);
        }

        // 计算余弦相似度值
        double dotProduct = 0.0;
        double vectorLength1 = 0.0;
        double vectorLength2 = 0.0;
        for (int i = 0; i < words.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            vectorLength1 += vector1[i] * vector1[i];
            vectorLength2 += vector2[i] * vector2[i];
        }
        vectorLength1 = Math.sqrt(vectorLength1);
        vectorLength2 = Math.sqrt(vectorLength2);
        double cosineSimilarity = dotProduct / (vectorLength1 * vectorLength2);

        return cosineSimilarity;
    }

    private static int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }

    public static void main(String[] args) {
        String text1 = "horse";
        String text2 = "ros";
        double cosineSimilarity = cosineSimilarity(text1, text2);
        System.out.println("Cosine similarity: " + cosineSimilarity);
    }
}
