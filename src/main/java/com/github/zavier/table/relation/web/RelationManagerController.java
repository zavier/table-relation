package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.manager.RelationShipManager;
import com.github.zavier.table.relation.service.domain.ColumnUsage;
import com.github.zavier.table.relation.service.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table/relation")
public class RelationManagerController {

    @Resource
    private RelationShipManager relationShipManager;

    @GetMapping("/list")
    public Result<List<ColumnUsage>> listAllDataSource() {
        return Result.success(relationShipManager.listAllColumnUsage());
    }

    @PostMapping("/update")
    public Result<Boolean> updateDataSource(@RequestBody ColumnUsage dataSourceConfig) {
        relationShipManager.updateColumnUsage(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/add")
    public Result<Boolean> addColumnUsage(@RequestBody ColumnUsage dataSourceConfig) {
        relationShipManager.addColumnUsage(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteDataSource(@PathVariable Integer id) {
        relationShipManager.deleteColumnUsage(id);
        return Result.success(true);
    }

    @GetMapping("/erDiagram")
    public Result<String> getTableRelationMermaidERDiagram(@RequestParam("schema") String schema,
                                                           @RequestParam("tableName") String tableName,
                                                           @RequestParam(defaultValue = "true") Boolean needTableInfo) {
        final String erDiagram = relationShipManager.getTableRelationMermaidERDiagram(schema, tableName, needTableInfo);
        return Result.success(erDiagram);
    }
}
