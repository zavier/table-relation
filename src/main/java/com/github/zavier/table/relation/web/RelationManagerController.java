package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.RelationManagerService;
import com.github.zavier.table.relation.service.domain.ColumnUsage;
import com.github.zavier.table.relation.service.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table/relation")
public class RelationManagerController {

    @Resource
    private RelationManagerService relationManagerService;

    @GetMapping("/list")
    public Result<List<ColumnUsage>> listAllDataSource() {
        return Result.success(relationManagerService.listAllColumnUsage());
    }

    @PostMapping("/update")
    public Result<Boolean> updateDataSource(@RequestBody ColumnUsage dataSourceConfig) {
        relationManagerService.updateColumnUsage(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/add")
    public Result<Boolean> addColumnUsage(@RequestBody ColumnUsage dataSourceConfig) {
        relationManagerService.addColumnUsage(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteDataSource(@PathVariable Integer id) {
        relationManagerService.deleteColumnUsage(id);
        return Result.success(true);
    }

    @GetMapping("/erDiagram")
    public Result<String> getTableRelationMermaidERDiagram(@RequestParam("schema") String schema, @RequestParam("tableName") String tableName) {
        final String erDiagram = relationManagerService.getTableRelationMermaidERDiagram(schema, tableName);
        return Result.success(erDiagram);
    }
}
