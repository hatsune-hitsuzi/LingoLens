package com.hatsune.lingolens.module.arscan.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.arscan.domain.ArScanWordDO;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Mapper
public interface ArScanWordMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<ArScanWordDO> selectMany(ArScanWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    ArScanWordDO selectOne(ArScanWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    ArScanWordDO selectByPrimaryKey(ArScanWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<ArScanWordDO> selectPage(@Param(ParamContants.ENTITIE) ArScanWordDO entity,
                                      @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                      @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(ArScanWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(ArScanWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<ArScanWordDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(ArScanWordDO entity);

    @Select("SELECT COUNT(1) FROM ar_scan_words WHERE user_pk_id = #{userPkId}")
    Integer countByUser(@Param("userPkId") Long userPkId);

    @Select("""
            <script>
            SELECT
                word.id,
                word.user_pk_id,
                word.word_asset_id,
                word.source_image_url,
                word.scene_name,
                word.first_palace_id,
                word.create_time,
                word.update_time,
                asset.word_key AS word_key,
                asset.word_key AS word,
                COALESCE(target_payload.payload ->> 'word', asset.canonical_word) AS display_word,
                target_payload.payload ->> 'phonetic' AS phonetic,
                COALESCE(asset.pronunciation_base_url, 'http://dict.youdao.com/dictvoice')
                    || COALESCE(target_payload.payload ->> 'pronunciationPath', '') AS pronunciation_url,
                target_payload.payload ->> 'pos' AS part_of_speech,
                COALESCE(source_payload.payload ->> 'word', asset.canonical_word) AS translation,
                target_payload.payload ->> 'explanation' AS definition,
                target_payload.payload ->> 'example' AS example_sentence,
                source_payload.payload ->> 'example' AS example_translation
            FROM ar_scan_words word
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
            <if test='keyword != null and keyword != ""'>
                AND (asset.word_key ILIKE CONCAT('%', #{keyword}, '%')
                OR asset.language_payloads ILIKE CONCAT('%', #{keyword}, '%')
                OR word.scene_name ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY word.update_time DESC, word.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<ArScanWordDO> pageByUser(@Param("userPkId") Long userPkId,
                                  @Param("keyword") String keyword,
                                  @Param("sourceLanguage") String sourceLanguage,
                                  @Param("targetLanguage") String targetLanguage,
                                  @Param("pageSize") int pageSize,
                                  @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM ar_scan_words word
            JOIN word_assets asset ON asset.id = word.word_asset_id
            WHERE word.user_pk_id = #{userPkId}
            <if test='keyword != null and keyword != ""'>
                AND (asset.word_key ILIKE CONCAT('%', #{keyword}, '%')
                OR asset.language_payloads ILIKE CONCAT('%', #{keyword}, '%')
                OR word.scene_name ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Integer countByUserAndKeyword(@Param("userPkId") Long userPkId,
                                  @Param("keyword") String keyword,
                                  @Param("sourceLanguage") String sourceLanguage,
                                  @Param("targetLanguage") String targetLanguage);

    @Select("""
            SELECT COUNT(1) FROM ar_scan_words
            WHERE user_pk_id = #{userPkId}
              AND create_time >= CURRENT_DATE
              AND create_time < CURRENT_DATE + INTERVAL '1 day'
            """)
    Integer countCreatedToday(@Param("userPkId") Long userPkId);
}
