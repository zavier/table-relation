package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.db.DatabaseType;
import com.github.zavier.table.relation.service.db.MySqlDatabaseAdapter;
import com.github.zavier.table.relation.service.db.PostgresDatabaseAdapter;
import com.github.zavier.table.relation.service.dto.Condition;
import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.dto.SqlWithParams;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryConditionTest {

    private final MySqlDatabaseAdapter mysqlAdapter = new MySqlDatabaseAdapter();
    private final PostgresDatabaseAdapter postgresAdapter = new PostgresDatabaseAdapter();

    @Test
    void buildSqlForMySql() {
        SqlWithParams sqlWithParams = buildCondition("emp_no", "=", "110022")
                .buildSql(mysqlAdapter, "employees");

        assertEquals("select * from `employees`.`dept_manager` where `emp_no` = ?", sqlWithParams.sql());
        assertEquals(List.of("110022"), sqlWithParams.params());
    }

    @Test
    void buildSqlForPostgres() {
        SqlWithParams sqlWithParams = buildCondition("emp_no", "=", "110022")
                .buildSql(postgresAdapter, "sales");

        assertEquals("select * from \"sales\".\"dept_manager\" where \"emp_no\" = ?", sqlWithParams.sql());
        assertEquals(List.of("110022"), sqlWithParams.params());
    }

    @Test
    void valueIsBoundAsParameterInsteadOfBeingConcatenated() {
        SqlWithParams sqlWithParams = buildCondition("name", "=", "x' OR '1'='1")
                .buildSql(postgresAdapter, "sales");

        assertEquals("select * from \"sales\".\"dept_manager\" where \"name\" = ?", sqlWithParams.sql());
        assertEquals(List.of("x' OR '1'='1"), sqlWithParams.params());
    }

    @Test
    void inOperatorExpandsPlaceholders() {
        SqlWithParams sqlWithParams = buildCondition("id", "in", List.of(1, 2, 3))
                .buildSql(mysqlAdapter, "employees");

        assertEquals("select * from `employees`.`dept_manager` where `id` IN (?, ?, ?)", sqlWithParams.sql());
        assertEquals(List.of(1, 2, 3), sqlWithParams.params());
    }

    @Test
    void emptyInDoesNotProduceInvalidSql() {
        SqlWithParams in = buildCondition("id", "IN", List.of()).buildSql(mysqlAdapter, "employees");
        assertEquals("select * from `employees`.`dept_manager` where 1 = 0", in.sql());
        assertTrue(in.params().isEmpty());

        SqlWithParams notIn = buildCondition("id", "NOT IN", List.of()).buildSql(mysqlAdapter, "employees");
        assertEquals("select * from `employees`.`dept_manager` where 1 = 1", notIn.sql());
        assertTrue(notIn.params().isEmpty());
    }

    @Test
    void unsupportedOperatorIsRejected() {
        QueryCondition queryCondition = buildCondition("id", "= 1 OR 1=1", 1);
        assertThrows(IllegalArgumentException.class,
                () -> queryCondition.buildSql(mysqlAdapter, "employees"));

        QueryCondition blankOperator = buildCondition("id", " ", 1);
        assertThrows(IllegalArgumentException.class,
                () -> blankOperator.buildSql(mysqlAdapter, "employees"));
    }

    @Test
    void inOperatorRequiresCollectionValue() {
        QueryCondition queryCondition = buildCondition("id", "IN", 1);
        assertThrows(IllegalArgumentException.class,
                () -> queryCondition.buildSql(mysqlAdapter, "employees"));
    }

    @Test
    void withAdditionalConditionKeepsOriginalUntouched() {
        QueryCondition queryCondition = buildCondition("emp_no", "=", "110022");
        queryCondition.setLogicNoDeleteCondition("status <> 'DELETED'");

        QueryCondition restricted = queryCondition.withAdditionalCondition(
                condition("status", "=", "PAID"));

        SqlWithParams originalSql = queryCondition.buildSql(mysqlAdapter, "employees");
        assertEquals("select * from `employees`.`dept_manager` where `emp_no` = ?"
                + " and status <> 'DELETED'", originalSql.sql());
        assertEquals(List.of("110022"), originalSql.params());

        SqlWithParams restrictedSql = restricted.buildSql(mysqlAdapter, "employees");
        assertEquals("select * from `employees`.`dept_manager` where `emp_no` = ? and `status` = ?"
                + " and status <> 'DELETED'", restrictedSql.sql());
        assertEquals(List.of("110022", "PAID"), restrictedSql.params());
    }

    @Test
    void databaseTypeFallsBackToMySql() {
        assertEquals(DatabaseType.MYSQL, DatabaseType.from(null));
        assertEquals(DatabaseType.MYSQL, DatabaseType.from(""));
        assertEquals(DatabaseType.MYSQL, DatabaseType.from("unknown"));
        assertEquals(DatabaseType.POSTGRESQL, DatabaseType.from("postgres"));
    }

    private QueryCondition buildCondition(String field, String operator, Object value) {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setSchema("employees");
        queryCondition.setTable("dept_manager");
        queryCondition.setConditions(List.of(condition(field, operator, value)));
        return queryCondition;
    }

    private Condition condition(String field, String operator, Object value) {
        final Condition condition = new Condition();
        condition.setField(field);
        condition.setOperator(operator);
        condition.setValue(value);
        return condition;
    }
}
