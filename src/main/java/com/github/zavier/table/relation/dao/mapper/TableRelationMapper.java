package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.service.constant.RelationType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TableRelationMapper {

    private static final Logger log = LoggerFactory.getLogger(TableRelationMapper.class);

    private static final RowMapper<TableRelation> ROW_MAPPER = TableRelationMapper::mapRow;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<TableRelation> listAllTableRelation() {
        String sql = "SELECT * FROM table_relation";
        log.info("listAllTableRelation sql:{}", sql);
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<TableRelation> listTableRelation(String tableSchema) {
        String sql = "SELECT * FROM table_relation where table_schema = ?";
        log.info("listTableRelation sql:{}", sql);
        return jdbcTemplate.query(sql, ROW_MAPPER, tableSchema);
    }

    public void addTableRelation(TableRelation tableRelation) {
        String sql = "INSERT INTO table_relation (table_schema, table_name, column_name, condition_field, condition_operator, condition_value, referenced_table_schema, referenced_table_name, referenced_column_name, relation_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        log.info("addTableRelation sql:{}", sql);
        jdbcTemplate.update(sql,
                tableRelation.tableSchema(),
                tableRelation.tableName(),
                tableRelation.columnName(),
                tableRelation.conditionField(),
                tableRelation.conditionOperator(),
                tableRelation.conditionValue(),
                tableRelation.referencedTableSchema(),
                tableRelation.referencedTableName(),
                tableRelation.referencedColumnName(),
                tableRelation.relationType().getValue());
    }

    public void deleteTableRelation(Integer id) {
        String sql = "DELETE FROM table_relation WHERE id = ?";
        log.info("deleteTableRelation sql:{}", sql);
        jdbcTemplate.update(sql, id);
    }

    public void updateTableRelation(TableRelation tableRelation) {
        String sql = "UPDATE table_relation SET table_schema = ?, table_name = ?, column_name = ?, condition_field = ?, condition_operator = ?, condition_value = ?, referenced_table_schema = ?, referenced_table_name = ?, referenced_column_name = ?, relation_type = ? WHERE id = ?";
        log.info("updateTableRelation sql:{}", sql);
        final int i = jdbcTemplate.update(sql,
                tableRelation.tableSchema(),
                tableRelation.tableName(),
                tableRelation.columnName(),
                tableRelation.conditionField(),
                tableRelation.conditionOperator(),
                tableRelation.conditionValue(),
                tableRelation.referencedTableSchema(),
                tableRelation.referencedTableName(),
                tableRelation.referencedColumnName(),
                tableRelation.relationType().getValue(),
                tableRelation.id());
        if (i != 1) {
            throw new RuntimeException("更新表关系失败");
        }
    }

    public List<TableRelation> listTableRelationByTableName(String tableName) {
        String sql = "SELECT * FROM table_relation WHERE table_name = ?";
        log.info("listTableRelationByTableName sql:{}", sql);
        return jdbcTemplate.query(sql, ROW_MAPPER, tableName);
    }

    private static TableRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TableRelation(
                rs.getInt("id"),
                rs.getString("table_schema"),
                rs.getString("table_name"),
                rs.getString("column_name"),
                rs.getString("condition_field"),
                rs.getString("condition_operator"),
                rs.getString("condition_value"),
                rs.getString("referenced_table_schema"),
                rs.getString("referenced_table_name"),
                rs.getString("referenced_column_name"),
                RelationType.getRelationType(rs.getInt("relation_type"))
        );
    }
}
