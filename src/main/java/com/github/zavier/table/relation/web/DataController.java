package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.DataQueryService;
import com.github.zavier.table.relation.service.abilty.TableRelationRegistry;
import com.github.zavier.table.relation.service.dto.EntityRelationShip;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.Result;
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
    private TableRelationRegistry tableRelationRegistry;

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

    @GetMapping("/erDiagram")
    public Result<String> getTableRelationMermaidERDiagram(@RequestParam("schema") String schema, @RequestParam("tableName") String tableName) {
        final List<EntityRelationShip> allReferenced = tableRelationRegistry.getAllReferenced(schema, tableName);
        String head = "erDiagram";
        String template = "  %s ||--o{ %s : \"%s\"";
        final StringBuilder builder = new StringBuilder(head);
        for (EntityRelationShip entityRelationship : allReferenced) {
            final String format = String.format(template, entityRelationship.sourceTable(), entityRelationship.targetTable(), entityRelationship.label());
            builder.append("\n").append(format);
        }
        return Result.success(builder.toString());
    }

    @PostMapping("/sqlQuery")
    public Result<Map<String, List<Map<String, Object>>>> queryRelaData(@RequestBody QueryCondition queryCondition) {
        return dataQueryService.queryRelaData(queryCondition);
    }

}
