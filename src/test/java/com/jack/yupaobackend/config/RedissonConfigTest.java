package com.jack.yupaobackend.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonConfigTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void redissonClient() {
        RList<Object> rList = redissonClient.getList("test-list");
//        rList.add("dog");
//        rList.add(33);
//        rList.add(true);
        System.out.println(rList.get(1));
    }
}