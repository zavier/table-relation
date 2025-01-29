CREATE TABLE `table_relation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `source_db` VARCHAR(64) NOT NULL COMMENT '主库名',
  `source_table` VARCHAR(64) NOT NULL COMMENT '主表名',
  `source_column` VARCHAR(64) NOT NULL COMMENT '主表字段',
  `target_db` VARCHAR(64) NOT NULL COMMENT '关联库名',
  `target_table` VARCHAR(64) NOT NULL COMMENT '关联表名',
  `target_column` VARCHAR(64) NOT NULL COMMENT '关联表字段',
  `relation_type` tinyint NOT NULL COMMENT '关联类型 1:一对一, 2:一对多',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='表关系';

CREATE TABLE `database_connection_info` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `db` VARCHAR(64) NOT NULL COMMENT '主库名',
  `host` VARCHAR(20) NOT NULL COMMENT 'host',
  `username` VARCHAR(30) NOT NULL COMMENT '用户名',
  `password` VARCHAR(30) NOT NULL COMMENT '密码',
  `port` INT NOT NULL COMMENT '端口',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='数据库连接信息';