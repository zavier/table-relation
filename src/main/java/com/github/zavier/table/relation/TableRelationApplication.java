package com.github.zavier.table.relation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.github.zavier.table.relation.dao.mapper")
public class TableRelationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TableRelationApplication.class, args);
	}

}
