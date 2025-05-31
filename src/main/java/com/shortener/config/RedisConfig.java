package com.shortener.config;

import com.shortener.entity.ShortToLong;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Profile("dev")
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ShortToLong> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ShortToLong> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Optional: Set custom serializers (e.g., Jackson)
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}