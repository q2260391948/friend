package com.my.xiaozhang.utilsTest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.xiaozhang.service.UserService;
import com.my.xiaozhang.service.impl.UserServiceImpl;
import com.my.xiaozhang.utils.AllUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author:22603
 * @Date:2023/4/28 0:22
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AllUtilsTest {


    private UserServiceImpl userService =new UserServiceImpl();

    @Test
    void test() throws IOException {

        String s = "[\"java\",\"男\",\"c++\",\"美食\",\"宠物\",\"打球\"]";
//        String s1 = "[\"男\",\"打球\",\"英语\",\"学习Java\"]";
        String s1 = "[\"女\",\"看书\",\"设计\",\"美食\"]";
        System.out.println(s);
        System.out.println(s1);
        Gson gson = new Gson();
        List<String> resultList1 = gson.fromJson(s, new TypeToken<List<String>>() {
        }.getType());
        List<String> resultList2 = gson.fromJson(s1, new TypeToken<List<String>>() {
        }.getType());
        System.out.println(resultList1 +","+resultList2);
        int EditDistanceSorce = AllUtils.calculateEditDistance(resultList1, resultList2);
        double maxEditDistance = Math.max(resultList1.size(), resultList2.size());
        double EditDistance = 1 - EditDistanceSorce / maxEditDistance;
        double JaccardSorce = AllUtils.calculateJaccardSimilarity(resultList1, resultList2);
        double similaritySorce = AllUtils.cosineSimilarity(resultList1, resultList2);
        System.out.println("EditDistance:"+EditDistance);
        System.out.println("JaccardSorce:"+JaccardSorce);
        System.out.println("similaritySorce:"+similaritySorce);
        List<String> resultList3 = resultList1.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> resultList4 = resultList2.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> quotedList1 = resultList3.stream()
                .map(str -> "\"" + str + "\"")
                .collect(Collectors.toList());
        List<String> quotedList2 = resultList4.stream()
                .map(str -> "\"" + str + "\"")
                .collect(Collectors.toList());
        String tags1 = AllUtils.collectChineseChars(quotedList1);
        List<String> Ls = AllUtils.analyzeText(tags1);
        String tags2 = AllUtils.collectChineseChars(quotedList2);
        List<String> Ls2 = AllUtils.analyzeText(tags2);
       double ikSorce = AllUtils.calculateJaccardSimilarity(Ls, Ls2);
        double totalSorce = EditDistance * 0.5 + (JaccardSorce+ikSorce) * 0.3 + similaritySorce * 0.2;
        System.out.println("ikSorce:"+ikSorce);
        System.out.println(totalSorce);

    }
}
