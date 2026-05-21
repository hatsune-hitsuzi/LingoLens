package com.hatsune.lingolens.common.lock;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Acquire a Redis distributed lock before running business logic.
     *
     * @param redisKeyConstant Redis key enum, including key/value format and meaning
     * @param ttl              lock ttl
     * @param supplier         business logic after lock acquired
     * @param keyArgs          key format arguments
     * @return business result
     */
    public <T> T execute(RedisKeyConstant redisKeyConstant, Duration ttl, Supplier<T> supplier, Object... keyArgs) {
        String lockKey = redisKeyConstant.key(keyArgs);
        String lockToken = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, ttl);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BizException("request is processing, please retry later");
        }
        try {
            return supplier.get();
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), lockToken);
        }
    }
}
