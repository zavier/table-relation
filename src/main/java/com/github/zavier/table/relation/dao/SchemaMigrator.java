package com.github.zavier.table.relation.dao;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Objects;

/**
 * 轻量级幂等迁移：{@code CREATE TABLE IF NOT EXISTS} 不会给存量表补列，
 * 启动时按数据库元数据检查并补上新增列。
 */
@Component
public class SchemaMigrator {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrator.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    public void migrate() {
        ensureColumn("database_connection_info", "database_type", "VARCHAR(32) NOT NULL DEFAULT 'MYSQL'");
        ensureColumn("database_connection_info", "schema_name", "VARCHAR(64)");
        ensureColumn("table_relation", "condition", "TEXT");
    }

    private void ensureColumn(String tableName, String columnName, String definition) {
        if (columnExists(tableName, columnName)) {
            return;
        }
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition;
        log.info("Schema migration: {}", sql);
        jdbcTemplate.execute(sql);
    }

    private boolean columnExists(String tableName, String columnName) {
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource(), "dataSource can not be null");
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
                return columns.next();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to check column " + tableName + "." + columnName, e);
        }
    }
}
