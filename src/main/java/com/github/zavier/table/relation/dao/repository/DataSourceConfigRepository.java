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
        return databaseConnectionInfos.stream().map(databaseConnectionInfo -> {
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.setId(databaseConnectionInfo.id());
            dataSourceConfig.setDatabase(databaseConnectionInfo.database());
            dataSourceConfig.setHost(databaseConnectionInfo.host());
            dataSourceConfig.setUsername(databaseConnectionInfo.username());
            dataSourceConfig.setPassword(databaseConnectionInfo.password());
            dataSourceConfig.setPort(databaseConnectionInfo.port());
            return dataSourceConfig;
        }).toList();
    }

    public void addDataSource(DataSourceConfig dataSourceConfig) {
        DatabaseConnectionInfo databaseConnectionInfo = new DatabaseConnectionInfo(
                null,
                dataSourceConfig.getDatabase(),
                dataSourceConfig.getHost(),
                dataSourceConfig.getUsername(),
                dataSourceConfig.getPassword(),
                dataSourceConfig.getPort()
        );
        databaseConnectionMapper.addDatabaseConnectionInfo(databaseConnectionInfo);
    }

    public void deleteDataSource(Integer id) {
        databaseConnectionMapper.deleteDatabaseConnectionInfo(id);
    }

    public void updateDataSource(DataSourceConfig dataSourceConfig) {
        DatabaseConnectionInfo databaseConnectionInfo = new DatabaseConnectionInfo(
                dataSourceConfig.getId(),
                dataSourceConfig.getDatabase(),
                dataSourceConfig.getHost(),
                dataSourceConfig.getUsername(),
                dataSourceConfig.getPassword(),
                dataSourceConfig.getPort()
        );
        databaseConnectionMapper.updateDatabaseConnectionInfo(databaseConnectionInfo);
    }

    public @Nullable DataSourceConfig getDataSourceConfig(Integer id) {
        DatabaseConnectionInfo databaseConnectionInfo = databaseConnectionMapper.getDatabaseConnectionInfo(id);
        if (databaseConnectionInfo == null) {
            return null;
        }
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setId(databaseConnectionInfo.id());
        dataSourceConfig.setDatabase(databaseConnectionInfo.database());
        dataSourceConfig.setHost(databaseConnectionInfo.host());
        dataSourceConfig.setUsername(databaseConnectionInfo.username());
        dataSourceConfig.setPassword(databaseConnectionInfo.password());
        dataSourceConfig.setPort(databaseConnectionInfo.port());
        return dataSourceConfig;
    }


}
