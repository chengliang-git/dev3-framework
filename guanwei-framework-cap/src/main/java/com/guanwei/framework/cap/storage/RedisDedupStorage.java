package com.guanwei.framework.cap.storage;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的幂等去重存储
 */
public class RedisDedupStorage implements DedupStorage {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREFIX = "cap:dedup:";

    public RedisDedupStorage(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryMarkProcessed(String key, long ttlSeconds) {
        String redisKey = PREFIX + key;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1");
        if (Boolean.TRUE.equals(success)) {
            if (ttlSeconds > 0) {
                stringRedisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);
            }
            return true;
        }
        return false;
    }
}


