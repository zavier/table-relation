package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.dao.entity.DatabaseConnectionInfo;
import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.dao.mapper.DatabaseConnectionMapper;
import com.github.zavier.table.relation.dao.mapper.TableRelationMapper;
import com.github.zavier.table.relation.service.abilty.DataSourceManager;
import com.github.zavier.table.relation.service.abilty.TableRelationRegistry;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
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
    private DataSourceManager dataSourceManager;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

    @Resource
    private TableRelationMapper tableRelationMapper;
    @Resource
    private DatabaseConnectionMapper databaseConnectionInfoMapper;


    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        log.info("refresh table relation");
        final List<DatabaseConnectionInfo> databaseConnectionInfos = databaseConnectionInfoMapper.listAllDatabaseConnectionInfo();
        databaseConnectionInfos.forEach(databaseConnectionInfo -> {
            // 目前仅支持mysql
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", databaseConnectionInfo.host(), databaseConnectionInfo.port(), databaseConnectionInfo.database());
            dataSourceManager.addDataSource(databaseConnectionInfo.database(), jdbcUrl, databaseConnectionInfo.username(), databaseConnectionInfo.password());
        });

        final List<TableRelation> tableRelations = tableRelationMapper.listAllTableRelation();
        tableRelations.forEach(tableRelation -> {
            Column sourceColumn = new Column(tableRelation.tableSchema(), tableRelation.tableName(), tableRelation.columnName());
            Column relatedColumn = new Column(tableRelation.referencedTableSchema(), tableRelation.referencedTableName(), tableRelation.referencedColumnName());
            final ColumnRelation columnRelation = new ColumnRelation(sourceColumn, relatedColumn, tableRelation.relationType());
            tableRelationRegistry.register(columnRelation);
        });

    }
}
