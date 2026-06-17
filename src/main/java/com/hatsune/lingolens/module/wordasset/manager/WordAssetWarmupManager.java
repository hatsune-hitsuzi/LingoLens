package com.hatsune.lingolens.module.wordasset.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pre-generates common language projections after mobile object detection.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WordAssetWarmupManager {

    private static final int MAX_WORDS_PER_SCAN = 16;
    private static final Duration WARMUP_LOCK_TTL = Duration.ofSeconds(45);
    private static final List<LanguagePair> CORE_LANGUAGE_PAIRS = List.of(
            new LanguagePair("zh-CN", "en"),
            new LanguagePair("zh-CN", "ja"),
            new LanguagePair("ja", "zh-CN"),
            new LanguagePair("ja", "en"),
            new LanguagePair("en", "ja"),
            new LanguagePair("en", "zh-CN")
    );

    private final WordAssetManager wordAssetManager;
    private final RedisDistributedLock redisDistributedLock;

    @Async("wordAssetWarmupExecutor")
    public void warmupCoreLanguagePairs(Collection<String> words) {
        Set<String> wordKeys = normalizeWords(words);
        if (wordKeys.isEmpty()) {
            return;
        }
        for (String wordKey : wordKeys) {
            for (LanguagePair pair : CORE_LANGUAGE_PAIRS) {
                try {
                    redisDistributedLock.execute(
                            RedisKeyConstant.WORD_ASSET_RESOLVE_LOCK,
                            WARMUP_LOCK_TTL,
                            () -> {
                                wordAssetManager.enrichWord(wordKey, pair.sourceLanguage(), pair.targetLanguage());
                                return true;
                            },
                            pair.sourceLanguage(),
                            pair.targetLanguage(),
                            wordKey);
                } catch (BizException e) {
                    log.debug("word asset warmup already running, wordKey={}, sourceLanguage={}, targetLanguage={}",
                            wordKey, pair.sourceLanguage(), pair.targetLanguage());
                } catch (Exception e) {
                    log.warn("word asset warmup skipped, wordKey={}, sourceLanguage={}, targetLanguage={}",
                            wordKey, pair.sourceLanguage(), pair.targetLanguage(), e);
                }
            }
            try {
                wordAssetManager.markWordEnriched(wordKey);
            } catch (Exception e) {
                log.warn("word asset warmup source mark skipped, wordKey={}", wordKey, e);
            }
        }
    }

    private Set<String> normalizeWords(Collection<String> words) {
        Set<String> wordKeys = new LinkedHashSet<>();
        if (words == null) {
            return wordKeys;
        }
        for (String word : words) {
            if (!StringUtils.hasText(word) || wordKeys.size() >= MAX_WORDS_PER_SCAN) {
                continue;
            }
            wordKeys.add(word.trim().toLowerCase(Locale.ROOT));
        }
        return wordKeys;
    }

    private record LanguagePair(String sourceLanguage, String targetLanguage) {
    }
}
