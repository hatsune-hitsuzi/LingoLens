package com.hatsune.lingolens.common.mybatis.provider;

import com.hatsune.lingolens.common.mybatis.ParamContants;

import java.util.List;
import java.util.Map;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public class BaseSelectProvider extends BaseSqlProviderSupport {

    public String select(Object entity) {
        Class<?> entityClass = entity.getClass();
        return "<script>SELECT * FROM " + tableName(entityClass)
                + whereByNotNullFields(fields(entityClass), "")
                + "</script>";
    }

    public String selectByPrimaryKey(Object entity) {
        Class<?> entityClass = entity.getClass();
        FieldMeta primaryKey = primaryKey(entityClass);
        return "SELECT * FROM " + tableName(entityClass)
                + " WHERE " + primaryKey.column() + " = #{" + primaryKey.property() + "}";
    }

    public String selectPage(Map<String, Object> params) {
        Object entity = params.get(ParamContants.ENTITIE);
        Class<?> entityClass = entity.getClass();
        int pageNumber = Math.max(1, numberParam(params, ParamContants.PAGE_NUMBER));
        int pageSize = Math.max(1, numberParam(params, ParamContants.PAGE_SIZE));
        int offset = (pageNumber - 1) * pageSize;
        return "<script>SELECT * FROM " + tableName(entityClass)
                + whereByNotNullFields(fields(entityClass), ParamContants.ENTITIE + ".")
                + " LIMIT " + pageSize
                + " OFFSET " + offset
                + "</script>";
    }

    private int numberParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
