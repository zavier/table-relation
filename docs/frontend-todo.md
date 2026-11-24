# 前端工程化 TODO

背景：前端代码已从 `table-relation-front` 仓库合并回本仓库的 `frontend/` 目录，构建由 Maven `frontend` profile 集成（`mvn package` 自动构建并打入 jar）。合并只解决了"两个仓库"的问题，前端本身的质量保障与结构问题单独跟进。

优先级从高到低：

## P0：接口层收敛（已完成）

- [x] 抽取 `frontend/src/api/`，统一 axios 实例（baseURL、超时、错误提示）
- [x] 把 6 个页面里散落的 `axios.get/post('/api/...')` 收敛为按领域划分的 API 函数
  - `request.js`：axios 实例、`Result` 解包、`ApiError`
  - `meta.js`（allSchema/schemaTables/tableColumns）
  - `dataSource.js`（list/add/update/delete/test）
  - `query.js`（sqlQuery/generateInsertSql）
  - `relation.js`（list/add/update/delete/erDiagram）
- [x] 后端返回结构（`Result`）统一处理，页面只关心 data 与错误分支

验收：页面中不再直接出现 `/api` 字符串。

## P1：静态检查与构建约束

- [x] 接入 ESLint + Prettier（`eslint-plugin-vue`），修复存量告警
- [x] `package.json` 增加 `lint`、`format`、`format:check` 脚本
- [x] CI 增加 `npm run lint` 与 `npm run format:check`
- [x] 固定 Node 版本（`engines` + `.nvmrc`），与 `pom.xml` 中 `node.version` 保持一致

验收：CI 在存在 lint / 格式错误时失败；Node 版本三处一致。

## P1：测试

- [x] 引入 Vitest + @vue/test-utils（jsdom 环境）
- [x] 覆盖 API 层（`Result` 解包、错误处理、接口路径）与数据库查询页状态流转
- [x] `npm run test` 接入 CI
- [x] 补充表关系表单校验/提交/删除、ER 图生成与渲染异常用例（共 38 个用例）

验收：核心模块有单测，CI 执行通过。

## P2：TypeScript / 结构

- [ ] 评估迁移 TypeScript（`vue-tsc` 类型检查）
- [ ] 拆分为 `components/` + `composables/`，削减单文件 300~500 行的页面组件
- [ ] 评估用 pinia 管理数据源/表关系等跨页面状态（目前每次进入页面重新拉取）

## P2：发布与配置

- [ ] 版本号策略统一：`pom.xml` 为唯一版本源，`frontend/package.json` 同步或标记 private
- [ ] 后端敏感配置（如 `spring.ai.openai.api-key`）改为环境变量 / 本地 profile，避免明文入库
- [ ] 评估 CI 缓存 Node 与 `node_modules`，缩短打包时间

## 已随合并完成

- [x] 前端源码并入后端仓库（`git subtree`，保留提交历史）
- [x] 移除 `main-front` 分支与 static 产物入库的做法
- [x] `mvn package` 一条命令产出带 UI 的 jar（`frontend` profile，默认启用）
- [x] 前端构建输出到 `target/classes/static`，并加入 `.gitignore`
- [x] CI 构建前后端并上传 jar 产物
- [x] P0：API 层收敛（见上）
