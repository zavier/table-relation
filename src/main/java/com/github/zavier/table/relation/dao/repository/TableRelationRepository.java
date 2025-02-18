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
        columnUsage.setCondition(tableRelation.condition());
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
        final TableRelation tableRelation = new TableRelation(
                null,
                columnUsage.getTableSchema(),
                columnUsage.getTableName(),
                columnUsage.getColumnName(),
                columnUsage.getCondition(),
                columnUsage.getReferencedTableSchema(),
                columnUsage.getReferencedTableName(),
                columnUsage.getReferencedColumnName(),
                RelationType.getRelationType(columnUsage.getRelationType())
        );
        tableRelationMapper.addTableRelation(tableRelation);
    }

    public void deleteTableRelation(Integer id) {
        tableRelationMapper.deleteTableRelation(id);
    }

    public void updateTableRelation(ColumnUsage columnUsage) {
        final TableRelation tableRelation = new TableRelation(
                columnUsage.getId(),
                columnUsage.getTableSchema(),
                columnUsage.getTableName(),
                columnUsage.getColumnName(),
                columnUsage.getCondition(),
                columnUsage.getReferencedTableSchema(),
                columnUsage.getReferencedTableName(),
                columnUsage.getReferencedColumnName(),
                RelationType.getRelationType(columnUsage.getRelationType())
        );
        tableRelationMapper.updateTableRelation(tableRelation);
    }


}
