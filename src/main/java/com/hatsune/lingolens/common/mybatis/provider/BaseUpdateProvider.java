package com.hatsune.lingolens.common.mybatis.provider;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public class BaseUpdateProvider extends BaseSqlProviderSupport {

    public String updateByPrimaryKey(Object entity) {
        Class<?> entityClass = entity.getClass();
        FieldMeta primaryKey = primaryKey(entityClass);
        List<FieldMeta> fields = fields(entityClass);
        StringBuilder sql = new StringBuilder("<script>UPDATE ");
        sql.append(tableName(entityClass)).append(" <set>");
        for (FieldMeta field : fields) {
            if (field.primaryKey()) {
                continue;
            }
            sql.append(" <if test='").append(field.property()).append(" != null'>")
                    .append(field.column())
                    .append(" = #{")
                    .append(field.property())
                    .append("},</if>");
        }
        sql.append(" </set> WHERE ")
                .append(primaryKey.column())
                .append(" = #{")
                .append(primaryKey.property())
                .append("}</script>");
        return sql.toString();
    }
}
