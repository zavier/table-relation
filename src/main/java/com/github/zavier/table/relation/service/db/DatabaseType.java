package com.github.zavier.table.relation.service.db;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * 支持的数据库类型。
 */
public enum DatabaseType {

    MYSQL(3306),
    POSTGRESQL(5432);

    private final int defaultPort;

    DatabaseType(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int defaultPort() {
        return defaultPort;
    }

    /**
     * 兼容历史配置与大小写差异，未知值回落到 MYSQL。
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DatabaseType from(String value) {
        if (value == null || value.isBlank()) {
            return MYSQL;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "MYSQL", "MYSQL8", "MARIADB" -> MYSQL;
            case "POSTGRES", "POSTGRESQL", "PG" -> POSTGRESQL;
            default -> {
                try {
                    yield DatabaseType.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    yield MYSQL;
                }
            }
        };
    }
}
