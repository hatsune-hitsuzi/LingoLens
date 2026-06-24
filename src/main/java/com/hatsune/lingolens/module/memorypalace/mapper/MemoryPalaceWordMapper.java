package com.hatsune.lingolens.module.memorypalace.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchWordDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Mapper
public interface MemoryPalaceWordMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<MemoryPalaceWordDO> selectMany(MemoryPalaceWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    MemoryPalaceWordDO selectOne(MemoryPalaceWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    MemoryPalaceWordDO selectByPrimaryKey(MemoryPalaceWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<MemoryPalaceWordDO> selectPage(@Param(ParamContants.ENTITIE) MemoryPalaceWordDO entity,
                                            @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                            @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(MemoryPalaceWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(MemoryPalaceWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<MemoryPalaceWordDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(MemoryPalaceWordDO entity);

    @Select("""
            SELECT
                word.id,
                word.palace_id,
                word.user_pk_id,
                word.word_asset_id,
                word.anchor_x_percent,
                word.anchor_y_percent,
                word.source_image_url,
                word.create_time,
                word.update_time,
                asset.word_key AS word_key,
                asset.word_key AS word,
                COALESCE(target_payload.payload ->> 'word', asset.canonical_word) AS display_word,
                target_payload.payload ->> 'phonetic' AS phonetic,
                COALESCE(target_payload.payload ->> 'pronunciationPath', '') AS pronunciation_url,
                target_payload.payload ->> 'pos' AS part_of_speech,
                COALESCE(source_payload.payload ->> 'word', asset.canonical_word) AS translation,
                target_payload.payload ->> 'explanation' AS definition,
                target_payload.payload ->> 'example' AS example_sentence,
                source_payload.payload ->> 'example' AS example_translation
            FROM memory_palace_words word
            JOIN word_assets asset ON asset.id = word.word_asset_id
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
              AND word.palace_id = #{palaceId}
            ORDER BY word.create_time DESC, word.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<MemoryPalaceWordDO> listByPalace(@Param("userPkId") Long userPkId,
                                          @Param("palaceId") Long palaceId,
                                          @Param("sourceLanguage") String sourceLanguage,
                                          @Param("targetLanguage") String targetLanguage,
                                          @Param("pageSize") int pageSize,
                                          @Param("offset") int offset);

    @Select("""
            SELECT COUNT(1) FROM memory_palace_words
            WHERE user_pk_id = #{userPkId}
              AND palace_id = #{palaceId}
            """)
    Integer countByPalace(@Param("userPkId") Long userPkId, @Param("palaceId") Long palaceId);

    @Select("SELECT COUNT(1) FROM memory_palace_words WHERE user_pk_id = #{userPkId}")
    Integer countByUser(@Param("userPkId") Long userPkId);

    @Select("""
            SELECT COUNT(1) FROM memory_palace_words
            WHERE user_pk_id = #{userPkId}
              AND word_asset_id = #{wordAssetId}
            """)
    Integer countByUserAndAsset(@Param("userPkId") Long userPkId,
                                @Param("wordAssetId") Long wordAssetId);

    @Delete("""
            DELETE FROM memory_palace_words
            WHERE id = #{palaceWordId}
              AND user_pk_id = #{userPkId}
            """)
    int deleteByUserAndId(@Param("userPkId") Long userPkId,
                          @Param("palaceWordId") Long palaceWordId);

    @Update("""
            UPDATE memory_palace_words
            SET palace_id = #{targetPalaceId},
                update_time = now()
            WHERE id = #{palaceWordId}
              AND user_pk_id = #{userPkId}
            """)
    int moveToPalace(@Param("userPkId") Long userPkId,
                     @Param("palaceWordId") Long palaceWordId,
                     @Param("targetPalaceId") Long targetPalaceId);

    @Update("""
            UPDATE memory_palace_words
            SET source_image_url = #{newImageUrl},
                update_time = now()
            WHERE id = #{palaceWordId}
              AND user_pk_id = #{userPkId}
              AND source_image_url = #{expectedImageUrl}
            """)
    int updateSourceImageUrlIfCurrent(@Param("userPkId") Long userPkId,
                                      @Param("palaceWordId") Long palaceWordId,
                                      @Param("expectedImageUrl") String expectedImageUrl,
                                      @Param("newImageUrl") String newImageUrl);

    @Select("""
            SELECT source_image_url
            FROM (
                SELECT
                    source_image_url,
                    MAX(create_time) AS latest_time,
                    MAX(id) AS latest_id
                FROM memory_palace_words
                WHERE user_pk_id = #{userPkId}
                  AND palace_id = #{palaceId}
                  AND source_image_url IS NOT NULL
                  AND source_image_url <> ''
                GROUP BY source_image_url
            ) image
            ORDER BY latest_time DESC, latest_id DESC
            LIMIT #{limit}
            """)
    List<String> listLatestSourceImageUrlsByPalace(@Param("userPkId") Long userPkId,
                                                   @Param("palaceId") Long palaceId,
                                                   @Param("limit") int limit);

    @Select("""
            SELECT *
            FROM memory_palace_words
            WHERE user_pk_id = #{userPkId}
              AND word_asset_id = #{wordAssetId}
            ORDER BY update_time DESC, id DESC
            LIMIT 1
            """)
    MemoryPalaceWordDO selectLatestByAsset(@Param("userPkId") Long userPkId,
                                           @Param("wordAssetId") Long wordAssetId);

    @Select("""
            SELECT
                word.id AS palace_word_id,
                word.palace_id,
                palace.scene_name,
                palace.cover_url,
                word.source_image_url,
                word.word_asset_id,
                COALESCE(target_payload.payload ->> 'word', asset.canonical_word) AS display_word,
                COALESCE(source_payload.payload ->> 'word', asset.canonical_word) AS translation,
                to_char(word.create_time, 'YYYY/MM/DD') AS create_time
            FROM memory_palace_words word
            JOIN memory_palaces palace ON palace.id = word.palace_id
            JOIN word_assets asset ON asset.id = word.word_asset_id
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
              AND (
                  palace.scene_name ILIKE CONCAT('%', #{keyword}, '%')
                  OR asset.word_key ILIKE CONCAT('%', #{keyword}, '%')
                  OR asset.canonical_word ILIKE CONCAT('%', #{keyword}, '%')
                  OR EXISTS (
                      SELECT 1
                      FROM jsonb_array_elements(asset.language_payloads::jsonb) AS search_item(value)
                      WHERE search_item.value ->> 'lang' IN (
                          CASE
                              WHEN lower(coalesce(#{sourceLanguage}, 'zh-CN')) LIKE 'zh%' THEN 'zh'
                              WHEN lower(coalesce(#{sourceLanguage}, 'zh-CN')) LIKE 'ja%' THEN 'ja'
                              ELSE 'en'
                          END,
                          CASE
                              WHEN lower(coalesce(#{targetLanguage}, 'en')) LIKE 'zh%' THEN 'zh'
                              WHEN lower(coalesce(#{targetLanguage}, 'en')) LIKE 'ja%' THEN 'ja'
                              ELSE 'en'
                          END
                      )
                        AND (
                            search_item.value ->> 'word' ILIKE CONCAT('%', #{keyword}, '%')
                            OR search_item.value ->> 'phonetic' ILIKE CONCAT('%', #{keyword}, '%')
                            OR search_item.value ->> 'explanation' ILIKE CONCAT('%', #{keyword}, '%')
                            OR search_item.value ->> 'example' ILIKE CONCAT('%', #{keyword}, '%')
                        )
                  )
              )
            ORDER BY word.update_time DESC, word.id DESC
            LIMIT #{limit}
            """)
    List<MemoryPalaceSearchWordDTO> searchWords(@Param("userPkId") Long userPkId,
                                                @Param("keyword") String keyword,
                                                @Param("sourceLanguage") String sourceLanguage,
                                                @Param("targetLanguage") String targetLanguage,
                                                @Param("limit") int limit);

    @Delete("""
            DELETE FROM memory_palace_words
            WHERE user_pk_id = #{userPkId}
              AND palace_id = #{palaceId}
            """)
    int deleteByPalace(@Param("userPkId") Long userPkId,
                       @Param("palaceId") Long palaceId);
}
