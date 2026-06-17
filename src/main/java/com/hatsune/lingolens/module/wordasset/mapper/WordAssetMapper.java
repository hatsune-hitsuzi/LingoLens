package com.hatsune.lingolens.module.wordasset.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.wordasset.domain.WordAssetDO;
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
public interface WordAssetMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<WordAssetDO> selectMany(WordAssetDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    WordAssetDO selectOne(WordAssetDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    WordAssetDO selectByPrimaryKey(WordAssetDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<WordAssetDO> selectPage(@Param(ParamContants.ENTITIE) WordAssetDO entity,
                                     @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                     @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(WordAssetDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(WordAssetDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<WordAssetDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(WordAssetDO entity);

    @Select("""
            SELECT *
            FROM word_assets
            WHERE word_key = #{wordKey}
            LIMIT 1
            """)
    WordAssetDO selectByWordKey(@Param("wordKey") String wordKey);
}
