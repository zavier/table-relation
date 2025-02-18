package com.github.zavier.table.relation.manager.ai;

import com.github.zavier.table.relation.service.dto.ColumnUsage;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TableRelationAnalyzer {
    @Resource
    private ChatClient chatClient;

    public List<ColumnUsage> analyzeTableRelation(String erDiagram) {
        String prompt = """
                根据如下的mermaid格式的【ER图】信息，分析一下可能存在的外键关系，需要返回表名和字段名，以及关联的表名和字段名信息
                可以参数MySQL中的 `information_schema`的 `KEY_COLUMN_USAGE`表的信息，
                如果`order`订单表有个`order_no`订单编号，和`order_detail`订单明细表的 `order_no`订单编号是关联关系，
                那么可以返回 `"tableName":"order", "columnName":"order_no", "referencedTableName":"order_detail", "referencedColumnName":"order_no"`
                
                ER图: {erDiagram}
                """;
        final List<ColumnUsage> usageList = this.chatClient.prompt()
                .user(u -> u.text(prompt).params(Map.of("erDiagram", erDiagram)))
                .call()
                .entity(new ParameterizedTypeReference<List<ColumnUsage>>() {
                });
        return usageList;
    }
}
