package com.github.zavier.table.relation.dao.repository;

import com.github.zavier.table.relation.dao.entity.DatabaseConnectionInfo;
import com.github.zavier.table.relation.dao.mapper.DatabaseConnectionMapper;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DataSourceConfigRepository {

    @Resource
    private DatabaseConnectionMapper databaseConnectionMapper;

    public List<DataSourceConfig> listAllDataSourceConfig() {
        final List<DatabaseConnectionInfo> databaseConnectionInfos = databaseConnectionMapper.listAllDatabaseConnectionInfo();
        return databaseConnectionInfos.stream().map(this::toDataSourceConfig).toList();
    }

    public void addDataSource(DataSourceConfig dataSourceConfig) {
        databaseConnectionMapper.addDatabaseConnectionInfo(toDatabaseConnectionInfo(null, dataSourceConfig));
    }

    public void deleteDataSource(Integer id) {
        databaseConnectionMapper.deleteDatabaseConnectionInfo(id);
    }

    public void updateDataSource(DataSourceConfig dataSourceConfig) {
        databaseConnectionMapper.updateDatabaseConnectionInfo(
                toDatabaseConnectionInfo(dataSourceConfig.getId(), dataSourceConfig));
    }

    public @Nullable DataSourceConfig getDataSourceConfig(Integer id) {
        DatabaseConnectionInfo databaseConnectionInfo = databaseConnectionMapper.getDatabaseConnectionInfo(id);
        if (databaseConnectionInfo == null) {
            return null;
        }
        return toDataSourceConfig(databaseConnectionInfo);
    }

    private DatabaseConnectionInfo toDatabaseConnectionInfo(Integer id, DataSourceConfig dataSourceConfig) {
        return new DatabaseConnectionInfo(
                id,
                dataSourceConfig.getDatabase(),
                dataSourceConfig.getHost(),
                dataSourceConfig.getUsername(),
                dataSourceConfig.getPassword(),
                dataSourceConfig.getPort(),
                dataSourceConfig.getDatabaseType(),
                dataSourceConfig.getSchemaName()
        );
    }

    private DataSourceConfig toDataSourceConfig(DatabaseConnectionInfo databaseConnectionInfo) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setId(databaseConnectionInfo.id());
        dataSourceConfig.setDatabase(databaseConnectionInfo.database());
        dataSourceConfig.setHost(databaseConnectionInfo.host());
        dataSourceConfig.setUsername(databaseConnectionInfo.username());
        dataSourceConfig.setPassword(databaseConnectionInfo.password());
        dataSourceConfig.setPort(databaseConnectionInfo.port());
        dataSourceConfig.setDatabaseType(databaseConnectionInfo.databaseType());
        dataSourceConfig.setSchemaName(databaseConnectionInfo.schemaName());
        return dataSourceConfig;
    }
}
