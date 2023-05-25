package com.my.xiaozhang.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.service.TestService;
import com.my.xiaozhang.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TeamUserMapperTest {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Resource
    private UserService userService;

    @Test
    public void test() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        List<User> users = userService.list(queryWrapper);
        Map<String, Integer> map = new HashMap<>();
        for (User user : users) {
            String Tags = user.getTags();
            List<String> list = Arrays.asList(Tags);
            if (list == null) {
                continue;
            }
            Gson gson = new Gson();
            List<String> tagList = gson.fromJson(Tags, new TypeToken<List<String>>() {
            }.getType());
            for (String tag : tagList) {
                if (map.get(tag) == null) {
                    map.put(tag, 1);
                } else {
                    map.put(tag, map.get(tag) + 1);
                }
            }
        }
        System.out.println("mp:" + map);
        Map<Integer, List<String>> Map = new TreeMap<>(new Comparator<Integer>() {
            public int compare(Integer key1, Integer key2) {
                //降序排序
                return key2.compareTo(key1);
            }
        });
        map.forEach((value, key) -> {
            System.out.println(value + "=>" + key);
            System.out.println("map.containsKey:" + key + " , ");
            if (Map.size() == 0) {
                Map.put(key, Arrays.asList(value));
            } else if (Map.get(key) == null) {
                Map.put(key, Arrays.asList(value));
                System.out.println(Map);
            } else {
                List<String> list = new ArrayList(Map.get(key));
                list.add(value);
                System.out.println(list);
                Map.put(key, list);
            }
        });
        System.out.println(Map);
        Set<String> set = new HashSet<>();
        List<String> tagList3 = Arrays.asList("Python", "大二", "女");
        tagList3.forEach(tag -> set.add(tag));
        for (Map.Entry<Integer, List<String>> entry : Map.entrySet()) {
            System.out.println(entry.getValue());
            entry.getValue().forEach(tag -> set.add(tag));
        }
        System.out.println(set);
    }

    @Test
    public void test1() {

        new TestService() {
            @Override
            public void test() {
                System.out.println("这是测试");
            }
        }.test();
        ((TestService) () -> System.out.println("这是测试")).test();
        TestService testService = () -> {
            System.out.println("测试");
        };
        testService.test();

        ((TestService) () -> {

        }).test();

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });

        new Thread(() -> {
        }).start();

    }
}
