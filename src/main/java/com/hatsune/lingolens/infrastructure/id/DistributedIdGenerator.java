package com.hatsune.lingolens.infrastructure.id;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public interface DistributedIdGenerator {

    Long id(String bizType);
}
