package com.github.zavier.table.relation.service.abilty;

import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import com.github.zavier.table.relation.service.dto.EntityRelationShip;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TableRelationRegistry {

    // schema -> tableName -> columns
    private Map<String, Map<String, List<Column>>> schemaTableColumnMap = new HashMap<>();
    // column -> referencedColumns
    private Map<Column, List<Column>> columnRelationMap = new HashMap<>();

    // 暂时没有使用，本来是想用来生成ER图时体现一下关系
    private Set<ColumnRelation> columnRelations = new HashSet<>();

    public void clear() {
        schemaTableColumnMap.clear();
        columnRelationMap.clear();
        columnRelations.clear();
    }

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

        columnRelations.add(columnRelation);
    }


    public @NotNull Map<Column, List<Column>> getDirectReferenced(String schema, String tableName) {
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

    public List<EntityRelationShip> getAllReferenced(String schema, String tableName) {
        final Map<Column, List<Column>> directReferenced = getDirectReferenced(schema, tableName);
        if (directReferenced.isEmpty()) {
            return List.of();
        }
        Set<Column> uniqueKey = new HashSet<>(directReferenced.keySet());

        // table1, table2 , label(col1 -> col2)
        List<EntityRelationShip> relationships = new ArrayList<>();

        Queue<Column> columnSet = new ArrayDeque<>();
        columnSet.addAll(directReferenced.keySet());
        while (!columnSet.isEmpty()) {
            Column column = columnSet.poll();
            final List<Column> referencedColumns = columnRelationMap.get(column);
            if (referencedColumns.isEmpty()) {
                continue;
            }
            for (Column referencedColumn : referencedColumns) {
                if (!uniqueKey.add(referencedColumn)) {
                    continue;
                }
                columnSet.add(referencedColumn);

                StringBuilder labelBuilder = new StringBuilder(column.columnName());
                if (StringUtils.isNotBlank(column.condition())) {
                    labelBuilder.append("(").append(column.condition()).append(")");
                }
                labelBuilder.append(" → ");
                labelBuilder.append(referencedColumn.columnName());
                if (StringUtils.isNotBlank(referencedColumn.condition())) {
                    labelBuilder.append("(").append(referencedColumn.condition()).append(")");
                }

                String label = labelBuilder.toString();
                relationships.add(new EntityRelationShip(column.tableName(), referencedColumn.tableName(), label));
            }
        }

        return relationships;
    }
}
