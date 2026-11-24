package com.github.zavier.table.relation.dao;

import com.github.zavier.table.relation.service.domain.ConditionFilter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 轻量级幂等迁移：{@code CREATE TABLE IF NOT EXISTS} 不会给存量表补列，
 * 启动时按数据库元数据检查并补上新增列；同时把旧的原始 SQL 条件解析成结构化条件。
 */
@Component
public class SchemaMigrator {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrator.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    public void migrate() {
        ensureColumn("database_connection_info", "database_type", "VARCHAR(32) NOT NULL DEFAULT 'MYSQL'");
        ensureColumn("database_connection_info", "schema_name", "VARCHAR(64)");

        // 旧版本的条件列，保留用于迁移
        ensureColumn("table_relation", "condition", "TEXT");
        ensureColumn("table_relation", "condition_field", "VARCHAR(128)");
        ensureColumn("table_relation", "condition_operator", "VARCHAR(16)");
        ensureColumn("table_relation", "condition_value", "VARCHAR(255)");

        migrateLegacyConditions();
    }

    private void migrateLegacyConditions() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, condition FROM table_relation "
                        + "WHERE condition IS NOT NULL AND TRIM(condition) <> '' AND condition_field IS NULL");

        int migrated = 0;
        for (Map<String, Object> row : rows) {
            int id = ((Number) row.get("id")).intValue();
            String rawCondition = Objects.toString(row.get("condition"), "");
            ConditionFilter condition = ConditionFilter.parseLegacy(rawCondition);
            if (condition == null) {
                log.warn("Unable to parse legacy relation condition, please reconfigure it. id={} condition={}",
                        id, rawCondition);
                continue;
            }
            jdbcTemplate.update(
                    "UPDATE table_relation SET condition_field = ?, condition_operator = ?, condition_value = ?, condition = '' WHERE id = ?",
                    condition.field(), condition.operator(), condition.value(), id);
            migrated++;
            log.info("Migrated legacy relation condition id={} -> {}", id, condition.display());
        }
        if (!rows.isEmpty()) {
            log.info("Legacy relation condition migration finished, migrated={} skipped={}",
                    migrated, rows.size() - migrated);
        }
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
