package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.dao.repository.TableRelationRepository;
import com.github.zavier.table.relation.service.abilty.DataSourceRegistry;
import com.github.zavier.table.relation.service.abilty.SqlExecutor;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class RelationManagerService {

    private static final Logger log = LoggerFactory.getLogger(RelationManagerService.class);

    @Resource
    private TableRelationRepository tableRelationRepository;
    @Resource
    private Initializer initializer;
    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private SqlExecutor sqlExecutor;

    public List<ColumnUsage> listAllColumnUsage() {
        return tableRelationRepository.listAllTableRelation();
    }

    public void addColumnUsage(ColumnUsage columnUsage) {
        checkParam(columnUsage);
        // 是否已存在
        final List<ColumnUsage> columnUsages = listAllColumnUsage();
        if (!columnUsages.isEmpty()) {
            final boolean exist = columnUsages.stream()
                    .anyMatch(it -> Objects.equals(it.getTableSchema(), columnUsage.getTableSchema())
                            && Objects.equals(it.getTableName(), columnUsage.getTableName())
                            && Objects.equals(it.getColumnName(), columnUsage.getColumnName())
                            && Objects.equals(it.getCondition(), columnUsage.getCondition())
                            && Objects.equals(it.getReferencedTableSchema(), columnUsage.getReferencedTableSchema())
                            && Objects.equals(it.getReferencedTableName(), columnUsage.getReferencedTableName())
                            && Objects.equals(it.getReferencedColumnName(), columnUsage.getReferencedColumnName())
                    );
            Validate.isTrue(!exist, "table relation already exists");
        }

        tableRelationRepository.addTableRelation(columnUsage);

        refreshRegistry();
    }

    public void deleteColumnUsage(Integer id) {
        Validate.notNull(id, "id can not be null");
        tableRelationRepository.deleteTableRelation(id);

        refreshRegistry();
    }

    public void updateColumnUsage(ColumnUsage columnUsage) {
        checkParam(columnUsage);
        Validate.notNull(columnUsage.getId(), "id can not be null");

        tableRelationRepository.updateTableRelation(columnUsage);

        refreshRegistry();
    }

    private void refreshRegistry() {
        // 刷新注册表
        initializer.refreshTableRelation();
    }

    public void refreshColumnUsage(String tableSchema) {
        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource(tableSchema);
        Validate.isTrue(sourceOptional.isPresent(), "dataSource not found:" + tableSchema);
        final DataSource dataSource = sourceOptional.get();

        final List<ColumnUsage> existColumnUsages = tableRelationRepository.listTableRelation(tableSchema);

        String sql = "SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`COLUMN_NAME`,`REFERENCED_TABLE_SCHEMA`,`REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = ? and `REFERENCED_COLUMN_NAME` IS NOT NULL";
        final List<Map<String, Object>> maps = sqlExecutor.sqlQuery(dataSource, sql, tableSchema);
        final List<ColumnUsage> thisColumnUsageList = maps.stream().map(RelationManagerService::convert2ColumnUsage).toList();

        thisColumnUsageList.forEach(thisUsage -> {
            if (!contains(existColumnUsages, thisUsage)) {
                tableRelationRepository.addTableRelation(thisUsage);
            }
        });
    }

    private boolean contains(List<ColumnUsage> columnUsages, ColumnUsage columnUsage) {
        if (columnUsages.isEmpty()) {
            return false;
        }
        return columnUsages.stream()
                .anyMatch(usage -> {
                    boolean equals = Objects.equals(usage.getTableSchema(), columnUsage.getTableSchema())
                            && Objects.equals(usage.getTableName(), columnUsage.getTableName())
                            && Objects.equals(usage.getColumnName(), columnUsage.getColumnName())
                            && Objects.equals(usage.getCondition(), columnUsage.getCondition())
                            && Objects.equals(usage.getReferencedTableSchema(), columnUsage.getReferencedTableSchema())
                            && Objects.equals(usage.getReferencedTableName(), columnUsage.getReferencedTableName())
                            && Objects.equals(usage.getReferencedColumnName(), columnUsage.getReferencedColumnName());
                    return equals;
                });
    }

    private static ColumnUsage convert2ColumnUsage(Map<String, Object> it) {
        final String schema = (String) it.get("TABLE_SCHEMA");
        final String tableName = (String) it.get("TABLE_NAME");
        final String columnName = (String) it.get("COLUMN_NAME");
        final String referencedTableSchema = (String) it.get("REFERENCED_TABLE_SCHEMA");
        final String referencedTableName = (String) it.get("REFERENCED_TABLE_NAME");
        final String referencedColumnName = (String) it.get("REFERENCED_COLUMN_NAME");

        final ColumnUsage usage = new ColumnUsage();
        usage.setTableSchema(schema);
        usage.setTableName(tableName);
        usage.setColumnName(columnName);
        usage.setCondition("");
        usage.setReferencedTableSchema(referencedTableSchema);
        usage.setReferencedTableName(referencedTableName);
        usage.setReferencedColumnName(referencedColumnName);
        usage.setRelationType(RelationType.ONE_TO_MANY.getValue());
        return usage;
    }

    private void checkParam(ColumnUsage columnUsage) {
        Validate.notNull(columnUsage, "dataSourceConfig can not be null");
        Validate.notBlank(columnUsage.getTableSchema(), "tableSchema can not be null");
        Validate.notBlank(columnUsage.getTableName(), "tableName can not be null");
        Validate.notBlank(columnUsage.getColumnName(), "columnName can not be null");
        Validate.notNull(columnUsage.getCondition(), "condition can not be null");
        Validate.notBlank(columnUsage.getReferencedTableSchema(), "referencedTableSchema can not be null");
        Validate.notBlank(columnUsage.getReferencedTableName(), "referencedTableName can not be null");
        Validate.notBlank(columnUsage.getReferencedColumnName(), "referencedColumnName can not be null");
        Validate.notNull(columnUsage.getRelationType(), "relationType can not be null");
    }

}
