package com.github.zavier.table.relation.service.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryCondition {

    private String schema;
    private String table;
    private List<Condition> conditions;
    private List<String> customizeConditionSqlList;

    public String buildSql() {
        final String schema = this.getSchema();
        final String tableName = this.getTable();
        final List<Condition> conditionList = this.getConditions();

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(schema).append(".").append(tableName);
        if (conditionList != null && !conditionList.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditionList.size(); i++) {
                final Condition condition = conditionList.get(i);
                final String column = condition.getField();
                final String operator = condition.getOperator();
                final Object value = condition.getValue();
                // TODO SQL注入
                sql.append(column).append(" ").append(operator).append(" ").append(convertSqlValue(value));
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
        return sql.toString();
    }

    private String convertSqlValue(Object value) {
        if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            List<String> values = new ArrayList<>();
            for (Object obj : collection) {
                values.add(convertSqlValue(obj));
            }
            return "(" + String.join(",", values) + ")";
        } else if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value.toString() + "'";
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


    @Override
    public String toString() {
        return "QueryCondition{" +
                "schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", conditions=" + conditions +
                ", customizeConditionSqlList=" + customizeConditionSqlList +
                '}';
    }
}
