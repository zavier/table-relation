package com.github.zavier.table.relation.service.abilty;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class SqlExecutor {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SqlExecutor.class);

    private static final int MAX_SQL_LENGTH = 10;

    public List<Map<String, Object>> sqlQueryWithLimit(DataSource dataSource, String sql, Object... args) {
        String limitSql = wrapLimit2Sql(sql);
        final JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(limitSql, args);
        return result;
    }

    public List<Map<String, Object>> sqlQueryWithoutLimit(DataSource dataSource, String sql, Object... args) {
        final JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, args);
        return result;
    }

    private String wrapLimit2Sql(String sql) {
        return sql + " limit " + MAX_SQL_LENGTH;
    }

    private JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
