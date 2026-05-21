package com.hatsune.lingolens.common.mybatis.provider;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hatsune.lingolens.common.exception.BizException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
abstract class BaseSqlProviderSupport {

    protected String tableName(Class<?> entityClass) {
        TableName tableName = entityClass.getAnnotation(TableName.class);
        if (tableName != null && !tableName.value().isBlank()) {
            return tableName.value();
        }
        return camelToSnake(entityClass.getSimpleName());
    }

    protected List<FieldMeta> fields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(this::isTableField)
                .map(this::toFieldMeta)
                .collect(Collectors.toList());
    }

    protected FieldMeta primaryKey(Class<?> entityClass) {
        return fields(entityClass).stream()
                .filter(FieldMeta::primaryKey)
                .findFirst()
                .orElseThrow(() -> new BizException("primary key field is not found"));
    }

    protected String whereByNotNullFields(List<FieldMeta> fields, String prefix) {
        StringBuilder sql = new StringBuilder(" <where>");
        for (FieldMeta field : fields) {
            sql.append(" <if test='")
                    .append(prefix)
                    .append(field.property())
                    .append(" != null'> AND ")
                    .append(field.column())
                    .append(" = #{")
                    .append(prefix)
                    .append(field.property())
                    .append("}</if>");
        }
        return sql.append(" </where>").toString();
    }

    protected String columnList(List<FieldMeta> fields) {
        return fields.stream()
                .map(FieldMeta::column)
                .collect(Collectors.joining(", "));
    }

    protected String valueList(List<FieldMeta> fields, String prefix) {
        return fields.stream()
                .map(field -> "#{" + prefix + field.property() + "}")
                .collect(Collectors.joining(", "));
    }

    private boolean isTableField(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField == null || tableField.exist();
    }

    private FieldMeta toFieldMeta(Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        TableField tableField = field.getAnnotation(TableField.class);
        boolean primaryKey = tableId != null || "id".equals(field.getName());
        String column = field.getName();
        if (tableId != null && !tableId.value().isBlank()) {
            column = tableId.value();
        } else if (tableField != null && !tableField.value().isBlank()) {
            column = tableField.value();
        } else {
            column = camelToSnake(field.getName());
        }
        return new FieldMeta(field.getName(), column, primaryKey);
    }

    private String camelToSnake(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isUpperCase(ch) && i > 0) {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(ch));
        }
        return builder.toString();
    }

    protected record FieldMeta(String property, String column, boolean primaryKey) {
    }
}
