package com.hatsune.lingolens.module.review.mapper;

import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.module.review.bo.ReviewCardBO;
import com.hatsune.lingolens.module.review.domain.ReviewRecordDO;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Mapper
public interface ReviewRecordMapper {

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(ReviewRecordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<ReviewRecordDO> entities);

    @Select("""
            SELECT COUNT(DISTINCT word.word_asset_id)
            FROM memory_palace_words word
            LEFT JOIN review_word_progress progress
              ON progress.user_pk_id = word.user_pk_id
             AND progress.word_asset_id = word.word_asset_id
            WHERE word.user_pk_id = #{userPkId}
              AND (progress.last_review_date IS NULL OR progress.last_review_date < CURRENT_DATE)
            """)
    Integer countDueToday(@Param("userPkId") Long userPkId);

    @Select("""
            SELECT COUNT(DISTINCT word_asset_id)
            FROM review_word_progress
            WHERE user_pk_id = #{userPkId}
              AND last_review_date = CURRENT_DATE
            """)
    Integer countAnsweredToday(@Param("userPkId") Long userPkId);

    @Select("""
            SELECT source_image_url
            FROM (
                SELECT
                    word.source_image_url AS source_image_url,
                    MAX(word.create_time) AS latest_time,
                    MAX(word.id) AS latest_id
                FROM memory_palace_words word
                WHERE word.user_pk_id = #{userPkId}
                  AND word.source_image_url IS NOT NULL
                  AND word.source_image_url <> ''
                GROUP BY word.source_image_url
            ) image
            ORDER BY latest_time DESC, latest_id DESC
            LIMIT #{limit}
            """)
    List<String> listLatestSourceImageUrls(@Param("userPkId") Long userPkId,
                                           @Param("limit") int limit);

    @Select("""
            WITH due_words AS (
                SELECT DISTINCT ON (word.word_asset_id)
                    word.id AS card_id,
                    word.palace_id AS palace_id,
                    word.word_asset_id AS word_asset_id,
                    palace.scene_name AS scene_name,
                    asset.word_key AS word,
                    COALESCE(target_payload.payload ->> 'word', asset.canonical_word) AS display_word,
                    target_payload.payload ->> 'phonetic' AS phonetic,
                    COALESCE(target_payload.payload ->> 'pronunciationPath', '') AS pronunciation_url,
                    target_payload.payload ->> 'pos' AS part_of_speech,
                    COALESCE(source_payload.payload ->> 'word', asset.canonical_word) AS translation,
                    target_payload.payload ->> 'explanation' AS definition,
                    target_payload.payload ->> 'example' AS example_sentence,
                    source_payload.payload ->> 'example' AS example_translation,
                    word.source_image_url AS source_image_url,
                    word.create_time AS create_time
                FROM memory_palace_words word
                LEFT JOIN memory_palaces palace ON palace.id = word.palace_id
                JOIN word_assets asset ON asset.id = word.word_asset_id
                LEFT JOIN review_word_progress progress
                  ON progress.user_pk_id = word.user_pk_id
                 AND progress.word_asset_id = word.word_asset_id
                LEFT JOIN LATERAL (
                    SELECT item.value AS payload
                    FROM jsonb_array_elements(asset.language_payloads::jsonb) AS item(value)
                    WHERE item.value ->> 'lang' = CASE
                        WHEN lower(coalesce(#{targetLanguage}, 'en')) LIKE 'zh%' THEN 'zh'
                        WHEN lower(coalesce(#{targetLanguage}, 'en')) LIKE 'ja%' THEN 'ja'
                        ELSE 'en'
                    END
                    LIMIT 1
                ) target_payload ON TRUE
                LEFT JOIN LATERAL (
                    SELECT item.value AS payload
                    FROM jsonb_array_elements(asset.language_payloads::jsonb) AS item(value)
                    WHERE item.value ->> 'lang' = CASE
                        WHEN lower(coalesce(#{sourceLanguage}, 'zh-CN')) LIKE 'zh%' THEN 'zh'
                        WHEN lower(coalesce(#{sourceLanguage}, 'zh-CN')) LIKE 'ja%' THEN 'ja'
                        ELSE 'en'
                    END
                    LIMIT 1
                ) source_payload ON TRUE
                WHERE word.user_pk_id = #{userPkId}
                  AND (progress.last_review_date IS NULL OR progress.last_review_date < CURRENT_DATE)
                ORDER BY word.word_asset_id, word.create_time DESC, word.id DESC
            )
            SELECT *
            FROM due_words
            ORDER BY create_time DESC, card_id DESC
            LIMIT #{pageSize}
            """)
    List<ReviewCardBO> listDueToday(@Param("userPkId") Long userPkId,
                                    @Param("sourceLanguage") String sourceLanguage,
                                    @Param("targetLanguage") String targetLanguage,
                                    @Param("pageSize") int pageSize);

    @Insert("""
            INSERT INTO review_word_progress (
                user_pk_id,
                word_asset_id,
                last_word_id,
                last_result,
                correct_count,
                wrong_count,
                total_count,
                last_answer_time,
                last_review_date,
                create_time,
                update_time
            ) VALUES (
                #{userPkId},
                #{wordAssetId},
                #{wordId},
                #{lastResult},
                CASE WHEN #{lastResult} = 1 THEN 1 ELSE 0 END,
                CASE WHEN #{lastResult} = 1 THEN 0 ELSE 1 END,
                1,
                #{answerTime},
                CAST(#{answerTime} AS date),
                now(),
                now()
            )
            ON CONFLICT (user_pk_id, word_asset_id) DO UPDATE SET
                last_word_id = EXCLUDED.last_word_id,
                last_result = EXCLUDED.last_result,
                correct_count = review_word_progress.correct_count
                    + CASE WHEN EXCLUDED.last_result = 1 THEN 1 ELSE 0 END,
                wrong_count = review_word_progress.wrong_count
                    + CASE WHEN EXCLUDED.last_result = 1 THEN 0 ELSE 1 END,
                total_count = review_word_progress.total_count + 1,
                last_answer_time = EXCLUDED.last_answer_time,
                last_review_date = EXCLUDED.last_review_date,
                update_time = now()
            """)
    int upsertProgress(@Param("userPkId") Long userPkId,
                       @Param("wordId") Long wordId,
                       @Param("wordAssetId") Long wordAssetId,
                       @Param("lastResult") int lastResult,
                       @Param("answerTime") OffsetDateTime answerTime);
}
