package com.hatsune.lingolens.module.wordbook.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.wordbook.domain.WordBookWordDO;
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
public interface WordBookWordMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<WordBookWordDO> selectMany(WordBookWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    WordBookWordDO selectOne(WordBookWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    WordBookWordDO selectByPrimaryKey(WordBookWordDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<WordBookWordDO> selectPage(@Param(ParamContants.ENTITIE) WordBookWordDO entity,
                                        @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                        @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(WordBookWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(WordBookWordDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<WordBookWordDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(WordBookWordDO entity);

    @Select("""
            <script>
            SELECT * FROM word_book_words
            WHERE book_id = #{bookId}
            <if test='keyword != null and keyword != ""'>
                AND (word ILIKE CONCAT('%', #{keyword}, '%')
                OR translation ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY sort_no ASC, id ASC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<WordBookWordDO> listWords(@Param("bookId") Long bookId,
                                   @Param("keyword") String keyword,
                                   @Param("pageSize") int pageSize,
                                   @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(1) FROM word_book_words
            WHERE book_id = #{bookId}
            <if test='keyword != null and keyword != ""'>
                AND (word ILIKE CONCAT('%', #{keyword}, '%')
                OR translation ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Integer countWords(@Param("bookId") Long bookId,
                       @Param("keyword") String keyword);
}
