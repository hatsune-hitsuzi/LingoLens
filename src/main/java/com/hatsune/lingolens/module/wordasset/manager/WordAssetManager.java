package com.hatsune.lingolens.module.wordasset.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailRequest;
import com.hatsune.lingolens.module.wordasset.bo.GeneratedWordAssetBO;
import com.hatsune.lingolens.module.wordasset.domain.WordAssetDO;
import com.hatsune.lingolens.module.wordasset.mapper.WordAssetMapper;
import com.hatsune.lingolens.module.wordasset.support.PronunciationUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * Projects one conceptual word row into one requested language payload.
 *
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WordAssetManager {

    private static final String WORD_ASSET_ID_BIZ_TYPE = "word_asset";
    private static final String ASSET_SOURCE_LLM = "llm";
    private static final String ASSET_SOURCE_LLM_IMAGE = "llm_image";
    private static final String ASSET_SOURCE_SEED = "seed";
    private static final String ENRICHMENT_STATUS_PENDING = "pending";
    private static final String ENRICHMENT_STATUS_ENRICHED = "enriched";
    private static final String ENRICHMENT_STATUS_FAILED = "failed";
    private static final Duration CACHE_TTL = Duration.ofDays(30);

    private final WordAssetMapper wordAssetMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final DistributedIdGenerator distributedIdGenerator;
    private final WordAssetGenerator wordAssetGenerator;
    private final PronunciationUrlBuilder pronunciationUrlBuilder;

    public LensWordInfoBO resolveWord(LensWordDetailRequest request) {
        String wordKey = normalizeKey(request.getWord());
        String sourceLanguage = normalizeLanguage(request.getSourceLanguage(), "zh-CN");
        String targetLanguage = normalizeLanguage(request.getTargetLanguage(), "en");
        String cacheKey = RedisKeyConstant.WORD_ASSET.key(sourceLanguage, targetLanguage, wordKey);

        LensWordInfoBO cachedWord = loadFromCache(cacheKey);
        if (cachedWord != null && cachedWord.getWordAssetId() != null) {
            return cachedWord;
        }

        WordAssetDO asset = findByWordKey(wordKey);
        if (asset == null) {
            LensWordInfoBO generatedWord = wordAssetGenerator.generate(wordKey, sourceLanguage, targetLanguage);
            asset = insertGeneratedAsset(wordKey, sourceLanguage, targetLanguage, generatedWord, ASSET_SOURCE_LLM);
        } else if (!hasPayload(asset, sourceLanguage) || !hasPayload(asset, targetLanguage)) {
            LensWordInfoBO generatedWord = wordAssetGenerator.generate(wordKey, sourceLanguage, targetLanguage);
            mergeGeneratedPayloads(asset, sourceLanguage, targetLanguage, generatedWord);
        }

        LensWordInfoBO wordInfo = toWordInfo(asset, sourceLanguage, targetLanguage);
        writeCache(cacheKey, wordInfo);
        return wordInfo;
    }

    public LensWordInfoBO resolveWord(String word, String sourceLanguage, String targetLanguage) {
        LensWordDetailRequest request = new LensWordDetailRequest();
        request.setWord(word);
        request.setSourceLanguage(sourceLanguage);
        request.setTargetLanguage(targetLanguage);
        return resolveWord(request);
    }

    public LensWordInfoBO resolveImage(String imageUrl, String sourceLanguage, String targetLanguage) {
        String normalizedSourceLanguage = normalizeLanguage(sourceLanguage, "zh-CN");
        String normalizedTargetLanguage = normalizeLanguage(targetLanguage, "en");
        GeneratedWordAssetBO generated = wordAssetGenerator.generateFromImage(imageUrl);
        if (generated == null || !StringUtils.hasText(generated.getWordKey())) {
            return null;
        }
        String wordKey = normalizeKey(generated.getWordKey());
        String cacheKey = RedisKeyConstant.WORD_ASSET.key(normalizedSourceLanguage, normalizedTargetLanguage, wordKey);

        WordAssetDO asset = findByWordKey(wordKey);
        asset = asset == null
                ? insertGeneratedAsset(generated, ASSET_SOURCE_LLM_IMAGE, imageUrl)
                : mergeGeneratedPayloads(asset, generated, imageUrl);

        LensWordInfoBO wordInfo = toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
        writeCache(cacheKey, wordInfo);
        return wordInfo;
    }

    public LensWordInfoBO resolveWordByAssetId(Long wordAssetId, String sourceLanguage, String targetLanguage) {
        String normalizedSourceLanguage = normalizeLanguage(sourceLanguage, "zh-CN");
        String normalizedTargetLanguage = normalizeLanguage(targetLanguage, "en");
        WordAssetDO asset = wordAssetMapper.selectByPrimaryKey(WordAssetDO.builder()
                .id(wordAssetId)
                .build());
        if (asset == null) {
            return null;
        }
        if (!hasPayload(asset, normalizedSourceLanguage) || !hasPayload(asset, normalizedTargetLanguage)) {
            LensWordInfoBO generatedWord = wordAssetGenerator.generate(
                    asset.getWordKey(),
                    normalizedSourceLanguage,
                    normalizedTargetLanguage
            );
            mergeGeneratedPayloads(asset, normalizedSourceLanguage, normalizedTargetLanguage, generatedWord);
        }
        return toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
    }

    public LensWordInfoBO resolveWordByAssetIdFast(Long wordAssetId, String sourceLanguage, String targetLanguage) {
        String normalizedSourceLanguage = normalizeLanguage(sourceLanguage, "zh-CN");
        String normalizedTargetLanguage = normalizeLanguage(targetLanguage, "en");
        WordAssetDO asset = wordAssetMapper.selectByPrimaryKey(WordAssetDO.builder()
                .id(wordAssetId)
                .build());
        return asset == null ? null : toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
    }

    public LensWordInfoBO resolveWordFast(String word, String sourceLanguage, String targetLanguage) {
        String wordKey = normalizeKey(word);
        String normalizedSourceLanguage = normalizeLanguage(sourceLanguage, "zh-CN");
        String normalizedTargetLanguage = normalizeLanguage(targetLanguage, "en");
        String cacheKey = RedisKeyConstant.WORD_ASSET.key(normalizedSourceLanguage, normalizedTargetLanguage, wordKey);

        LensWordInfoBO cachedWord = loadFromCache(cacheKey);
        if (cachedWord != null && cachedWord.getWordAssetId() != null) {
            return cachedWord;
        }

        WordAssetDO asset = findByWordKey(wordKey);
        if (asset != null && hasPayload(asset, normalizedTargetLanguage)) {
            LensWordInfoBO wordInfo = toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
            writeCache(cacheKey, wordInfo);
            return wordInfo;
        }

        LensWordInfoBO localSeedWord = wordAssetGenerator.generateFastSeed(
                wordKey,
                normalizedSourceLanguage,
                normalizedTargetLanguage
        );
        asset = asset == null
                ? insertGeneratedAsset(wordKey, normalizedSourceLanguage, normalizedTargetLanguage, localSeedWord, ASSET_SOURCE_SEED)
                : mergeGeneratedPayloads(asset, normalizedSourceLanguage, normalizedTargetLanguage, localSeedWord);
        LensWordInfoBO wordInfo = toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
        writeCache(cacheKey, wordInfo);
        return wordInfo;
    }

    public LensWordInfoBO enrichWord(String word, String sourceLanguage, String targetLanguage) {
        String wordKey = normalizeKey(word);
        String normalizedSourceLanguage = normalizeLanguage(sourceLanguage, "zh-CN");
        String normalizedTargetLanguage = normalizeLanguage(targetLanguage, "en");
        WordAssetDO asset = findByWordKey(wordKey);
        if (asset == null) {
            LensWordInfoBO generatedWord = wordAssetGenerator.generateEnriched(
                    wordKey,
                    normalizedSourceLanguage,
                    normalizedTargetLanguage
            );
            if (generatedWord == null) {
                generatedWord = wordAssetGenerator.generateFastSeed(wordKey, normalizedSourceLanguage, normalizedTargetLanguage);
                asset = insertGeneratedAsset(wordKey, normalizedSourceLanguage, normalizedTargetLanguage, generatedWord, ASSET_SOURCE_SEED);
                markAssetEnrichmentStatus(asset, ENRICHMENT_STATUS_FAILED);
                return toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
            }
            asset = insertGeneratedAsset(wordKey, normalizedSourceLanguage, normalizedTargetLanguage, generatedWord, ASSET_SOURCE_LLM);
        } else if (shouldEnrichAsset(asset, normalizedSourceLanguage, normalizedTargetLanguage)) {
            LensWordInfoBO generatedWord = wordAssetGenerator.generateEnriched(
                    wordKey,
                    normalizedSourceLanguage,
                    normalizedTargetLanguage
            );
            if (generatedWord == null) {
                markAssetEnrichmentStatus(asset, ENRICHMENT_STATUS_FAILED);
                return toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
            }
            mergeGeneratedPayloads(asset, normalizedSourceLanguage, normalizedTargetLanguage, generatedWord);
        }
        markAssetEnrichmentStatus(asset, ENRICHMENT_STATUS_ENRICHED);

        LensWordInfoBO wordInfo = toWordInfo(asset, normalizedSourceLanguage, normalizedTargetLanguage);
        String cacheKey = RedisKeyConstant.WORD_ASSET.key(normalizedSourceLanguage, normalizedTargetLanguage, wordKey);
        writeCache(cacheKey, wordInfo);
        return wordInfo;
    }

    public void markWordEnriched(String word) {
        String wordKey = normalizeKey(word);
        WordAssetDO asset = findByWordKey(wordKey);
        if (asset != null) {
            markAssetSource(asset, ASSET_SOURCE_LLM);
        }
    }

    private boolean shouldEnrichAsset(WordAssetDO asset, String sourceLanguage, String targetLanguage) {
        return ASSET_SOURCE_SEED.equals(asset.getAssetSource())
                || !hasPayload(asset, sourceLanguage)
                || !hasPayload(asset, targetLanguage);
    }

    private void markAssetSource(WordAssetDO asset, String assetSource) {
        if (!StringUtils.hasText(assetSource) || assetSource.equals(asset.getAssetSource())) {
            return;
        }
        asset.setAssetSource(assetSource);
        asset.setUpdateTime(OffsetDateTime.now());
        wordAssetMapper.updateByPrimaryKey(WordAssetDO.builder()
                .id(asset.getId())
                .assetSource(asset.getAssetSource())
                .updateTime(asset.getUpdateTime())
                .build());
    }

    private void markAssetEnrichmentStatus(WordAssetDO asset, String enrichmentStatus) {
        if (asset == null || !StringUtils.hasText(enrichmentStatus)) {
            return;
        }
        if (enrichmentStatus.equals(asset.getEnrichmentStatus())) {
            return;
        }
        asset.setEnrichmentStatus(enrichmentStatus);
        asset.setUpdateTime(OffsetDateTime.now());
        wordAssetMapper.updateByPrimaryKey(WordAssetDO.builder()
                .id(asset.getId())
                .enrichmentStatus(asset.getEnrichmentStatus())
                .updateTime(asset.getUpdateTime())
                .build());
    }

    private LensWordInfoBO loadFromCache(String cacheKey) {
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(cachedJson)) {
            return null;
        }
        try {
            return JSON.parseObject(cachedJson, LensWordInfoBO.class);
        } catch (Exception e) {
            log.warn("word asset cache parse failed, cacheKey={}", cacheKey, e);
            stringRedisTemplate.delete(cacheKey);
            return null;
        }
    }

    private void writeCache(String cacheKey, LensWordInfoBO wordInfo) {
        stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(wordInfo), CACHE_TTL);
    }

    private WordAssetDO findByWordKey(String wordKey) {
        return wordAssetMapper.selectByWordKey(wordKey);
    }

    private WordAssetDO insertGeneratedAsset(String wordKey,
                                            String sourceLanguage,
                                            String targetLanguage,
                                            LensWordInfoBO wordInfo,
                                            String assetSource) {
        OffsetDateTime now = OffsetDateTime.now();
        WordAssetDO asset = WordAssetDO.builder()
                .id(distributedIdGenerator.id(WORD_ASSET_ID_BIZ_TYPE))
                .wordKey(wordKey)
                .canonicalWord(canonicalWordFor(wordKey, sourceLanguage, targetLanguage, wordInfo))
                .languagePayloads(buildGeneratedPayloads(wordKey, sourceLanguage, targetLanguage, wordInfo).toJSONString())
                .assetSource(StringUtils.hasText(assetSource) ? assetSource : ASSET_SOURCE_LLM)
                .enrichmentStatus(enrichmentStatusForAssetSource(assetSource))
                .createTime(now)
                .updateTime(now)
                .build();
        try {
            wordAssetMapper.insert(asset);
            return asset;
        } catch (DuplicateKeyException e) {
            WordAssetDO existed = findByWordKey(wordKey);
            return existed == null ? asset : existed;
        }
    }

    private WordAssetDO insertGeneratedAsset(GeneratedWordAssetBO generated,
                                            String assetSource,
                                            String imageUrl) {
        OffsetDateTime now = OffsetDateTime.now();
        String wordKey = normalizeKey(generated.getWordKey());
        WordAssetDO asset = WordAssetDO.builder()
                .id(distributedIdGenerator.id(WORD_ASSET_ID_BIZ_TYPE))
                .wordKey(wordKey)
                .canonicalWord(defaultIfBlank(generated.getCanonicalWord(), wordKey))
                .languagePayloads(buildGeneratedPayloads(generated).toJSONString())
                .assetImageUrl(defaultIfBlank(imageUrl, ""))
                .assetSource(StringUtils.hasText(assetSource) ? assetSource : ASSET_SOURCE_LLM_IMAGE)
                .enrichmentStatus(enrichmentStatusForAssetSource(assetSource))
                .createTime(now)
                .updateTime(now)
                .build();
        try {
            wordAssetMapper.insert(asset);
            return asset;
        } catch (DuplicateKeyException e) {
            WordAssetDO existed = findByWordKey(wordKey);
            return existed == null ? asset : existed;
        }
    }

    private WordAssetDO mergeGeneratedPayloads(WordAssetDO asset,
                                              String sourceLanguage,
                                              String targetLanguage,
                                              LensWordInfoBO wordInfo) {
        JSONArray currentPayloads = parsePayloads(asset.getLanguagePayloads());
        JSONArray generatedPayloads = buildGeneratedPayloads(asset.getWordKey(), sourceLanguage, targetLanguage, wordInfo);
        for (int i = 0; i < generatedPayloads.size(); i++) {
            JSONObject payload = generatedPayloads.getJSONObject(i);
            upsertPayload(currentPayloads, payload);
        }
        asset.setLanguagePayloads(currentPayloads.toJSONString());
        asset.setUpdateTime(OffsetDateTime.now());
        wordAssetMapper.updateByPrimaryKey(WordAssetDO.builder()
                .id(asset.getId())
                .languagePayloads(asset.getLanguagePayloads())
                .enrichmentStatus(asset.getEnrichmentStatus())
                .updateTime(asset.getUpdateTime())
                .build());
        return asset;
    }

    private WordAssetDO mergeGeneratedPayloads(WordAssetDO asset,
                                              GeneratedWordAssetBO generated,
                                              String imageUrl) {
        JSONArray currentPayloads = parsePayloads(asset.getLanguagePayloads());
        JSONArray generatedPayloads = buildGeneratedPayloads(generated);
        for (int i = 0; i < generatedPayloads.size(); i++) {
            JSONObject payload = generatedPayloads.getJSONObject(i);
            upsertPayload(currentPayloads, payload);
        }
        asset.setLanguagePayloads(currentPayloads.toJSONString());
        asset.setCanonicalWord(defaultIfBlank(asset.getCanonicalWord(), generated.getCanonicalWord()));
        if (StringUtils.hasText(imageUrl)) {
            asset.setAssetImageUrl(imageUrl.trim());
        }
        asset.setUpdateTime(OffsetDateTime.now());
        wordAssetMapper.updateByPrimaryKey(WordAssetDO.builder()
                .id(asset.getId())
                .canonicalWord(asset.getCanonicalWord())
                .languagePayloads(asset.getLanguagePayloads())
                .assetImageUrl(asset.getAssetImageUrl())
                .enrichmentStatus(asset.getEnrichmentStatus())
                .updateTime(asset.getUpdateTime())
                .build());
        return asset;
    }

    private JSONArray buildGeneratedPayloads(String wordKey,
                                             String sourceLanguage,
                                             String targetLanguage,
                                             LensWordInfoBO wordInfo) {
        JSONArray payloads = new JSONArray();
        String targetLang = contentLanguage(targetLanguage);
        payloads.add(payload(
                targetLang,
                defaultIfBlank(wordInfo.getDisplayWord(), wordKey),
                wordInfo.getPhonetic(),
                wordInfo.getPartOfSpeech(),
                wordInfo.getDefinition(),
                wordInfo.getExampleSentence()
        ));
        String sourceLang = contentLanguage(sourceLanguage);
        if (!sourceLang.equals(targetLang)) {
            payloads.add(payload(
                    sourceLang,
                    defaultIfBlank(wordInfo.getTranslation(), wordKey),
                    "",
                    "",
                    wordInfo.getTranslation(),
                    wordInfo.getExampleTranslation()
            ));
        }
        return payloads;
    }

    private JSONArray buildGeneratedPayloads(GeneratedWordAssetBO generated) {
        JSONArray payloads = new JSONArray();
        if (generated.getPayloads() == null) {
            return payloads;
        }
        for (GeneratedWordAssetBO.LanguagePayload languagePayload : generated.getPayloads()) {
            if (languagePayload == null || !StringUtils.hasText(languagePayload.getLang())) {
                continue;
            }
            payloads.add(payload(
                    contentLanguage(languagePayload.getLang()),
                    languagePayload.getWord(),
                    languagePayload.getPhonetic(),
                    languagePayload.getPos(),
                    languagePayload.getExplanation(),
                    languagePayload.getExample()
            ));
        }
        return payloads;
    }

    private JSONObject payload(String lang,
                               String word,
                               String phonetic,
                               String pos,
                               String explanation,
                               String example) {
        JSONObject payload = new JSONObject();
        payload.put("lang", lang);
        payload.put("word", defaultIfBlank(word, ""));
        payload.put("phonetic", defaultIfBlank(phonetic, ""));
        payload.put("pos", defaultIfBlank(pos, ""));
        payload.put("explanation", defaultIfBlank(explanation, ""));
        payload.put("example", defaultIfBlank(example, ""));
        payload.put("pronunciationPath", pronunciationUrlBuilder.buildPath(defaultIfBlank(word, ""), lang));
        return payload;
    }

    private LensWordInfoBO toWordInfo(WordAssetDO asset, String sourceLanguage, String targetLanguage) {
        String targetLang = contentLanguage(targetLanguage);
        String sourceLang = contentLanguage(sourceLanguage);
        JSONObject targetPayload = findPayload(asset, targetLang);
        JSONObject sourcePayload = findPayload(asset, sourceLang);
        String displayWord = targetPayload.getString("word");
        String pronunciationBaseUrl = pronunciationUrlBuilder.baseUrl();
        String pronunciationPath = defaultIfBlank(
                targetPayload.getString("pronunciationPath"),
                pronunciationUrlBuilder.buildPath(displayWord, targetLang)
        );
        String pos = targetPayload.getString("pos");
        String explanation = targetPayload.getString("explanation");
        String example = targetPayload.getString("example");
        return LensWordInfoBO.builder()
                .wordAssetId(asset.getId())
                .lang(targetLang)
                .word(asset.getWordKey())
                .displayWord(displayWord)
                .phonetic(targetPayload.getString("phonetic"))
                .pronunciationBaseUrl(pronunciationBaseUrl)
                .pronunciationPath(pronunciationPath)
                .pronunciationUrl(pronunciationUrlBuilder.buildFromPath(pronunciationBaseUrl, pronunciationPath))
                .pos(pos)
                .partOfSpeech(pos)
                .explanation(explanation)
                .translation(sourcePayload.getString("word"))
                .definition(explanation)
                .example(example)
                .exampleSentence(example)
                .exampleTranslation(sourcePayload.getString("example"))
                .assetImageUrl(asset.getAssetImageUrl())
                .build();
    }

    private boolean hasPayload(WordAssetDO asset, String language) {
        if (asset == null) {
            return false;
        }
        return findPayloadOrNull(asset, contentLanguage(language)) != null;
    }

    private JSONObject findPayload(WordAssetDO asset, String language) {
        JSONObject payload = findPayloadOrNull(asset, language);
        if (payload != null) {
            return payload;
        }
        payload = findPayloadOrNull(asset, "en");
        if (payload != null) {
            return payload;
        }
        JSONArray payloads = parsePayloads(asset.getLanguagePayloads());
        return payloads.isEmpty() ? new JSONObject() : payloads.getJSONObject(0);
    }

    private JSONObject findPayloadOrNull(WordAssetDO asset, String language) {
        JSONArray payloads = parsePayloads(asset.getLanguagePayloads());
        String normalizedLanguage = contentLanguage(language);
        for (int i = 0; i < payloads.size(); i++) {
            JSONObject payload = payloads.getJSONObject(i);
            if (normalizedLanguage.equals(contentLanguage(payload.getString("lang")))) {
                return payload;
            }
        }
        return null;
    }

    private void upsertPayload(JSONArray payloads, JSONObject newPayload) {
        String lang = contentLanguage(newPayload.getString("lang"));
        for (int i = 0; i < payloads.size(); i++) {
            JSONObject current = payloads.getJSONObject(i);
            if (lang.equals(contentLanguage(current.getString("lang")))) {
                payloads.set(i, newPayload);
                return;
            }
        }
        payloads.add(newPayload);
    }

    private JSONArray parsePayloads(String payloadsJson) {
        if (!StringUtils.hasText(payloadsJson)) {
            return new JSONArray();
        }
        try {
            return JSON.parseArray(payloadsJson);
        } catch (Exception e) {
            log.warn("word asset payload parse failed, payloadsJson={}", payloadsJson, e);
            return new JSONArray();
        }
    }

    private String normalizeKey(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguage(String language, String defaultValue) {
        return StringUtils.hasText(language) ? language.trim() : defaultValue;
    }

    private String contentLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "en";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh")) {
            return "zh";
        }
        if (normalized.startsWith("ja")) {
            return "ja";
        }
        return "en";
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String canonicalWordFor(String wordKey,
                                    String sourceLanguage,
                                    String targetLanguage,
                                    LensWordInfoBO wordInfo) {
        if ("en".equals(contentLanguage(targetLanguage)) && StringUtils.hasText(wordInfo.getDisplayWord())) {
            return wordInfo.getDisplayWord().trim();
        }
        if ("en".equals(contentLanguage(sourceLanguage)) && StringUtils.hasText(wordInfo.getTranslation())) {
            return wordInfo.getTranslation().trim();
        }
        return wordKey.replace('-', ' ');
    }

    private String enrichmentStatusForAssetSource(String assetSource) {
        String normalized = defaultIfBlank(assetSource, ASSET_SOURCE_LLM);
        return ASSET_SOURCE_LLM.equals(normalized) || ASSET_SOURCE_LLM_IMAGE.equals(normalized)
                ? ENRICHMENT_STATUS_ENRICHED
                : ENRICHMENT_STATUS_PENDING;
    }
}
