package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.DataSourceManagerService;
import com.github.zavier.table.relation.service.Initializer;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import com.github.zavier.table.relation.service.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table/datasource")
public class DataSourceManagerController {

    @Resource
    private DataSourceManagerService dataSourceManagerService;
    @Resource
    private Initializer initializer;

    @GetMapping("/list")
    public Result<List<DataSourceConfig>> listAllDataSource() {
        return Result.success(dataSourceManagerService.listAllDataSourceConfig());
    }

    @PostMapping("/update")
    public Result<Boolean> updateDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        dataSourceManagerService.updateDataSourceConfig(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/add")
    public Result<Boolean> addDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        dataSourceManagerService.addDataSourceConfig(dataSourceConfig);
        return Result.success(true);
    }

    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteDataSource(@PathVariable Integer id) {
        dataSourceManagerService.deleteDataSourceConfig(id);
        return Result.success(true);
    }

    @PostMapping("/test")
    public Result<Boolean> testDataSource(@RequestBody DataSourceConfig dataSourceConfig) {
        final boolean res = dataSourceManagerService.testDataSourceConfigConn(dataSourceConfig);
        return Result.success(res);
    }

    @GetMapping("/refresh")
    public Result<String> refresh() {
        initializer.refreshDataSource();
        initializer.refreshTableRelation();
        return Result.success("success");
    }

}
