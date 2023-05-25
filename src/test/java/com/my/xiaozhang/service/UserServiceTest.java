package com.my.xiaozhang.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.model.enums.ListTypeEnum;
import com.my.xiaozhang.utils.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.my.xiaozhang.constant.UserConstant.MapConstant;
import static com.my.xiaozhang.model.enums.ListTypeEnum.MIXEECAE;
import static java.util.stream.Collectors.toList;


/**
 * @author:22603
 * @Date:2023/3/17 11:06
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class UserServiceTest {

    @Resource
    private UserService userService;


    @Test
    public void test() {
        List<String> study = Stream.of("java", "python", "go", "c++", "c", "php").collect(toList());
        MapConstant.put("编程", study);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = userService.list(queryWrapper);
        String tags = "[\"java\",\"男\",\"c++\",\"Python\",\"Go\"]";
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        log.info("tagList:" + tagList.toString());
        // 用户列表的下标 => 相似度
        List<Pair<User, Double>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == 1) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            double distance = Sorce(tagList, userTagList);
            System.out.println(distance);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Double>> topUserPairList = list.stream()
                .sorted((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()))
                .limit(20)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        System.out.println(topUserPairList);
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = userService.list(userQueryWrapper)
                .stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        System.out.println(finalUserList);
    }

    public double Sorce(List<String> list1, List<String> list2) {
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
        double totalSorce = EditDistance * 0.5 + JaccardSorce * 0.3 + similaritySorce * 0.2;
        return totalSorce;
    }
        @Test
        void Delete(){
        QueryWrapper<User>queryWrapper=new QueryWrapper<>();
        queryWrapper.gt("id",40);
            userService.remove(queryWrapper);
        }

}
