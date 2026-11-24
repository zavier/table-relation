package com.github.zavier.table.relation.service.db;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * SQL 运算符白名单，供查询条件与表关系条件共用。
 */
public final class SqlOperators {

    private static final Set<String> SUPPORTED = Set.of(
            "=", "!=", "<>", ">", ">=", "<", "<=", "LIKE", "NOT LIKE", "IN", "NOT IN");

    /**
     * 表关系条件只允许与单个值比较，不支持 IN / NOT IN。
     */
    private static final Set<String> COMPARISON = Set.of(
            "=", "!=", "<>", ">", ">=", "<", "<=", "LIKE", "NOT LIKE");

    private SqlOperators() {
    }

    public static String normalize(String operator) {
        if (StringUtils.isBlank(operator)) {
            throw new IllegalArgumentException("operator can not be blank");
        }
        String normalized = operator.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        return normalized;
    }

    public static boolean isSupported(String operator) {
        return StringUtils.isNotBlank(operator)
                && SUPPORTED.contains(operator.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean isComparisonSupported(String operator) {
        return StringUtils.isNotBlank(operator)
                && COMPARISON.contains(operator.trim().toUpperCase(Locale.ROOT));
    }
}
