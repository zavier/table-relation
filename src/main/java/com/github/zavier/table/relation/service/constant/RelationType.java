package com.github.zavier.table.relation.service.constant;

public enum RelationType {
    ONE_TO_ONE(1),
    ONE_TO_MANY(2),
    MANY_TO_MANY(3);

    private final int value;

    RelationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
