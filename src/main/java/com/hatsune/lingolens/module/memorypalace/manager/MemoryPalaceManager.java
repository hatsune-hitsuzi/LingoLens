package com.hatsune.lingolens.module.memorypalace.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceListBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordAddBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordPageBO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageRequest;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceMapper;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceWordMapper;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

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

    public MemoryPalaceWordAddBO addWord(Long userPkId, MemoryPalaceWordAddRequest request) {
        MemoryPalaceDO palace = getOrCreatePalace(userPkId, request);
        String wordKey = normalizeKey(request.getWord());
        MemoryPalaceWordDO existedWord = memoryPalaceWordMapper.selectOne(MemoryPalaceWordDO.builder()
                .userPkId(userPkId)
                .palaceId(palace.getId())
                .wordKey(wordKey)
                .build());
        if (existedWord != null) {
            return MemoryPalaceWordAddBO.builder()
                    .palaceId(palace.getId())
                    .palaceWordId(existedWord.getId())
                    .palaceWordCount(palace.getWordCount())
                    .added(false)
                    .build();
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long palaceWordId = distributedIdGenerator.id(PALACE_WORD_ID_BIZ_TYPE);
        MemoryPalaceWordDO word = MemoryPalaceWordDO.builder()
                .id(palaceWordId)
                .palaceId(palace.getId())
                .userPkId(userPkId)
                .wordKey(wordKey)
                .word(request.getWord())
                .displayWord(StringUtils.hasText(request.getDisplayWord()) ? request.getDisplayWord() : request.getWord())
                .phonetic(request.getPhonetic())
                .pronunciationUrl(request.getPronunciationUrl())
                .partOfSpeech(request.getPartOfSpeech())
                .translation(request.getTranslation())
                .definition(request.getDefinition())
                .exampleSentence(request.getExampleSentence())
                .exampleTranslation(request.getExampleTranslation())
                .sourceImageUrl(request.getSourceImageUrl())
                .createTime(now)
                .updateTime(now)
                .build();
        memoryPalaceWordMapper.insert(word);

        int newWordCount = defaultZero(palace.getWordCount()) + 1;
        memoryPalaceMapper.updateByPrimaryKey(MemoryPalaceDO.builder()
                .id(palace.getId())
                .coverUrl(StringUtils.hasText(palace.getCoverUrl()) ? null : request.getSourceImageUrl())
                .wordCount(newWordCount)
                .updateTime(now)
                .build());

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
        List<MemoryPalaceWordDO> words = memoryPalaceWordMapper.listByPalace(userPkId, palace.getId(), pageSize, offset);
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
        return normalizeKey(word);
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

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
