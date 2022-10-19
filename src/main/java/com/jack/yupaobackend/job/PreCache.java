package com.jack.yupaobackend.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PreCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;

    private List<Long> mainUsers = Arrays.asList(6L); // 存储vip客户名单，不是为所有用户都缓存数据，提高用户体验
    @Scheduled(cron = " 0 3 0 * * *")
    public void doPreCache(){
        for (Long userId : mainUsers) {
            String redisKey = String.format("yupao:user:recommend:%s", userId);
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            Page<User> userPage = new Page<>(1, 20); // 第一个参数表示当前是第几页，第二个参数表示一页内有几条数据
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 查完数据库，写到缓存里
            try {
                valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("redis save error: " + e);
            }
        }
    }

}
