package com.github.zavier.table.relation.dao.entity;

import com.github.zavier.table.relation.service.db.DatabaseType;

public record DatabaseConnectionInfo(
        Integer id,
        String database,
        String host,
        String username,
        String password,
        Integer port,
        DatabaseType databaseType,
        String schemaName
) {
}
