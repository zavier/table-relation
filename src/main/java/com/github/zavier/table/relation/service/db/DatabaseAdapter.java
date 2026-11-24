package com.github.zavier.table.relation.service.db;

import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 单一数据库类型的全部差异：连接串、SQL 方言、元数据查询、结果值归一化。
 * <p>
 * 业务代码只依赖本接口，通过 {@link DatabaseAdapterRegistry} 按数据源类型选择实现。
 */
public interface DatabaseAdapter {

    DatabaseType type();

    String buildJdbcUrl(DataSourceConfig config);

    /**
     * 物理 schema：MySQL 等于库名，PostgreSQL 默认 public。
     */
    default String physicalSchema(DataSourceConfig config) {
        return config.getDatabase();
    }

    /**
     * 标识符引用：MySQL 使用反引号，PostgreSQL 使用双引号。
     */
    String quoteIdentifier(String identifier);

    default String tableRef(String physicalSchema, String tableName) {
        if (physicalSchema == null || physicalSchema.isBlank()) {
            return quoteIdentifier(tableName);
        }
        return quoteIdentifier(physicalSchema) + "." + quoteIdentifier(tableName);
    }

    /**
     * 生成 SQL 字面量，用于当前尚不支持参数化的查询链路。
     */
    String literal(Object value);

    /**
     * 生成 INSERT 语句中的值表达式，默认与 {@link #literal} 一致。
     * PostgreSQL 的数组列需要 {@code ARRAY[...]} 形式，单独留一个扩展点。
     */
    default String insertValue(Object value) {
        return literal(value);
    }

    /**
     * 原始列类型归一化为页面与 ER 图使用的简单类型。
     */
    String normalizeColumnType(String rawColumnType);

    List<TableColumnInfo> listTableColumns(DataSourceContext context);

    TableColumnInfo describeTable(DataSourceContext context, String tableName);

    List<ColumnUsage> listForeignKeys(DataSourceContext context);

    /**
     * JDBC 结果行归一化，例如 PostgreSQL 的 jsonb / 数组。
     */
    default Map<String, Object> normalizeRow(Map<String, Object> row) {
        return row;
    }

    default String sanitizeType(String type) {
        if (type == null || type.isBlank()) {
            return "unknown";
        }
        String sanitized = type.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return sanitized.isBlank() ? "unknown" : sanitized;
    }
}
