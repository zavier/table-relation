# 查询条件参数化设计

> 版本：2026-11-24
> 状态：已实现
> 前置：`2026-11-24-postgresql-support-design.md`

## 1. 背景

在 PG 适配改造前，`QueryCondition#buildSql` 直接把值拼进 SQL：

```java
sql.append(column).append(" ").append(operator).append(" ").append(convertSqlValue(value));
```

字段名、运算符、值都来自接口入参，其中值完全可控，属于标准 SQL 注入面。PG 适配时虽然补了转义，但转义不是正确的防御手段，而且 MySQL / PG 的转义规则还不一致。

代码中原本就留有 `// TODO SQL注入` 标记（对应旧 PR #2 的方向），本次在该分支基础上统一处理。

## 2. 目标与范围

### 目标

1. 条件值全部走 JDBC 参数绑定，不再出现在 SQL 文本里。
2. 运算符做白名单校验，避免运算符位置被注入。
3. 标识符继续按方言引用，避免列名/表名注入与大小写问题。
4. MySQL / PostgreSQL 行为一致，BFS 关联查询（`IN` 列表）不受影响。

### 不在范围

| 项目 | 说明 |
|---|---|
| 表关系上的「关联条件」 | 本次未处理；后续已改为结构化条件，见 `2026-11-24-structured-relation-condition.md` |
| `logic.no.delete.condition` | `application.properties` 配置的 SQL 片段，属于部署方可信输入 |
| INSERT SQL 导出 | 输出的是给人复制的 SQL 文本，不是执行语句，仍用方言字面量 |

## 3. 方案

### 3.1 SqlWithParams

```java
public record SqlWithParams(String sql, List<Object> params) {
}
```

`QueryCondition#buildSql(adapter, physicalSchema)` 返回 `SqlWithParams`，SQL 中只保留 `?` 占位符。

### 3.2 条件构建

```sql
-- 之前
select * from `demo`.`users` where `name` = 'x'' OR ''1''=''1'

-- 现在
sql:    select * from `demo`.`users` where `name` = ?
params: ["x' OR '1'='1"]
```

- 运算符白名单：`=`、`!=`、`<>`、`>`、`>=`、`<`、`<=`、`LIKE`、`NOT LIKE`、`IN`、`NOT IN`
- 不在白名单内的运算符（包括 `= 1 OR 1=1` 这类拼接）直接抛 `IllegalArgumentException`
- 列名统一走 `adapter.quoteIdentifier`，内部引号按方言转义
- `IN` / `NOT IN` 按集合长度展开占位符；空集合生成 `1 = 0` / `1 = 1`，避免非法的 `IN ()`

### 3.3 PostgreSQL 参数类型推断

前端输入框提交的值都是字符串（例如 `id` 输入 `1`）。参数化后 PG 会把字符串绑定为 `varchar`，与 `bigint`、`timestamp` 等列比较时会直接报错：

```
ERROR: operator does not exist: bigint = character varying
```

之前的字面量写法 `id = '1'` 之所以能工作，是因为 `'1'` 是 unknown 类型，由 PG 按列类型推断。

解决办法是在 PG 连接串上增加 `stringtype=unspecified`（PostgreSQL JDBC 官方参数），让字符串参数以 unknown 类型发送，由服务端按列类型推断，与字面量行为一致：

```
jdbc:postgresql://host:port/db?ApplicationName=table-relation&stringtype=unspecified
```

MySQL 驱动本身支持隐式转换，不需要该参数。

### 3.4 执行

`DataQuery#executeQuery` 把 `sql` 与 `params.toArray()` 一起传给 `SqlExecutor`，复用已有的 `JdbcTemplate#queryForList(sql, args)`，无需改底层执行器。

BFS 关联查询本来就是构造 `IN (值列表)`，现在自动变成多占位符绑定，逻辑不变。

## 4. 代码变更

| 文件 | 变更 |
|---|---|
| `service/dto/SqlWithParams.java` | 新增，SQL + 参数列表 |
| `service/dto/QueryCondition.java` | 参数化构建、运算符白名单、`IN` 展开 |
| `service/DataQuery.java` | 传递参数执行 |
| `service/db/*` | 保留 `literal` / `insertValue`，仅用于 INSERT 导出与自定义条件 |
| `QueryConditionTest` | 断言 SQL 与 params，覆盖注入串、`IN`、空集合、非法运算符 |
| `ParameterizedQueryTest` | 用内存 SQLite 实际执行，验证注入串被当作普通值、`LIKE` / `IN` 正常 |

## 5. 验证

- `ParameterizedQueryTest#injectionValueIsTreatedAsPlainData`：值 `x' OR '1'='1` 查询结果为空，原表数据未被破坏
- `QueryConditionTest`：9 个用例，覆盖两种方言、`IN`、空集合、运算符拒绝
- 手工验证：PG / MySQL 两种数据源分别执行单条件查询、关联查询、INSERT 导出
- 全量：`mvn clean package`、`npm run lint` / `test` / `build`

## 6. 后续

1. ~~表关系「关联条件」改为结构化（字段 + 运算符 + 值），彻底去掉原始 SQL 片段输入~~ 已完成，见 `2026-11-24-structured-relation-condition.md`
2. 条件值类型按列元数据转换（当前交给 JDBC 驱动隐式转换）
3. 与多 schema 演进一起，补充 Testcontainers 下的参数化回归测试
