package com.github.zavier.table.relation.service.converter;

import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableInfoConverter {

    public static List<TableColumnInfo> convert2TableColumnInfo(List<Map<String, Object>> columnDataList,
                                                                List<Map<String, Object>> tableDataList) {
        if (columnDataList.isEmpty() || tableDataList.isEmpty()) {
            return List.of();
        }
        List<TableColumnInfo> result = new ArrayList<>();

        final Map<String, List<Map<String, Object>>> tableColumnsMap = columnDataList.stream()
                .collect(Collectors.groupingBy((dataMap) -> dataMap.get("TABLE_SCHEMA") + (String) dataMap.get("TABLE_NAME")));

        final Map<String, String> tableKeyCommentMap = tableDataList.stream()
                .collect(Collectors.toMap((dataMap) -> dataMap.get("TABLE_SCHEMA") + (String) dataMap.get("TABLE_NAME"), (dataMap) -> (String) (dataMap.get("TABLE_COMMENT")),
                        (v1, v2) -> v1));

        tableColumnsMap.forEach((key, columnInfos) -> {
            final List<ColumnInfo> columnInfoList = columnInfos.stream().map(TableInfoConverter::convert2ColumnInfo).toList();
            final Map<String, Object> column = columnInfos.get(0);
            final TableColumnInfo tableColumnInfo = new TableColumnInfo(
                    (String) column.get("TABLE_SCHEMA"),
                    (String) column.get("TABLE_NAME"),
                    tableKeyCommentMap.getOrDefault(key, ""),
                    columnInfoList
            );
            result.add(tableColumnInfo);
        });
        return result;
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
        if (compareColumnType.startsWith("char")
                || compareColumnType.startsWith("varchar")
                || compareColumnType.startsWith("text")
                || compareColumnType.startsWith("longtext")
                || compareColumnType.startsWith("mediumtext")) {
            return "string";
        }
        if (compareColumnType.startsWith("int")
                || compareColumnType.startsWith("tinyint")
                || compareColumnType.startsWith("smallint")
                || compareColumnType.startsWith("mediumint")
                || compareColumnType.startsWith("bigint")) {
            return "int";
        }
        if (compareColumnType.startsWith("float")
                || compareColumnType.startsWith("double")
                || compareColumnType.startsWith("decimal")) {
            return "float";
        }
        return compareColumnType;
    }
}
