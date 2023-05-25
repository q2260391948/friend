package com.my.xiaozhang.utils;

import com.my.xiaozhang.Enum.ListTypeEnum;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.my.xiaozhang.Enum.ListTypeEnum.MIXEECAE;
import static java.util.stream.Collectors.toList;

public class EditDistance {


    public static void main(String[] args) {


        List<String> list1 = Stream.of("jAVA", "python", "go", "c++", "php", "编程").collect(toList());
        List<String> list3 = Stream.of("java", "Python", "Go", "C++", "Php").collect(toList());
        List<String> list2 = Stream.of("python", "go", "c", "c++", "a1编程").collect(toList());
//        List<String> list3 = Stream.of("Python", "Go", "c", "c++", "PHP", "编程").collect(toList());
        List<String> resultList1 = list1.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> resultList2 = list2.stream().map(String::toLowerCase).collect(Collectors.toList());
        int strType = AllUtils.getStrType(resultList1);
        int type = AllUtils.getStrType(resultList2);
        ListTypeEnum enumByValue = ListTypeEnum.getEnumByValue(strType);
        ListTypeEnum enumByValue1 = ListTypeEnum.getEnumByValue(type);
        if (enumByValue == MIXEECAE) {
            resultList1 = AllUtils.tokenize(resultList1);
        }
        if (enumByValue1 == MIXEECAE) {
            resultList2 = AllUtils.tokenize(resultList2);
        }
//        System.out.println("resultList1:" + resultList1);
//        System.out.println("resultList2:" + resultList2);
        int EditDistanceSorce = AllUtils.calculateEditDistance(resultList1, resultList2);
//        System.out.println(EditDistanceSorce);
        double maxEditDistance = Math.max(resultList1.size(), resultList2.size());
        double EditDistance = 1 - EditDistanceSorce / maxEditDistance;
//        System.out.println("EditDistanceSorce:" + EditDistance);
        double JaccardSorce = AllUtils.calculateJaccardSimilarity(resultList1, resultList2);
//        System.out.println("JaccardSorce:" + JaccardSorce);
        double similaritySorce = AllUtils.cosineSimilarity(resultList1, resultList2);
//        System.out.println("similaritySorce:" + similaritySorce);
        /**
         * 编辑距离 权重为0.5
         * Jaccard相似度算法 权重为0.3
         *  余弦相似度 权重为0.2
         */
        double totalSorce =EditDistance*0.5+JaccardSorce*0.3+similaritySorce*0.2;
        System.out.println(totalSorce);
    }
}
