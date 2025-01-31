package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.abilty.DataSourceRegistry;
import com.github.zavier.table.relation.service.abilty.SqlExecutor;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.Result;
import com.github.zavier.table.relation.service.query.DataQuery;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DataQueryService {

    @Resource
    private DataQuery dataQuery;
    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private SqlExecutor sqlExecutor;

    public Result<List<String>> getAllSchema() {
        return Result.success(dataSourceRegistry.getAllSchema());
    }

    public Result<List<String>> getSchemaTables(String schema) {
        Validate.notBlank(schema, "schema can not be blank");

        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            return Result.success(List.of());
        }
        final DataSource dataSource = sourceOptional.get();
        return Result.success(sqlExecutor.getSchemaTables(schema, dataSource));
    }

    public Result<List<String>> getTableColumns(String schema, String tableName) {
        Validate.notBlank(schema, "schema can not be blank");
        Validate.notBlank(tableName, "tableName can not be blank");

        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            return Result.success(List.of());
        }
        final DataSource dataSource = sourceOptional.get();
        return Result.success(sqlExecutor.getTableColumns(schema, tableName, dataSource));
    }


    public Result<Map<String, List<Map<String, Object>>>> queryRelaData(QueryCondition queryCondition) {
        checkParam(queryCondition);

        return Result.success(dataQuery.query(queryCondition));
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
