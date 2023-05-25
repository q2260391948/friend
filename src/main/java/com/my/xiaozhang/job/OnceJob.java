package com.my.xiaozhang.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 22603
 */
@Component
@Slf4j
public class OnceJob {
    List<Long> list = new ArrayList<>();
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0 0/3 * * ?")
    public void cache() {
        List<User> listUser = userService.list();
        listUser.forEach(user -> list.add(user.getId()));
//        对进来的用户加锁
        RLock lock = redissonClient.getLock("my:precachejob:docache:lock");
        try {
            System.out.println("getLock: " + Thread.currentThread().getId());
//         尝试加锁, 最多等待0秒, 30秒后自动解锁 ,第一个参数时等待时间，第二个为过期时间，如果要实现自动续期需要把过期时间设置为-1
            if (lock.tryLock(0, -1, TimeUnit.MICROSECONDS)) {
                for (User user : listUser) {
                    String format = String.format("my:user:match:%s", user.getId());
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.ne("id",user.getId());
                    List<User> users = userService.matchUsers(20, user);
                    try {
                        valueOperations.set(format, users, 3, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            只能释放自己的锁，必须放到final里面，try中报错就不往下执行，所以必须放finally里面
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
