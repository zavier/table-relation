package com.github.zavier.table.relation.dao.repository;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.dao.mapper.TableRelationMapper;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TableRelationRepository {

    @Resource
    private TableRelationMapper tableRelationMapper;

    public List<ColumnUsage> listAllTableRelation() {
        final List<TableRelation> tableRelations = tableRelationMapper.listAllTableRelation();
        return tableRelations.stream().map(TableRelationRepository::convert2ColumnUsage).toList();
    }

    private static @NotNull ColumnUsage convert2ColumnUsage(TableRelation tableRelation) {
        final ColumnUsage columnUsage = new ColumnUsage();
        columnUsage.setId(tableRelation.id());
        columnUsage.setTableSchema(tableRelation.tableSchema());
        columnUsage.setTableName(tableRelation.tableName());
        columnUsage.setColumnName(tableRelation.columnName());
        columnUsage.setConditionField(tableRelation.conditionField());
        columnUsage.setConditionOperator(tableRelation.conditionOperator());
        columnUsage.setConditionValue(tableRelation.conditionValue());
        columnUsage.setReferencedTableSchema(tableRelation.referencedTableSchema());
        columnUsage.setReferencedTableName(tableRelation.referencedTableName());
        columnUsage.setReferencedColumnName(tableRelation.referencedColumnName());
        columnUsage.setRelationType(tableRelation.relationType().getValue());
        return columnUsage;
    }

    public List<ColumnUsage> listTableRelation(String tableSchema) {
        final List<TableRelation> tableRelations = tableRelationMapper.listTableRelation(tableSchema);
        return tableRelations.stream().map(TableRelationRepository::convert2ColumnUsage).toList();
    }

    public void addTableRelation(ColumnUsage columnUsage) {
        tableRelationMapper.addTableRelation(toTableRelation(null, columnUsage));
    }

    public void deleteTableRelation(Integer id) {
        tableRelationMapper.deleteTableRelation(id);
    }

    public void updateTableRelation(ColumnUsage columnUsage) {
        tableRelationMapper.updateTableRelation(toTableRelation(columnUsage.getId(), columnUsage));
    }

    private TableRelation toTableRelation(Integer id, ColumnUsage columnUsage) {
        return new TableRelation(
                id,
                columnUsage.getTableSchema(),
                columnUsage.getTableName(),
                columnUsage.getColumnName(),
                columnUsage.getConditionField(),
                columnUsage.getConditionOperator(),
                columnUsage.getConditionValue(),
                columnUsage.getReferencedTableSchema(),
                columnUsage.getReferencedTableName(),
                columnUsage.getReferencedColumnName(),
                RelationType.getRelationType(columnUsage.getRelationType())
        );
    }
}
