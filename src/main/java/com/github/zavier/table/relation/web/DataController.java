package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.DataSourceManager;
import com.github.zavier.table.relation.service.SqlExecutor;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.query.DataQuery;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/table")
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Resource
    private DataQuery dataQuery;
    @Resource
    private DataSourceManager dataSourceManager;
    @Resource
    private SqlExecutor sqlExecutor;

    @GetMapping("/allSchema")
    public List<String> getAllSchema() {
        return dataSourceManager.getAllSchema();
    }

    @GetMapping("/schemaTables")
    public List<String> getSchemaTables(@RequestParam("schema") String schema) {
        final Optional<DataSource> sourceOptional = dataSourceManager.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            return List.of();
        }
        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.getSchemaTables(schema, dataSource);
    }

    @GetMapping("/tableColumns")
    public List<String> getTableColumns(@RequestParam("schema") String schema, @RequestParam("tableName") String tableName) {
        final Optional<DataSource> sourceOptional = dataSourceManager.getDataSource(schema);
        if (sourceOptional.isEmpty()) {
            return List.of();
        }
        final DataSource dataSource = sourceOptional.get();
        return sqlExecutor.getTableColumns(schema, tableName, dataSource);
    }


    @PostMapping("/sqlQuery")
    public Map<String, List<Map<String, Object>>> queryRelaData(@RequestBody QueryCondition queryCondition) {
        return dataQuery.query(queryCondition);
    }

}
