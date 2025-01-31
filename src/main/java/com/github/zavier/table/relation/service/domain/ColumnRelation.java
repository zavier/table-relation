package com.github.zavier.table.relation.service.domain;

import com.github.zavier.table.relation.service.constant.RelationType;

public record ColumnRelation(Column sourceColumn, Column referencedColumn, RelationType relationType) { }
