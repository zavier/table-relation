package com.github.zavier.table.relation.service.db;

import com.github.zavier.table.relation.service.SqlExecutor;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MySQL 适配器：保持改造前的 SQL 与类型映射，仅把标识符与字面量方言化。
 */
@Component
public class MySqlDatabaseAdapter extends BaseDatabaseAdapter {

    private static final String COLUMN_SQL = """
            SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = ?
            """;
    private static final String COLUMN_SQL_BY_TABLE = COLUMN_SQL + " AND TABLE_NAME = ?";
    private static final String TABLE_SQL = """
            SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = ?
            """;
    private static final String TABLE_SQL_BY_TABLE = TABLE_SQL + " AND TABLE_NAME = ?";
    private static final String RELATION_SQL = """
            SELECT `TABLE_SCHEMA`, `TABLE_NAME`, `COLUMN_NAME`, `REFERENCED_TABLE_SCHEMA`,
                   `REFERENCED_TABLE_NAME`, `REFERENCED_COLUMN_NAME`
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = ? AND `REFERENCED_COLUMN_NAME` IS NOT NULL
            """;

    @Resource
    private SqlExecutor sqlExecutor;

    @Override
    public DatabaseType type() {
        return DatabaseType.MYSQL;
    }

    @Override
    public String buildJdbcUrl(DataSourceConfig dataSourceConfig) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false",
                dataSourceConfig.getHost(), dataSourceConfig.getPort(), dataSourceConfig.getDatabase());
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    @Override
    protected String escapeString(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    @Override
    protected String booleanLiteral(boolean value) {
        return value ? "1" : "0";
    }

    @Override
    protected String byteArrayLiteral(byte[] value) {
        return "X'" + java.util.HexFormat.of().formatHex(value) + "'";
    }

    @Override
    public String normalizeColumnType(String rawColumnType) {
        if (rawColumnType == null) {
            return "unknown";
        }
        String columnType = rawColumnType.toLowerCase(Locale.ROOT);
        if (columnType.startsWith("enum")) {
            return "enum";
        }
        if (columnType.startsWith("char")
                || columnType.startsWith("varchar")
                || columnType.startsWith("text")
                || columnType.startsWith("longtext")
                || columnType.startsWith("mediumtext")
                || columnType.startsWith("set")) {
            return "string";
        }
        if (columnType.startsWith("int")
                || columnType.startsWith("tinyint")
                || columnType.startsWith("smallint")
                || columnType.startsWith("mediumint")
                || columnType.startsWith("bigint")) {
            return "int";
        }
        if (columnType.startsWith("float")
                || columnType.startsWith("double")
                || columnType.startsWith("decimal")) {
            return "float";
        }
        return sanitizeType(columnType);
    }

    @Override
    public List<TableColumnInfo> listTableColumns(DataSourceContext context) {
        List<Map<String, Object>> columnRows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), COLUMN_SQL, context.physicalSchema());
        List<Map<String, Object>> tableRows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), TABLE_SQL, context.physicalSchema());
        return toTableColumnInfos(context.key(), columnRows, tableRows);
    }

    @Override
    public TableColumnInfo describeTable(DataSourceContext context, String tableName) {
        List<Map<String, Object>> columnRows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), COLUMN_SQL_BY_TABLE, context.physicalSchema(), tableName);
        List<Map<String, Object>> tableRows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), TABLE_SQL_BY_TABLE, context.physicalSchema(), tableName);
        List<TableColumnInfo> tableColumnInfos = toTableColumnInfos(context.key(), columnRows, tableRows);

        Validate.notEmpty(tableColumnInfos, "tableColumnInfos is empty");
        Validate.isTrue(tableColumnInfos.size() == 1, "tableColumnInfos size is not 1");
        return tableColumnInfos.get(0);
    }

    @Override
    public List<ColumnUsage> listForeignKeys(DataSourceContext context) {
        List<Map<String, Object>> rows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), RELATION_SQL, context.physicalSchema());
        return rows.stream().map(this::toColumnUsage).toList();
    }

    private List<TableColumnInfo> toTableColumnInfos(String schema,
                                                     List<Map<String, Object>> columnRows,
                                                     List<Map<String, Object>> tableRows) {
        if (columnRows.isEmpty() || tableRows.isEmpty()) {
            return List.of();
        }
        Map<String, String> tableCommentMap = tableRows.stream()
                .collect(Collectors.toMap(
                        row -> Objects.toString(row.get("TABLE_NAME"), ""),
                        row -> Objects.toString(row.get("TABLE_COMMENT"), ""),
                        (left, right) -> left));

        Map<String, List<Map<String, Object>>> tableColumnsMap = columnRows.stream()
                .collect(Collectors.groupingBy(
                        row -> Objects.toString(row.get("TABLE_NAME"), ""),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<TableColumnInfo> result = new ArrayList<>(tableColumnsMap.size());
        tableColumnsMap.forEach((tableName, rows) -> {
            List<ColumnInfo> columns = rows.stream().map(this::toColumnInfo).toList();
            result.add(new TableColumnInfo(schema, tableName, tableCommentMap.getOrDefault(tableName, ""), columns));
        });
        return result;
    }

    private ColumnInfo toColumnInfo(Map<String, Object> row) {
        return new ColumnInfo(
                Objects.toString(row.get("COLUMN_NAME"), ""),
                normalizeColumnType(Objects.toString(row.get("COLUMN_TYPE"), "")),
                Objects.toString(row.get("COLUMN_COMMENT"), ""));
    }

    private ColumnUsage toColumnUsage(Map<String, Object> row) {
        ColumnUsage columnUsage = new ColumnUsage();
        columnUsage.setTableSchema(Objects.toString(row.get("TABLE_SCHEMA"), ""));
        columnUsage.setTableName(Objects.toString(row.get("TABLE_NAME"), ""));
        columnUsage.setColumnName(Objects.toString(row.get("COLUMN_NAME"), ""));
        columnUsage.setCondition("");
        columnUsage.setReferencedTableSchema(Objects.toString(row.get("REFERENCED_TABLE_SCHEMA"), ""));
        columnUsage.setReferencedTableName(Objects.toString(row.get("REFERENCED_TABLE_NAME"), ""));
        columnUsage.setReferencedColumnName(Objects.toString(row.get("REFERENCED_COLUMN_NAME"), ""));
        columnUsage.setRelationType(RelationType.ONE_TO_MANY.getValue());
        return columnUsage;
    }
}
