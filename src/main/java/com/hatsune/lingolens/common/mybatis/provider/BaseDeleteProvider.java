package com.hatsune.lingolens.common.mybatis.provider;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public class BaseDeleteProvider extends BaseSqlProviderSupport {

    public String deleteByPrimaryKey(Object entity) {
        Class<?> entityClass = entity.getClass();
        FieldMeta primaryKey = primaryKey(entityClass);
        return "DELETE FROM " + tableName(entityClass)
                + " WHERE " + primaryKey.column() + " = #{" + primaryKey.property() + "}";
    }
}
