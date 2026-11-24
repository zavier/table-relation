package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.DatabaseConnectionInfo;
import com.github.zavier.table.relation.service.db.DatabaseType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DatabaseConnectionMapper {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionMapper.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<DatabaseConnectionInfo> listAllDatabaseConnectionInfo() {
        String sql = "SELECT * FROM database_connection_info";
        log.info("listAllDatabaseConnectionInfo sql:{}", sql);
        return jdbcTemplate.query(sql, rowMapper());
    }

    public void addDatabaseConnectionInfo(DatabaseConnectionInfo databaseConnectionInfo) {
        String sql = "INSERT INTO database_connection_info (`database`, `host`, `username`, `password`, `port`, `database_type`, `schema_name`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        log.info("addDatabaseConnectionInfo sql:{}", sql);
        final int update = jdbcTemplate.update(sql,
                databaseConnectionInfo.database(),
                databaseConnectionInfo.host(),
                databaseConnectionInfo.username(),
                databaseConnectionInfo.password(),
                databaseConnectionInfo.port(),
                databaseConnectionInfo.databaseType() == null ? DatabaseType.MYSQL.name() : databaseConnectionInfo.databaseType().name(),
                databaseConnectionInfo.schemaName());
        if (update != 1) {
            throw new RuntimeException("添加数据库连接失败");
        }
    }

    public void deleteDatabaseConnectionInfo(Integer id) {
        String sql = "DELETE FROM database_connection_info WHERE id = ?";
        log.info("deleteDatabaseConnectionInfo sql:{}", sql);
        final int update = jdbcTemplate.update(sql, id);
        if (update != 1) {
            throw new RuntimeException("删除数据库连接失败");
        }
    }

    public void updateDatabaseConnectionInfo(DatabaseConnectionInfo databaseConnectionInfo) {
        String sql = "UPDATE database_connection_info SET `database` = ?, `host` = ?, `username` = ?, `password` = ?, `port` = ?, `database_type` = ?, `schema_name` = ? WHERE id = ?";
        log.info("updateDatabaseConnectionInfo sql:{}", sql);
        final int update = jdbcTemplate.update(sql,
                databaseConnectionInfo.database(),
                databaseConnectionInfo.host(),
                databaseConnectionInfo.username(),
                databaseConnectionInfo.password(),
                databaseConnectionInfo.port(),
                databaseConnectionInfo.databaseType() == null ? DatabaseType.MYSQL.name() : databaseConnectionInfo.databaseType().name(),
                databaseConnectionInfo.schemaName(),
                databaseConnectionInfo.id());
        if (update != 1) {
            throw new RuntimeException("更新数据库连接失败");
        }
    }

    public DatabaseConnectionInfo getDatabaseConnectionInfo(Integer id) {
        String sql = "SELECT * FROM database_connection_info WHERE id = ?";
        log.info("getDatabaseConnectionInfo sql:{}", sql);
        return jdbcTemplate.queryForObject(sql, rowMapper(), id);
    }

    private RowMapper<DatabaseConnectionInfo> rowMapper() {
        return (rs, rowNum) -> new DatabaseConnectionInfo(
                rs.getInt("id"),
                rs.getString("database"),
                rs.getString("host"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("port"),
                DatabaseType.from(rs.getString("database_type")),
                rs.getString("schema_name")
        );
    }
}
