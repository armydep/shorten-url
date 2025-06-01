package com.shortener.service;

import com.shortener.entity.ShortToLong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class CacheService {
    private static final String KEY_PREFIX = "code:";
    private final RedisTemplate<String, ShortToLong> redisTemplate;

    public void put(String code, ShortToLong value) {
        redisTemplate.opsForValue().set(KEY_PREFIX + code, value, Duration.ofMinutes(60));
    }

    public ShortToLong get(String code) {
        ShortToLong value = redisTemplate.opsForValue().get(KEY_PREFIX + code);
        if (value != null) {
            redisTemplate.expire(KEY_PREFIX + code, Duration.ofMinutes(60));
            return value;
        }
        return null;
    }
}
