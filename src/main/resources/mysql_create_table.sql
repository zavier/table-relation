CREATE TABLE IF NOT EXISTS `table_relation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `table_schema` VARCHAR(64) NOT NULL COMMENT '主库名',
  `table_name` VARCHAR(64) NOT NULL COMMENT '主表名',
  `column_name` VARCHAR(64) NOT NULL COMMENT '主表字段',
  `condition` VARCHAR(512) DEFAULT NULL COMMENT '自定义条件',
  `referenced_table_schema` VARCHAR(64) NOT NULL COMMENT '关联库名',
  `referenced_table_name` VARCHAR(64) NOT NULL COMMENT '关联表名',
  `referenced_column_name` VARCHAR(64) NOT NULL COMMENT '关联表字段',
  `relation_type` tinyint NOT NULL COMMENT '关联类型 1:一对一, 2:一对多',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='表关系';

CREATE TABLE IF NOT EXISTS `database_connection_info` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `database` VARCHAR(64) NOT NULL COMMENT '主库名',
  `host` VARCHAR(20) NOT NULL COMMENT 'host',
  `username` VARCHAR(30) NOT NULL COMMENT '用户名',
  `password` VARCHAR(30) NOT NULL COMMENT '密码',
  `port` INT NOT NULL COMMENT '端口',
  `database_type` VARCHAR(32) NOT NULL DEFAULT 'MYSQL' COMMENT '数据库类型 MYSQL/POSTGRESQL',
  `schema_name` VARCHAR(64) DEFAULT NULL COMMENT 'PostgreSQL schema，空则 public',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='数据库连接信息';
