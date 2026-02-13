```
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
```

## 项目概述

这是一个 **表关系管理工具**，后端基于 Spring Boot 3.4.2 开发，主要功能包括：
- 数据源管理（数据库连接配置）
- 表字段关系管理
- ER 图可视化
- 数据查询（支持关联表查询）
- AI 辅助 SQL 生成
- AI 辅助表关系分析

## 开发环境要求

- JDK 版本：≥ 21
- 构建工具：Maven 3.9+

## 常用命令

### 编译项目

```bash
# 使用 Maven Wrapper 编译
./mvnw clean compile

# 或使用系统 Maven
mvn clean compile
```

### 运行项目

```bash
# 直接运行 Spring Boot 应用
./mvnw spring-boot:run

# 或编译后运行
mvn clean package
java -jar target/table-relation-0.0.2.jar
```

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=类名

# 运行单个测试方法
./mvnw test -Dtest=类名#方法名
```

### 代码格式化

```bash
# 使用 Maven 插件格式化代码
./mvnw spring-javaformat:apply
```

## 项目架构

### 核心模块

```
src/main/java/com/github/zavier/table/relation/
├── config/                    # 配置类（ChatClientConfig）
├── dao/                       # 数据访问层
│   ├── entity/               # 数据库实体
│   ├── mapper/               # MyBatis 映射器
│   └── repository/           # 数据仓库接口
├── manager/                   # 业务管理层
│   ├── ai/                   # AI 相关功能
│   ├── DataQueryManager.java       # 数据查询管理
│   ├── DataSourceManager.java      # 数据源管理
│   ├── Initializer.java            # 初始化器
│   └── RelationShipManager.java    # 关系管理
├── service/                   # 业务服务层
│   ├── extpt/                # 数据库元信息查询接口（MySQL）
│   ├── converter/            # 数据转换器
│   ├── domain/               # 领域模型
│   ├── dto/                  # 数据传输对象
│   └── constant/             # 常量定义
├── utils/                     # 工具类
└── web/                       # Web 控制层
    ├── DataController.java
    ├── DataSourceManagerController.java
    ├── RelationManagerController.java
    └── GlobalExceptionHandler.java
```

### 主要依赖

- **Spring Boot 3.4.2**：核心框架
- **Spring AI**：AI 功能集成（SQL 生成、表关系分析）
- **SQLite**：本地配置存储
- **MySQL Connector**：MySQL 数据库连接
- **Apache Commons Lang3**：工具库
- **JetBrains Annotations**：注解库

### 数据存储

- **配置存储**：SQLite 数据库（本地文件）
- **业务数据**：连接的外部数据库（MySQL 等）

## 核心功能

### 1. 数据源管理

- 配置数据库连接信息（支持 MySQL）
- 存储在本地 SQLite 中
- 连接验证功能

### 2. 表关系管理

- 手动配置表字段关系
- 支持带条件的关联关系（如：`type=1`）
- 关系存储在本地 SQLite 中

### 3. ER 图可视化

- 根据配置的表关系生成 Mermaid 格式的 ER 图
- 支持查询特定表的关联关系图

### 4. 数据查询

- 单表查询
- 关联表数据展示
- 支持查询条件
- 限制单表结果最多 10 条

### 5. AI 功能

- **SQL 生成**：根据 ER 图和自然语言描述生成 SQL 查询
- **表关系分析**：使用大模型自动分析表关系并保存

## 注意事项

1. **安全性**：无鉴权及 SQL 注入防护，仅建议本地部署个人使用
2. **部署方式**：前后端打包在一起，直接运行 jar 文件即可
3. **访问地址**：http://localhost:8080/

## 前端项目

对应前端项目代码：https://github.com/zavier/table-relation-front

## OpenSpec 配置

项目使用 OpenSpec 进行规范管理，配置文件位于 `openspec/config.yaml`，支持：
- 变更管理
- 规范文档
