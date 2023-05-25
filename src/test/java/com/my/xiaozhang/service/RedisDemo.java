package com.my.xiaozhang.service;

import com.my.xiaozhang.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
@SpringBootTest
public class RedisDemo {

    @Resource
    private RedisTemplate redisTemplate;
    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("zxqAge",20);
        valueOperations.set("zxqName","zhangxinqi");
        int zxqAge = (int)valueOperations.get("zxqAge");
        Assertions.assertTrue(20==zxqAge);
        String zxqName = (String) valueOperations.get("zxqName");
        Assertions.assertTrue("zhangxinqi".equals(zxqName));

        User user = new User();
        user.setId(1L);
        user.setUsername("User");
        valueOperations.set("User", user);
        System.out.println(valueOperations.get("User"));
    }
}
