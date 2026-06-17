package com.hatsune.lingolens.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Keeps background word-asset warmup small and predictable for low-memory hosts.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-12
 */
@Configuration
public class AsyncConfig {

    @Bean("wordAssetWarmupExecutor")
    public Executor wordAssetWarmupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix("word-asset-warmup-");
        executor.initialize();
        return executor;
    }
}
