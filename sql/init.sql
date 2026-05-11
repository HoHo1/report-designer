-- 在线报表系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS report_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE report_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN/DESIGNER/USER',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 数据集表
CREATE TABLE IF NOT EXISTS `dataset` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `dataset_name` VARCHAR(100) NOT NULL COMMENT '数据集名称',
    `dataset_type` VARCHAR(20) NOT NULL COMMENT '类型：SQL/API/FILE',
    `config` TEXT COMMENT '配置信息JSON',
    `status` INT(1) NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1可用 2禁用',
    `create_user` VARCHAR(32) NOT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_dataset_type` (`dataset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据集表';

-- 报表模板表
CREATE TABLE IF NOT EXISTS `report_template` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `report_name` VARCHAR(100) NOT NULL COMMENT '报表名称',
    `design_content` TEXT NOT NULL COMMENT '设计器内容JSON',
    `dataset_ids` VARCHAR(500) DEFAULT NULL COMMENT '绑定数据集ID，逗号分隔',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `status` INT(1) NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布',
    `is_public` INT(1) NOT NULL DEFAULT 0 COMMENT '是否公开：0私有 1公开',
    `create_user` VARCHAR(32) NOT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_user` (`create_user`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表模板表';

-- 报表版本表
CREATE TABLE IF NOT EXISTS `report_version` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报表ID',
    `design_content` TEXT NOT NULL COMMENT '设计器内容JSON',
    `version_num` INT(11) NOT NULL COMMENT '版本号',
    `create_user` VARCHAR(32) NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表版本表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `module` VARCHAR(50) NOT NULL COMMENT '模块',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `result` TEXT COMMENT '执行结果',
    `operator` VARCHAR(50) NOT NULL COMMENT '操作人',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_operator` (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 初始化管理员账号（密码：admin123）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `email`, `status`)
VALUES ('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 'admin@example.com', 1);

-- 初始化测试用户（密码：user123）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `email`, `status`)
VALUES ('2', 'designer', '$2a$10$7K3JbQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM8lE', '报表设计师', 'DESIGNER', 'designer@example.com', 1);
