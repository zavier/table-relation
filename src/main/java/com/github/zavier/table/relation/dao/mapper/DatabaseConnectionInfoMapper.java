package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.DatabaseConnectionInfo;

import java.util.List;

public interface DatabaseConnectionInfoMapper {
    DatabaseConnectionInfo selectById(Integer id);

    List<DatabaseConnectionInfo> selectAll();

    int insert(DatabaseConnectionInfo databaseConnectionInfo);

    int update(DatabaseConnectionInfo databaseConnectionInfo);

    int deleteById(Integer id);
}
