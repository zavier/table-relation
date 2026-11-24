package com.github.zavier.table.relation.service.db;

import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseAdapterTest {

    private final MySqlDatabaseAdapter mysql = new MySqlDatabaseAdapter();
    private final PostgresDatabaseAdapter postgres = new PostgresDatabaseAdapter();

    @Test
    void buildJdbcUrl() {
        assertEquals("jdbc:mysql://127.0.0.1:3306/demo?allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false",
                mysql.buildJdbcUrl(config(DatabaseType.MYSQL, "demo", null)));
        assertEquals("jdbc:postgresql://127.0.0.1:5432/demo?ApplicationName=table-relation&stringtype=unspecified",
                postgres.buildJdbcUrl(config(DatabaseType.POSTGRESQL, "demo", null)));
    }

    @Test
    void physicalSchemaDefaultsToPublicForPostgres() {
        assertEquals("demo", mysql.physicalSchema(config(DatabaseType.MYSQL, "demo", "ignored")));
        assertEquals("public", postgres.physicalSchema(config(DatabaseType.POSTGRESQL, "demo", null)));
        assertEquals("public", postgres.physicalSchema(config(DatabaseType.POSTGRESQL, "demo", " ")));
        assertEquals("sales", postgres.physicalSchema(config(DatabaseType.POSTGRESQL, "demo", "sales")));
    }

    @Test
    void quoteIdentifier() {
        assertEquals("`user`", mysql.quoteIdentifier("user"));
        assertEquals("`a``b`", mysql.quoteIdentifier("a`b"));
        assertEquals("\"user\"", postgres.quoteIdentifier("user"));
        assertEquals("\"a\"\"b\"", postgres.quoteIdentifier("a\"b"));
    }

    @Test
    void tableRef() {
        assertEquals("`demo`.`users`", mysql.tableRef("demo", "users"));
        assertEquals("\"sales\".\"users\"", postgres.tableRef("sales", "users"));
        assertEquals("\"users\"", postgres.tableRef(null, "users"));
    }

    @Test
    void mysqlLiterals() {
        assertEquals("NULL", mysql.literal(null));
        assertEquals("1", mysql.literal(1));
        assertEquals("1.50", mysql.literal(new BigDecimal("1.50")));
        assertEquals("1", mysql.literal(true));
        assertEquals("0", mysql.literal(false));
        assertEquals("'O''Reilly'", mysql.literal("O'Reilly"));
        assertEquals("'a\\\\b'", mysql.literal("a\\b"));
        assertEquals("'2026-01-02'", mysql.literal(LocalDate.of(2026, 1, 2)));
        assertEquals("('a','b')", mysql.literal(List.of("a", "b")));
        assertEquals("(1,2)", mysql.literal(List.of(1, 2)));
        assertEquals("X'0a0b'", mysql.literal(new byte[]{10, 11}));
    }

    @Test
    void postgresLiterals() {
        assertEquals("NULL", postgres.literal(null));
        assertEquals("1", postgres.literal(1));
        assertEquals("TRUE", postgres.literal(true));
        assertEquals("FALSE", postgres.literal(false));
        assertEquals("'O''Reilly'", postgres.literal("O'Reilly"));
        assertEquals("'a\\b'", postgres.literal("a\\b"));
        assertEquals("('a','b')", postgres.literal(List.of("a", "b")));
        assertEquals("decode('0a0b', 'hex')", postgres.literal(new byte[]{10, 11}));
    }

    @Test
    void mysqlColumnTypes() {
        assertEquals("string", mysql.normalizeColumnType("varchar(64)"));
        assertEquals("string", mysql.normalizeColumnType("longtext"));
        assertEquals("int", mysql.normalizeColumnType("tinyint(1)"));
        assertEquals("int", mysql.normalizeColumnType("bigint"));
        assertEquals("float", mysql.normalizeColumnType("decimal(10,2)"));
        assertEquals("enum", mysql.normalizeColumnType("enum('A','B')"));
        assertEquals("datetime", mysql.normalizeColumnType("datetime"));
        assertEquals("timestamp", mysql.normalizeColumnType("timestamp"));
    }

    @Test
    void postgresColumnTypes() {
        assertEquals("string", postgres.normalizeColumnType("character varying(64)"));
        assertEquals("string", postgres.normalizeColumnType("uuid"));
        assertEquals("int", postgres.normalizeColumnType("bigint"));
        assertEquals("int", postgres.normalizeColumnType("integer"));
        assertEquals("float", postgres.normalizeColumnType("numeric(12,2)"));
        assertEquals("float", postgres.normalizeColumnType("double precision"));
        assertEquals("boolean", postgres.normalizeColumnType("boolean"));
        assertEquals("datetime", postgres.normalizeColumnType("timestamp with time zone"));
        assertEquals("datetime", postgres.normalizeColumnType("timestamp(3) without time zone"));
        assertEquals("date", postgres.normalizeColumnType("date"));
        assertEquals("time", postgres.normalizeColumnType("time without time zone"));
        assertEquals("json", postgres.normalizeColumnType("jsonb"));
        assertEquals("bytes", postgres.normalizeColumnType("bytea"));
        assertEquals("array", postgres.normalizeColumnType("text[]"));
        assertEquals("array", postgres.normalizeColumnType("integer[]"));
        assertEquals("order_status", postgres.normalizeColumnType("order_status"));
        assertEquals("geometry_point_4326_", postgres.normalizeColumnType("geometry(Point,4326)"));
    }

    private DataSourceConfig config(DatabaseType type, String database, String schemaName) {
        DataSourceConfig config = new DataSourceConfig();
        config.setHost("127.0.0.1");
        config.setPort(type.defaultPort());
        config.setDatabase(database);
        config.setUsername("user");
        config.setPassword("password");
        config.setDatabaseType(type);
        config.setSchemaName(schemaName);
        return config;
    }
}
