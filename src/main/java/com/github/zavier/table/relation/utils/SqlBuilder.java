package com.github.zavier.table.relation.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SqlBuilder {

    public static String generateInsertSql(String tableName, List<Map<String, Object>> dataMapList) {
        if (dataMapList.isEmpty()) {
            return "";
        }
        final List<String> columnNameList = new ArrayList<>(dataMapList.get(0).keySet());

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO `").append(tableName).append("` (");
        final String values = columnNameList.stream()
                .map(it -> " `" + it + "`")
                .collect(Collectors.joining(","));
        sqlBuilder.append(values).append(") VALUES ");

        List<String> valueSqlList = new ArrayList<>();
        for (Map<String, Object> dataMap : dataMapList) {
            final String columnValues = columnNameList.stream()
                    .map(it -> {
                        Object value = dataMap.get(it);
                        return convertSqlValue(value);
                    })
                    .collect(Collectors.joining(","));
            valueSqlList.add("(" + columnValues + ")");
        }
        final String collect = valueSqlList.stream().collect(Collectors.joining(","));
        sqlBuilder.append(collect);
        sqlBuilder.append(";");
        return sqlBuilder.toString();
    }

    private static String convertSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            return "'" + value + "'";
        }
        if (value instanceof BigDecimal) {
            return "'" + value + "'";
        }

        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + sdf.format(value) + "'";
        }
        if (value instanceof LocalDate) {
            LocalDate localDate = (LocalDate) value;
            // 格式化成 yyyy-MM-dd HH:mm:ss 格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return "'" + localDate.format(formatter) + "'";
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            // 格式化成 yyyy-MM-dd HH:mm:ss 格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return "'" + localDateTime.format(formatter) + "'";
        }

        return value.toString();
    }
}
