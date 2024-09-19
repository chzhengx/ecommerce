package com.mimo.ecommerce.business.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by zcl on 2024/9/10.
 */
@Component
public class JWTBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    public JWTBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addTokenToBlacklist(String token, long expiration) {
        redisTemplate.opsForValue().set(token, "BLACKLISTED", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}