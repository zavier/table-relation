package com.github.zavier.table.relation.service.converter;

import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.domain.TableInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableInfoConverter {

    public static List<TableColumnInfo> convert2TableColumnInfo(List<Map<String, Object>> dataMapList) {
        if (dataMapList.isEmpty()) {
            return List.of();
        }

        List<TableColumnInfo> result = new ArrayList<>();

        final Map<String, List<Map<String, Object>>> tableColumnsMap = dataMapList.stream()
                .collect(Collectors.groupingBy((dataMap) -> dataMap.get("TABLE_SCHEMA") + (String) dataMap.get("TABLE_NAME")));
        tableColumnsMap.forEach((key, columnInfos) -> {
            final List<ColumnInfo> columnInfoList = columnInfos.stream().map(TableInfoConverter::convert2ColumnInfo).toList();
            final Map<String, Object> column = columnInfos.get(0);
            final TableColumnInfo tableColumnInfo = new TableColumnInfo(
                    (String) column.get("TABLE_SCHEMA"),
                    (String) column.get("TABLE_NAME"),
                    "", // 暂未设置
                    columnInfoList
            );
            result.add(tableColumnInfo);
        });
        return result;
    }

    public static List<TableInfo> convert2TableBaseInfo(List<Map<String, Object>> dataMapList) {
        if (dataMapList.isEmpty()) {
            return List.of();
        }
        return dataMapList.stream().map(dataMap -> {
            return new TableInfo(
                    (String) dataMap.get("TABLE_SCHEMA"),
                    (String) dataMap.get("TABLE_NAME"),
                    (String) dataMap.get("TABLE_COMMENT")
            );
        }).toList();
    }

    public static ColumnInfo convert2ColumnInfo(Map<String, Object> dataMap) {
        return new ColumnInfo(
                (String) dataMap.get("COLUMN_NAME"),
                convertColumnType((String) dataMap.get("COLUMN_TYPE")),
                (String) dataMap.get("COLUMN_COMMENT")
        );
    }

    private static String convertColumnType(String columnType) {
        String compareColumnType = columnType.toLowerCase();
        if (compareColumnType.startsWith("enum")) {
            return "enum";
        }
        if (compareColumnType.startsWith("char") || compareColumnType.startsWith("varchar")) {
            return "string";
        }
        return compareColumnType;
    }
}
