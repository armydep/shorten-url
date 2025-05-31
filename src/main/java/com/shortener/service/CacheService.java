package com.shortener.service;

import com.shortener.entity.ShortToLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {
    private static final String KEY_PREFIX = "code:";
    @Autowired
    private RedisTemplate<String, ShortToLong> redisTemplate;

    public void put(String code, ShortToLong value) {
        redisTemplate.opsForValue().set(KEY_PREFIX + code, value, Duration.ofSeconds(60));
//        redisTemplate.opsForValue().set("code:" + cod, value);
    }

    public ShortToLong get(String code) {
        ShortToLong value = redisTemplate.opsForValue().get(KEY_PREFIX + code);
        if (value != null) {
            redisTemplate.expire(KEY_PREFIX + code, Duration.ofSeconds(60));
            return value;
        }
        return null;
//        return redisTemplate.opsForValue().get("code:" + code);
    }

//    public void deleteCachedUser(String id) {
//        redisTemplate.delete("user:" + id);
//    }

}
