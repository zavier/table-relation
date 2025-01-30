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
        log.info("query:{}", queryCondition);
        return queryByBfs(queryCondition);
    }

    private Map<String, List<Map<String, Object>>> queryByBfs(QueryCondition queryCondition) {
        Queue<QueryCondition> queue = new LinkedList<>();
        queue.add(queryCondition);

        Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
        Set<Column> uniqueKey = new HashSet<>();

        while (!queue.isEmpty()) {
            QueryCondition currentCondition = queue.poll();
            final List<Map<String, Object>> dataMapList = executeQuery(currentCondition);
            if (dataMapList.isEmpty()) {
                log.warn("{} dataMapList is empty", currentCondition);
                continue;
            }

            final Map<Column, List<Column>> referenced =
                    tableRelationRegistry.getDirectReferenced(currentCondition.getSchema(), currentCondition.getTable());
            log.info("schema:{} table:{} referenced tables:{} ", currentCondition.getSchema(), currentCondition.getTable(),
                    referenced);

            resultMap.put(currentCondition.getTable(), dataMapList);
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

                    final List<Object> valueList = dataMapList.stream()
                            .map(dataMap -> dataMap.get(columnName))
                            .filter(Objects::nonNull)
                            .toList();
                    if (valueList.isEmpty()) {
                        log.warn("{} dataMapList col:{} valueList is empty", currentCondition, columnName);
                        continue;
                    }

                    final QueryCondition innerQueryCondition = new QueryCondition();
                    innerQueryCondition.setSchema(referencedColumn.schema());
                    innerQueryCondition.setTable(referencedColumn.tableName());
                    final Condition innerCondition = new Condition();
                    innerCondition.setField(referencedColumn.columnName());
                    innerCondition.setOperator("IN");
                    innerCondition.setValue(valueList);
                    innerQueryCondition.setConditions(List.of(innerCondition));

                    queue.add(innerQueryCondition);
                }
            }
        }

        return resultMap;
    }

    private List<Map<String, Object>> executeQuery(QueryCondition queryCondition) {
        final String schema = queryCondition.getSchema();
        final Optional<DataSource> sourceOptional = dataSourceManager.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            throw new RuntimeException("dataSource not found:" + schema);
        }
        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.sqlQuery(dataSource, queryCondition.buildSql());
    }

}
