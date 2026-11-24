package com.github.zavier.table.relation.dao.entity;


import com.github.zavier.table.relation.service.constant.RelationType;

public record TableRelation(
        Integer id,
        String tableSchema,
        String tableName,
        String columnName,
        String conditionField,
        String conditionOperator,
        String conditionValue,
        String referencedTableSchema,
        String referencedTableName,
        String referencedColumnName,
        RelationType relationType
        ) {
}
