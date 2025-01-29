package com.github.zavier.table.relation.service.query;

import com.github.zavier.table.relation.service.Column;
import com.github.zavier.table.relation.service.DataSourceManager;
import com.github.zavier.table.relation.service.SqlExecutor;
import com.github.zavier.table.relation.service.TableRelationRegistry;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

@Component
public class DataQuery {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DataQuery.class);

    @Resource
    private DataSourceManager dataSourceManager;
    @Resource
    private SqlExecutor sqlExecutor;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

    public Map<String, List<Map<String, Object>>> query(QueryCondition queryCondition) {
        final String schema = queryCondition.getSchema();
        final String tableName = queryCondition.getTableName();

        final Map<Column, List<Column>> referenced = tableRelationRegistry.getReferenced(schema, tableName);

        Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
        Set<Column> uniqueKey = new HashSet<>();
        doQuery(queryCondition, referenced, resultMap, uniqueKey);


        return resultMap;
    }

    private void doQuery(QueryCondition queryCondition,
                         Map<Column, List<Column>> referenced,
                         Map<String, List<Map<String, Object>>> resultMap,
                         Set<Column> uniqueKey) {
        final List<Map<String, Object>> dataMapList = executeQuery(queryCondition);
        if (dataMapList.isEmpty()) {
            log.warn("{} is empty", queryCondition);
            return;
        }

        resultMap.put(queryCondition.getTableName(), dataMapList);
        for (Map.Entry<Column, List<Column>> entry : referenced.entrySet()) {
            final Column column = entry.getKey();
            final String columnName = column.columnName();

            // 避免重复查询
            uniqueKey.add(column);

            final List<Column> referencedColumns = entry.getValue();
            for (Column referencedColumn : referencedColumns) {
                if (!uniqueKey.add(referencedColumn)) {
                    continue;
                }

                // TODO 优化使用 sql IN
                final List<Object> valueList = dataMapList.stream()
                        .map(dataMap -> dataMap.get(columnName))
                        .filter(Objects::nonNull)
                        .toList();
                if (valueList.isEmpty()) {
                    log.warn("{} dataMapList value is empty", columnName);
                    continue;
                }

                final QueryCondition innerQueryCondition = new QueryCondition();
                innerQueryCondition.setSchema(referencedColumn.schema());
                innerQueryCondition.setTableName(referencedColumn.tableName());
                final Condition innerCondition = new Condition();
                innerCondition.setColumn(referencedColumn.columnName());
                innerCondition.setOperator("IN");
                innerCondition.setValue(valueList);
                innerQueryCondition.setConditionList(List.of(innerCondition));

                final Map<Column, List<Column>> innerReferencedMap = tableRelationRegistry.getReferenced(referencedColumn.schema(), referencedColumn.tableName());
                doQuery(innerQueryCondition, innerReferencedMap, resultMap, uniqueKey);
            }
        }

    }

    private List<Map<String, Object>> executeQuery(QueryCondition queryCondition) {
        final String schema = queryCondition.getSchema();
        final Optional<DataSource> sourceOptional = dataSourceManager.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            throw new RuntimeException("dataSource not found:" + schema);
        }
        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.execute(dataSource, queryCondition.buildSql());
    }

}
