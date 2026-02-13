package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryConditionTest {

    @Test
    void buildSql() {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("employees");
        queryCondition.setTable("dept_manager");

        final Condition condition = new Condition();
        condition.setField("emp_no");
        condition.setOperator("=");
        condition.setValue("110022");
        queryCondition.setConditions(List.of(condition));

        final QueryCondition.SqlWithParams sqlWithParams = queryCondition.buildSql();
        assertEquals("select * from employees.dept_manager where emp_no = ?", sqlWithParams.getSql());
        assertEquals(1, sqlWithParams.getParams().size());
        assertEquals("110022", sqlWithParams.getParams().get(0));
    }

    @Test
    void buildSqlWithInCondition() {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("employees");
        queryCondition.setTable("dept_manager");

        final Condition condition = new Condition();
        condition.setField("emp_no");
        condition.setOperator("IN");
        condition.setValue(Arrays.asList(110022, 110039));
        queryCondition.setConditions(List.of(condition));

        final QueryCondition.SqlWithParams sqlWithParams = queryCondition.buildSql();
        assertEquals("select * from employees.dept_manager where emp_no in (?,?)", sqlWithParams.getSql());
        assertEquals(2, sqlWithParams.getParams().size());
        assertEquals(110022, sqlWithParams.getParams().get(0));
        assertEquals(110039, sqlWithParams.getParams().get(1));
    }
}