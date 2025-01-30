package com.github.zavier.table.relation.service;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SqlExecutor {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SqlExecutor.class);

    private static final int MAX_SQL_LENGTH = 10;

    public List<String> getSchemaTables(String schema, DataSource dataSource) {
        // 从dataSource中获取所有的表名称
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            final List<String> tableNames = jdbcTemplate.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = ?", String.class, schema);
            return tableNames;
        } catch (Exception e) {
            log.error("Failed to get schema tables: {}", schema, e);
        }
        return Collections.emptyList();
    }

    public List<String> getTableColumns(String schema, String tableName, DataSource dataSource) {
        try {
            final JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
            final List<String> columnNames = jdbcTemplate.queryForList("SELECT column_name FROM information_schema.columns WHERE table_schema = ? AND table_name = ?", String.class, schema, tableName);
            return columnNames;
        } catch (Exception e) {
            log.error("Failed to get table columns: {} {}", schema, tableName, e);
        }
        return Collections.emptyList();
    }

    public List<Map<String, Object>> sqlQuery(DataSource dataSource, String sql, Object... args) {
        String limitSql = wrapLimit2Sql(sql);
        final JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(limitSql, args);
        log.info("execute sql: {} args: {} resultSize:{}", limitSql, Arrays.toString(args), result.size());
        return result;
    }

    private String wrapLimit2Sql(String sql) {
        return sql + " limit " + MAX_SQL_LENGTH;
    }

    private JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
