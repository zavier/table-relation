package com.github.zavier.table.relation.service.domain;

import com.github.zavier.table.relation.service.dto.Condition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConditionFilterTest {

    @Test
    void parseLegacySimpleCondition() {
        ConditionFilter condition = ConditionFilter.parseLegacy("type=1");
        assertEquals("type", condition.field());
        assertEquals("=", condition.operator());
        assertEquals("1", condition.value());
    }

    @Test
    void parseLegacyQuotedValue() {
        ConditionFilter condition = ConditionFilter.parseLegacy(" status = 'NEW' ");
        assertEquals("status", condition.field());
        assertEquals("=", condition.operator());
        assertEquals("NEW", condition.value());
    }

    @Test
    void parseLegacyComparisonOperator() {
        ConditionFilter condition = ConditionFilter.parseLegacy("amount >= 100");
        assertEquals("amount", condition.field());
        assertEquals(">=", condition.operator());
        assertEquals("100", condition.value());
    }

    @Test
    void parseLegacyLike() {
        ConditionFilter condition = ConditionFilter.parseLegacy("name like '%a%'");
        assertEquals("name", condition.field());
        assertEquals("LIKE", condition.operator());
        assertEquals("%a%", condition.value());
    }

    @Test
    void parseLegacyUnsupportedConditionReturnsNull() {
        assertNull(ConditionFilter.parseLegacy(null));
        assertNull(ConditionFilter.parseLegacy(" "));
        assertNull(ConditionFilter.parseLegacy("type IN (1,2)"));
        assertNull(ConditionFilter.parseLegacy("custom raw sql here"));
        assertNull(ConditionFilter.parseLegacy("type="));
    }

    @Test
    void ofRequiresAllFields() {
        assertNull(ConditionFilter.of(null, "=", "1"));
        assertNull(ConditionFilter.of("type", " ", "1"));
        assertNull(ConditionFilter.of("type", "=", ""));
        assertEquals("type", ConditionFilter.of(" type ", "= ", "1").field());
    }

    @Test
    void toConditionAndDisplay() {
        ConditionFilter filter = new ConditionFilter("type", "=", "1");
        Condition condition = filter.toCondition();
        assertEquals("type", condition.getField());
        assertEquals("=", condition.getOperator());
        assertEquals("1", condition.getValue());
        assertEquals("type = 1", filter.display());
    }
}
