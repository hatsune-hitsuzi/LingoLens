package com.hatsune.lingolens.module.memorypalace.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
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
            ORDER BY update_time DESC, id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<MemoryPalaceDO> listByUser(@Param("userPkId") Long userPkId,
                                    @Param("pageSize") int pageSize,
                                    @Param("offset") int offset);

    @Select("SELECT COUNT(1) FROM memory_palaces WHERE user_pk_id = #{userPkId}")
    Integer countByUser(@Param("userPkId") Long userPkId);
}
