package com.github.zavier.table.relation.service.abilty;

import com.github.zavier.table.relation.service.converter.ColumnUsageConverter;
import com.github.zavier.table.relation.service.converter.TableInfoConverter;
import com.github.zavier.table.relation.service.domain.ColumnUsage;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class MySqlTableMetaInfoQuery {

    @Resource
    private SqlExecutor sqlExecutor;

    public List<TableColumnInfo> getTableColumnMetaInfo(String schema, DataSource dataSource) {
        String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ?";
        final List<Map<String, Object>> maps = sqlExecutor.sqlQuery(dataSource, sql, schema);
        return TableInfoConverter.convert2TableColumnInfo(maps);
    }

    public List<TableColumnInfo> getTableColumnMetaInfo(String schema, String table, DataSource dataSource) {
        String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        final List<Map<String, Object>> maps = sqlExecutor.sqlQuery(dataSource, sql, schema, table);
        return TableInfoConverter.convert2TableColumnInfo(maps);
    }

    public List<ColumnUsage> getTableRelationMetaInfo(String schema, DataSource dataSource) {
        String sql = "SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`COLUMN_NAME`,`REFERENCED_TABLE_SCHEMA`,`REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = ? and `REFERENCED_COLUMN_NAME` IS NOT NULL";
        final List<Map<String, Object>> maps = sqlExecutor.sqlQuery(dataSource, sql, schema);
        return maps.stream().map(ColumnUsageConverter::convert2ColumnUsage).toList();
    }

}
