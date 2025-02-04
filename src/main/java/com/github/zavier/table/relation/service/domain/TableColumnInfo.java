package com.github.zavier.table.relation.service.domain;

import java.util.List;

public record TableColumnInfo(String schema, String tableName, String tableComment, List<ColumnInfo> columns) {
}

