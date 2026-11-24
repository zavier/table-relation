package com.github.zavier.table.relation.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SchemaMigratorTest {

    private Connection connection;
    private JdbcTemplate jdbcTemplate;
    private SchemaMigrator schemaMigrator;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        jdbcTemplate.execute("""
                create table database_connection_info (
                    id integer primary key autoincrement,
                    database text, host text, username text, password text, port integer
                )
                """);
        jdbcTemplate.execute("""
                create table table_relation (
                    id integer primary key autoincrement,
                    table_schema text, table_name text, column_name text, condition text,
                    referenced_table_schema text, referenced_table_name text,
                    referenced_column_name text, relation_type integer
                )
                """);
        schemaMigrator = new SchemaMigrator();
        ReflectionTestUtils.setField(schemaMigrator, "jdbcTemplate", jdbcTemplate);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void addsMissingColumnsAndMigratesLegacyConditions() {
        jdbcTemplate.update("insert into database_connection_info(database, host, username, password, port) "
                + "values ('demo', '127.0.0.1', 'root', 'root', 3306)");
        insertRelation("type=1");
        insertRelation("status = 'NEW'");
        insertRelation("custom raw sql here");

        schemaMigrator.migrate();

        Map<String, Object> connectionInfo = jdbcTemplate.queryForMap("select * from database_connection_info");
        assertEquals("MYSQL", connectionInfo.get("database_type"));

        Map<String, Object> parsed = jdbcTemplate.queryForMap("select * from table_relation where id = 1");
        assertEquals("type", parsed.get("condition_field"));
        assertEquals("=", parsed.get("condition_operator"));
        assertEquals("1", parsed.get("condition_value"));
        assertEquals("", parsed.get("condition"));

        Map<String, Object> quoted = jdbcTemplate.queryForMap("select * from table_relation where id = 2");
        assertEquals("status", quoted.get("condition_field"));
        assertEquals("NEW", quoted.get("condition_value"));

        Map<String, Object> unparsed = jdbcTemplate.queryForMap("select * from table_relation where id = 3");
        assertNull(unparsed.get("condition_field"));
        assertEquals("custom raw sql here", unparsed.get("condition"));
    }

    @Test
    void migrationIsIdempotent() {
        insertRelation("type=1");

        schemaMigrator.migrate();
        schemaMigrator.migrate();

        Map<String, Object> parsed = jdbcTemplate.queryForMap("select * from table_relation where id = 1");
        assertEquals("type", parsed.get("condition_field"));
        assertEquals("1", parsed.get("condition_value"));
    }

    private void insertRelation(String condition) {
        jdbcTemplate.update("""
                insert into table_relation(table_schema, table_name, column_name, condition,
                    referenced_table_schema, referenced_table_name, referenced_column_name, relation_type)
                values ('demo', 't', 'c', ?, 'demo', 'r', 'id', 2)
                """, condition);
    }
}
