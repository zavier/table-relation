package com.github.zavier.table.relation.integrate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zavier.table.relation.service.abilty.DataSourceRegistry;
import com.github.zavier.table.relation.service.abilty.TableRelationRegistry;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.domain.Column;
import com.github.zavier.table.relation.service.domain.ColumnRelation;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.query.DataQuery;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DataQueryIntegrateTest {

    @Resource
    private DataQuery dataQuery;

    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private TableRelationRegistry tableRelationRegistry;

    @Resource
    private ObjectMapper objectMapper;

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
    public void test() throws Exception {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("employees");
        queryCondition.setTable("dept_manager");

        final Condition condition = new Condition();
        condition.setField("emp_no");
        condition.setOperator("=");
        condition.setValue("110022");

        // select * from employees.employees where emp_no = '21710'
        queryCondition.setConditions(List.of(condition));

        System.out.println(objectMapper.writeValueAsString(queryCondition));
        final Map<String, List<Map<String, Object>>> query = dataQuery.query(queryCondition);
//        System.out.println(objectMapper.writeValueAsString(query));
        assertNotNull(query);
        assertEquals(1, query.get("dept_manager").size());
        assertEquals(1, query.get("dept_emp").size());
        assertEquals(1, query.get("employees").size());
        assertEquals(1, query.get("departments").size());

    }
}
