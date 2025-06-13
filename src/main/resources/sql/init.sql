-- 创建数据库
CREATE DATABASE IF NOT EXISTS demo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE demo_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `uuid` varchar(36) NOT NULL COMMENT '用户UUID (对外唯一标识)',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT 0 COMMENT '性别 (0-未知, 1-男, 2-女)',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `role` varchar(20) DEFAULT 'user' COMMENT '角色',
  `wechat_id` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `alipay_id` varchar(100) DEFAULT NULL COMMENT '支付宝UserID',
  `status` tinyint DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_time` datetime DEFAULT NULL COMMENT '删除时间 (逻辑删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid` (`uuid`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_deleted_time` (`deleted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据（使用正确的UUID格式）
INSERT INTO `sys_user` (`uuid`, `username`, `password`, `phone`, `email`, `nickname`, `role`, `status`) VALUES
('admin-uuid-1234-5678-9012-345678901234', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 'admin@example.com', '管理员', 'admin', 1),
('user-uuid-1234-5678-9012-345678901234', 'testuser', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', 'test@example.com', '测试用户', 'user', 1);

-- 注意：上面的密码是 '123456' 经过MD5+盐值加密后的结果，现已升级为BCrypt加密
-- 新用户注册将使用BCrypt加密算法

-- 添加性能优化索引
ALTER TABLE `sys_user` ADD INDEX `idx_username_status` (`username`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_phone_status` (`phone`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_email_status` (`email`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_role_status` (`role`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_created_time_status` (`created_time`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_updated_time` (`updated_time`);

-- 为日志表添加复合索引优化查询性能
ALTER TABLE `sys_log` ADD INDEX `idx_operation_time` (`operation`, `created_time`);
ALTER TABLE `sys_log` ADD INDEX `idx_user_operation` (`user_uuid`, `operation`, `created_time`);
ALTER TABLE `sys_log` ADD INDEX `idx_ip_time` (`ip`, `created_time`);

-- 创建操作日志表（可选，用于记录用户操作）
CREATE TABLE IF NOT EXISTS `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `user_uuid` varchar(36) DEFAULT NULL COMMENT '操作用户UUID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(100) NOT NULL COMMENT '操作类型',
  `method` varchar(100) NOT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_uuid` (`user_uuid`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';