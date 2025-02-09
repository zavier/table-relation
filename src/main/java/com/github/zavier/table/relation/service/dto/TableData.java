package com.github.zavier.table.relation.service.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableData {

    // table -> list<col, value>
    Map<String, List<Map<String, Object>>> tableData;

    // table -> col -> comment
    Map<String, Map<String, String>> comments;


    public TableData() {
        tableData = new HashMap<>();
        comments = new HashMap<>();
    }

    public TableData(Map<String, List<Map<String, Object>>> tableData, Map<String, Map<String, String>> comments) {
        this.tableData = tableData;
        this.comments = comments;
    }

    public Map<String, List<Map<String, Object>>> getTableData() {
        return tableData;
    }

    public Map<String, Map<String, String>> getComments() {
        return comments;
    }
}
