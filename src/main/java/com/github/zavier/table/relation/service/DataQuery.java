package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

@Component
public class DataQuery {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DataQuery.class);

    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private SqlExecutor sqlExecutor;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

    @Value("${logic.no.delete.condition}")
    private String logicNoDeleteCondition;

    // schema -> table -> list<col, value>
    public Map<String, Map<String, List<Map<String, Object>>>> query(QueryCondition queryCondition) {
        return queryByBfs(queryCondition);
    }

    public List<Map<String, Object>> queryBySql(String schema, String sql) {
        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            throw new RuntimeException("dataSource not found:" + schema);
        }
        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.sqlQueryWithoutLimit(dataSource, sql);
    }

    private Map<String, Map<String, List<Map<String, Object>>>> queryByBfs(QueryCondition queryCondition) {
        Queue<QueryCondition> queue = new LinkedList<>();
        queue.add(queryCondition);
        log.info("enqueue queryCondition:{}", queryCondition);

        Map<String, Map<String, List<Map<String, Object>>>> resultMap = new HashMap<>();
        Set<Column> uniqueKey = new HashSet<>();

        while (!queue.isEmpty()) {
            QueryCondition currentCondition = queue.poll();
            log.info("dequeue queryCondition:{}", currentCondition);
            List<Map<String, Object>> dataMapList = executeQuery(currentCondition);
            if (dataMapList.isEmpty()) {
                log.warn("{} dataMapList is empty", currentCondition);
                continue;
            }

            final Map<Column, List<Column>> referenced =
                    tableRelationRegistry.getDirectReferenced(currentCondition.getSchema(), currentCondition.getTable());
            log.info("schema:{} table:{} referenced tables:{} ", currentCondition.getSchema(), currentCondition.getTable(),
                    referenced);

            // 取对应表第一次查询到的数据
            final Map<String, List<Map<String, Object>>> tableDataMap = resultMap.getOrDefault(currentCondition.getSchema(), new HashMap<>());
            tableDataMap.putIfAbsent(currentCondition.getTable(), dataMapList);
            resultMap.put(currentCondition.getSchema(), tableDataMap);

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

                    // 如果表有条件限制，则需要使用限制后的数据做关联查询(之前查询结果不影响)
                    if (StringUtils.isNotBlank(column.condition())) {
                        currentCondition.addCustomizeConditionSql(column.condition());
                        dataMapList = executeQuery(currentCondition);
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

                    if (StringUtils.isNotBlank(referencedColumn.condition())) {
                        innerQueryCondition.addCustomizeConditionSql(referencedColumn.condition());
                    }
                    log.info("enqueue queryCondition:{}", innerQueryCondition);
                    queue.add(innerQueryCondition);
                }
            }
        }

        return resultMap;
    }

    private List<Map<String, Object>> executeQuery(QueryCondition queryCondition) {
        final String schema = queryCondition.getSchema();
        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            throw new RuntimeException("dataSource not found:" + schema);
        }

        // 增加逻辑删除条件
        if (StringUtils.isNotBlank(logicNoDeleteCondition)) {
            queryCondition.setLogicNoDeleteCondition(logicNoDeleteCondition);
        }

        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.sqlQueryWithLimit(dataSource, queryCondition.buildSql());
    }

}
