package com.github.zavier.table.relation.service.dto;

import java.util.List;

/**
 * 参数化查询语句：SQL 中使用 {@code ?} 占位符，值按顺序放在 params 中。
 */
public record SqlWithParams(String sql, List<Object> params) {
}
