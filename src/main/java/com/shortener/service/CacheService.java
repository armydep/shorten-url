package com.shortener.service;

import com.shortener.entity.ShortToLong;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class CacheService {
    private static final String KEY_PREFIX = "code:";
    private final RedisTemplate<String, ShortToLong> redisTemplate;
    private final Counter hitCounter;
    private final Counter missCounter;

    @Autowired
    public CacheService(RedisTemplate<String, ShortToLong> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.hitCounter = meterRegistry.counter("redis_cache_hits");
        this.missCounter = meterRegistry.counter("redis_cache_misses");
    }

    public void put(String code, ShortToLong value) {
        redisTemplate.opsForValue().set(KEY_PREFIX + code, value, Duration.ofMinutes(60));
    }

    public ShortToLong get(String code) {
        ShortToLong value = redisTemplate.opsForValue().get(KEY_PREFIX + code);
        if (value != null) {
            hitCounter.increment();
            redisTemplate.expire(KEY_PREFIX + code, Duration.ofMinutes(60));
            return value;
        }
        missCounter.increment();
        return null;
    }
}
