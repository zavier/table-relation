package com.github.zavier.table.relation.integrate;

import com.github.zavier.table.relation.service.RelationManagerService;
import com.github.zavier.table.relation.service.abilty.DataSourceRegistry;
import com.github.zavier.table.relation.service.abilty.TableRelationRegistry;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RelationManagerServiceIntegrateTest {

    @Resource
    private RelationManagerService relationManagerService;

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
    void getTableRelationMermaidERDiagram() {
        final String erDiagram = relationManagerService.getTableRelationMermaidERDiagram("employees", "employees");
        String expected = """
                erDiagram
                  employees ||--o{ dept_emp : "emp_no → emp_no"
                  employees ||--o{ dept_manager : "emp_no → emp_no"
                  employees ||--o{ salaries : "emp_no → emp_no"
                  employees ||--o{ titles : "emp_no → emp_no"
                  dept_emp ||--o{ departments : "dept_no → dept_no"
                  dept_manager ||--o{ departments : "dept_no → dept_no"
                  dept_manager {
                      int emp_no\s
                      string dept_no\s
                      date from_date\s
                      date to_date\s
                  }
                  dept_emp {
                      int emp_no\s
                      string dept_no\s
                      date from_date\s
                      date to_date\s
                  }
                  departments {
                      string dept_no\s
                      string dept_name\s
                  }
                  employees {
                      int emp_no\s
                      date birth_date\s
                      string first_name\s
                      string last_name\s
                      enum gender\s
                      date hire_date\s
                  }
                  titles {
                      int emp_no\s
                      string title\s
                      date from_date\s
                      date to_date\s
                  }
                  salaries {
                      int emp_no\s
                      int salary\s
                      date from_date\s
                      date to_date\s
                  }
                """.strip();
        assertEquals(expected, erDiagram);
    }
}