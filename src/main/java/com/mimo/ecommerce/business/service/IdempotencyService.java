package com.mimo.ecommerce.business.service;

import com.mimo.ecommerce.core.utils.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by zcl on 2024/9/10.
 */
@Service
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;

    public IdempotencyService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查请求是否已经处理过
     */
    public boolean isRequestProcessed(String requestId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(requestId));
    }

    /**
     * 标记请求为已处理
     */
    public void markRequestProcessed(String requestId) {
        redisTemplate.opsForValue().set(requestId, "PROCESSED", 10, TimeUnit.MINUTES); // 10 分钟有效期
    }

    /**
     * 生成唯一的 requestId
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}