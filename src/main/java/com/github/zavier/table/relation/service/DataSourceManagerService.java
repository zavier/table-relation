package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.dao.repository.DataSourceConfigRepository;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import com.github.zavier.table.relation.utils.DataSourceUrlBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

@Service
public class DataSourceManagerService {

    private static final Logger log = LoggerFactory.getLogger(DataSourceManagerService.class);

    @Resource
    private DataSourceConfigRepository dataSourceConfigRepository;
    @Resource
    private Initializer initializer;
    @Resource
    private RelationManagerService relationManagerService;

    public List<DataSourceConfig> listAllDataSourceConfig() {
        return dataSourceConfigRepository.listAllDataSourceConfig();
    }

    public void addDataSourceConfig(DataSourceConfig dataSourceConfig) {
        checkParam(dataSourceConfig);

        // 测试配置是否正确
        final boolean conn = testDataSourceConfigConn(dataSourceConfig);
        Validate.isTrue(conn, "Failed to connect to DataSource");
        // 是否已存在
        final List<DataSourceConfig> dataSourceConfigs = dataSourceConfigRepository.listAllDataSourceConfig();
        if (!dataSourceConfigs.isEmpty()) {
            final boolean exist = dataSourceConfigs.stream()
                    .anyMatch(it -> Objects.equals(it.getDatabase(), dataSourceConfig.getDatabase()));
            Validate.isTrue(!exist, "DataSource " + dataSourceConfig.getDatabase() + " already exists");
        }

        dataSourceConfigRepository.addDataSource(dataSourceConfig);

        initializer.refreshDataSource();
        updateTableRelation(dataSourceConfig.getDatabase());
        initializer.refreshTableRelation();
    }

    public void deleteDataSourceConfig(Integer id) {
        Validate.notNull(id, "id can not be null");
        dataSourceConfigRepository.deleteDataSource(id);

        initializer.refreshDataSource();
    }

    public void updateDataSourceConfig(DataSourceConfig dataSourceConfig) {
        checkParam(dataSourceConfig);
        Validate.notNull(dataSourceConfig.getId(), "id can not be null");

        final boolean conn = testDataSourceConfigConn(dataSourceConfig);
        Validate.isTrue(conn, "Failed to connect to DataSource");

        dataSourceConfigRepository.updateDataSource(dataSourceConfig);

        initializer.refreshDataSource();
        updateTableRelation(dataSourceConfig.getDatabase());
        initializer.refreshTableRelation();
    }

    private void updateTableRelation(String tableSchema) {
        try {
            relationManagerService.refreshColumnUsage(tableSchema);
        } catch (Exception e) {
            log.error("refreshColumnUsage tableSchema:{} failed", tableSchema, e);
        }
    }


    public boolean testDataSourceConfigConn(DataSourceConfig dataSourceConfig) {
        checkParam(dataSourceConfig);

        final String url = DataSourceUrlBuilder.buildUrlForMySql(dataSourceConfig);
        try {
            final HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(dataSourceConfig.getUsername());
            hikariConfig.setPassword(dataSourceConfig.getPassword());

            try (HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig)) {
                final Connection connection = hikariDataSource.getConnection();
                // 通过执行select 1, 验证一下是否能连接执行成功
                try (PreparedStatement preparedStatement = connection.prepareStatement("select 1")) {
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    final int result = resultSet.getInt(1);
                    if (result != 1) {
                        log.error("Failed to connect to DataSource:{}", url);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to DataSource:{}", url, e);
            return false;
        }
    }


    private void checkParam(DataSourceConfig dataSourceConfig) {
        Validate.notNull(dataSourceConfig, "dataSourceConfig can not be null");
        Validate.notBlank(dataSourceConfig.getDatabase(), "database can not be null");
        Validate.notBlank(dataSourceConfig.getHost(), "host can not be null");
        Validate.notNull(dataSourceConfig.getPort(), "port can not be null");
        Validate.notBlank(dataSourceConfig.getUsername(), "username can not be null");
        Validate.notBlank(dataSourceConfig.getPassword(), "password can not be null");

    }

}
