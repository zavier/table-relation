# 表关系关联条件结构化设计

> 版本：2026-11-24
> 状态：已实现
> 前置：`2026-11-24-parameterized-query.md`

## 1. 背景

表关系上有一个「关联条件」，用于「一张表按条件关联不同表」的场景（例如操作日志表按 `type` 区分来源表）。原实现是一个自由文本输入框，保存后直接拼进 SQL：

```java
currentCondition.addCustomizeConditionSql(column.condition());
```

这是查询链路上最后一个 SQL 注入面，也无法校验语法、不能在页面上准确展示。改为「字段 + 运算符 + 值」的结构化条件后，和普通查询条件一样走参数绑定。

## 2. 数据模型

`table_relation` 表新增三列，旧的 `condition` 列保留用于迁移，不再参与查询：

| 列 | 说明 |
|---|---|
| `condition_field` | 条件字段，如 `type` |
| `condition_operator` | 运算符，限制在白名单内 |
| `condition_value` | 条件值，按字符串保存，由 JDBC / PG 推断类型 |
| `condition`（旧） | 历史原始 SQL，迁移后清空 |

领域对象：

```java
public record ConditionFilter(String field, String operator, String value) {
    Condition toCondition();   // 转成参数化查询条件
    String display();          // ER 图 / 页面展示：type = 1
}
```

`Column` 的 `condition` 从 `String` 改为 `ConditionFilter`，`null` 表示无条件。

## 3. 迁移

启动时 `SchemaMigrator`：

1. 幂等补上三列；
2. 扫描 `condition` 非空且 `condition_field` 为空的历史行，用正则解析形如 `type=1`、`status = 'NEW'`、`amount >= 100` 的条件；
3. 解析成功：写入三列并把 `condition` 置空；
4. 解析失败（多条件、`IN`、自由 SQL）：保留原 `condition` 并打 `warn` 日志，提示用户重新配置。

`condition_value` 始终是字符串，查询时以参数绑定，PostgreSQL 连接串的 `stringtype=unspecified` 会按列类型推断。

## 4. 查询

BFS 关联查询不再拼 SQL 片段，而是复制当前条件并追加一个 `Condition`：

```java
if (column.condition() != null) {
    dataMapList = executeQuery(
            currentCondition.withAdditionalCondition(column.condition().toCondition()));
}
```

- `withAdditionalCondition` 返回新对象，避免原来的实现把条件反复追加到同一个 `QueryCondition` 上
- 条件最终由 `QueryCondition` 统一参数化，不会进入 SQL 文本
- 运算符白名单与普通查询条件共用 `SqlOperators`

## 5. 接口与前端

`ColumnUsage` / 关系接口字段变化：

```json
{
  "conditionField": "status",
  "conditionOperator": "=",
  "conditionValue": "PAID"
}
```

前端「表关系管理」：

- 列表按 `字段 运算符 值` 展示，空条件显示 `-`
- 表单改为「字段下拉（主表字段）+ 运算符下拉 + 值输入」
- 三者要么全部填写，要么全部留空；未选字段时前端会把运算符/值归一化为空
- 后端 `RelationShipManager` 同样校验三者一致性，并拒绝 `IN` 等不适合单值条件的运算符

## 6. 验证

| 场景 | 结果 |
|---|---|
| 旧数据 `type=1` 启动迁移 | 解析为 `type = 1`，`condition` 清空，日志可查 |
| 无法解析的旧条件 | 保留原文本，打 warn，不再参与查询 |
| 条件 `status = PAID`（PG） | 关联查询生成 `... and "status" = ?`，参数为 `PAID`；不满足条件的订单不会带出关联表 |
| 不完整条件 | 接口返回 `condition field, operator and value must be filled together` |
| 运算符 `IN` | 接口返回 `Unsupported condition operator: IN` |
| ER 图 | 标签显示 `customer_id(status = PAID) → id` |
| 单元测试 | `ConditionFilterTest`、`SchemaMigratorTest`、`QueryConditionTest`、前端 2 个新用例 |

## 7. 限制

1. 每个关系只支持一个条件；多条件场景需要拆成多条关系或后续扩展为条件列表
2. 条件字段不做存在性校验（SQL 中字段名会被引用，不存在时数据库会报错）
3. 旧版多条件 / 自由 SQL 无法自动迁移，需要手动重新配置
