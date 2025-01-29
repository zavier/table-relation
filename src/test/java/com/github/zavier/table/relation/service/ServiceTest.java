package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.constant.RelationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceTest {

    @Test
    public void testGetTableRelation() {
        TableRelationRegistry relationRegistry = new TableRelationRegistry();
        Column col1 = new Column("employees", "dept_manager", "emp_no");
        Column col2 = new Column("employees", "employees", "emp_no");
        final ColumnRelation columnRelation = new ColumnRelation(col1, col2, RelationType.ONE_TO_MANY);
        relationRegistry.register(columnRelation);

        Map<Column, List<Column>> referenced = relationRegistry.getDirectReferenced("employees", "dept_manager");
        assertEquals(1, referenced.size());
        referenced.forEach((column, referencedColumns) -> {
            assertEquals(col1, column);
            assertEquals(1, referencedColumns.size());
            assertEquals(col2, referencedColumns.get(0));
        });


        Column col3 = new Column("employees", "dept_manager", "dept_no");
        Column col4 = new Column("employees", "departments", "dept_no");
        final ColumnRelation columnRelation2 = new ColumnRelation(col3, col4, RelationType.ONE_TO_MANY);
        relationRegistry.register(columnRelation2);

        referenced = relationRegistry.getDirectReferenced("employees", "dept_manager");
        assertEquals(2, referenced.size());

        final List<Column> columns = referenced.get(col3);
        assertNotNull(columns);
        assertEquals(1, columns.size());
        assertEquals(col4, columns.get(0));

    }

}
