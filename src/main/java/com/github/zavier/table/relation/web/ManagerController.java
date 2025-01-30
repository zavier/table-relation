package com.github.zavier.table.relation.web;

import com.github.zavier.table.relation.service.Initializer;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/table")
public class ManagerController {

    @Resource
    private Initializer initializer;

    @GetMapping("/refresh")
    public String refresh() {
        initializer.refresh();
        return "success";
    }
}
