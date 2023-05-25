package com.my.xiaozhang.service;

import com.my.xiaozhang.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author:22603
 * @Date:2023/4/3 19:24
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestServiceImpl implements TestService {
    @Override
    public void test() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });

        new Thread(() -> {

        }).start();
    }


    @Test
    void test1() {
        ArrayList<UserEntity> arrayList = new ArrayList();
        System.out.println("arrayList----------");
        arrayList.add(new UserEntity("mayikt", 22));
        arrayList.add(new UserEntity("xiaomin", 18));
        arrayList.add(new UserEntity("xiaoha", 36));

        System.out.println(arrayList);
//        arrayList.sort((o1, o2) -> o1-o2);
        System.out.println("Stream----------");
        List<UserEntity> list = arrayList.stream().sorted(Comparator.comparingInt(UserEntity::getAge)).collect(Collectors.toList());
        list.forEach(p -> {
            System.out.println(p);
        });
        System.out.println("list转Map----------");
        Map<Integer, UserEntity> collect = list.stream().collect(Collectors.toMap(UserEntity -> UserEntity.getAge(), UserEntity -> UserEntity));
        collect.forEach((p,user)-> System.out.println(p+""+user));
        System.out.println("Set----------");
        Set<Map.Entry<Integer, UserEntity>> entries = collect.entrySet();
        entries.forEach(p-> System.out.println(p));
        System.out.println("求和----------");
        Optional<UserEntity> reduce = arrayList.stream().reduce((UserEntity1, UserEntity2) -> {
            UserEntity sum = new UserEntity("sum", UserEntity1.getAge() + UserEntity2.getAge());
            return sum;
        });
        System.out.println(reduce.get().getAge());
        System.out.println("最大值----------");
        arrayList.stream().max((user1,user2)->{
            return user1.getAge()- user2.getAge();
        });
        Optional<UserEntity> max = arrayList.stream().max(Comparator.comparingInt(UserEntity::getAge));
        System.out.println("max:"+ max.get());
        Optional<UserEntity> min = arrayList.stream().min(Comparator.comparingInt(UserEntity::getAge));
        System.out.println("最小值----------");
        System.out.println("min:"+min);
        System.out.println("排序----------");
        List<UserEntity> userEntityList = arrayList.stream().sorted(Comparator.comparingInt(UserEntity::getAge)).collect(Collectors.toList());
        userEntityList.forEach(p-> System.out.println(p));
        System.out.println("匹配----------");
        boolean match = arrayList.stream().anyMatch((user) -> user.getAge() > 35);
        System.out.println(match);
    }
    @Test
    void test2(){
        ArrayList<UserEntity> arrayList = new ArrayList();
        System.out.println("arrayList----------");
        arrayList.add(new UserEntity("mayikt", 22));
        arrayList.add(new UserEntity("xiaomin", 18));
        arrayList.add(new UserEntity("xiaoha", 36));
        System.out.println("匹配----------");
        boolean match = arrayList.stream().anyMatch((user) -> user.getAge() > 35);
        System.out.println(match);
        System.out.println("过滤----------");
        List<UserEntity> collect = arrayList.stream().filter(user -> user.getAge() > 35 && user.getName().equals("xiaoha")).collect(Collectors.toList());
        collect.forEach(p-> System.out.println(p));
        System.out.println("限制----------");
//        skip从哪里开始   limit限制几条
        arrayList.stream().skip(1).limit(2).forEach(p-> System.out.println(p));
    }

    @Test
    void test3(){
        Scanner scanner=new Scanner(System.in);
        String input=scanner.next();
        if (input.length()%2==0){




        }
    }

    public class UserEntity {

        private String name;
        private Integer age;

        public UserEntity(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "UserEntity{" + "name='" + name + '\'' + ", age=" + age + '}';
        }
    }

}
