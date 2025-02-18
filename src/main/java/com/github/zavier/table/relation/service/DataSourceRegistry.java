package com.github.zavier.table.relation.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;

@Service
public class DataSourceRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRegistry.class);

    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    public void addDataSource(String name, String url, String username, String password) {
        log.info("Add DataSource: {} url:{} username:{}", name, url, username);
        final HikariConfig hikariConfig = buildHikariConfig(url, username, password);

        final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        final DataSource dataSource = dataSourceMap.putIfAbsent(name, hikariDataSource);
        if (dataSource != null) {
            log.error("DataSource already exists: {}", name);
            throw new RuntimeException("DataSource already exists: " + name);
        }
    }

    private static @NotNull HikariConfig buildHikariConfig(String url, String username, String password) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // 设置连接池参数
        hikariConfig.setConnectionTimeout(30 * 1000);  // 30秒
        hikariConfig.setIdleTimeout(0);     // 闲置连接永不回收
        hikariConfig.setMaxLifetime(30 * 60 * 1000);    // 30分钟
        hikariConfig.setMinimumIdle(1);          // 最小空闲连接数
        hikariConfig.setMaximumPoolSize(10);     // 最大连接数
        hikariConfig.setValidationTimeout(5000); // 5秒
        hikariConfig.setConnectionTestQuery("SELECT 1"); // MySQL保活语句
        hikariConfig.setKeepaliveTime(5 * 60 * 1000);    // 每5分钟发送一次保活查询
        return hikariConfig;
    }

    public List<String> getAllSchema() {
        return new ArrayList<>(dataSourceMap.keySet());
    }

    public Optional<DataSource> getDataSource(String name) {
        final DataSource dataSource = dataSourceMap.get(name);
        return Optional.ofNullable(dataSource);
    }

    public void clear() {
        destroy();
        dataSourceMap.clear();
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
