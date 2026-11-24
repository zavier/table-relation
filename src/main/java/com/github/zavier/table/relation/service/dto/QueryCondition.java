package com.github.zavier.table.relation.service.dto;

import com.github.zavier.table.relation.service.db.DatabaseAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class QueryCondition {

    private String schema;
    private String table;
    private List<Condition> conditions;
    private List<String> customizeConditionSqlList;
    private String logicNoDeleteCondition;

    public String buildSql(DatabaseAdapter adapter, String physicalSchema) {
        final String tableName = this.getTable();
        final List<Condition> conditionList = this.getConditions();

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(adapter.tableRef(physicalSchema, tableName));
        if (conditionList != null && !conditionList.isEmpty()) {
            sql.append(" where ");
            for (int i = 0; i < conditionList.size(); i++) {
                final Condition condition = conditionList.get(i);
                final String column = condition.getField();
                final String operator = condition.getOperator();
                final Object value = condition.getValue();
                // TODO SQL注入
                sql.append(adapter.quoteIdentifier(column)).append(" ").append(operator).append(" ").append(adapter.literal(value));
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
        return sql.toString();
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
