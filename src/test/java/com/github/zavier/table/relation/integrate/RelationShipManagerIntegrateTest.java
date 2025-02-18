package com.github.zavier.table.relation.integrate;

import com.github.zavier.table.relation.manager.RelationShipManager;
import com.github.zavier.table.relation.service.DataSourceRegistry;
import com.github.zavier.table.relation.service.TableRelationRegistry;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RelationShipManagerIntegrateTest {

    @Resource
    private RelationShipManager relationShipManager;

    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

//    @BeforeEach
    public void initData() {
        dataSourceRegistry.addDataSource("employees", "jdbc:mysql://localhost:3306/employees", "root", "mysqlroot");

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
    @Disabled
    void getTableRelationMermaidERDiagram() {
        final String erDiagram = relationShipManager.getTableRelationMermaidERDiagram("employees", "employees", true);
        String expected = """
                erDiagram
                  employees ||--o{ dept_emp : "emp_no → emp_no"
                  employees ||--o{ dept_manager : "emp_no → emp_no"
                  employees ||--o{ salaries : "emp_no → emp_no"
                  employees ||--o{ titles : "emp_no → emp_no"
                  dept_emp ||--o{ departments : "dept_no → dept_no"
                  dept_manager ||--o{ departments : "dept_no → dept_no"
                  dept_manager {
                      emp_no int
                      dept_no string
                      from_date date
                      to_date date
                  }
                  dept_emp {
                      emp_no int
                      dept_no string
                      from_date date
                      to_date date
                  }
                  departments {
                      dept_no string
                      dept_name string
                  }
                  employees {
                      emp_no int
                      birth_date date
                      first_name string
                      last_name string
                      gender enum
                      hire_date date
                  }
                  titles {
                      emp_no int
                      title string
                      from_date date
                      to_date date
                  }
                  salaries {
                      emp_no int
                      salary int
                      from_date date
                      to_date date
                  }
                """.strip();
        assertEquals(expected, erDiagram);
    }
}