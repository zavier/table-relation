package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.db.DatabaseAdapter;
import com.github.zavier.table.relation.service.db.DatabaseAdapterRegistry;
import com.github.zavier.table.relation.service.db.DataSourceContext;
import com.github.zavier.table.relation.service.db.DatabaseType;
import com.github.zavier.table.relation.service.dto.DataSourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataSourceRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRegistry.class);

    @Resource
    private DatabaseAdapterRegistry databaseAdapterRegistry;

    private final Map<String, DataSourceContext> contextMap = new ConcurrentHashMap<>();

    public void addDataSource(DataSourceConfig dataSourceConfig) {
        DatabaseAdapter adapter = databaseAdapterRegistry.get(dataSourceConfig.getDatabaseType());
        addDataSource(dataSourceConfig.getDatabase(), adapter.buildJdbcUrl(dataSourceConfig),
                dataSourceConfig.getUsername(), dataSourceConfig.getPassword(),
                adapter.type(), adapter.physicalSchema(dataSourceConfig));
    }

    /**
     * 兼容旧调用，默认按 MySQL 处理。
     */
    public void addDataSource(String name, String url, String username, String password) {
        addDataSource(name, url, username, password, DatabaseType.MYSQL, name);
    }

    public void addDataSource(String name, String url, String username, String password,
                              DatabaseType type, String physicalSchema) {
        log.info("Add DataSource: {} type:{} url:{} username:{}", name, type, url, username);
        final HikariConfig hikariConfig = buildHikariConfig(url, username, password);
        final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        final DatabaseAdapter adapter = databaseAdapterRegistry.get(type);
        final DataSourceContext context = new DataSourceContext(
                name, type, name, physicalSchema, hikariDataSource, adapter);

        final DataSourceContext existing = contextMap.putIfAbsent(name, context);
        if (existing != null) {
            hikariDataSource.close();
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
        hikariConfig.setConnectionTestQuery("SELECT 1"); // 保活语句，MySQL / PostgreSQL 通用
        hikariConfig.setKeepaliveTime(5 * 60 * 1000);    // 每5分钟发送一次保活查询
        return hikariConfig;
    }

    public List<String> getAllSchema() {
        return new ArrayList<>(contextMap.keySet());
    }

    public Optional<DataSourceContext> getContext(String name) {
        return Optional.ofNullable(contextMap.get(name));
    }

    public Optional<DataSource> getDataSource(String name) {
        final DataSourceContext context = contextMap.get(name);
        return context == null ? Optional.empty() : Optional.of(context.dataSource());
    }

    public void clear() {
        destroy();
        contextMap.clear();
    }

    @PreDestroy
    public void destroy() {
        contextMap.forEach((name, context) -> {
            try {
                if (context.dataSource() instanceof HikariDataSource hikariDataSource) {
                    hikariDataSource.close();
                }
                log.info("Close DataSource: {}", name);
            } catch (Exception e) {
                log.error("Failed to close DataSource: {}", name, e);
            }
        });
    }
}
