package com.hatsune.lingolens.module.admin.mapper;

import com.hatsune.lingolens.common.mybatis.PageList;
import com.hatsune.lingolens.common.mybatis.ParamContants;
import com.hatsune.lingolens.common.mybatis.provider.BaseDeleteProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseInsertProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseSelectProvider;
import com.hatsune.lingolens.common.mybatis.provider.BaseUpdateProvider;
import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
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
 * @since 2026-05-22
 */
@Mapper
public interface AdminMenuMapper {

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    List<AdminMenuDO> selectMany(AdminMenuDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "select")
    AdminMenuDO selectOne(AdminMenuDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectByPrimaryKey")
    AdminMenuDO selectByPrimaryKey(AdminMenuDO entity);

    @SelectProvider(type = BaseSelectProvider.class, method = "selectPage")
    PageList<AdminMenuDO> selectPage(@Param(ParamContants.ENTITIE) AdminMenuDO entity,
                                     @Param(ParamContants.PAGE_NUMBER) int pageNumber,
                                     @Param(ParamContants.PAGE_SIZE) int pageSize);

    @DeleteProvider(type = BaseDeleteProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(AdminMenuDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "insert")
    int insert(AdminMenuDO entity);

    @InsertProvider(type = BaseInsertProvider.class, method = "batchInsert")
    int batchInsert(@Param(ParamContants.ENTITIES) List<AdminMenuDO> entities);

    @UpdateProvider(type = BaseUpdateProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(AdminMenuDO entity);

    @Select("""
            <script>
            SELECT * FROM app_page_menus
            WHERE 1 = 1
            <if test='pageCode != null and pageCode != ""'> AND page_code = #{pageCode}</if>
            <if test='menuType != null'> AND menu_type = #{menuType}</if>
            <if test='visibleStatus != null'> AND visible_status = #{visibleStatus}</if>
            <if test='keyword != null and keyword != ""'>
                AND (menu_code ILIKE CONCAT('%', #{keyword}, '%')
                OR menu_name ILIKE CONCAT('%', #{keyword}, '%')
                OR remark ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY sort_no ASC, id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<AdminMenuDO> listMenus(@Param("keyword") String keyword,
                                @Param("pageCode") String pageCode,
                                @Param("menuType") Integer menuType,
                                @Param("visibleStatus") Integer visibleStatus,
                                @Param("pageSize") int pageSize,
                                @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(1) FROM app_page_menus
            WHERE 1 = 1
            <if test='pageCode != null and pageCode != ""'> AND page_code = #{pageCode}</if>
            <if test='menuType != null'> AND menu_type = #{menuType}</if>
            <if test='visibleStatus != null'> AND visible_status = #{visibleStatus}</if>
            <if test='keyword != null and keyword != ""'>
                AND (menu_code ILIKE CONCAT('%', #{keyword}, '%')
                OR menu_name ILIKE CONCAT('%', #{keyword}, '%')
                OR remark ILIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Integer countMenus(@Param("keyword") String keyword,
                       @Param("pageCode") String pageCode,
                       @Param("menuType") Integer menuType,
                       @Param("visibleStatus") Integer visibleStatus);

    @Select("""
            <script>
            SELECT * FROM app_page_menus
            WHERE page_code = #{pageCode}
              AND visible_status = 1
            <if test='menuType != null'> AND menu_type = #{menuType}</if>
            ORDER BY sort_no ASC, id DESC
            </script>
            """)
    List<AdminMenuDO> listVisibleMenus(@Param("pageCode") String pageCode,
                                       @Param("menuType") Integer menuType);
}
