package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.dto.QueryCondition;
import com.github.zavier.table.relation.service.query.DataQuery;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Resource
    private DataQuery dataQuery;

    @PostMapping("/getRelaData")
    public Map<String, List<Map<String, Object>>> queryRelaData(@RequestBody QueryCondition queryCondition) {
        return dataQuery.query(queryCondition);
    }

}
