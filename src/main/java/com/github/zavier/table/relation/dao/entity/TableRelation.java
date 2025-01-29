package com.github.zavier.table.relation.dao.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TableRelation {
    private Integer id;
    private String sourceDb;
    private String sourceTable;
    private String sourceColumn;
    private String targetDb;
    private String targetTable;
    private String targetColumn;
    private Byte relationType;
    private Date createdAt;
    private Date updatedAt;
}