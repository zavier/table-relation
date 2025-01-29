package com.github.zavier.table.relation.service.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryCondition {

    private String schema;
    private String tableName;
    private List<Condition> conditionList;

    public String buildSql() {
        final String schema = this.getSchema();
        final String tableName = this.getTableName();
        final List<Condition> conditionList = this.getConditionList();

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(schema).append(".").append(tableName);
        if (conditionList != null && !conditionList.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditionList.size(); i++) {
                final Condition condition = conditionList.get(i);
                final String column = condition.getColumn();
                final String operator = condition.getOperator();
                final Object value = condition.getValue();
                // TODO SQL注入
                sql.append(column).append(" ").append(operator).append(" ").append(convertSqlValue(value));
                if (i < conditionList.size() - 1) {
                    sql.append(" and ");
                }
            }
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList) {
        this.conditionList = conditionList;
    }

    @Override
    public String toString() {
        return "QueryCondition{" +
                "schema='" + schema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", conditionList=" + conditionList +
                '}';
    }
}
