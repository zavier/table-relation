package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.db.PostgresDatabaseAdapter;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.SqlWithParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用内存 SQLite 实际执行生成的 SQL，验证值确实走了参数绑定。
 */
class ParameterizedQueryTest {

    private final PostgresDatabaseAdapter adapter = new PostgresDatabaseAdapter();
    private JdbcTemplate jdbcTemplate;
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // 内存 SQLite 的连接互相隔离，这里复用同一个连接
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        jdbcTemplate.execute("create table users (id integer primary key, name text, enabled integer)");
        jdbcTemplate.update("insert into users values (1, 'alice', 1)");
        jdbcTemplate.update("insert into users values (2, 'bob', 0)");
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void injectionValueIsTreatedAsPlainData() {
        SqlWithParams query = build("name", "=", "x' OR '1'='1").buildSql(adapter, null);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.params().toArray());

        assertTrue(rows.isEmpty());
        assertEquals(2, countUsers());
    }

    @Test
    void normalQueryStillWorks() {
        SqlWithParams query = build("id", "=", 1).buildSql(adapter, null);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.params().toArray());

        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get("name"));
    }

    @Test
    void likeParameterIsBound() {
        SqlWithParams query = build("name", "LIKE", "%li%").buildSql(adapter, null);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.params().toArray());

        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get("name"));
    }

    @Test
    void inQueryBindsEveryValue() {
        SqlWithParams query = build("id", "IN", List.of(1, 2, 3)).buildSql(adapter, null);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.params().toArray());

        assertEquals(2, rows.size());
    }

    @Test
    void emptyInQueryReturnsNoRows() {
        SqlWithParams query = build("id", "IN", List.of()).buildSql(adapter, null);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.params().toArray());

        assertTrue(rows.isEmpty());
    }

    private QueryCondition build(String field, String operator, Object value) {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("ignored");
        queryCondition.setTable("users");
        Condition condition = new Condition();
        condition.setField(field);
        condition.setOperator(operator);
        condition.setValue(value);
        queryCondition.setConditions(List.of(condition));
        return queryCondition;
    }

    private int countUsers() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
        return count == null ? 0 : count;
    }
}
