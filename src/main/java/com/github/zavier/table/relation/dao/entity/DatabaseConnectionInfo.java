package com.github.zavier.table.relation.dao.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DatabaseConnectionInfo {
    private Integer id;
    private String db;
    private String host;
    private String username;
    private String password;
    private Integer port;
    private Date createdAt;
    private Date updatedAt;

}
