package com.project.me.authjavaservice.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class AuthAppScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public AuthAppScheduler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void cleanRedisBlacklist() {
        Set<String> keys = redisTemplate.keys("*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
