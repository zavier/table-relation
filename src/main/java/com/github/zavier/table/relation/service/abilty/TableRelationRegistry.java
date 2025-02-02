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
        final List<Column> tableColumns = getTableAllColumns(schema, tableName);
        if (tableColumns.isEmpty()) {
            return Collections.emptyList();
        }

        // table1, table2 , label(col1 -> col2)
        List<EntityRelationShip> relationships = new ArrayList<>();

        Set<String> uniqueKey = new HashSet<>();
        Queue<Column> columnSet = new ArrayDeque<>(tableColumns);
        while (!columnSet.isEmpty()) {
            Column column = columnSet.poll();

            final List<Column> referencedColumns = columnRelationMap.get(column);
            if (referencedColumns.isEmpty()) {
                continue;
            }
            for (Column referencedColumn : referencedColumns) {
                if (!uniqueKey.add(generateRelationUniqueKey(column, referencedColumn))) {
                    continue;
                }
                // 添加关联表的所有列
                final List<Column> relaTableAllColumns = getTableAllColumns(referencedColumn.schema(), referencedColumn.tableName());
                columnSet.addAll(relaTableAllColumns);

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

    public String generateRelationUniqueKey(Column column1, Column column2) {
        List<String> uniqueKey = new ArrayList<>();
        uniqueKey.add("s:" + column1.schema());
        uniqueKey.add("t:" + column1.tableName());
        uniqueKey.add("c:" + column1.columnName());
        uniqueKey.add("s:" + column2.schema());
        uniqueKey.add("t:" + column2.tableName());
        uniqueKey.add("c:" + column2.columnName());

        Collections.sort(uniqueKey);

        return String.join(",", uniqueKey);
    }

    private @NotNull List<Column> getTableAllColumns(String schema, String tableName) {
        return schemaTableColumnMap.getOrDefault(schema, Collections.emptyMap())
                .getOrDefault(tableName, Collections.emptyList());
    }
}
