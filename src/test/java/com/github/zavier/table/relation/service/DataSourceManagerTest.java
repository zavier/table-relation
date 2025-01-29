package com.github.zavier.table.relation.service;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataSourceManagerTest {

    private final DataSourceManager dataSourceManager = new DataSourceManager();

//    @Test
    public void test() {
        dataSourceManager.addDataSource("test", "jdbc:mysql://localhost:3306/test", "root", "mysqlroot");

        final Optional<DataSource> dataSourceOptional = dataSourceManager.getDataSource("test");
        assertTrue(dataSourceOptional.isPresent());

        final Optional<DataSource> sourceOptional = dataSourceManager.getDataSource("aa");
        assertFalse(sourceOptional.isPresent());
    }
}
