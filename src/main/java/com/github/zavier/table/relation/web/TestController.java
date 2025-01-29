package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.dao.entity.TableRelation;
import com.github.zavier.table.relation.dao.mapper.TableRelationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Resource
    private TableRelationMapper tableRelationMapper;


    @GetMapping("/all")
    @ResponseBody
    public List<TableRelation> getAllTableRelations() {
        log.info("get all table relations");
        return tableRelationMapper.selectAll();
    }
}
