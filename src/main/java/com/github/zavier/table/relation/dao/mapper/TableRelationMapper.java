package com.github.zavier.table.relation.dao.mapper;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.service.constant.RelationType;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TableRelationMapper {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<TableRelation> listAllTableRelation() {
        String sql = "SELECT * FROM table_relation";
        RowMapper<TableRelation> rowMapper = new RowMapper<TableRelation>() {
            @Override
            public TableRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TableRelation(
                        rs.getInt("id"),
                        rs.getString("table_schema"),
                        rs.getString("table_name"),
                        rs.getString("column_name"),
                        rs.getString("referenced_table_schema"),
                        rs.getString("referenced_table_name"),
                        rs.getString("referenced_column_name"),
                        RelationType.getRelationType(rs.getInt("relation_type"))
                );
            }
        };
        return jdbcTemplate.query(sql, rowMapper);
    }

}
