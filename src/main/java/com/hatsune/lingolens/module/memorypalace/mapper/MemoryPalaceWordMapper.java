package com.hatsune.lingolens.module.memorypalace.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
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
            SELECT * FROM memory_palace_words
            WHERE user_pk_id = #{userPkId}
              AND palace_id = #{palaceId}
            ORDER BY create_time DESC, id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<MemoryPalaceWordDO> listByPalace(@Param("userPkId") Long userPkId,
                                          @Param("palaceId") Long palaceId,
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
}
