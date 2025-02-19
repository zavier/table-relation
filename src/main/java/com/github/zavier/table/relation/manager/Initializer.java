package com.github.zavier.table.relation.manager;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.dao.mapper.TableRelationMapper;
import com.github.zavier.table.relation.dao.repository.DataSourceConfigRepository;
import com.github.zavier.table.relation.service.DataSourceRegistry;
import com.github.zavier.table.relation.service.TableRelationRegistry;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import com.github.zavier.table.relation.utils.DataSourceUrlBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Initializer {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

    @Resource
    private TableRelationMapper tableRelationMapper;
    @Resource
    private DataSourceConfigRepository dataSourceConfigRepository;


    @PostConstruct
    public void init() {
        refreshDataSource();
        refreshTableRelation();
    }

    public void refreshDataSource() {
        log.info("refreshDataSource");
        dataSourceRegistry.clear();

        final List<DataSourceConfig> dataSourceConfigs = dataSourceConfigRepository.listAllDataSourceConfig();
        dataSourceConfigs.forEach(dataSourceConfig -> {
            // 目前仅支持mysql
            final String jdbcUrl = DataSourceUrlBuilder.buildUrlForMySql(dataSourceConfig);
            dataSourceRegistry.addDataSource(dataSourceConfig.getDatabase(), jdbcUrl, dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
        });
    }

    public void refreshTableRelation() {
        log.info("refreshTableRelation");
        tableRelationRegistry.clear();
        final List<TableRelation> tableRelations = tableRelationMapper.listAllTableRelation();
        tableRelations.forEach(tableRelation -> {
            Column sourceColumn = new Column(tableRelation.tableSchema(), tableRelation.tableName(), tableRelation.columnName(), tableRelation.condition());
            Column relatedColumn = new Column(tableRelation.referencedTableSchema(), tableRelation.referencedTableName(), tableRelation.referencedColumnName(), "");
            final ColumnRelation columnRelation = new ColumnRelation(sourceColumn, relatedColumn, tableRelation.relationType());
            tableRelationRegistry.register(columnRelation);
        });
    }
}
