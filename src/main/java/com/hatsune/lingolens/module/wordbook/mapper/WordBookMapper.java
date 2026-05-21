package com.hatsune.lingolens.module.wordbook.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.wordbook.domain.WordBookDO;
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
public interface WordBookMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<WordBookDO> selectMany(WordBookDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    WordBookDO selectOne(WordBookDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    WordBookDO selectByPrimaryKey(WordBookDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<WordBookDO> selectPage(@Param(ParamContants.ENTITIE) WordBookDO entity,
                                    @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                    @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(WordBookDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(WordBookDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<WordBookDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(WordBookDO entity);

    @Select("""
            <script>
            SELECT * FROM word_books
            WHERE 1 = 1
            <if test='status != null'> AND status = #{status}</if>
            <if test='difficultyLevel != null'> AND difficulty_level = #{difficultyLevel}</if>
            <if test='keyword != null and keyword != ""'>
                AND (book_name ILIKE CONCAT('%', #{keyword}, '%')
                OR description ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<WordBookDO> listWordBooks(@Param("keyword") String keyword,
                                   @Param("difficultyLevel") Integer difficultyLevel,
                                   @Param("status") Integer status,
                                   @Param("pageSize") int pageSize,
                                   @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(1) FROM word_books
            WHERE 1 = 1
            <if test='status != null'> AND status = #{status}</if>
            <if test='difficultyLevel != null'> AND difficulty_level = #{difficultyLevel}</if>
            <if test='keyword != null and keyword != ""'>
                AND (book_name ILIKE CONCAT('%', #{keyword}, '%')
                OR description ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Integer countWordBooks(@Param("keyword") String keyword,
                           @Param("difficultyLevel") Integer difficultyLevel,
                           @Param("status") Integer status);
}
