package com.github.zavier.table.relation.service.db;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 按数据库类型选择适配器。
 */
@Component
public class DatabaseAdapterRegistry {

    private final Map<DatabaseType, DatabaseAdapter> adapterMap = new EnumMap<>(DatabaseType.class);

    public DatabaseAdapterRegistry(List<DatabaseAdapter> adapters) {
        for (DatabaseAdapter adapter : adapters) {
            adapterMap.put(adapter.type(), adapter);
        }
    }

    public DatabaseAdapter get(DatabaseType type) {
        DatabaseType databaseType = type == null ? DatabaseType.MYSQL : type;
        DatabaseAdapter adapter = adapterMap.get(databaseType);
        if (adapter == null) {
            throw new IllegalStateException("No database adapter for type: " + databaseType);
        }
        return adapter;
    }
}
