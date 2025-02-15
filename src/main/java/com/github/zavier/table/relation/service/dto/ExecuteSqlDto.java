package com.github.zavier.table.relation.service.dto;

public class ExecuteSqlDto {
    private String sql;
    private String schema;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
