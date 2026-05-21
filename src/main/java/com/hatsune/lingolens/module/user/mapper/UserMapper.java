package com.hatsune.lingolens.module.user.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.user.domain.UserDO;
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
 * @since 2026-05-21
 */
@Mapper
public interface UserMapper {

    /**
     * 根据实体对象的字段值查询多条记录，查询条件会跳过NULL值的字段。
     *
     * @param entity 实体对象
     * @return 实体对象列表
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<UserDO> selectMany(UserDO entity);

    /**
     * 根据实体对象的字段值查询单条记录，查询条件会跳过NULL值的字段。
     *
     * @param entity 实体对象
     * @return 单个实体对象
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    UserDO selectOne(UserDO entity);

    /**
     * 根据实体对象中主键字段查询单条数据。
     *
     * @param entity 实体对象
     * @return 单个实体对象
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    UserDO selectByPrimaryKey(UserDO entity);

    /**
     * 根据实体对象的字段值查询分页记录，查询条件会跳过NULL值的字段。
     *
     * @param entity     实体对象
     * @param pageNumber 页码
     * @param pageSize   页大小
     * @return 分页数据
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<UserDO> selectPage(@Param(ParamContants.ENTITIE) UserDO entity,
                                @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                @Param(ParamContants.PAGE_SIZE) int pageSize);

    /**
     * 根据实体对象中主键字段删除数据。
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(UserDO entity);

    /**
     * 将实体对象写入数据库，会跳过NULL值的字段。
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(UserDO entity);

    /**
     * 批量将实体对象写入数据库，不会跳过NULL值。
     *
     * @param entities 实体对象列表
     * @return 影响行数
     */
    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<UserDO> entities);

    /**
     * 根据实体类中主键字段更新数据，会跳过NULL值的字段。
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(UserDO entity);

    /**
     * 获取下一个用户可见ID序列。
     *
     * @return 用户可见ID序列
     */
    @Select("SELECT nextval('user_id_seq')")
    Long nextUserIdSequence();
}
