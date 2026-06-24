package com.hatsune.lingolens.module.memorypalace.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
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
public interface MemoryPalaceMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<MemoryPalaceDO> selectMany(MemoryPalaceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    MemoryPalaceDO selectOne(MemoryPalaceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    MemoryPalaceDO selectByPrimaryKey(MemoryPalaceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<MemoryPalaceDO> selectPage(@Param(ParamContants.ENTITIE) MemoryPalaceDO entity,
                                        @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                        @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(MemoryPalaceDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(MemoryPalaceDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<MemoryPalaceDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(MemoryPalaceDO entity);

    @Select("""
            SELECT * FROM memory_palaces
            WHERE user_pk_id = #{userPkId}
            ORDER BY sort_order ASC, update_time DESC, id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<MemoryPalaceDO> listByUser(@Param("userPkId") Long userPkId,
                                    @Param("pageSize") int pageSize,
                                    @Param("offset") int offset);

    @Select("""
            SELECT * FROM memory_palaces
            WHERE user_pk_id = #{userPkId}
              AND scene_name ILIKE CONCAT('%', #{keyword}, '%')
            ORDER BY sort_order ASC, update_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<MemoryPalaceDO> searchByName(@Param("userPkId") Long userPkId,
                                      @Param("keyword") String keyword,
                                      @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM memory_palaces WHERE user_pk_id = #{userPkId}")
    Integer countByUser(@Param("userPkId") Long userPkId);

    @Select("SELECT COALESCE(MIN(sort_order), 0) FROM memory_palaces WHERE user_pk_id = #{userPkId}")
    Integer minSortOrderByUser(@Param("userPkId") Long userPkId);

    @Update("""
            UPDATE memory_palaces
            SET sort_order = #{sortOrder}
            WHERE id = #{palaceId}
              AND user_pk_id = #{userPkId}
            """)
    int updateSortOrder(@Param("userPkId") Long userPkId,
                        @Param("palaceId") Long palaceId,
                        @Param("sortOrder") int sortOrder);

    @Update("""
            UPDATE memory_palaces
            SET scene_key = #{sceneKey},
                scene_name = #{sceneName},
                update_time = now()
            WHERE id = #{palaceId}
              AND user_pk_id = #{userPkId}
            """)
    int updateSceneName(@Param("userPkId") Long userPkId,
                        @Param("palaceId") Long palaceId,
                        @Param("sceneKey") String sceneKey,
                        @Param("sceneName") String sceneName);

    @Update("""
            UPDATE memory_palaces
            SET cover_url = #{newCoverUrl},
                update_time = now()
            WHERE id = #{palaceId}
              AND user_pk_id = #{userPkId}
              AND cover_url = #{expectedCoverUrl}
            """)
    int updateCoverUrlIfCurrent(@Param("userPkId") Long userPkId,
                                @Param("palaceId") Long palaceId,
                                @Param("expectedCoverUrl") String expectedCoverUrl,
                                @Param("newCoverUrl") String newCoverUrl);

    @Delete("""
            DELETE FROM memory_palaces
            WHERE id = #{palaceId}
              AND user_pk_id = #{userPkId}
            """)
    int deleteByUserAndId(@Param("userPkId") Long userPkId,
                          @Param("palaceId") Long palaceId);
}
