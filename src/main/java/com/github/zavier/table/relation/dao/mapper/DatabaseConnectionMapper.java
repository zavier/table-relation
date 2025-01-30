package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.DatabaseConnectionInfo;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DatabaseConnectionMapper {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<DatabaseConnectionInfo> listAllDatabaseConnectionInfo() {
        String sql = "SELECT * FROM database_connection_info";
        RowMapper<DatabaseConnectionInfo> rowMapper = (rs, rowNum) -> new DatabaseConnectionInfo(
                rs.getInt("id"),
                rs.getString("database"),
                rs.getString("host"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("port")
        );
        return jdbcTemplate.query(sql, rowMapper);
    }
}