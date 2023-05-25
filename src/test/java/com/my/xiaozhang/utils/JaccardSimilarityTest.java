package com.my.xiaozhang.utils;

import org.assertj.core.util.Arrays;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class JaccardSimilarityTest {
    public static void main(String[] args) {
        List<String> list1 = Stream.of("jAVA", "python", "go", "c++","编程").collect(toList());
        List<String> list2 = Stream.of("python", "go","c","编程").collect(toList());
        double jaccardSimilarity = calculateJaccardSimilarity(list1, list2);
        System.out.println("Jaccard similarity between " + list1 + " and " + list2 + " is: " + jaccardSimilarity);
    }

    private static double calculateJaccardSimilarity(List<String> list1, List<String> list2) {
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }
}
