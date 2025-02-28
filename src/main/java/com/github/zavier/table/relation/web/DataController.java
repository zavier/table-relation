package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.manager.DataQueryManager;
import com.github.zavier.table.relation.manager.RelationShipManager;
import com.github.zavier.table.relation.manager.ai.SqlGenerator;
import com.github.zavier.table.relation.service.dto.ExecuteSqlDto;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.Result;
import com.github.zavier.table.relation.service.dto.TableData;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table")
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Resource
    private DataQueryManager dataQueryManager;
    @Resource
    private RelationShipManager relationShipManager;
    @Resource
    private SqlGenerator sqlGenerator;

    @GetMapping("/allSchema")
    public Result<List<String>> getAllSchema() {
        return dataQueryManager.getAllSchema();
    }

    @GetMapping("/schemaTables")
    public Result<List<String>> getSchemaTables(@RequestParam("schema") String schema) {
        return dataQueryManager.getSchemaTables(schema);
    }

    @GetMapping("/tableColumns")
    public Result<List<String>> getTableColumns(@RequestParam("schema") String schema, @RequestParam("tableName") String tableName) {
        return dataQueryManager.getTableColumns(schema, tableName);
    }

    @GetMapping("/generateSql")
    public Result<String> generateSql(@RequestParam("schema") String schema, @RequestParam("demand") String demand) {
        return Result.success(sqlGenerator.generateSql(schema, demand));
    }

    @PostMapping("/executeSql")
    public Result<List<Map<String, Object>>> executeSql(@RequestBody ExecuteSqlDto executeSqlDto) {
        return dataQueryManager.executeSql(executeSqlDto.getSchema(), executeSqlDto.getSql());
    }

    @PostMapping("/sqlQuery")
    public Result<TableData> queryRelaData(@RequestBody QueryCondition queryCondition) {
        return dataQueryManager.queryTableData(queryCondition);
    }

    @PostMapping("/generateInsertSql")
    public Result<Map<String, List<String>>> generateInsertSql(@RequestBody QueryCondition queryCondition) {
        return dataQueryManager.generateInsertSql(queryCondition);
    }

}
