package com.shortener.service;

import com.shortener.entity.ShortToLong;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheServiceTest {

    private RedisTemplate<String, ShortToLong> redisTemplate;
    private ValueOperations<String, ShortToLong> valueOps;
    private CacheService cacheService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        meterRegistry = new SimpleMeterRegistry();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        cacheService = new CacheService(redisTemplate, meterRegistry);
    }

    @Test
    void testPutStoresValueInRedis() {
        ShortToLong entity = new ShortToLong();
        String code = "abc123";
        String longUrl = "https://example.com";
        entity.setLongUrl(longUrl);
        entity.setCode(code);
        cacheService.put(code, entity);
        verify(valueOps).set("code:" + code, entity, Duration.ofMinutes(60));
    }

    @Test
    void testGetReturnsValueAndRefreshesTTL() {
        ShortToLong expected = new ShortToLong();
        String code = "abc123";
        String longUrl = "https://example.com";
        expected.setLongUrl(longUrl);
        expected.setCode(code);
        when(valueOps.get("code:abc123")).thenReturn(expected);
        ShortToLong result = cacheService.get("abc123");
        assertNotNull(result);
        assertEquals("https://example.com", result.getLongUrl());
        verify(redisTemplate).expire("code:abc123", Duration.ofMinutes(60));
    }

    @Test
    void testGetReturnsNullWhenNotCached() {
        when(valueOps.get("code:missing")).thenReturn(null);
        ShortToLong result = cacheService.get("missing");
        assertNull(result);
        verify(redisTemplate, never()).expire(anyString(), any());
    }
}