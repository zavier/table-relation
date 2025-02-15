package com.github.zavier.table.relation.service;

import com.github.zavier.table.relation.service.domain.TableColumnInfo;
import com.github.zavier.table.relation.service.dto.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SqlGenerateService {

    private final ChatClient chatClient;

    private final DataQueryService dataQueryService;
    private final RelationManagerService relationManagerService;

    public SqlGenerateService(ChatClient.Builder chatClientBuilder,
                              DataQueryService dataQueryService,
                              RelationManagerService relationManagerService) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.dataQueryService = dataQueryService;
        this.relationManagerService = relationManagerService;
    }

    public String generateSql(String schema, String demand) {
        // 考虑使用多个表？
        final String useTable = findRelatedUseTable(schema, demand);

        final String erDiagram = relationManagerService.getTableRelationMermaidERDiagram(schema, useTable, true);

        return generateSqlByErDiagram(demand, erDiagram);
    }

    private String findRelatedUseTable(String schema, String demand) {
        final Result<List<TableColumnInfo>> tableColumnInfos = dataQueryService.getTableColumnInfos(schema);
        Validate.isTrue(tableColumnInfos.isSuccess(), "查询表信息失败");
        Validate.isTrue(!tableColumnInfos.getData().isEmpty(), "表信息不存在");

        // 生成表信息字符串
        final List<TableColumnInfo> data = tableColumnInfos.getData();
        final String tableStr = data.stream()
                .map(it -> {
                    final String comment = it.tableComment();
                    if (StringUtils.isNotBlank(comment)) {
                        return it.tableName() + "(" + comment + ")";
                    }
                    return it.tableName();
                }).collect(Collectors.joining(","));

        // 查询最可能使用的表 TODO 这里可以进行一下表名校验&重试
        return this.chatClient.prompt()
                .user(u -> u.text("""
                        根据如下用户的数据库表查询的【用户需求】，在【可选择的表名】选择如下表中最可能使用到的**一个**表是哪个？**只需要返回返回可能性最大的一个表名**
                        用户需求: {demand}
                        可选择的表名: {tables}
                        """).params(Map.of("demand", demand, "tables", tableStr)))
                .call()
                .entity(String.class);
    }

    private @Nullable String generateSqlByErDiagram(String demand, String erDiagram) {
        return this.chatClient.prompt()
                .user(u -> u.text("""
                        根据如下【mermaid格式的ER图】，为这个**MySQL**数据库表查询的【用户需求】,生成对应的查询SQL语句，只需要返回SQL语句
                        mermaid格式ER图: {erDiagram}
                        用户需求: {demand}
                        """).params(Map.of("demand", demand, "erDiagram", erDiagram)))
                .call()
                .entity(String.class);
    }
}
