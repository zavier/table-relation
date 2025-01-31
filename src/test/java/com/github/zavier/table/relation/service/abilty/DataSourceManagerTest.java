package com.github.zavier.table.relation.service.abilty;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataSourceManagerTest {

    private final DataSourceRegistry dataSourceRegistry = new DataSourceRegistry();

//    @Test
    public void test() {
        dataSourceRegistry.addDataSource("test", "jdbc:mysql://localhost:3306/test", "root", "mysqlroot");

        final Optional<DataSource> dataSourceOptional = dataSourceRegistry.getDataSource("test");
        assertTrue(dataSourceOptional.isPresent());

        final Optional<DataSource> sourceOptional = dataSourceRegistry.getDataSource("aa");
        assertFalse(sourceOptional.isPresent());
    }
}
