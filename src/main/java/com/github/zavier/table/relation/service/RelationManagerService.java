package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.dao.repository.TableRelationRepository;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RelationManagerService {

    private static final Logger log = LoggerFactory.getLogger(RelationManagerService.class);

    @Resource
    private TableRelationRepository tableRelationRepository;
    @Resource
    private Initializer initializer;


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

        refresh();
    }

    private void refresh() {
        initializer.refresh();
    }

    public void deleteColumnUsage(Integer id) {
        Validate.notNull(id, "id can not be null");
        tableRelationRepository.deleteTableRelation(id);
        refresh();
    }

    public void updateColumnUsage(ColumnUsage columnUsage) {
        checkParam(columnUsage);
        Validate.notNull(columnUsage.getId(), "id can not be null");

        tableRelationRepository.updateTableRelation(columnUsage);
        refresh();
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
