package com.github.zavier.table.relation.service.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryCondition {

    private String schema;
    private String table;
    private List<Condition> conditions;
    private List<String> customizeConditionSqlList;
    private String logicNoDeleteCondition;

    public static class SqlWithParams {
        private String sql;
        private List<Object> params;

        public SqlWithParams(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getParams() {
            return params;
        }
    }

    public SqlWithParams buildSql() {
        final String schema = this.getSchema();
        final String tableName = this.getTable();
        final List<Condition> conditionList = this.getConditions();

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("select * from ").append(schema).append(".").append(tableName);
        if (conditionList != null && !conditionList.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditionList.size(); i++) {
                final Condition condition = conditionList.get(i);
                final String column = condition.getField();
                final String operator = condition.getOperator();
                final Object value = condition.getValue();

                if ("IN".equalsIgnoreCase(operator) && value instanceof Collection<?>) {
                    Collection<?> collection = (Collection<?>) value;
                    sql.append(column).append(" in (");
                    for (Object obj : collection) {
                        sql.append("?,");
                        params.add(obj);
                    }
                    sql.setLength(sql.length() - 1);
                    sql.append(")");
                } else {
                    sql.append(column).append(" ").append(operator).append(" ?");
                    params.add(value);
                }

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
