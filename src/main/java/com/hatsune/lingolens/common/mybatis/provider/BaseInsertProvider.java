package com.hatsune.lingolens.common.mybatis.provider;

import com.hatsune.lingolens.common.mybatis.ParamContants;

import java.util.List;
import java.util.Map;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public class BaseInsertProvider extends BaseSqlProviderSupport {

    public String insert(Object entity) {
        Class<?> entityClass = entity.getClass();
        List<FieldMeta> fields = fields(entityClass);
        StringBuilder sql = new StringBuilder("<script>INSERT INTO ");
        sql.append(tableName(entityClass));
        sql.append(" <trim prefix='(' suffix=')' suffixOverrides=','>");
        for (FieldMeta field : fields) {
            sql.append(" <if test='").append(field.property()).append(" != null'>")
                    .append(field.column())
                    .append(",</if>");
        }
        sql.append(" </trim>");
        sql.append(" <trim prefix='VALUES (' suffix=')' suffixOverrides=','>");
        for (FieldMeta field : fields) {
            sql.append(" <if test='").append(field.property()).append(" != null'>#{")
                    .append(field.property())
                    .append("},</if>");
        }
        sql.append(" </trim></script>");
        return sql.toString();
    }

    public String batchInsert(Map<String, Object> params) {
        Object firstEntity = ((List<?>) params.get(ParamContants.ENTITIES)).get(0);
        Class<?> entityClass = firstEntity.getClass();
        List<FieldMeta> fields = fields(entityClass);
        return "<script>INSERT INTO " + tableName(entityClass)
                + " (" + columnList(fields) + ") VALUES "
                + "<foreach collection='" + ParamContants.ENTITIES + "' item='item' separator=','>"
                + "(" + valueList(fields, "item.") + ")"
                + "</foreach></script>";
    }
}
