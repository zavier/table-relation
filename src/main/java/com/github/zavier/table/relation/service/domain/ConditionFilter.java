package com.github.zavier.table.relation.service.domain;

import com.github.zavier.table.relation.service.db.SqlOperators;
import com.github.zavier.table.relation.service.dto.Condition;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表关系上的结构化条件：字段 + 运算符 + 值。
 * <p>
 * 值为空表示该关系没有额外条件；{@code null} 与「无条件」等价。
 */
public record ConditionFilter(String field, String operator, String value) {

    private static final Pattern LEGACY_PATTERN = Pattern.compile(
            "^\\s*([A-Za-z0-9_.$`\"]+)\\s*(!=|<>|>=|<=|=|>|<|~~\\*|~~|!~~\\*|!~~|like)\\s*(.+?)\\s*$",
            Pattern.CASE_INSENSITIVE);

    public static ConditionFilter of(String field, String operator, String value) {
        if (StringUtils.isAnyBlank(field, operator, value)) {
            return null;
        }
        return new ConditionFilter(field.trim(), operator.trim(), value);
    }

    /**
     * 解析历史版本中手工填写的条件，如 {@code type=1}、{@code status = 'NEW'}。
     * 无法解析时返回 null，由调用方决定如何处理。
     */
    public static ConditionFilter parseLegacy(String rawCondition) {
        if (StringUtils.isBlank(rawCondition)) {
            return null;
        }
        Matcher matcher = LEGACY_PATTERN.matcher(rawCondition);
        if (!matcher.matches()) {
            return null;
        }
        String field = matcher.group(1);
        String operator = matcher.group(2);
        String value = unquote(matcher.group(3));
        // PostgreSQL 的 LIKE 写法
        if ("~~".equals(operator) || "~~*".equals(operator)) {
            operator = "LIKE";
        } else if ("!~~".equals(operator) || "!~~*".equals(operator)) {
            operator = "NOT LIKE";
        }
        if (!SqlOperators.isComparisonSupported(operator) || StringUtils.isBlank(value)) {
            return null;
        }
        return new ConditionFilter(field, SqlOperators.normalize(operator), value);
    }

    public Condition toCondition() {
        Condition condition = new Condition();
        condition.setField(field);
        condition.setOperator(operator);
        condition.setValue(value);
        return condition;
    }

    public String display() {
        return field + " " + operator + " " + value;
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                String unquoted = trimmed.substring(1, trimmed.length() - 1);
                String quote = String.valueOf(first);
                return unquoted.replace(quote + quote, quote);
            }
        }
        return trimmed;
    }
}
