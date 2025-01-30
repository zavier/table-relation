package com.github.zavier.table.relation.service.dto;

import org.junit.jupiter.api.Test;

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

        final String sql = queryCondition.buildSql();
        assertEquals("select * from employees.dept_manager where emp_no = '110022'", sql);
    }
}