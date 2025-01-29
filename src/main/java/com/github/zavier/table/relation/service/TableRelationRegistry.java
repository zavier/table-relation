package com.github.zavier.table.relation.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TableRelationRegistry {

    private Map<Column, List<Column>> columnRelationMap = new HashMap<>();

    private Map<String, Map<String, List<Column>>> schemaTableColumnMap = new HashMap<>();

    public void register(ColumnRelation columnRelation) {
        final Column sourceColumn = columnRelation.sourceColumn();
        final Column referencedColumn = columnRelation.referencedColumn();

        columnRelationMap.computeIfAbsent(sourceColumn, k -> new ArrayList<>())
                .add(referencedColumn);
        columnRelationMap.computeIfAbsent(referencedColumn, k -> new ArrayList<>())
                .add(sourceColumn);

        schemaTableColumnMap.computeIfAbsent(sourceColumn.schema(), k -> new HashMap<>())
                .computeIfAbsent(sourceColumn.tableName(), k -> new ArrayList<>())
                .add(sourceColumn);

        schemaTableColumnMap.computeIfAbsent(referencedColumn.schema(), k -> new HashMap<>())
                .computeIfAbsent(referencedColumn.tableName(), k -> new ArrayList<>())
                .add(referencedColumn);
    }


    public @NotNull Map<Column, List<Column>>  getReferenced(String schema, String tableName) {
        final Map<String, List<Column>> tableMap = schemaTableColumnMap.get(schema);
        if (tableMap == null) {
            return new HashMap<>();
        }
        final List<Column> columns = tableMap.get(tableName);
        if (columns == null) {
            return new HashMap<>();
        }
        Map<Column, List<Column>> result = new HashMap<>();
        for (Column column : columns) {
            final List<Column> referencedColumns = columnRelationMap.get(column);
            if (referencedColumns != null) {
                result.put(column, referencedColumns);
            }
        }
        return result;
    }
}
