package com.github.zavier.table.relation.service.abilty;

import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import com.github.zavier.table.relation.service.dto.EntityRelationShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class TableRelationRegistryTest {

    private TableRelationRegistry tableRelationRegistry = new TableRelationRegistry();

    @BeforeEach
    public void initData() {
        Column col1 = new Column("employees", "dept_manager", "emp_no", "");
        Column col2 = new Column("employees", "employees", "emp_no", "");
        final ColumnRelation columnRelation = new ColumnRelation(col1, col2, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation);

        Column col3 = new Column("employees", "dept_manager", "dept_no", "");
        Column col4 = new Column("employees", "departments", "dept_no", "");
        final ColumnRelation columnRelation2 = new ColumnRelation(col3, col4, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation2);


        Column col5 = new Column("employees", "dept_emp", "emp_no", "");
        Column col6 = new Column("employees", "employees", "emp_no", "");
        final ColumnRelation columnRelation3 = new ColumnRelation(col5, col6, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation3);

        Column col7 = new Column("employees", "dept_emp", "dept_no", "");
        Column col8 = new Column("employees", "departments", "dept_no", "");
        final ColumnRelation columnRelation4 = new ColumnRelation(col7, col8, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation4);

        Column col9 = new Column("employees", "titles", "emp_no", "");
        Column col10 = new Column("employees", "employees", "emp_no", "");
        final ColumnRelation columnRelation5 = new ColumnRelation(col9, col10, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation5);

        Column col11 = new Column("employees", "salaries", "emp_no", "");
        Column col12 = new Column("employees", "employees", "emp_no", "");
        final ColumnRelation columnRelation6 = new ColumnRelation(col11, col12, RelationType.ONE_TO_MANY);
        tableRelationRegistry.register(columnRelation6);
    }

    @Test
    void getDirectReferenced() {
        final Map<Column, List<Column>> referenced = tableRelationRegistry.getDirectReferenced("employees", "dept_manager");
        System.out.println(referenced);
    }

    @Test
    void getAllReferenced() {
        final List<EntityRelationShip> allReferenced = tableRelationRegistry.getAllReferenced("employees", "dept_manager");
        System.out.println(allReferenced);
        String head = "erDiagram";
        String template = "  %s ||--o{ %s : \"%s\"";
        final StringBuilder builder = new StringBuilder(head);
        for (EntityRelationShip entityRelationship : allReferenced) {
            final String format = String.format(template, entityRelationship.sourceTable(), entityRelationship.targetTable(), entityRelationship.label());
            builder.append("\n")
                    .append(format);
        }
        System.out.println(builder.toString());
    }
}