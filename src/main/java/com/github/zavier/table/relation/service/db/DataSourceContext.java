package com.github.zavier.table.relation.service.db;

import javax.sql.DataSource;

/**
 * 运行期数据源视图。
 *
 * @param key            应用内的 schema 标识，也是 DataSourceRegistry 的 key
 * @param type           数据库类型
 * @param database       JDBC 连接使用的数据库名
 * @param physicalSchema 物理 schema：MySQL 等于 database，PostgreSQL 由配置指定（默认 public）
 * @param dataSource     连接池
 * @param adapter        该类型的适配器
 */
public record DataSourceContext(
        String key,
        DatabaseType type,
        String database,
        String physicalSchema,
        DataSource dataSource,
        DatabaseAdapter adapter) {
}
