package com.github.zavier.table.relation.service.db;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用字面量拼装逻辑，数据库差异通过 {@link #escapeString} / {@link #booleanLiteral} /
 * {@link #byteArrayLiteral} 三个钩子实现。
 */
public abstract class BaseDatabaseAdapter implements DatabaseAdapter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String literal(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean bool) {
            return booleanLiteral(bool);
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return quoteString(value.toString());
        }
        if (value instanceof LocalDate localDate) {
            return quoteString(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (value instanceof LocalDateTime localDateTime) {
            return quoteString(localDateTime.format(DATE_TIME_FORMATTER));
        }
        if (value instanceof LocalTime localTime) {
            return quoteString(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return quoteString(timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER));
        }
        if (value instanceof java.sql.Date date) {
            return quoteString(date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (value instanceof java.sql.Time time) {
            return quoteString(time.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return quoteString(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (value instanceof byte[] bytes) {
            return byteArrayLiteral(bytes);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::literal)
                    .collect(Collectors.joining(",", "(", ")"));
        }
        if (value.getClass().isArray()) {
            return reflectArrayLiteral(value);
        }
        return quoteString(value.toString());
    }

    protected abstract String escapeString(String value);

    protected abstract String booleanLiteral(boolean value);

    protected abstract String byteArrayLiteral(byte[] value);

    private String quoteString(String value) {
        return "'" + escapeString(value) + "'";
    }

    private String reflectArrayLiteral(Object array) {
        int length = Array.getLength(array);
        List<String> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(literal(Array.get(array, i)));
        }
        return values.stream().collect(Collectors.joining(",", "(", ")"));
    }
}
