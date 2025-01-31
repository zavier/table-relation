CREATE TABLE IF NOT EXISTS table_relation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_schema TEXT,
    table_name TEXT,
    column_name TEXT,
    referenced_table_schema TEXT,
    referenced_table_name TEXT,
    referenced_column_name TEXT,
    relation_type INTEGER
);

CREATE TABLE IF NOT EXISTS database_connection_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    database TEXT,
    host TEXT,
    username TEXT,
    password TEXT,
    port INTEGER
);