package com.hatsune.lingolens.module.user.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Mapper
public interface UserPreferenceMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<UserPreferenceDO> selectMany(UserPreferenceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    UserPreferenceDO selectOne(UserPreferenceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    UserPreferenceDO selectByPrimaryKey(UserPreferenceDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<UserPreferenceDO> selectPage(@Param(ParamContants.ENTITIE) UserPreferenceDO entity,
                                          @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                          @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(UserPreferenceDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(UserPreferenceDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<UserPreferenceDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(UserPreferenceDO entity);
}
