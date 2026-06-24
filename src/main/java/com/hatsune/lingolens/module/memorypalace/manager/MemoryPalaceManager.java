package com.hatsune.lingolens.module.memorypalace.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.arscan.manager.ArScanWordManager;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceCreateBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceListBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordAddBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordPageBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordMutationBO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchWordDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageRequest;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceMapper;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceWordMapper;
import com.hatsune.lingolens.module.memorypalace.support.SceneNameLocalizer;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetManager;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetWarmupManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Component
@RequiredArgsConstructor
public class MemoryPalaceManager {

    private static final String PALACE_ID_BIZ_TYPE = "memory_palace";
    private static final String PALACE_WORD_ID_BIZ_TYPE = "memory_palace_word";
    private static final String DEFAULT_SCENE_NAME = "Default Scene";

    private final MemoryPalaceMapper memoryPalaceMapper;
    private final MemoryPalaceWordMapper memoryPalaceWordMapper;
    private final DistributedIdGenerator distributedIdGenerator;
    private final ArScanWordManager arScanWordManager;
    private final WordAssetManager wordAssetManager;
    private final WordAssetWarmupManager wordAssetWarmupManager;
    private final UserPreferenceManager userPreferenceManager;
    private final SceneNameLocalizer sceneNameLocalizer;
    private final MemoryPalaceImageEnhancementManager imageEnhancementManager;

    public MemoryPalaceWordAddBO addWord(Long userPkId, MemoryPalaceWordAddRequest request) {
        MemoryPalaceDO palace = getOrCreatePalace(userPkId, request);
        Long wordAssetId = resolveWordAssetId(userPkId, request);
        MemoryPalaceWordDO existedWord = memoryPalaceWordMapper.selectOne(MemoryPalaceWordDO.builder()
                .userPkId(userPkId)
                .palaceId(palace.getId())
                .wordAssetId(wordAssetId)
                .build());
        if (existedWord != null) {
            memoryPalaceWordMapper.updateByPrimaryKey(MemoryPalaceWordDO.builder()
                    .id(existedWord.getId())
                    .sourceImageUrl(request.getSourceImageUrl())
                    .anchorXPercent(request.getXPercent())
                    .anchorYPercent(request.getYPercent())
                    .updateTime(OffsetDateTime.now())
                    .build());
            arScanWordManager.recordWord(userPkId, palace.getId(), palace.getSceneName(), wordAssetId, request);
            scheduleImageEnhancementAfterCommit(userPkId, palace.getId(), existedWord.getId(), request);
            return MemoryPalaceWordAddBO.builder()
                    .palaceId(palace.getId())
                    .palaceWordId(existedWord.getId())
                    .palaceWordCount(palace.getWordCount())
                    .added(false)
                    .build();
        }

        MemoryPalaceWordDO latestWord = memoryPalaceWordMapper.selectLatestByAsset(
                userPkId, wordAssetId);
        if (latestWord != null) {
            MemoryPalaceDO previousPalace = getUserPalace(userPkId, latestWord.getPalaceId());
            memoryPalaceWordMapper.moveToPalace(userPkId, latestWord.getId(), palace.getId());
            memoryPalaceWordMapper.updateByPrimaryKey(MemoryPalaceWordDO.builder()
                    .id(latestWord.getId())
                    .sourceImageUrl(request.getSourceImageUrl())
                    .anchorXPercent(request.getXPercent())
                    .anchorYPercent(request.getYPercent())
                    .updateTime(OffsetDateTime.now())
                    .build());
            int previousCount = Math.max(0, defaultZero(previousPalace.getWordCount()) - 1);
            int newCount = defaultZero(palace.getWordCount()) + 1;
            updatePalaceWordCount(previousPalace.getId(), previousCount, null);
            updatePalaceWordCount(palace.getId(), newCount, request.getSourceImageUrl());
            arScanWordManager.recordWord(
                    userPkId, palace.getId(), palace.getSceneName(), wordAssetId, request);
            scheduleImageEnhancementAfterCommit(userPkId, palace.getId(), latestWord.getId(), request);
            return MemoryPalaceWordAddBO.builder()
                    .palaceId(palace.getId())
                    .palaceWordId(latestWord.getId())
                    .palaceWordCount(newCount)
                    .added(true)
                    .build();
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long palaceWordId = distributedIdGenerator.id(PALACE_WORD_ID_BIZ_TYPE);
        MemoryPalaceWordDO word = MemoryPalaceWordDO.builder()
                .id(palaceWordId)
                .palaceId(palace.getId())
                .userPkId(userPkId)
                .wordAssetId(wordAssetId)
                .sourceImageUrl(request.getSourceImageUrl())
                .anchorXPercent(request.getXPercent())
                .anchorYPercent(request.getYPercent())
                .createTime(now)
                .updateTime(now)
                .build();
        memoryPalaceWordMapper.insert(word);
        arScanWordManager.recordWord(userPkId, palace.getId(), palace.getSceneName(), wordAssetId, request);

        int newWordCount = defaultZero(palace.getWordCount()) + 1;
        memoryPalaceMapper.updateByPrimaryKey(MemoryPalaceDO.builder()
                .id(palace.getId())
                .coverUrl(StringUtils.hasText(palace.getCoverUrl()) ? null : request.getSourceImageUrl())
                .wordCount(newWordCount)
                .updateTime(now)
                .build());

        scheduleImageEnhancementAfterCommit(userPkId, palace.getId(), palaceWordId, request);
        return MemoryPalaceWordAddBO.builder()
                .palaceId(palace.getId())
                .palaceWordId(palaceWordId)
                .palaceWordCount(newWordCount)
                .added(true)
                .build();
    }

    public MemoryPalaceListBO list(Long userPkId, MemoryPalaceListRequest request) {
        int pageNumber = normalizePageNumber(request.getPageNumber());
        int pageSize = normalizePageSize(request.getPageSize());
        int offset = (pageNumber - 1) * pageSize;
        List<MemoryPalaceDO> palaces = memoryPalaceMapper.listByUser(userPkId, pageSize, offset);
        Integer total = memoryPalaceMapper.countByUser(userPkId);
        return MemoryPalaceListBO.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .total(defaultZero(total))
                .palaces(palaces)
                .build();
    }

    public MemoryPalaceCreateBO createPalace(Long userPkId, String sceneName, String coverUrl) {
        String normalizedSceneName = normalizeSceneName(sceneName);
        String sceneKey = normalizeKey(normalizedSceneName);
        MemoryPalaceDO existedPalace = memoryPalaceMapper.selectOne(MemoryPalaceDO.builder()
                .userPkId(userPkId)
                .sceneKey(sceneKey)
                .build());
        if (existedPalace != null) {
            return MemoryPalaceCreateBO.builder()
                    .created(false)
                    .palace(existedPalace)
                    .build();
        }

        OffsetDateTime now = OffsetDateTime.now();
        int sortOrder = nextTopSortOrder(userPkId);
        MemoryPalaceDO palace = MemoryPalaceDO.builder()
                .id(distributedIdGenerator.id(PALACE_ID_BIZ_TYPE))
                .userPkId(userPkId)
                .sceneKey(sceneKey)
                .sceneName(normalizedSceneName)
                .coverUrl(coverUrl)
                .wordCount(0)
                .sortOrder(sortOrder)
                .createTime(now)
                .updateTime(now)
                .build();
        memoryPalaceMapper.insert(palace);
        return MemoryPalaceCreateBO.builder()
                .created(true)
                .palace(palace)
                .build();
    }

    public int reorder(Long userPkId, List<Long> palaceIds) {
        if (palaceIds == null || palaceIds.isEmpty()) {
            return 0;
        }
        int acceptedCount = 0;
        for (int index = 0; index < palaceIds.size(); index++) {
            Long palaceId = palaceIds.get(index);
            if (palaceId == null) {
                continue;
            }
            acceptedCount += memoryPalaceMapper.updateSortOrder(userPkId, palaceId, index);
        }
        return acceptedCount;
    }

    public MemoryPalaceDO updatePalaceName(Long userPkId, Long palaceId, String sceneName) {
        MemoryPalaceDO palace = getUserPalace(userPkId, palaceId);
        String normalizedSceneName = normalizeSceneName(sceneName);
        String sceneKey = normalizeKey(normalizedSceneName);
        MemoryPalaceDO existedPalace = memoryPalaceMapper.selectOne(MemoryPalaceDO.builder()
                .userPkId(userPkId)
                .sceneKey(sceneKey)
                .build());
        if (existedPalace != null && !existedPalace.getId().equals(palaceId)) {
            throw new BizException("memory palace name already exists");
        }
        int updated = memoryPalaceMapper.updateSceneName(userPkId, palaceId, sceneKey, normalizedSceneName);
        if (updated < 1) {
            throw new BizException("memory palace not found");
        }
        palace.setSceneKey(sceneKey);
        palace.setSceneName(normalizedSceneName);
        palace.setUpdateTime(OffsetDateTime.now());
        return palace;
    }

    public boolean deletePalace(Long userPkId, Long palaceId) {
        getUserPalace(userPkId, palaceId);
        memoryPalaceWordMapper.deleteByPalace(userPkId, palaceId);
        return memoryPalaceMapper.deleteByUserAndId(userPkId, palaceId) > 0;
    }

    public MemoryPalaceWordMutationBO deleteWord(Long userPkId, Long palaceWordId) {
        MemoryPalaceWordDO word = getUserPalaceWord(userPkId, palaceWordId);
        MemoryPalaceDO palace = getUserPalace(userPkId, word.getPalaceId());
        int deleted = memoryPalaceWordMapper.deleteByUserAndId(userPkId, palaceWordId);
        if (deleted < 1) {
            throw new BizException("memory palace word not found");
        }
        int newCount = Math.max(0, defaultZero(palace.getWordCount()) - 1);
        updatePalaceWordCount(palace.getId(), newCount, null);
        if (defaultZero(memoryPalaceWordMapper.countByUserAndAsset(userPkId, word.getWordAssetId())) == 0) {
            arScanWordManager.removeWord(userPkId, word.getWordAssetId());
        }
        return MemoryPalaceWordMutationBO.builder()
                .palaceWordId(palaceWordId)
                .sourcePalaceId(palace.getId())
                .sourcePalaceWordCount(newCount)
                .changed(true)
                .build();
    }

    public MemoryPalaceWordMutationBO moveWord(Long userPkId,
                                                Long palaceWordId,
                                                Long targetPalaceId) {
        MemoryPalaceWordDO word = getUserPalaceWord(userPkId, palaceWordId);
        MemoryPalaceDO sourcePalace = getUserPalace(userPkId, word.getPalaceId());
        MemoryPalaceDO targetPalace = getUserPalace(userPkId, targetPalaceId);
        if (sourcePalace.getId().equals(targetPalace.getId())) {
            return MemoryPalaceWordMutationBO.builder()
                    .palaceWordId(word.getId())
                    .sourcePalaceId(sourcePalace.getId())
                    .targetPalaceId(targetPalace.getId())
                    .sourcePalaceWordCount(defaultZero(sourcePalace.getWordCount()))
                    .targetPalaceWordCount(defaultZero(targetPalace.getWordCount()))
                    .changed(false)
                    .build();
        }

        MemoryPalaceWordDO targetWord = memoryPalaceWordMapper.selectOne(MemoryPalaceWordDO.builder()
                .userPkId(userPkId)
                .palaceId(targetPalaceId)
                .wordAssetId(word.getWordAssetId())
                .build());
        Long resultingWordId = word.getId();
        int targetCount = defaultZero(targetPalace.getWordCount());
        if (targetWord == null) {
            int moved = memoryPalaceWordMapper.moveToPalace(userPkId, word.getId(), targetPalaceId);
            if (moved < 1) {
                throw new BizException("memory palace word not found");
            }
            targetCount++;
        } else {
            resultingWordId = targetWord.getId();
            memoryPalaceWordMapper.updateByPrimaryKey(MemoryPalaceWordDO.builder()
                    .id(targetWord.getId())
                    .sourceImageUrl(word.getSourceImageUrl())
                    .anchorXPercent(word.getAnchorXPercent())
                    .anchorYPercent(word.getAnchorYPercent())
                    .updateTime(OffsetDateTime.now())
                    .build());
            memoryPalaceWordMapper.deleteByUserAndId(userPkId, word.getId());
        }

        int sourceCount = Math.max(0, defaultZero(sourcePalace.getWordCount()) - 1);
        updatePalaceWordCount(sourcePalace.getId(), sourceCount, null);
        updatePalaceWordCount(targetPalace.getId(), targetCount, word.getSourceImageUrl());
        arScanWordManager.moveWord(
                userPkId,
                word.getWordAssetId(),
                targetPalace.getId(),
                targetPalace.getSceneName());
        return MemoryPalaceWordMutationBO.builder()
                .palaceWordId(resultingWordId)
                .sourcePalaceId(sourcePalace.getId())
                .targetPalaceId(targetPalace.getId())
                .sourcePalaceWordCount(sourceCount)
                .targetPalaceWordCount(targetCount)
                .changed(true)
                .build();
    }

    public List<MemoryPalaceDO> searchPalaces(Long userPkId, String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int normalizedLimit = normalizeSearchLimit(limit);
        String normalizedKeyword = keyword.trim();
        Map<Long, MemoryPalaceDO> resultMap = new LinkedHashMap<>();
        memoryPalaceMapper.searchByName(userPkId, normalizedKeyword, normalizedLimit)
                .forEach(palace -> resultMap.putIfAbsent(palace.getId(), palace));
        if (resultMap.size() < normalizedLimit) {
            memoryPalaceMapper.listByUser(userPkId, 100, 0).stream()
                    .filter(palace -> sceneNameLocalizer.matches(palace.getSceneName(), normalizedKeyword))
                    .forEach(palace -> resultMap.putIfAbsent(palace.getId(), palace));
        }
        return resultMap.values().stream()
                .limit(normalizedLimit)
                .toList();
    }

    public List<MemoryPalaceSearchWordDTO> searchWords(Long userPkId, String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        return memoryPalaceWordMapper.searchWords(
                userPkId,
                keyword.trim(),
                preference.getSourceLanguage(),
                preference.getTargetLanguage(),
                normalizeSearchLimit(limit));
    }

    public List<String> latestSavedPhotos(Long userPkId, Long palaceId, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 6));
        return memoryPalaceWordMapper.listLatestSourceImageUrlsByPalace(userPkId, palaceId, normalizedLimit);
    }

    public MemoryPalaceWordPageBO pageWords(Long userPkId, MemoryPalaceWordPageRequest request) {
        MemoryPalaceDO palace = memoryPalaceMapper.selectByPrimaryKey(MemoryPalaceDO.builder()
                .id(request.getPalaceId())
                .build());
        if (palace == null || !userPkId.equals(palace.getUserPkId())) {
            throw new BizException("memory palace not found");
        }

        int pageNumber = normalizePageNumber(request.getPageNumber());
        int pageSize = normalizePageSize(request.getPageSize());
        int offset = (pageNumber - 1) * pageSize;
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        List<MemoryPalaceWordDO> words = memoryPalaceWordMapper.listByPalace(
                userPkId,
                palace.getId(),
                preference.getSourceLanguage(),
                preference.getTargetLanguage(),
                pageSize,
                offset);
        Integer total = memoryPalaceWordMapper.countByPalace(userPkId, palace.getId());
        return MemoryPalaceWordPageBO.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .total(defaultZero(total))
                .words(words)
                .build();
    }

    public int countWords(Long userPkId) {
        return defaultZero(memoryPalaceWordMapper.countByUser(userPkId));
    }

    private MemoryPalaceDO getUserPalace(Long userPkId, Long palaceId) {
        MemoryPalaceDO palace = memoryPalaceMapper.selectByPrimaryKey(MemoryPalaceDO.builder()
                .id(palaceId)
                .build());
        if (palace == null || !userPkId.equals(palace.getUserPkId())) {
            throw new BizException("memory palace not found");
        }
        return palace;
    }

    private MemoryPalaceWordDO getUserPalaceWord(Long userPkId, Long palaceWordId) {
        MemoryPalaceWordDO word = memoryPalaceWordMapper.selectByPrimaryKey(
                MemoryPalaceWordDO.builder().id(palaceWordId).build());
        if (word == null || !userPkId.equals(word.getUserPkId())) {
            throw new BizException("memory palace word not found");
        }
        return word;
    }

    private void updatePalaceWordCount(Long palaceId, int wordCount, String coverUrl) {
        memoryPalaceMapper.updateByPrimaryKey(MemoryPalaceDO.builder()
                .id(palaceId)
                .wordCount(wordCount)
                .coverUrl(coverUrl)
                .updateTime(OffsetDateTime.now())
                .build());
    }

    private MemoryPalaceDO getOrCreatePalace(Long userPkId, MemoryPalaceWordAddRequest request) {
        String sceneName = normalizeSceneName(request.getSceneName());
        String sceneKey = normalizeKey(sceneName);
        MemoryPalaceDO palace = memoryPalaceMapper.selectOne(MemoryPalaceDO.builder()
                .userPkId(userPkId)
                .sceneKey(sceneKey)
                .build());
        if (palace != null) {
            return palace;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long palaceId = distributedIdGenerator.id(PALACE_ID_BIZ_TYPE);
        MemoryPalaceDO newPalace = MemoryPalaceDO.builder()
                .id(palaceId)
                .userPkId(userPkId)
                .sceneKey(sceneKey)
                .sceneName(sceneName)
                .coverUrl(request.getSourceImageUrl())
                .wordCount(0)
                .sortOrder(nextTopSortOrder(userPkId))
                .createTime(now)
                .updateTime(now)
                .build();
        memoryPalaceMapper.insert(newPalace);
        return newPalace;
    }

    public String normalizeSceneName(String sceneName) {
        if (!StringUtils.hasText(sceneName)) {
            return DEFAULT_SCENE_NAME;
        }
        return sceneName.trim();
    }

    public String normalizeWordKey(String word) {
        return StringUtils.hasText(word) ? normalizeKey(word) : "";
    }

    public String normalizeWordLockKey(MemoryPalaceWordAddRequest request) {
        if (request.getWordAssetId() != null) {
            return "asset:" + request.getWordAssetId();
        }
        return normalizeWordKey(request.getWord());
    }

    private Long resolveWordAssetId(Long userPkId, MemoryPalaceWordAddRequest request) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        if (request.getWordAssetId() != null) {
            LensWordInfoBO wordInfo = wordAssetManager.resolveWordByAssetIdFast(
                    request.getWordAssetId(),
                    preference.getSourceLanguage(),
                    preference.getTargetLanguage()
            );
            if (wordInfo == null) {
                throw new BizException("word asset not found");
            }
            scheduleWordAssetWarmupAfterCommit(wordInfo.getWord());
            return request.getWordAssetId();
        }
        LensWordInfoBO wordInfo = wordAssetManager.resolveCapturedWordFast(
                request.getWord(),
                preference.getSourceLanguage(),
                preference.getTargetLanguage(),
                request.getDisplayLanguage(),
                request.getDisplayWord(),
                request.getPhonetic(),
                request.getPartOfSpeech(),
                request.getTranslationLanguage(),
                request.getTranslation(),
                request.getDefinition(),
                request.getExampleSentence(),
                request.getExampleTranslation()
        );
        request.setWordAssetId(wordInfo.getWordAssetId());
        scheduleWordAssetWarmupAfterCommit(wordInfo.getWord());
        return wordInfo.getWordAssetId();
    }

    private void scheduleWordAssetWarmupAfterCommit(String word) {
        if (!StringUtils.hasText(word)) {
            return;
        }
        Runnable warmupTask = () -> wordAssetWarmupManager.warmupCoreLanguagePairs(List.of(word));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            warmupTask.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                warmupTask.run();
            }
        });
    }

    private void scheduleImageEnhancementAfterCommit(Long userPkId,
                                                     Long palaceId,
                                                     Long palaceWordId,
                                                     MemoryPalaceWordAddRequest request) {
        if (!StringUtils.hasText(request.getSourceImageUrl())) {
            return;
        }
        String targetWord = StringUtils.hasText(request.getDisplayWord())
                ? request.getDisplayWord()
                : request.getWord();
        if (!StringUtils.hasText(targetWord)) {
            return;
        }
        Runnable task = () -> imageEnhancementManager.enhanceAfterAdd(
                userPkId,
                palaceId,
                palaceWordId,
                request.getSourceImageUrl(),
                targetWord,
                request.getXPercent(),
                request.getYPercent());
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private String normalizeKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private int normalizePageNumber(Integer pageNumber) {
        return pageNumber == null || pageNumber < 1 ? 1 : pageNumber;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    private int normalizeSearchLimit(int limit) {
        return Math.max(1, Math.min(limit, 50));
    }

    private int nextTopSortOrder(Long userPkId) {
        Integer minSortOrder = memoryPalaceMapper.minSortOrderByUser(userPkId);
        return minSortOrder == null ? 0 : Math.min(minSortOrder - 1, 0);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
