# PostgreSQL 数据源支持设计

> 版本：2026-11-24
> 状态：已实现，待合并
> 目标版本：0.0.3

## 1. 目标与范围

### 目标

1. 数据源同时支持 MySQL 与 PostgreSQL，两者可并存，互不影响。
2. 对 PG 数据源，下列能力与 MySQL 对齐：
   - 数据库 / 表 / 字段元数据（含表注释、字段注释）
   - 外键关系自动导入
   - 按表关系 BFS 关联查询、结果列注释
   - ER 图生成
   - INSERT SQL 导出
3. MySQL 现有行为零回归（接口、SQL 形态、关系数据不变）。

### 不在本期范围

| 项目 | 说明 |
|---|---|
| 同一 PG 数据库内多 schema 并存 | 一个数据源绑定一个物理 schema（默认 `public`） |
| 跨 schema 外键自动导入 | 检测到后跳过并打 warn 日志，不做错误映射 |
| 应用自身元数据库切到 PG | 应用仍使用 SQLite / MySQL 存 `table_relation`、`database_connection_info`（见 §6） |
| SQL 参数化改造 | 本期只做方言化；参数化在后续提交完成，见 `2026-11-24-parameterized-query.md` |
| 复合类型 / 几何类型深度支持 | `text[]` 归一化为 `array`，其余自定义类型按符号安全化后原样展示 |

## 2. 现状分析：MySQL 耦合点

代码库共 40 个 Java 文件，真正的数据库差异集中在 10 处：

| 位置 | 耦合内容 | 对 PG 的影响 |
|---|---|---|
| `utils/DataSourceUrlBuilder` | `jdbc:mysql://...` 硬编码 | 无法建立 PG 连接 |
| `manager/Initializer#refreshDataSource` | 注释「目前仅支持mysql」 | 同上 |
| `manager/DataSourceManager#testDataSourceConfigConn` | 直接拼 MySQL URL | PG「测试连接」必然失败 |
| `service/extpt/MySqlTableMetaInfoQuery` | `information_schema.COLUMNS` 的 `COLUMN_TYPE`/`COLUMN_COMMENT`；`KEY_COLUMN_USAGE` 取外键 | PG 没有 `COLUMN_TYPE`，注释在 `pg_description`，外键结构也不同 |
| `service/converter/TableInfoConverter#convertColumnType` | MySQL 类型前缀匹配 | PG 的 `timestamp with time zone`、`text[]` 含空格/方括号，会破坏 mermaid 语法 |
| `dto/QueryCondition#buildSql` | `schema.table` 不加引号、字符串单引号拼接 | PG 未加引号的标识符会被折叠为小写；布尔/时间字面量不通用 |
| `utils/SqlBuilder#generateInsertSql` | 反引号 + 未转义字符串 | PG 语法错误 |
| `manager/DataQueryManager`、`manager/RelationShipManager` | 直接注入 `MySqlTableMetaInfoQuery` | 无法按数据源类型选择实现 |
| `dao/entity/DatabaseConnectionInfo`、mapper、建表脚本 | 无「类型」「schema」字段 | 无法区分 MySQL / PG，也无法指定 PG schema |
| `service/DataSourceRegistry` | 只保存 `DataSource` | 调用方拿不到类型、数据库名、物理 schema |
| `service/SqlExecutor` 返回值 | 原样返回 JDBC 对象 | PG 的 `jsonb` 返回 `PGobject`、数组返回 `java.sql.Array`，Jackson 序列化会得到异常结构甚至报错 |

另外发现一个与 PG 无关的存量问题：`mysql_create_table.sql` 的 `table_relation` 建表语句缺少 `condition` 列，但 mapper 会读取它。用 MySQL 作为应用元数据库时会启动失败，顺手在本期修掉。

## 3. 总体方案

**把「数据库差异」收敛到一个适配器接口，业务代码只面向抽象。**

```
web  →  manager  →  DataQuery / DataSourceRegistry / RelationShipManager
                          │
                          ▼
                 DatabaseAdapterRegistry
                    ├── MySqlDatabaseAdapter
                    └── PostgresDatabaseAdapter
```

新增/改造的核心类型：

| 类型 | 职责 |
|---|---|
| `DatabaseType` | 枚举：`MYSQL`、`POSTGRESQL`，携带默认端口 |
| `DatabaseAdapter` | 单一类型数据库的全部差异：JDBC URL、标识符引用、字面量、类型归一化、元数据 SQL、结果值归一化 |
| `DataSourceContext` | 运行期数据源视图：注册 key、类型、数据库名、物理 schema、连接池、适配器 |
| `DatabaseAdapterRegistry` | `DatabaseType → DatabaseAdapter`，Spring 注入 `List<DatabaseAdapter>` 构建 |

### 关键接口

```java
public enum DatabaseType {
    MYSQL(3306),
    POSTGRESQL(5432);

    private final int defaultPort;

    // 兼容历史配置：null / 空 / 未知值一律回落到 MYSQL
    public static DatabaseType from(String value) { ... }
}

public interface DatabaseAdapter {
    DatabaseType type();

    String buildJdbcUrl(DataSourceConfig config);

    /** MySQL -> `t`，PG -> "t" */
    String quoteIdentifier(String identifier);

    /** 物理 schema + 表名的引用形式，null schema 时只引用表名 */
    default String tableRef(String physicalSchema, String tableName) { ... }

    /** 生成 SQL 字面量（字符串转义、布尔、时间、集合） */
    String literal(Object value);

    /** 原始列类型 -> 页面/ER 图使用的简单类型 */
    String normalizeColumnType(String rawColumnType);

    List<TableColumnInfo> listTableColumns(DataSourceContext ctx);

    TableColumnInfo describeTable(DataSourceContext ctx, String tableName);

    List<ColumnUsage> listForeignKeys(DataSourceContext ctx);

    /** JDBC 结果值归一化，PG 用于展开 jsonb / 数组 */
    default Map<String, Object> normalizeRow(Map<String, Object> row) {
        return row;
    }
}

public record DataSourceContext(
        String key,             // 应用内 "schema" 标识，即前端下拉框看到的值
        DatabaseType type,
        String database,        // JDBC 连接使用的数据库名
        String physicalSchema,  // MySQL = database；PG = schemaName，默认 public
        DataSource dataSource,
        DatabaseAdapter adapter) {
}
```

调用方不再关心具体数据库：

```java
DataSourceContext ctx = dataSourceRegistry.getContext(schema)
        .orElseThrow(() -> new RuntimeException("dataSource not found:" + schema));

String sql = queryCondition.buildSql(ctx.adapter(), ctx.physicalSchema());
```

## 4. 详细设计

### 4.1 数据源配置模型

`DataSourceConfig` / `DatabaseConnectionInfo` 增加两个字段：

| 字段 | 数据库列 | 说明 |
|---|---|---|
| `databaseType` | `database_type VARCHAR(32) NOT NULL DEFAULT 'MYSQL'` | `MYSQL` / `POSTGRESQL`；历史数据自动回填 MySQL |
| `schemaName` | `schema_name VARCHAR(64) NULL` | 仅 PG 使用，空则按 `public` 处理 |

`database` 字段语义保持不变：既是数据源的唯一标识（前端下拉框的值），也是 MySQL 的库名 / PG 的数据库名。

```
MySQL: key = database = 物理 schema
PG:    key = database，物理 schema = schemaName（默认 public）
```

> 因为 key 就是 `database`，同一个应用实例里不能同时挂载 `shop.public` 和 `shop.sales`。这是本期的已知限制，见 §6。界面上「数据库名」对 PG 填写真实 database，「Schema」另填。

建表脚本（新装）同步更新：

```sql
-- sqlite_create_table.sql
CREATE TABLE IF NOT EXISTS database_connection_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    database TEXT,
    host TEXT,
    username TEXT,
    password TEXT,
    port INTEGER,
    database_type TEXT NOT NULL DEFAULT 'MYSQL',
    schema_name TEXT
);
```

```sql
-- mysql_create_table.sql（同时补上缺失的 table_relation.condition）
`database_type` VARCHAR(32) NOT NULL DEFAULT 'MYSQL' COMMENT '数据库类型',
`schema_name` VARCHAR(64) DEFAULT NULL COMMENT 'PG schema，空则 public'
```

#### 存量库迁移

`CREATE TABLE IF NOT EXISTS` 不会给旧表加列，而应用元数据库可能是用户已有的 SQLite 文件（仓库根目录就有一个 `table_database.db`）或 MySQL 库。增加一个启动时执行的幂等迁移器：

```java
@Component
public class SchemaMigrator {
    // 通过 DatabaseMetaData.getColumns 判断列是否存在，不存在才 ALTER
    // ALTER TABLE database_connection_info ADD COLUMN database_type VARCHAR(32) NOT NULL DEFAULT 'MYSQL'
    // ALTER TABLE database_connection_info ADD COLUMN schema_name VARCHAR(64)
}
```

SQLite 与 MySQL 都接受上述 `ADD COLUMN` 语法（SQLite 允许带非空默认值）。迁移在 `Initializer#init` 之前完成（注入同一个 `JdbcTemplate`，Spring 会保证 `spring.sql.init` 先执行）。

### 4.2 连接与注册

| 类型 | JDBC URL |
|---|---|
| MySQL | `jdbc:mysql://host:port/db?allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false`（保持现状） |
| PostgreSQL | `jdbc:postgresql://host:port/db?ApplicationName=table-relation` |

- PG 不设置 `currentSchema`，SQL 显式带 schema 前缀，避免 schema 不存在时连接直接失败。
- Hikari 的保活语句 `SELECT 1` 两种数据库都适用，不需要改。
- `pom.xml` 增加 `org.postgresql:postgresql`（runtime scope，版本由 Spring Boot BOM 管理）。
- `DataSourceRegistry` 从 `Map<String, DataSource>` 改为 `Map<String, DataSourceContext>`：
  - `getContext(key)`：业务使用
  - `getDataSource(key)`：保留，供 `SqlExecutor` 等底层使用
  - `addDataSource(config, jdbcUrl)`：内部根据类型选 adapter 并组装 context
- `DataSourceManager#testDataSourceConfigConn` 改用 `adapter.buildJdbcUrl`，其余逻辑不变。

### 4.3 元数据查询

`MySqlTableMetaInfoQuery` 改名/改造为 `DatabaseAdapter` 的实现之一，SQL 本身不变；新增 `PostgresDatabaseAdapter`。

#### 表与字段（PG，已在 PostgreSQL 16 实测）

```sql
SELECT n.nspname            AS table_schema,
       cl.relname           AS table_name,
       obj_description(cl.oid, 'pg_class')          AS table_comment,
       a.attname            AS column_name,
       format_type(a.atttypid, a.atttypmod)         AS column_type,
       col_description(cl.oid, a.attnum)            AS column_comment
FROM pg_class cl
JOIN pg_namespace n ON n.oid = cl.relnamespace
JOIN pg_attribute a ON a.attrelid = cl.oid AND a.attnum > 0 AND NOT a.attisdropped
WHERE n.nspname = ? AND cl.relkind IN ('r', 'p', 'v', 'm', 'f')
ORDER BY cl.relname, a.attnum
```

- `relkind` 覆盖普通表、分区表、视图、物化视图、外部表，与 MySQL 侧 `TABLES` 的口径大致对齐。
- 用 `pg_class` 而非 `information_schema.columns`：注释只能从 `pg_description` 取；`format_type` 能保留 `varchar(64)`、`numeric(12,2)` 这类长度信息。
- 单表查询在 `WHERE` 中追加 `AND cl.relname = ?`。
- 实测返回示例：`bigint`、`character varying(64)`、`boolean`、`timestamp with time zone`、`text[]`、`jsonb`、`numeric(12,2)`。

#### 外键关系（PG，已实测）

```sql
SELECT n.nspname   AS table_schema,
       cl.relname  AS table_name,
       a.attname   AS column_name,
       rn.nspname  AS referenced_table_schema,
       rcl.relname AS referenced_table_name,
       ra.attname  AS referenced_column_name
FROM pg_constraint con
JOIN pg_class cl      ON cl.oid = con.conrelid
JOIN pg_namespace n   ON n.oid = cl.relnamespace
JOIN pg_class rcl     ON rcl.oid = con.confrelid
JOIN pg_namespace rn  ON rn.oid = rcl.relnamespace
CROSS JOIN LATERAL unnest(con.conkey, con.confkey) AS k(attnum, refattnum)
JOIN pg_attribute a   ON a.attrelid = con.conrelid  AND a.attnum = k.attnum
JOIN pg_attribute ra  ON ra.attrelid = con.confrelid AND ra.attnum = k.refattnum
WHERE con.contype = 'f' AND n.nspname = ?
ORDER BY cl.relname, a.attnum
```

用 `pg_constraint` 而不是 `information_schema`：复合外键可以按列展开，且能直接拿到被引用列名。

**结果映射规则**（关键）：

1. `ColumnUsage.tableSchema`、`referencedTableSchema` 一律写成 `ctx.key()`，而不是物理 schema，保证与关系注册表、前端 schema 下拉一致。
2. 被引用 schema 与当前物理 schema 不一致的外键（跨 schema）跳过，记 `warn`，避免把 `public.customers` 错映射成 `sales.customers`。

### 4.4 SQL 方言

#### 标识符

| | MySQL | PG |
|---|---|---|
| 表引用 | `` `demo`.`users` `` | `"demo"."users"` |
| 生成 SQL 变化 | `select * from `demo`.`users``（原样） | `select * from "sales"."users"` |

`QueryCondition#buildSql` 签名改为 `buildSql(DatabaseAdapter adapter, String physicalSchema)`。MySQL 加反引号后语义不变，现有单测只需调整断言。

#### 字面量

| 值类型 | MySQL | PostgreSQL |
|---|---|---|
| `null` | `NULL` | `NULL` |
| `Number` / `BigDecimal` | 原样 | 原样 |
| `Boolean` | `1` / `0` | `TRUE` / `FALSE` |
| `String` | `'...'`，转义 `\` 与 `'` | `'...'`，单引号加倍 |
| `LocalDate` / `LocalDateTime` / `Timestamp` / `OffsetDateTime` | `'yyyy-MM-dd HH:mm:ss'` | 同左（PG 可隐式转换） |
| `Collection` | `(v1,v2)` | `(v1,v2)` |
| 其他 | `toString()` | `toString()` |

顺带修掉现有问题：BFS 关联查询里如果字段是布尔型，JDBC 返回 `Boolean`，原实现会拼出 `'true'`，MySQL 下是错误值；方言化后 MySQL 拼 `1`，PG 拼 `TRUE`。

> 假设 PG 使用默认的 `standard_conforming_strings=on`，此时 `''` 转义是标准行为。条件值后续已改为参数化绑定，见 `2026-11-24-parameterized-query.md`。

#### 类型归一化

MySQL 保持现有映射（`varchar/char/text*` → `string`，`int*` → `int`，`float/double/decimal` → `float`，`enum` → `enum`，其余原样小写），避免回归。

PG 映射：

| PG 原始类型 | 归一化 |
|---|---|
| `character varying(n)`、`character(n)`、`text`、`citext`、`uuid`、`name` | `string` |
| `smallint`、`integer`、`bigint`、`smallserial`、`serial`、`bigserial` | `int` |
| `numeric`、`decimal`、`real`、`double precision`、`money` | `float` |
| `boolean` | `boolean` |
| `timestamp ...`、`timestamptz` | `datetime` |
| `date` | `date` |
| `time ...`、`timetz` | `time` |
| `json`、`jsonb` | `json` |
| `bytea` | `bytes` |
| `xxx[]`（数组） | `array` |
| 其他（自定义枚举等） | 小写，并把 `[^a-z0-9_]` 替换为 `_`，保证 mermaid 语法合法 |

最后一步的字符安全化是必须的：mermaid 的 ER 属性类型不允许空格和方括号，`timestamp with time zone`、`text[]` 直接输出会让整个 ER 图渲染失败。

#### INSERT 导出

`SqlBuilder.generateInsertSql` 改为 `generateInsertSql(DatabaseAdapter adapter, String physicalSchema, String table, rows)`：

- 表名、列名走 `adapter.quoteIdentifier`
- 值走 `adapter.insertValue`（默认等同于 `literal`）；PG 的数组列需要 `ARRAY[...]` 形式，单独由 PG 适配器实现
- `DataQueryManager#generateInsertSql` 需要从 `ctx` 传入 schema（原实现丢了 schema，跨库导出时表名不完整）

### 4.5 关系导入与关联查询

- `RelationShipManager#refreshColumnUsage(schema)`：按 `ctx.adapter().listForeignKeys(ctx)` 取外键；关系表的 `table_schema` 存 `ctx.key()`。
- `DataQueryManager`、`RelationShipManager` 不再注入 `MySqlTableMetaInfoQuery`，改为经 `DataSourceRegistry` 拿 context。
- BFS 查询逻辑本身不需要改：它逐表查询、用 `IN (值列表)` 拼接，天然支持跨数据源（甚至 MySQL + PG 混合关系）。SQL 生成走方言即可。
- 列注释映射：`DataQueryManager#getSchemaTableCommentMap` 调用的 `describeTable` 走适配器，PG 返回 `pg_description` 中的注释。

### 4.6 结果值归一化

`DataQuery#executeQuery` 在 SQL 执行后增加一次行归一化：

```java
return sqlExecutor.sqlQueryWithLimit(dataSource, sql).stream()
        .map(ctx.adapter()::normalizeRow)
        .toList();
```

PG 实现需要处理：

| JDBC 值 | 处理 |
|---|---|
| `org.postgresql.util.PGobject`（`jsonb`、`json`、`inet` 等） | `getValue()`，以字符串返回 |
| `java.sql.Array`（`text[]` 等） | 展开为 `List<Object>`（递归处理元素） |
| `java.sql.Timestamp` / `OffsetDateTime` / `UUID` / `BigDecimal` | 保持原值，Spring Boot 的 Jackson 配置可正常序列化 |
| `bytea` | `byte[]`，Jackson 输出 base64 |

MySQL 适配器使用默认实现（identity），不改变现有输出。

### 4.7 前端改动

`frontend/src/views/DataSourceManage.vue`：

1. 表单新增「数据库类型」下拉：MySQL / PostgreSQL，默认 MySQL。
2. 切换类型时自动把端口改为默认值（3306 / 5432）；用户手动改过端口后不再覆盖（用 `typeChangedByUser` 标记即可）。
3. 「Schema」输入框仅 PG 显示，默认 `public`。
4. 列表增加「类型」列（Tag 展示）、PG 显示 schema。
5. 表单提交继续整个对象提交，后端字段默认值兜底，旧数据不需要前端处理。
6. 补充一个 `DataSourceManage.test.js`：类型切换端口、PG 显示 schema 字段、提交带 `databaseType` / `schemaName`。

其余页面（查询、表关系、ER 图）无需改动，schema 下拉值仍是 `database`。

## 5. PG 差异速查

| 关注点 | MySQL | PostgreSQL |
|---|---|---|
| 标识符引用 | 反引号 | 双引号；未引用时折叠为小写 |
| 字符串转义 | 反斜杠 + `''` | `''`（`standard_conforming_strings=on`） |
| 布尔字面量 | `1` / `0` | `TRUE` / `FALSE` |
| 分页 | `limit n` | `limit n` |
| 表注释 | `TABLES.TABLE_COMMENT` | `obj_description(oid,'pg_class')` |
| 列注释 | `COLUMNS.COLUMN_COMMENT` | `col_description(oid, attnum)` |
| 列类型 | `COLUMN_TYPE`（含长度） | `format_type(atttypid, atttypmod)` |
| 外键 | `KEY_COLUMN_USAGE` | `pg_constraint` + `unnest(conkey, confkey)` |
| 特殊返回值 | 基本无 | `jsonb` → `PGobject`；数组 → `java.sql.Array` |
| 跨库 | schema 即库，可跨库查询 | 不能跨 database，只能跨 schema |

## 6. 已知限制

1. 一个 PG 数据源只能绑定一个物理 schema；同一 database 的多个 schema 需要拆成多个连接池。真正的多 schema 支持需要把「数据源 key」与「物理 schema」解耦（registry key 改为 `database.schema`，UI 展示两级），属于后续演进。
2. 跨 schema 外键不自动导入。
3. 应用自身元数据库仍只支持 SQLite / MySQL：`DatabaseConnectionMapper` 等 mapper 使用了反引号，切 PG 需要额外的 DDL 与 SQL 改写。
4. PG 的 `bytea`、自定义复合类型以字符串展示，不做结构化解析。
5. `sql.query.count.max` 固定追加 `limit`，两种数据库语法一致，但 PG 大表仍建议配合索引使用。
6. 关系的 `table_schema` 存的是数据源 key（即 database 名），不包含物理 schema。修改数据源指向的库或 schema 后，旧关系不会自动失效，需要手动清理，与原有 MySQL 行为一致。

## 7. 实施步骤

按「先抽象、后功能」推进，每阶段可独立验证、独立提交。

### 阶段一：抽象改造（不改变行为）

1. 新增 `DatabaseType`、`DatabaseAdapter`、`DataSourceContext`、`DatabaseAdapterRegistry`。
2. 把现有 MySQL 逻辑迁移成 `MySqlDatabaseAdapter`（URL、元数据 SQL、类型映射、字面量）。
3. `DataSourceRegistry` 改为存 context，调用方改取 context。
4. `QueryCondition`、`SqlBuilder` 方言化；`DataQuery`、`DataQueryManager`、`RelationShipManager`、`Initializer`、`DataSourceManager` 去掉对 `MySqlTableMetaInfoQuery` / `DataSourceUrlBuilder` 的直接依赖。
5. 验收：现有单测通过；用现有 MySQL 数据源跑一遍查询、关系、ER 图，SQL 只多出反引号，行为一致。

### 阶段二：配置模型与前端

1. 加 `databaseType`、`schemaName` 字段（DTO / entity / mapper / repository）。
2. 更新建表脚本 + 启动迁移器（含补 `table_relation.condition`）。
3. 前端数据源表单与列表改造，补单测。
4. 验收：老 SQLite 文件启动自动加列；老数据按 MySQL 正常工作。

### 阶段三：PG 适配器

1. `pom.xml` 增加 PG 驱动。
2. 实现 `PostgresDatabaseAdapter`：URL、元数据、外键、类型归一化、结果值归一化。
3. 补单元测试（URL / 引用 / 字面量 / 类型映射 / SQL 文本）。
4. 验收：见 §8 验收清单。

### 阶段四：验证与文档

1. Docker 验证脚本与样例数据。
2. README 增加 PG 使用说明与限制。
3. `docs/frontend-todo.md` 之外单独记录已知限制。

## 8. 测试与验证

### 单元测试

| 测试 | 内容 |
|---|---|
| `MysqlDatabaseAdapterTest` | URL、标识符、字面量（字符串转义 / 布尔 / 时间）、类型映射 |
| `PostgresDatabaseAdapterTest` | 同上 + 数组 / jsonb / 自定义类型归一化 |
| `QueryConditionTest` | 两种方言生成的 SQL 文本 |
| `SqlBuilderTest` | 两种方言的 INSERT 文本与转义 |
| `DataSourceConfigTest`（可选） | `DatabaseType.from` 的默认值兜底 |
| `DataSourceManage.test.js` | 类型切换端口、PG 显示 schema、提交字段 |

元数据 SQL 依赖真实数据库，用下面的 Docker 验证覆盖；后续可考虑加 Testcontainers（`@Tag("pg")`，不进默认 CI）。

### Docker 验证

仓库已提供 compose 与样例数据：

```bash
docker compose -f docker/pg/docker-compose.yml up -d

# 等容器就绪后即可使用
docker exec table-relation-pg pg_isready -U postgres -d shop
```

样例数据见 `docker/pg/init/01-demo.sql`（public / sales 两个 schema，含注释、复合主键、外键、jsonb、数组、布尔、数值），容器首次启动时自动执行。

如果不使用 compose，也可以手动：

```bash
docker run -d --name tr-pg \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=shop \
  -p 127.0.0.1:5544:5432 \
  postgres:16

docker exec -i tr-pg psql -U postgres -d shop < docker/pg/init/01-demo.sql
```

样例数据（设计阶段已用同结构在 PG 16 验证过元数据查询）：

```sql
CREATE SCHEMA IF NOT EXISTS sales;

CREATE TABLE sales.customers (
  id bigserial PRIMARY KEY,
  name varchar(64) NOT NULL,
  active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  tags text[],
  meta jsonb,
  balance numeric(12,2)
);
COMMENT ON TABLE sales.customers IS '客户';
COMMENT ON COLUMN sales.customers.name IS '客户名称';

CREATE TABLE sales.orders (
  id bigserial PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES sales.customers(id),
  status varchar(16) DEFAULT 'NEW',
  amount numeric(12,2),
  created_at timestamp
);
COMMENT ON TABLE sales.orders IS '订单';

CREATE TABLE sales.order_items (
  order_id bigint NOT NULL,
  line_no int NOT NULL,
  sku varchar(32) NOT NULL,
  qty int NOT NULL DEFAULT 1,
  PRIMARY KEY (order_id, line_no),
  CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES sales.orders(id)
);

INSERT INTO sales.customers(name, active, balance) VALUES ('张三', true, 100.50), ('李四', false, 0);
INSERT INTO sales.orders(customer_id, status, amount, created_at)
VALUES (1, 'PAID', 99.99, now()), (2, 'NEW', 10, now());
```

应用启动后按下面顺序验收：

| # | 操作 | 预期 |
|---|---|---|
| 1 | 添加数据源：POSTGRESQL / 127.0.0.1 / 5544 / shop / postgres / postgres / schema=sales | 保存成功 |
| 2 | `GET /api/table/allSchema` | `["shop"]` |
| 3 | `GET /api/table/schemaTables?schema=shop` | `customers, order_items, orders` |
| 4 | `GET /api/table/tableColumns?schema=shop&tableName=customers` | `id, name, active, created_at, tags, meta, balance`，顺序与表结构一致 |
| 5 | `GET /api/table/relation/list` | 自动导入 2 条外键关系 |
| 6 | `POST /api/table/sqlQuery`，条件 `customers.id = 1` | 返回 customers + orders + order_items 三张表数据，列注释正确 |
| 7 | `GET /api/table/relation/erDiagram?schema=shop&tableName=customers&needTableInfo=true` | mermaid 文本类型为 `int/string/boolean/datetime/array/json/float`，无空格或方括号 |
| 8 | `POST /api/table/generateInsertSql` | 双引号标识符、`TRUE`、数字不带引号、字符串正确转义 |
| 9 | 前端页面走一遍查询 / 表关系 / ER 图 | 展示正常，夜间模式正常 |
| 10 | `jsonb`、`text[]` 列在结果表格中 | 显示为 JSON 字符串 / 数组，不是 `PGobject` 结构 |
| 11 | 切回 MySQL 数据源（如 `share-expense-mysql` 127.0.0.1:3307） | 步骤 3–8 行为与改造前一致 |
| 12 | 删除 PG 数据源后重新添加 schema=public | 正常，无残留连接 |

### 回归

- `mvn -B clean package` 通过
- `npm run lint` / `format:check` / `test` 通过
- 现有 SQLite 老库（带旧表结构）启动自动迁移成功

## 9. 风险与应对

| 风险 | 应对 |
|---|---|
| 抽象改造引入 MySQL 回归 | 阶段一不新增功能，靠现有单测 + MySQL 实测对照；SQL 文本断言固定下来 |
| PG 类型/返回值差异导致页面异常 | 类型归一化 + 行值归一化统一在适配器内，验收清单覆盖 jsonb / 数组 / 布尔 / 时间 |
| 存量数据库升级失败 | 迁移器幂等、带默认值，并在启动日志打印加了哪些列 |
| schema 语义混淆（key vs 物理 schema） | 只在适配器和 registry 内部出现物理 schema；对外一律用 key，文档与注释写清楚 |
| 超大改动难以 review | 按阶段提交，阶段一为纯重构、阶段三才是新功能；必要时可拆成两个 PR |

## 10. 后续演进

1. 多 schema：registry key 改为 `database.schema`，前端 schema 下拉展示两级；跨 schema 外键自动导入。
2. Testcontainers 集成测试，CI 可选任务中执行。
3. ~~查询参数化~~ 已完成，见 `2026-11-24-parameterized-query.md`；剩余风险是表关系「关联条件」的原始 SQL 片段。
4. 支持更多数据源类型（SQL Server / Oracle）：新增一个 `DatabaseAdapter` 即可。
