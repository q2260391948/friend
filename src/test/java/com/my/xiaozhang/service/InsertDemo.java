package com.my.xiaozhang.service;

import com.my.xiaozhang.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InsertDemo {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 并发批量插入用户
     * 将总插入数据量分批，每一批使用一个线程
     * 效率和总线程数有关，线程少分的组多可能某些线程执行多次
     */
    @Test
    public void doInsertUsers() {
        int num=5000;
        int j=0;
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        List<CompletableFuture<Void>>list=new ArrayList<>();
//        分10组
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("测试数据");
                user.setUserAccount("fack"+i);
                user.setAvatarUrl("https://www.ncccu.org.cn/static/index/images/avatar.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("11111111111");
                user.setEmail("2168527775@qq.com");
                user.setTags("[]");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("11111111");
                userList.add(user);
                if (i % j == 0) {
                    break;
                }
            }
//            创建异步任务  十个异步任务
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, 1000);
            },executorService);
            list.add(completableFuture);
        }
//        阻塞使上面程序执行完再执行下面
        CompletableFuture.allOf(list.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
