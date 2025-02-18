package com.github.zavier.table.relation.service.extpt;

import com.github.zavier.table.relation.service.SqlExecutor;
import com.github.zavier.table.relation.service.converter.ColumnUsageConverter;
import com.github.zavier.table.relation.service.converter.TableInfoConverter;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class MySqlTableMetaInfoQuery {

    @Resource
    private SqlExecutor sqlExecutor;

    public List<TableColumnInfo> getTableColumnMetaInfo(String schema, DataSource dataSource) {
        String columnSql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ?";
        final List<Map<String, Object>> columnDataList = sqlExecutor.sqlQueryWithoutLimit(dataSource, columnSql, schema);

        String tableSql = "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";
        final List<Map<String, Object>> tableDataList = sqlExecutor.sqlQueryWithoutLimit(dataSource, tableSql, schema);
        return TableInfoConverter.convert2TableColumnInfo(columnDataList, tableDataList);
    }

    public TableColumnInfo getTableColumnMetaInfo(String schema, String table, DataSource dataSource) {
        String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        final List<Map<String, Object>> columnDataList = sqlExecutor.sqlQueryWithoutLimit(dataSource, sql, schema, table);

        String tableSql = "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?  AND TABLE_NAME = ?";
        final List<Map<String, Object>> tableDataList = sqlExecutor.sqlQueryWithoutLimit(dataSource, tableSql, schema, table);
        final List<TableColumnInfo> tableColumnInfos = TableInfoConverter.convert2TableColumnInfo(columnDataList, tableDataList);

        Validate.notEmpty(tableColumnInfos, "tableColumnInfos is empty");
        Validate.isTrue(tableColumnInfos.size() == 1, "tableColumnInfos size is not 1");

        return tableColumnInfos.get(0);
    }

    public List<ColumnUsage> getTableRelationMetaInfo(String schema, DataSource dataSource) {
        String sql = "SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`COLUMN_NAME`,`REFERENCED_TABLE_SCHEMA`,`REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = ? and `REFERENCED_COLUMN_NAME` IS NOT NULL";
        final List<Map<String, Object>> maps = sqlExecutor.sqlQueryWithoutLimit(dataSource, sql, schema);
        return maps.stream().map(ColumnUsageConverter::convert2ColumnUsage).toList();
    }

}
