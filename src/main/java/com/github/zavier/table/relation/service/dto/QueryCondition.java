package com.github.zavier.table.relation.service.dto;

import com.github.zavier.table.relation.service.db.DatabaseAdapter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class QueryCondition {

    /**
     * 支持的运算符白名单，未在列表中的运算符直接拒绝，避免运算符位置被注入。
     */
    private static final Set<String> SUPPORTED_OPERATORS = Set.of(
            "=", "!=", "<>", ">", ">=", "<", "<=", "LIKE", "NOT LIKE", "IN", "NOT IN");

    private String schema;
    private String table;
    private List<Condition> conditions;
    private List<String> customizeConditionSqlList;
    private String logicNoDeleteCondition;

    /**
     * 生成参数化 SQL：条件值统一使用 {@code ?} 占位符，由 JDBC 绑定。
     * <p>
     * 表关系上的「关联条件」（{@link #customizeConditionSqlList}）与
     * {@code logic.no.delete.condition} 是用户显式维护的 SQL 片段，仍原样拼接，
     * 属于可信配置，不在参数化范围内。
     */
    public SqlWithParams buildSql(DatabaseAdapter adapter, String physicalSchema) {
        final String tableName = this.getTable();
        final List<Condition> conditionList = this.getConditions();

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("select * from ").append(adapter.tableRef(physicalSchema, tableName));
        if (conditionList != null && !conditionList.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditionList.size(); i++) {
                final Condition condition = conditionList.get(i);
                appendCondition(sql, params, adapter.quoteIdentifier(condition.getField()),
                        condition.getOperator(), condition.getValue());
                if (i < conditionList.size() - 1) {
                    sql.append(" and ");
                }
            }
        }
        if (customizeConditionSqlList != null && !customizeConditionSqlList.isEmpty()) {
            customizeConditionSqlList.forEach(customizeSql -> {
                sql.append(" and ").append(customizeSql);
            });
        }
        if (StringUtils.isNotBlank(logicNoDeleteCondition)) {
            sql.append(" and ").append(logicNoDeleteCondition);
        }
        return new SqlWithParams(sql.toString(), params);
    }

    private void appendCondition(StringBuilder sql, List<Object> params,
                                 String quotedColumn, String rawOperator, Object value) {
        final String operator = normalizeOperator(rawOperator);
        if ("IN".equals(operator) || "NOT IN".equals(operator)) {
            final List<Object> values = toList(value, operator);
            if (values.isEmpty()) {
                // 空集合时避免生成非法的 IN ()，用恒假 / 恒真表达式代替
                sql.append("1 = ").append("IN".equals(operator) ? "0" : "1");
                return;
            }
            sql.append(quotedColumn).append(" ").append(operator).append(" (")
                    .append(String.join(", ", Collections.nCopies(values.size(), "?")))
                    .append(")");
            params.addAll(values);
            return;
        }
        sql.append(quotedColumn).append(" ").append(operator).append(" ?");
        params.add(value);
    }

    private String normalizeOperator(String rawOperator) {
        if (StringUtils.isBlank(rawOperator)) {
            throw new IllegalArgumentException("operator can not be blank");
        }
        final String operator = rawOperator.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_OPERATORS.contains(operator)) {
            throw new IllegalArgumentException("Unsupported operator: " + rawOperator);
        }
        return operator;
    }

    private List<Object> toList(Object value, String operator) {
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(Array.get(value, i));
            }
            return values;
        }
        throw new IllegalArgumentException("Operator " + operator + " requires a collection value");
    }


    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void addCustomizeConditionSql(String customizeConditionSql) {
        if (this.customizeConditionSqlList == null) {
            this.customizeConditionSqlList = new ArrayList<>();
        }
        this.customizeConditionSqlList.add(customizeConditionSql);
    }

    public void setLogicNoDeleteCondition(String logicNoDeleteCondition) {
        this.logicNoDeleteCondition = logicNoDeleteCondition;
    }

    @Override
    public String toString() {
        return "QueryCondition{" +
                "schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", conditions=" + conditions +
                ", customizeConditionSqlList=" + customizeConditionSqlList +
                ", logicNoDeleteCondition='" + logicNoDeleteCondition + '\'' +
                '}';
    }
}
