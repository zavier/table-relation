package com.github.zavier.table.relation.service.db;

import com.github.zavier.table.relation.service.SqlExecutor;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PostgreSQL 适配器。
 * <p>
 * 元数据从 {@code pg_class} / {@code pg_attribute} 读取，注释通过
 * {@code obj_description} / {@code col_description} 获取，外键从 {@code pg_constraint} 展开。
 */
@Component
public class PostgresDatabaseAdapter extends BaseDatabaseAdapter {

    private static final Logger log = LoggerFactory.getLogger(PostgresDatabaseAdapter.class);

    private static final String DEFAULT_SCHEMA = "public";

    private static final String TABLE_COLUMNS_SQL = """
            SELECT n.nspname                                    AS table_schema,
                   cl.relname                                   AS table_name,
                   obj_description(cl.oid, 'pg_class')           AS table_comment,
                   a.attname                                    AS column_name,
                   format_type(a.atttypid, a.atttypmod)          AS column_type,
                   col_description(cl.oid, a.attnum)             AS column_comment
            FROM pg_class cl
            JOIN pg_namespace n ON n.oid = cl.relnamespace
            JOIN pg_attribute a ON a.attrelid = cl.oid AND a.attnum > 0 AND NOT a.attisdropped
            WHERE n.nspname = ? AND cl.relkind IN ('r', 'p', 'v', 'm', 'f')
            """;

    private static final String TABLE_COLUMNS_BY_TABLE_SQL = TABLE_COLUMNS_SQL + " AND cl.relname = ?\n"
            + "ORDER BY cl.relname, a.attnum";

    private static final String TABLE_COLUMNS_ALL_SQL = TABLE_COLUMNS_SQL + " ORDER BY cl.relname, a.attnum";

    private static final String FOREIGN_KEY_SQL = """
            SELECT n.nspname   AS table_schema,
                   cl.relname  AS table_name,
                   a.attname   AS column_name,
                   rn.nspname  AS referenced_table_schema,
                   rcl.relname AS referenced_table_name,
                   ra.attname  AS referenced_column_name
            FROM pg_constraint con
            JOIN pg_class cl      ON cl.oid = con.conrelid
            JOIN pg_namespace n   ON n.oid = cl.relnamespace
            JOIN pg_class rcl     ON rcl.oid = con.confrelid
            JOIN pg_namespace rn  ON rn.oid = rcl.relnamespace
            CROSS JOIN LATERAL unnest(con.conkey, con.confkey) AS k(attnum, refattnum)
            JOIN pg_attribute a   ON a.attrelid = con.conrelid  AND a.attnum = k.attnum
            JOIN pg_attribute ra  ON ra.attrelid = con.confrelid AND ra.attnum = k.refattnum
            WHERE con.contype = 'f' AND n.nspname = ?
            ORDER BY cl.relname, a.attnum
            """;

    @Resource
    private SqlExecutor sqlExecutor;

    @Override
    public DatabaseType type() {
        return DatabaseType.POSTGRESQL;
    }

    @Override
    public String buildJdbcUrl(DataSourceConfig dataSourceConfig) {
        // stringtype=unspecified：条件值统一以字符串绑定，由 PostgreSQL 按列类型推断，
        // 避免 bigint/uuid/timestamp 等列出现 "operator does not exist" 错误
        return String.format(
                "jdbc:postgresql://%s:%d/%s?ApplicationName=table-relation&stringtype=unspecified",
                dataSourceConfig.getHost(), dataSourceConfig.getPort(), dataSourceConfig.getDatabase());
    }

    @Override
    public String physicalSchema(DataSourceConfig config) {
        return StringUtils.defaultIfBlank(config.getSchemaName(), DEFAULT_SCHEMA);
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    protected String escapeString(String value) {
        // 默认 standard_conforming_strings=on，只需处理单引号
        return value.replace("'", "''");
    }

    @Override
    protected String booleanLiteral(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    @Override
    public String insertValue(Object value) {
        if (value instanceof byte[]) {
            return literal(value);
        }
        if (value instanceof Collection<?> collection) {
            return "ARRAY[" + collection.stream()
                    .map(this::insertValue)
                    .collect(Collectors.joining(", ")) + "]";
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<String> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(insertValue(Array.get(value, i)));
            }
            return "ARRAY[" + String.join(", ", values) + "]";
        }
        return literal(value);
    }

    @Override
    protected String byteArrayLiteral(byte[] value) {
        return "decode('" + java.util.HexFormat.of().formatHex(value) + "', 'hex')";
    }

    @Override
    public String normalizeColumnType(String rawColumnType) {
        if (rawColumnType == null || rawColumnType.isBlank()) {
            return "unknown";
        }
        String columnType = rawColumnType.toLowerCase(Locale.ROOT).trim();
        if (columnType.endsWith("[]")) {
            return "array";
        }
        if (columnType.startsWith("timestamp")) {
            return "datetime";
        }
        if (columnType.equals("date")) {
            return "date";
        }
        if (columnType.startsWith("time")) {
            return "time";
        }
        if (columnType.startsWith("character")
                || columnType.startsWith("varchar")
                || columnType.startsWith("char")
                || columnType.startsWith("text")
                || columnType.equals("citext")
                || columnType.equals("uuid")
                || columnType.equals("name")
                || columnType.equals("bpchar")) {
            return "string";
        }
        if (columnType.startsWith("int")
                || columnType.equals("smallint")
                || columnType.equals("integer")
                || columnType.equals("bigint")
                || columnType.endsWith("serial")) {
            return "int";
        }
        if (columnType.startsWith("numeric")
                || columnType.startsWith("decimal")
                || columnType.startsWith("float")
                || columnType.equals("real")
                || columnType.equals("double precision")
                || columnType.equals("money")) {
            return "float";
        }
        if (columnType.equals("boolean") || columnType.equals("bool")) {
            return "boolean";
        }
        if (columnType.equals("json") || columnType.equals("jsonb")) {
            return "json";
        }
        if (columnType.equals("bytea")) {
            return "bytes";
        }
        return sanitizeType(columnType);
    }

    @Override
    public List<TableColumnInfo> listTableColumns(DataSourceContext context) {
        List<Map<String, Object>> rows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), TABLE_COLUMNS_ALL_SQL, context.physicalSchema());
        return toTableColumnInfos(context.key(), rows);
    }

    @Override
    public TableColumnInfo describeTable(DataSourceContext context, String tableName) {
        List<Map<String, Object>> rows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), TABLE_COLUMNS_BY_TABLE_SQL, context.physicalSchema(), tableName);
        List<TableColumnInfo> tableColumnInfos = toTableColumnInfos(context.key(), rows);

        Validate.notEmpty(tableColumnInfos, "tableColumnInfos is empty");
        Validate.isTrue(tableColumnInfos.size() == 1, "tableColumnInfos size is not 1");
        return tableColumnInfos.get(0);
    }

    @Override
    public List<ColumnUsage> listForeignKeys(DataSourceContext context) {
        List<Map<String, Object>> rows = sqlExecutor.sqlQueryWithoutLimit(
                context.dataSource(), FOREIGN_KEY_SQL, context.physicalSchema());

        List<ColumnUsage> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String referencedSchema = Objects.toString(row.get("referenced_table_schema"), "");
            if (!Objects.equals(referencedSchema, context.physicalSchema())) {
                log.warn("Skip cross-schema foreign key: {}.{} -> {}.{}",
                        row.get("table_name"), row.get("column_name"),
                        referencedSchema, row.get("referenced_table_name"));
                continue;
            }
            result.add(toColumnUsage(context.key(), row));
        }
        return result;
    }

    /**
     * PostgreSQL 的 jsonb / 数组 JDBC 类型无法被 Jackson 合理序列化，统一展开为字符串或列表。
     */
    @Override
    public Map<String, Object> normalizeRow(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return row;
        }
        Map<String, Object> normalized = new LinkedHashMap<>(row.size());
        row.forEach((column, value) -> normalized.put(column, normalizeValue(value)));
        return normalized;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        if (value instanceof java.sql.Array sqlArray) {
            try {
                return toList(sqlArray.getArray());
            } catch (SQLException e) {
                log.warn("Failed to read SQL array value", e);
                return null;
            }
        }
        return value;
    }

    private Object toList(Object array) {
        if (array == null) {
            return null;
        }
        if (!array.getClass().isArray()) {
            return array;
        }
        int length = Array.getLength(array);
        List<Object> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(normalizeValue(Array.get(array, i)));
        }
        return values;
    }

    private List<TableColumnInfo> toTableColumnInfos(String schema, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<String, List<Map<String, Object>>> tableRowsMap = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> Objects.toString(row.get("table_name"), ""),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<TableColumnInfo> result = new ArrayList<>(tableRowsMap.size());
        tableRowsMap.forEach((tableName, tableRows) -> {
            List<ColumnInfo> columns = tableRows.stream()
                    .map(row -> new ColumnInfo(
                            Objects.toString(row.get("column_name"), ""),
                            normalizeColumnType(Objects.toString(row.get("column_type"), "")),
                            Objects.toString(row.get("column_comment"), "")))
                    .toList();
            String tableComment = Objects.toString(tableRows.get(0).get("table_comment"), "");
            result.add(new TableColumnInfo(schema, tableName, tableComment, columns));
        });
        return result;
    }

    private ColumnUsage toColumnUsage(String schema, Map<String, Object> row) {
        ColumnUsage columnUsage = new ColumnUsage();
        columnUsage.setTableSchema(schema);
        columnUsage.setTableName(Objects.toString(row.get("table_name"), ""));
        columnUsage.setColumnName(Objects.toString(row.get("column_name"), ""));
        columnUsage.setCondition("");
        columnUsage.setReferencedTableSchema(schema);
        columnUsage.setReferencedTableName(Objects.toString(row.get("referenced_table_name"), ""));
        columnUsage.setReferencedColumnName(Objects.toString(row.get("referenced_column_name"), ""));
        columnUsage.setRelationType(RelationType.ONE_TO_MANY.getValue());
        return columnUsage;
    }
}
