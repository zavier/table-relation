package com.github.zavier.table.relation.utils;

import com.github.zavier.table.relation.service.db.MySqlDatabaseAdapter;
import com.github.zavier.table.relation.service.db.PostgresDatabaseAdapter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlBuilderTest {

    private final MySqlDatabaseAdapter mysql = new MySqlDatabaseAdapter();
    private final PostgresDatabaseAdapter postgres = new PostgresDatabaseAdapter();

    @Test
    void generateMySqlInsertSql() {
        String sql = SqlBuilder.generateInsertSql(mysql, "demo", "users", rows());
        assertEquals("INSERT INTO `demo`.`users` (`id`, `name`, `enabled`) "
                + "VALUES (1, 'O''Reilly', 1), (2, 'a\\\\b', 0);", sql);
    }

    @Test
    void generatePostgresInsertSql() {
        String sql = SqlBuilder.generateInsertSql(postgres, "sales", "users", rows());
        assertEquals("INSERT INTO \"sales\".\"users\" (\"id\", \"name\", \"enabled\") "
                + "VALUES (1, 'O''Reilly', TRUE), (2, 'a\\b', FALSE);", sql);
    }

    @Test
    void generatePostgresInsertSqlForArrayColumn() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("tags", List.of("vip", "new"));

        String sql = SqlBuilder.generateInsertSql(postgres, "public", "customers", List.of(row));
        assertEquals("INSERT INTO \"public\".\"customers\" (\"id\", \"tags\") "
                + "VALUES (1, ARRAY['vip', 'new']);", sql);
    }

    @Test
    void emptyRowsProducesEmptySql() {
        assertTrue(SqlBuilder.generateInsertSql(mysql, "demo", "users", List.of()).isEmpty());
    }

    private List<Map<String, Object>> rows() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("id", 1);
        first.put("name", "O'Reilly");
        first.put("enabled", true);

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("id", 2);
        second.put("name", "a\\b");
        second.put("enabled", false);

        return List.of(first, second);
    }
}
