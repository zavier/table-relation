package com.github.zavier.table.relation.service.dto;

import com.github.zavier.table.relation.service.db.DatabaseType;

public class DataSourceConfig {
    private Integer id;
    private String database;
    private String host;
    private String username;
    private String password;
    private Integer port;
    /**
     * 数据库类型，历史数据为空时按 MySQL 处理。
     */
    private DatabaseType databaseType;
    /**
     * PostgreSQL schema，为空时使用 public；MySQL 忽略该字段。
     */
    private String schemaName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public DatabaseType getDatabaseType() {
        return databaseType == null ? DatabaseType.MYSQL : databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
