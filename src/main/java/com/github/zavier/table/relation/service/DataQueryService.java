package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.abilty.DataSourceRegistry;
import com.github.zavier.table.relation.service.abilty.MySqlTableMetaInfoQuery;
import com.github.zavier.table.relation.service.abilty.SqlExecutor;
import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.Result;
import com.github.zavier.table.relation.service.dto.TableData;
import com.github.zavier.table.relation.service.query.DataQuery;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;

@Service
public class DataQueryService {

    @Resource
    private DataQuery dataQuery;
    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private SqlExecutor sqlExecutor;
    @Resource
    private MySqlTableMetaInfoQuery mySqlTableMetaInfoQuery;

    public Result<List<String>> getAllSchema() {
        final List<String> allSchema = dataSourceRegistry.getAllSchema();
        Collections.sort(allSchema);
        return Result.success(allSchema);
    }

    public Result<List<String>> getSchemaTables(String schema) {
        final Result<List<TableColumnInfo>> tableColumnInfoResult = getTableColumnInfos(schema);
        if (!tableColumnInfoResult.isSuccess()) {
            return Result.fail(tableColumnInfoResult.getMessage());
        }

        var tableColumnMetaInfo = tableColumnInfoResult.getData();
        final List<String> tableNameList = tableColumnMetaInfo.stream().map(TableColumnInfo::tableName)
                .sorted()
                .toList();
        return Result.success(tableNameList);
    }

    public Result<List<String>> getTableColumns(String schema, String tableName) {
        final TableColumnInfo tableColumnMetaInfo = getTableColumnInfo(schema, tableName);

        // 列就不进行排序了，保留原始顺序，便于页面查看
        final List<String> columnNames = tableColumnMetaInfo.columns()
                .stream()
                .map(ColumnInfo::columnName)
                .distinct()
                .toList();

        return Result.success(columnNames);
    }

    public Result<List<TableColumnInfo>> getTableColumnInfos(String schema) {
        Validate.notBlank(schema, "schema can not be blank");

        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            return Result.success(List.of());
        }
        final DataSource dataSource = sourceOptional.get();

        return Result.success(mySqlTableMetaInfoQuery.getTableColumnMetaInfo(schema, dataSource));
    }

    public Result<List<Map<String, Object>>> executeSql(String schema, String sql) {
        Validate.notBlank(schema, "schema can not be blank");
        Validate.notBlank(sql, "sql can not be blank");
        final List<Map<String, Object>> maps = dataQuery.queryBySql(schema, sql);
        return Result.success(maps);
    }


    public Result<TableData> queryTableData(QueryCondition queryCondition) {
        checkParam(queryCondition);

        // schema -> table -> list<col, value>
        final Map<String, Map<String, List<Map<String, Object>>>> schemaTableDataMap = dataQuery.query(queryCondition);

        // schema -> table -> col -> comment
        final Map<String, Map<String, Map<String, String>>> comments = getSchemaTableCommentMap(schemaTableDataMap);

        final Map<String, List<Map<String, Object>>> dataMap = mergerSchemaData(schemaTableDataMap);
        final Map<String, Map<String, String>> commentMap = mergerSchemaComment(comments);

        // 原始表名的数据再保留一份，用作前端主数据展示
        addSearchTableKeyIfNotExist(queryCondition, dataMap, commentMap);

        return Result.success(new TableData(dataMap, commentMap));
    }

    private static void addSearchTableKeyIfNotExist(QueryCondition queryCondition, Map<String, List<Map<String, Object>>> dataMap, Map<String, Map<String, String>> commentMap) {
        if (!dataMap.containsKey(queryCondition.getTable())) {
            final List<Map<String, Object>> maps = dataMap.get(queryCondition.getSchema() + "." + queryCondition.getTable());
            if (maps != null) {
                dataMap.put(queryCondition.getTable(), maps);
            }
        }
        if (!commentMap.containsKey(queryCondition.getTable())) {
            final Map<String, String> columnCommentMap = commentMap.get(queryCondition.getSchema() + "." + queryCondition.getTable());
            if (columnCommentMap != null) {
                commentMap.put(queryCondition.getTable(), columnCommentMap);
            }
        }
    }

    private @NotNull Map<String, Map<String, Map<String, String>>> getSchemaTableCommentMap(Map<String, Map<String, List<Map<String, Object>>>> schemaTableDataMap) {
        Map<String, Map<String, Map<String, String>>> comments = new HashMap<>();
        schemaTableDataMap.forEach((schema, tableDataMap) -> {
            final Map<String, Map<String, String>> tableColumnMap = comments.getOrDefault(schema, new HashMap<>());
            tableDataMap.forEach((tableName, dataMapList) -> {
                final TableColumnInfo columnInfo = getTableColumnInfo(schema, tableName);
                final Map<String, String> commentMap = new HashMap<>();
                columnInfo.columns().forEach(column -> {
                    commentMap.put(column.columnName(), column.columnComment());
                });
                tableColumnMap.put(tableName, commentMap);
            });
            comments.put(schema, tableColumnMap);
        });
        return comments;
    }


    private Map<String, Map<String, String>> mergerSchemaComment(Map<String, Map<String, Map<String, String>>> comments) {
        if (comments.isEmpty()) {
            return Collections.emptyMap();
        }
        if (comments.size() == 1) {
            return comments.values().iterator().next();
        }
        Map<String, Map<String, String>> result = new HashMap<>();
        comments.forEach((schema, tableCommentMap) -> {
            tableCommentMap.forEach((tableName, columnMap) -> {
                result.put(schema + "." + tableName, columnMap);
            });
        });
        return result;
    }

    private Map<String, List<Map<String, Object>>> mergerSchemaData(Map<String, Map<String, List<Map<String, Object>>>> schemaTableDataMap) {
        if (schemaTableDataMap.isEmpty()) {
            return Collections.emptyMap();
        }
        if (schemaTableDataMap.size() == 1) {
            return schemaTableDataMap.values().iterator().next();
        }
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        schemaTableDataMap.forEach((schema, tableDataMap) -> {
            tableDataMap.forEach((tableName, dataMapList) -> {
                result.put(schema + "." + tableName, dataMapList);
            });
        });
        return result;
    }

    private TableColumnInfo getTableColumnInfo(String schema, String tableName) {
        Validate.notBlank(schema, "schema can not be blank");
        Validate.notBlank(tableName, "tableName can not be blank");

        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        Validate.isTrue(sourceOptional.isPresent(), "dataSource not found:" + schema);
        final DataSource dataSource = sourceOptional.get();

        return mySqlTableMetaInfoQuery.getTableColumnMetaInfo(schema, tableName, dataSource);
    }

    private static void checkParam(QueryCondition queryCondition) {
        Validate.notNull(queryCondition, "queryCondition can not be null");
        Validate.notBlank(queryCondition.getSchema(), "schema can not be blank");
        Validate.notBlank(queryCondition.getTable(), "table can not be blank");
        Validate.notEmpty(queryCondition.getConditions(), "conditions can not be empty");
        for (Condition condition : queryCondition.getConditions()) {
            Validate.notBlank(condition.getField(), "field can not be blank");
            Validate.notBlank(condition.getOperator(), "operator can not be blank");
            Validate.notNull(condition.getValue(), "value can not be null");
            Validate.notBlank(condition.getValue().toString(), "value can not be blank");
        }
    }
}
