package com.hatsune.lingolens.infrastructure.id;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
@RequiredArgsConstructor
public class RedisDistributedIdGenerator implements DistributedIdGenerator {

    private static final long ID_BASE = 100_000_000_000_000L;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Long id(String bizType) {
        Long sequence = stringRedisTemplate.opsForValue().increment(RedisKeyConstant.GLOBAL_ID.key(bizType));
        if (sequence == null) {
            throw new BizException("failed to generate distributed id");
        }
        return ID_BASE + sequence;
    }
}
