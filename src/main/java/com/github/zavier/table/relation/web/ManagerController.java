package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.ManagerService;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import com.github.zavier.table.relation.service.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/table")
public class ManagerController {

    @Resource
    private ManagerService managerService;

    @GetMapping("/datasource/list")
    public Result<List<DataSourceConfig>> listAllDataSource() {
        return Result.success(managerService.listAllDataSourceConfig());
    }

    @PostMapping("/datasource/update")
    public Result<Boolean> updateDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        managerService.updateDataSourceConfig(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/datasource/add")
    public Result<Boolean> addDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        managerService.addDataSourceConfig(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/datasource/delete/{id}")
    public Result<Boolean> deleteDataSource(@PathVariable Integer id) {
        managerService.deleteDataSourceConfig(id);
        return Result.success(true);
    }

    @PostMapping("/datasource/test")
    public Result<Boolean> testDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        final boolean res = managerService.testDataSourceConfigConn(dataSourceConfig);
        return Result.success(res);
    }

    @GetMapping("/datasource/refresh")
    public Result<String> refresh() {
        managerService.refreshConfig();
        return Result.success("success");
    }
}
