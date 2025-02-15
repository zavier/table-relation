package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.DataQueryService;
import com.github.zavier.table.relation.service.RelationManagerService;
import com.github.zavier.table.relation.service.SqlGenerateService;
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
    private DataQueryService dataQueryService;
    @Resource
    private RelationManagerService relationManagerService;
    @Resource
    private SqlGenerateService sqlGenerateService;

    @GetMapping("/allSchema")
    public Result<List<String>> getAllSchema() {
        return dataQueryService.getAllSchema();
    }

    @GetMapping("/schemaTables")
    public Result<List<String>> getSchemaTables(@RequestParam("schema") String schema) {
        return dataQueryService.getSchemaTables(schema);
    }

    @GetMapping("/tableColumns")
    public Result<List<String>> getTableColumns(@RequestParam("schema") String schema, @RequestParam("tableName") String tableName) {
        return dataQueryService.getTableColumns(schema, tableName);
    }

    @GetMapping("/generateSql")
    public Result<String> generateSql(@RequestParam("schema") String schema, @RequestParam("demand") String demand) {
        return Result.success(sqlGenerateService.generateSql(schema, demand));
    }

    // executeSql
    @PostMapping("/executeSql")
    public Result<List<Map<String, Object>>> executeSql(@RequestBody ExecuteSqlDto executeSqlDto) {
        return dataQueryService.executeSql(executeSqlDto.getSchema(), executeSqlDto.getSql());
    }

    @PostMapping("/sqlQuery")
    public Result<TableData> queryRelaData(@RequestBody QueryCondition queryCondition) {
        return dataQueryService.queryTableData(queryCondition);
    }

}
