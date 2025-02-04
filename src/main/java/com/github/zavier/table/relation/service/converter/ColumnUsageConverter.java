package com.github.zavier.table.relation.service.converter;

import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.ColumnUsage;

import java.util.Map;

public class ColumnUsageConverter {
    public static ColumnUsage convert2ColumnUsage(Map<String, Object> dataMap) {
        final String schema = (String) dataMap.get("TABLE_SCHEMA");
        final String tableName = (String) dataMap.get("TABLE_NAME");
        final String columnName = (String) dataMap.get("COLUMN_NAME");
        final String referencedTableSchema = (String) dataMap.get("REFERENCED_TABLE_SCHEMA");
        final String referencedTableName = (String) dataMap.get("REFERENCED_TABLE_NAME");
        final String referencedColumnName = (String) dataMap.get("REFERENCED_COLUMN_NAME");

        final ColumnUsage usage = new ColumnUsage();
        usage.setTableSchema(schema);
        usage.setTableName(tableName);
        usage.setColumnName(columnName);
        usage.setCondition("");
        usage.setReferencedTableSchema(referencedTableSchema);
        usage.setReferencedTableName(referencedTableName);
        usage.setReferencedColumnName(referencedColumnName);
        usage.setRelationType(RelationType.ONE_TO_MANY.getValue());
        return usage;
    }
}
