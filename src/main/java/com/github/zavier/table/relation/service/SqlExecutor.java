package com.github.zavier.table.relation.service;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class SqlExecutor {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SqlExecutor.class);

    private static final int MAX_SQL_LENGTH = 10;

    public List<Map<String, Object>> execute(DataSource dataSource, String sql, Object... args) {
        String limitSql = wrapLimit2Sql(sql);
        final JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(limitSql, args);
        logger.info("execute sql: {} args: {} resultSize:{}", limitSql, Arrays.toString(args), result.size());
        return result;
    }

    private String wrapLimit2Sql(String sql) {
        return sql + " limit " + MAX_SQL_LENGTH;
    }

    private JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
