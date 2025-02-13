package com.github.zavier.table.relation.service.dto;

public record EntityRelationShip(String sourceSchema, String sourceTable, String targetSchema, String targetTable, String label) {

    public String sourceTableFullPath() {
        return sourceSchema + "." + sourceTable;
    }

    public String targetTableFullPath() {
        return targetSchema + "." + targetTable;
    }
}
