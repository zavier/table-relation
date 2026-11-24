package com.github.zavier.table.relation.utils;

import com.github.zavier.table.relation.service.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlBuilder {

    public static String generateInsertSql(DatabaseAdapter adapter,
                                           String physicalSchema,
                                           String tableName,
                                           List<Map<String, Object>> dataMapList) {
        if (dataMapList.isEmpty()) {
            return "";
        }
        final List<String> columnNameList = new ArrayList<>(dataMapList.get(0).keySet());

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ")
                .append(adapter.tableRef(physicalSchema, tableName))
                .append(" (");
        final String columns = columnNameList.stream()
                .map(adapter::quoteIdentifier)
                .collect(Collectors.joining(", "));
        sqlBuilder.append(columns).append(") VALUES ");

        List<String> valueSqlList = new ArrayList<>();
        for (Map<String, Object> dataMap : dataMapList) {
            final String columnValues = columnNameList.stream()
                    .map(column -> adapter.insertValue(dataMap.get(column)))
                    .collect(Collectors.joining(", "));
            valueSqlList.add("(" + columnValues + ")");
        }
        sqlBuilder.append(String.join(", ", valueSqlList));
        sqlBuilder.append(";");
        return sqlBuilder.toString();
    }
}
