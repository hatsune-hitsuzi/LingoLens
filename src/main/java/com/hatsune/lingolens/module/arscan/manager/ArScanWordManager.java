package com.hatsune.lingolens.module.arscan.manager;

import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.arscan.domain.ArScanWordDO;
import com.hatsune.lingolens.module.arscan.mapper.ArScanWordMapper;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddRequest;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Component
@RequiredArgsConstructor
public class ArScanWordManager {

    private static final String AR_SCAN_WORD_ID_BIZ_TYPE = "ar_scan_word";

    private final ArScanWordMapper arScanWordMapper;
    private final DistributedIdGenerator distributedIdGenerator;
    private final UserPreferenceManager userPreferenceManager;

    public boolean recordWord(Long userPkId,
                              Long palaceId,
                              String sceneName,
                              Long wordAssetId,
                              MemoryPalaceWordAddRequest request) {
        ArScanWordDO existedWord = arScanWordMapper.selectOne(ArScanWordDO.builder()
                .userPkId(userPkId)
                .wordAssetId(wordAssetId)
                .build());
        OffsetDateTime now = OffsetDateTime.now();
        if (existedWord != null) {
            arScanWordMapper.updateByPrimaryKey(toUpdateWord(existedWord, palaceId, sceneName, request, now));
            return false;
        }

        arScanWordMapper.insert(toNewWord(userPkId, palaceId, sceneName, wordAssetId, request, now));
        return true;
    }

    public int countWords(Long userPkId) {
        Integer count = arScanWordMapper.countByUser(userPkId);
        return count == null ? 0 : count;
    }

    public int countCreatedToday(Long userPkId) {
        Integer count = arScanWordMapper.countCreatedToday(userPkId);
        return count == null ? 0 : count;
    }

    public void removeWord(Long userPkId, Long wordAssetId) {
        arScanWordMapper.deleteByUserAndAsset(userPkId, wordAssetId);
    }

    public void moveWord(Long userPkId,
                         Long wordAssetId,
                         Long palaceId,
                         String sceneName) {
        ArScanWordDO existedWord = arScanWordMapper.selectOne(ArScanWordDO.builder()
                .userPkId(userPkId)
                .wordAssetId(wordAssetId)
                .build());
        if (existedWord == null) {
            return;
        }
        arScanWordMapper.updateByPrimaryKey(ArScanWordDO.builder()
                .id(existedWord.getId())
                .firstPalaceId(palaceId)
                .sceneName(sceneName)
                .updateTime(OffsetDateTime.now())
                .build());
    }

    public List<ArScanWordDO> pageWords(Long userPkId, String keyword, int pageNumber, int pageSize) {
        int normalizedPageNumber = normalizePageNumber(pageNumber);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPageNumber - 1) * normalizedPageSize;
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        return arScanWordMapper.pageByUser(
                userPkId,
                normalizeKeyword(keyword),
                preference.getSourceLanguage(),
                preference.getTargetLanguage(),
                normalizedPageSize,
                offset);
    }

    public int countWords(Long userPkId, String keyword) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        Integer count = arScanWordMapper.countByUserAndKeyword(
                userPkId,
                normalizeKeyword(keyword),
                preference.getSourceLanguage(),
                preference.getTargetLanguage());
        return count == null ? 0 : count;
    }

    private ArScanWordDO toNewWord(Long userPkId,
                                   Long palaceId,
                                   String sceneName,
                                   Long wordAssetId,
                                   MemoryPalaceWordAddRequest request,
                                   OffsetDateTime now) {
        return ArScanWordDO.builder()
                .id(distributedIdGenerator.id(AR_SCAN_WORD_ID_BIZ_TYPE))
                .userPkId(userPkId)
                .wordAssetId(wordAssetId)
                .sourceImageUrl(request.getSourceImageUrl())
                .sceneName(sceneName)
                .firstPalaceId(palaceId)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    private ArScanWordDO toUpdateWord(ArScanWordDO existedWord,
                                      Long palaceId,
                                      String sceneName,
                                      MemoryPalaceWordAddRequest request,
                                      OffsetDateTime now) {
        return ArScanWordDO.builder()
                .id(existedWord.getId())
                .sourceImageUrl(request.getSourceImageUrl())
                .sceneName(sceneName)
                .firstPalaceId(palaceId)
                .updateTime(now)
                .build();
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
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
}
