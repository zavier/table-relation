package com.github.zavier.table.relation.dao.entity;


public record DatabaseConnectionInfo(
        Integer id,
        String database,
        String host,
        String username,
        String password,
        Integer port
) {
}
