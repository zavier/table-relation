package com.github.zavier.table.relation.service.abilty;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;

@Service
public class DataSourceManager {

    private static final Logger log = LoggerFactory.getLogger(DataSourceManager.class);

    private Map<String, DataSource> dataSourceMap = new HashMap<>();


    public void addDataSource(String name, String url, String username, String password) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        final DataSource dataSource = dataSourceMap.putIfAbsent(name, hikariDataSource);
        if (dataSource != null) {
            log.error("DataSource already exists: {}", name);
            throw new RuntimeException("DataSource already exists: " + name);
        }
    }

    public List<String> getAllSchema() {
        return new ArrayList<>(dataSourceMap.keySet());
    }

    public Optional<DataSource> getDataSource(String name) {
        final DataSource dataSource = dataSourceMap.get(name);
        return Optional.ofNullable(dataSource);
    }

    @PreDestroy
    public void destroy() {
        dataSourceMap.forEach((name, dataSource) -> {
            try {
                if (dataSource instanceof HikariDataSource) {
                    ((HikariDataSource) dataSource).close();
                }
                log.info("Close DataSource: {}", name);
            } catch (Exception e) {
                log.error("Failed to close DataSource: {}", name, e);
            }
        });
    }
}
