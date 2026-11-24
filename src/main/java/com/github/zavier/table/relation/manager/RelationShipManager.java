package com.github.zavier.table.relation.manager;

import com.github.zavier.table.relation.dao.repository.TableRelationRepository;
import com.github.zavier.table.relation.manager.ai.TableRelationAnalyzer;
import com.github.zavier.table.relation.service.DataSourceRegistry;
import com.github.zavier.table.relation.service.constant.RelationType;
import com.github.zavier.table.relation.service.db.DataSourceContext;
import com.github.zavier.table.relation.service.db.SqlOperators;
import com.github.zavier.table.relation.service.TableRelationRegistry;
import com.github.zavier.table.relation.service.domain.ColumnInfo;
import com.github.zavier.table.relation.service.dto.ColumnUsage;
import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.EntityRelationShip;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class RelationShipManager {

    private static final Logger log = LoggerFactory.getLogger(RelationShipManager.class);

    @Resource
    private TableRelationRepository tableRelationRepository;
    @Resource
    private Initializer initializer;
    @Resource
    private DataSourceRegistry dataSourceRegistry;
    @Resource
    private TableRelationRegistry tableRelationRegistry;
    @Resource
    private TableRelationAnalyzer tableRelationAnalyzer;

    @Value("${analyze.column.usage.auto}")
    private boolean isAutoAnalyze;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public List<ColumnUsage> listAllColumnUsage() {
        return tableRelationRepository.listAllTableRelation();
    }

    public void addColumnUsage(ColumnUsage columnUsage) {
        checkParam(columnUsage);
        // 是否已存在
        final List<ColumnUsage> columnUsages = listAllColumnUsage();
        if (!columnUsages.isEmpty()) {
            final boolean exist = columnUsages.stream().anyMatch(it -> isSameUsage(it, columnUsage));
            Validate.isTrue(!exist, "table relation already exists");
        }

        tableRelationRepository.addTableRelation(columnUsage);

        refreshRegistry();
    }

    private void batchAddColumnUsage(List<ColumnUsage> columnUsages) {
        // 是否已存在
        final List<ColumnUsage> existAllColumnUsageList = listAllColumnUsage();
        if (!existAllColumnUsageList.isEmpty()) {
            for (ColumnUsage columnUsage : columnUsages) {
                try {
                    checkParam(columnUsage);
                    final boolean exist = existAllColumnUsageList.stream()
                            .anyMatch(it -> isSameUsage(it, columnUsage));
                    if (exist) {
                        log.warn("table relation already exists, skip it : {}", columnUsage);
                        continue;
                    }
                    tableRelationRepository.addTableRelation(columnUsage);
                } catch (Exception e) {
                    log.error("Failed to add table relation:{}", columnUsage, e);
                }
            }
        }
        refreshRegistry();
    }

    public void deleteColumnUsage(Integer id) {
        Validate.notNull(id, "id can not be null");
        tableRelationRepository.deleteTableRelation(id);

        refreshRegistry();
    }

    public void refreshColumnUsage(ColumnUsage columnUsage) {
        checkParam(columnUsage);
        Validate.notNull(columnUsage.getId(), "id can not be null");

        tableRelationRepository.updateTableRelation(columnUsage);

        refreshRegistry();
    }

    private void refreshRegistry() {
        // 刷新注册表
        initializer.refreshTableRelation();
    }

    public void refreshColumnUsage(String tableSchema) {
        final DataSourceContext context = dataSourceRegistry.getContext(tableSchema)
                .orElseThrow(() -> new IllegalArgumentException("dataSource not found:" + tableSchema));

        final List<ColumnUsage> existColumnUsages = tableRelationRepository.listTableRelation(tableSchema);

        final List<ColumnUsage> thisColumnUsageList = context.adapter().listForeignKeys(context);

        if (!thisColumnUsageList.isEmpty()) {
            thisColumnUsageList.forEach(thisUsage -> {
                if (!contains(existColumnUsages, thisUsage)) {
                    tableRelationRepository.addTableRelation(thisUsage);
                }
            });
        } else {
            executorService.execute(() -> analyzeByLlmAndSaveResult(tableSchema));
        }
    }

    private void analyzeByLlmAndSaveResult(String schema) {
        if (!isAutoAnalyze) {
            return;
        }
        // 如果开启大模型自动分析，则会分析后进行保存
        try {
            log.info("auto analyze table relation for schema:{}", schema);
            final String tableMermaidERDiagram = this.getTableMermaidERDiagram(schema);
            final List<ColumnUsage> columnUsages = tableRelationAnalyzer.analyzeTableRelation(tableMermaidERDiagram);
            if (columnUsages == null) {
                log.warn("analyze table relation result is empty");
                return;
            }
            log.info("analyze columnUsages result size:{} data:{}", columnUsages.size(), columnUsages);
            // 数据预处理一下
            columnUsages.forEach(it -> {
                it.setId(null);
                it.setConditionField(null);
                it.setConditionOperator(null);
                it.setConditionValue(null);
                it.setTableSchema(schema);
                it.setReferencedTableSchema(schema);
                // default
                it.setRelationType(RelationType.ONE_TO_MANY.getValue());
            });
            batchAddColumnUsage(columnUsages);
        } catch (Exception e) {
            log.error("analyze table relation error", e);
        }
    }

    public String getTableRelationMermaidERDiagram(String schema, String tableName, Boolean needTableInfo) {
        final List<EntityRelationShip> allReferenced = tableRelationRegistry.getAllReferenced(schema, tableName);

        final Set<String> schemaSet = allReferenced.stream()
                .map(it -> List.of(it.sourceSchema(), it.targetSchema()))
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        // 如果此时关系未维护，则使用当前schema
        if (schemaSet.isEmpty()) {
            schemaSet.add(schema);
        }

        boolean multiSchema = schemaSet.size() > 1;

        String head = "erDiagram";
        String template = "  \"%s\" ||--o{ \"%s\" : \"%s\"";
        final StringBuilder builder = new StringBuilder(head);
        // 关系
        Set<String> tables = new HashSet<>();
        for (EntityRelationShip entityRelationship : allReferenced) {
            String sourceTable = multiSchema ? entityRelationship.sourceTableFullPath() : entityRelationship.sourceTable();
            String targetTable = multiSchema ? entityRelationship.targetTableFullPath() : entityRelationship.targetTable();

            tables.add(sourceTable);
            tables.add(targetTable);

            final String format = String.format(template, sourceTable, targetTable, entityRelationship.label());
            builder.append("\n").append(format);
        }

        // 表关系不为空，同时选择不需要表信息，才不展示，否则兜底展示表信息
        if (!needTableInfo && !tables.isEmpty()) {
            return builder.toString();
        }

        // 表信息
        for (String schemaName : schemaSet) {
            final List<TableColumnInfo> schemaAllTableInfo = getSchemaAllTableInfo(schemaName);
            for (TableColumnInfo tableColumnInfo : schemaAllTableInfo) {
                if (!needGenerate(tableColumnInfo, tables, tableName, multiSchema)) {
                    continue;
                }

                String showTableName = multiSchema ? tableColumnInfo.tableNameFullPath() : tableColumnInfo.tableName();
                appendTableErDiagramInfo(tableColumnInfo, builder, showTableName);
            }
        }

        return builder.toString();
    }

    public String getTableMermaidERDiagram(String schemaName) {
        final List<TableColumnInfo> schemaAllTableInfo = getSchemaAllTableInfo(schemaName);
        String head = "erDiagram";
        final StringBuilder builder = new StringBuilder(head);
        for (TableColumnInfo tableColumnInfo : schemaAllTableInfo) {
            String showTableName = tableColumnInfo.tableName();
            appendTableErDiagramInfo(tableColumnInfo, builder, showTableName);
        }
        return builder.toString();
    }

    private static void appendTableErDiagramInfo(TableColumnInfo tableColumnInfo, StringBuilder builder, String showTableName) {
        builder.append("\n").append("  \"").append(showTableName).append("\" ").append("{");
        for (ColumnInfo columnInfo : tableColumnInfo.columns()) {
            builder.append("\n")
                    .append("      ")
                    .append(columnInfo.columnName())
                    .append(" ")
                    .append(columnInfo.columnType());
            if (StringUtils.isNotBlank(columnInfo.columnComment())) {
                builder.append(" ")
                        .append("\"")
                        .append(columnInfo.columnComment())
                        .append("\"");
            }
        }
        builder.append("\n").append("  }");
    }

    private static boolean needGenerate(TableColumnInfo tableColumnInfo,
                                        Set<String> tables,
                                        String selectedTableName,
                                        boolean multiSchema) {
        // 如果没有关联表，则只展示选择的表信息即可
        if (tables.isEmpty()) {
            return tableColumnInfo.tableName().equals(selectedTableName);
        }
        // 否则按找关系筛选
        String showTableName = multiSchema ? tableColumnInfo.tableNameFullPath() : tableColumnInfo.tableName();
        return tables.contains(showTableName);
    }

    private List<TableColumnInfo> getSchemaAllTableInfo(String schema) {
        final DataSourceContext context = dataSourceRegistry.getContext(schema)
                .orElseThrow(() -> new IllegalArgumentException("dataSource not found:" + schema));
        return context.adapter().listTableColumns(context);
    }

    private boolean contains(List<ColumnUsage> columnUsages, ColumnUsage columnUsage) {
        if (columnUsages.isEmpty()) {
            return false;
        }
        return columnUsages.stream().anyMatch(usage -> isSameUsage(usage, columnUsage));
    }

    private static boolean isSameUsage(ColumnUsage left, ColumnUsage right) {
        return Objects.equals(left.getTableSchema(), right.getTableSchema())
                && Objects.equals(left.getTableName(), right.getTableName())
                && Objects.equals(left.getColumnName(), right.getColumnName())
                && Objects.equals(left.getConditionField(), right.getConditionField())
                && Objects.equals(left.getConditionOperator(), right.getConditionOperator())
                && Objects.equals(left.getConditionValue(), right.getConditionValue())
                && Objects.equals(left.getReferencedTableSchema(), right.getReferencedTableSchema())
                && Objects.equals(left.getReferencedTableName(), right.getReferencedTableName())
                && Objects.equals(left.getReferencedColumnName(), right.getReferencedColumnName());
    }

    private void checkParam(ColumnUsage columnUsage) {
        Validate.notNull(columnUsage, "dataSourceConfig can not be null");
        Validate.notBlank(columnUsage.getTableSchema(), "tableSchema can not be null");
        Validate.notBlank(columnUsage.getTableName(), "tableName can not be null");
        Validate.notBlank(columnUsage.getColumnName(), "columnName can not be null");
        Validate.notBlank(columnUsage.getReferencedTableSchema(), "referencedTableSchema can not be null");
        Validate.notBlank(columnUsage.getReferencedTableName(), "referencedTableName can not be null");
        Validate.notBlank(columnUsage.getReferencedColumnName(), "referencedColumnName can not be null");
        Validate.notNull(columnUsage.getRelationType(), "relationType can not be null");
        validateCondition(columnUsage);
    }

    /**
     * 条件要么完全不填，要么字段 / 运算符 / 值同时填写；运算符限制在与单值比较的白名单内。
     */
    private void validateCondition(ColumnUsage columnUsage) {
        String field = columnUsage.getConditionField();
        String operator = columnUsage.getConditionOperator();
        String value = columnUsage.getConditionValue();

        boolean hasAny = !StringUtils.isAllBlank(field, operator, value);
        if (!hasAny) {
            columnUsage.setConditionField(null);
            columnUsage.setConditionOperator(null);
            columnUsage.setConditionValue(null);
            return;
        }
        Validate.isTrue(StringUtils.isNoneBlank(field, operator, value),
                "condition field, operator and value must be filled together");
        Validate.isTrue(SqlOperators.isComparisonSupported(operator),
                "Unsupported condition operator: " + operator);
        columnUsage.setConditionOperator(SqlOperators.normalize(operator));
    }

}
