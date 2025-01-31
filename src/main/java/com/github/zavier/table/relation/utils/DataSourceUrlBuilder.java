package com.github.zavier.table.relation.utils;

import com.github.zavier.table.relation.service.dto.DataSourceConfig;

public class DataSourceUrlBuilder {

    public static String buildUrlForMySql(DataSourceConfig dataSourceConfig) {
        return String.format("jdbc:mysql://%s:%d/%s?allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false", dataSourceConfig.getHost(), dataSourceConfig.getPort(), dataSourceConfig.getDatabase());
    }

}
