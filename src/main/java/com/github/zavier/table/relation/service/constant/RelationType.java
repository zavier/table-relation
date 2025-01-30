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

    public static RelationType getRelationType(int value) {
        for (RelationType relationType : RelationType.values()) {
            if (relationType.getValue() == value) {
                return relationType;
            }
        }
        return null;
    }
}
