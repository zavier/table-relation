package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.db.DatabaseType;
import com.github.zavier.table.relation.service.db.MySqlDatabaseAdapter;
import com.github.zavier.table.relation.service.db.PostgresDatabaseAdapter;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryConditionTest {

    private final MySqlDatabaseAdapter mysqlAdapter = new MySqlDatabaseAdapter();
    private final PostgresDatabaseAdapter postgresAdapter = new PostgresDatabaseAdapter();

    @Test
    void buildSqlForMySql() {
        final String sql = buildCondition().buildSql(mysqlAdapter, "employees");
        assertEquals("select * from `employees`.`dept_manager` where `emp_no` = '110022'", sql);
    }

    @Test
    void buildSqlForPostgres() {
        final String sql = buildCondition().buildSql(postgresAdapter, "sales");
        assertEquals("select * from \"sales\".\"dept_manager\" where \"emp_no\" = '110022'", sql);
    }

    @Test
    void buildSqlEscapesStringValue() {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("sales");
        queryCondition.setTable("customers");
        Condition condition = new Condition();
        condition.setField("name");
        condition.setOperator("=");
        condition.setValue("O'Reilly");
        queryCondition.setConditions(List.of(condition));

        assertEquals("select * from \"sales\".\"customers\" where \"name\" = 'O''Reilly'",
                queryCondition.buildSql(postgresAdapter, "sales"));
    }

    @Test
    void buildSqlAppendsCustomizeAndLogicDeleteConditions() {
        QueryCondition queryCondition = buildCondition();
        queryCondition.addCustomizeConditionSql("deleted = 0");
        queryCondition.setLogicNoDeleteCondition("status <> 'DELETED'");

        assertEquals("select * from `employees`.`dept_manager` where `emp_no` = '110022'"
                        + " and deleted = 0 and status <> 'DELETED'",
                queryCondition.buildSql(mysqlAdapter, "employees"));
    }

    @Test
    void databaseTypeFallsBackToMySql() {
        assertEquals(DatabaseType.MYSQL, DatabaseType.from(null));
        assertEquals(DatabaseType.MYSQL, DatabaseType.from(""));
        assertEquals(DatabaseType.MYSQL, DatabaseType.from("unknown"));
        assertEquals(DatabaseType.POSTGRESQL, DatabaseType.from("postgres"));
    }

    private QueryCondition buildCondition() {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("employees");
        queryCondition.setTable("dept_manager");

        final Condition condition = new Condition();
        condition.setField("emp_no");
        condition.setOperator("=");
        condition.setValue("110022");
        queryCondition.setConditions(List.of(condition));
        return queryCondition;
    }
}
