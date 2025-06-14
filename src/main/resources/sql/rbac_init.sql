-- RBAC权限管理系统数据库初始化脚本
-- 作者: spark
-- 日期: 2025-06-14

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) COMMENT '角色描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_time` DATETIME NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `type` TINYINT DEFAULT 3 COMMENT '权限类型 (1-菜单, 2-按钮, 3-接口)',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `path` VARCHAR(255) COMMENT '权限路径',
    `method` VARCHAR(10) COMMENT 'HTTP方法',
    `description` VARCHAR(255) COMMENT '权限描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_time` DATETIME NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_code` VARCHAR(50) NOT NULL COMMENT '菜单编码',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
    `path` VARCHAR(255) COMMENT '菜单路径',
    `component` VARCHAR(255) COMMENT '组件路径',
    `icon` VARCHAR(50) COMMENT '菜单图标',
    `type` TINYINT DEFAULT 2 COMMENT '菜单类型 (1-目录, 2-菜单, 3-按钮)',
    `hidden` TINYINT DEFAULT 0 COMMENT '是否隐藏 (0-显示, 1-隐藏)',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    `remark` VARCHAR(255) COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_time` DATETIME NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_menu_code` (`menu_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 初始化基础角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `sort`, `status`) VALUES
('super_admin', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1),
('admin', '管理员', '系统管理员，拥有大部分权限', 2, 1),
('user', '普通用户', '普通用户，拥有基础权限', 3, 1),
('guest', '访客', '访客用户，只有查看权限', 4, 1);

-- 初始化基础权限数据
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `type`, `parent_id`, `path`, `method`, `description`, `sort`, `status`) VALUES
-- 用户管理权限
('user', '用户管理', 1, 0, NULL, NULL, '用户管理模块', 1, 1),
('user:list', '用户列表', 3, 1, '/v1/users/list', 'GET', '查看用户列表', 1, 1),
('user:read', '查看用户', 3, 1, '/v1/users/*', 'GET', '查看用户详情', 2, 1),
('user:create', '创建用户', 3, 1, '/v1/users', 'POST', '创建新用户', 3, 1),
('user:update', '更新用户', 3, 1, '/v1/users/*', 'PUT', '更新用户信息', 4, 1),
('user:delete', '删除用户', 3, 1, '/v1/users/*', 'DELETE', '删除用户', 5, 1),

-- 角色管理权限
('role', '角色管理', 1, 0, NULL, NULL, '角色管理模块', 2, 1),
('role:list', '角色列表', 3, 7, '/v1/roles', 'GET', '查看角色列表', 1, 1),
('role:read', '查看角色', 3, 7, '/v1/roles/*', 'GET', '查看角色详情', 2, 1),
('role:create', '创建角色', 3, 7, '/v1/roles', 'POST', '创建新角色', 3, 1),
('role:update', '更新角色', 3, 7, '/v1/roles/*', 'PUT', '更新角色信息', 4, 1),
('role:delete', '删除角色', 3, 7, '/v1/roles/*', 'DELETE', '删除角色', 5, 1),

-- 权限管理权限
('permission', '权限管理', 1, 0, NULL, NULL, '权限管理模块', 3, 1),
('permission:list', '权限列表', 3, 13, '/v1/permissions', 'GET', '查看权限列表', 1, 1),
('permission:read', '查看权限', 3, 13, '/v1/permissions/*', 'GET', '查看权限详情', 2, 1),
('permission:create', '创建权限', 3, 13, '/v1/permissions', 'POST', '创建新权限', 3, 1),
('permission:update', '更新权限', 3, 13, '/v1/permissions/*', 'PUT', '更新权限信息', 4, 1),
('permission:delete', '删除权限', 3, 13, '/v1/permissions/*', 'DELETE', '删除权限', 5, 1),

-- 菜单管理权限
('menu', '菜单管理', 1, 0, NULL, NULL, '菜单管理模块', 4, 1),
('menu:list', '菜单列表', 3, 19, '/v1/menus', 'GET', '查看菜单列表', 1, 1),
('menu:read', '查看菜单', 3, 19, '/v1/menus/*', 'GET', '查看菜单详情', 2, 1),
('menu:create', '创建菜单', 3, 19, '/v1/menus', 'POST', '创建新菜单', 3, 1),
('menu:update', '更新菜单', 3, 19, '/v1/menus/*', 'PUT', '更新菜单信息', 4, 1),
('menu:delete', '删除菜单', 3, 19, '/v1/menus/*', 'DELETE', '删除菜单', 5, 1),

-- 系统管理权限
('system', '系统管理', 1, 0, NULL, NULL, '系统管理模块', 5, 1),
('system:status', '系统状态', 3, 25, '/v1/auth/status', 'GET', '查看系统状态', 1, 1),
('system:captcha', '验证码管理', 3, 25, '/v1/captcha/*', '*', '验证码相关操作', 2, 1);

-- 初始化基础菜单数据
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `path`, `component`, `icon`, `type`, `hidden`, `sort`, `status`, `remark`) VALUES
-- 一级菜单
('dashboard', '仪表盘', 0, '/dashboard', 'Dashboard', 'dashboard', 2, 0, 1, 1, '系统仪表盘'),
('system_management', '系统管理', 0, '/system', NULL, 'setting', 1, 0, 2, 1, '系统管理目录'),
('user_center', '个人中心', 0, '/profile', 'Profile', 'user', 2, 0, 3, 1, '个人中心'),

-- 系统管理子菜单
('user_management', '用户管理', 2, '/system/users', 'system/UserManagement', 'user', 2, 0, 1, 1, '用户管理页面'),
('role_management', '角色管理', 2, '/system/roles', 'system/RoleManagement', 'team', 2, 0, 2, 1, '角色管理页面'),
('permission_management', '权限管理', 2, '/system/permissions', 'system/PermissionManagement', 'lock', 2, 0, 3, 1, '权限管理页面'),
('menu_management', '菜单管理', 2, '/system/menus', 'system/MenuManagement', 'menu', 2, 0, 4, 1, '菜单管理页面');

-- 为超级管理员分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE `status` = 1;

-- 为管理员分配基础权限（除了超级管理员权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission` 
WHERE `status` = 1 AND `permission_code` NOT IN ('permission:create', 'permission:update', 'permission:delete');

-- 为普通用户分配基础权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission` 
WHERE `status` = 1 AND `permission_code` IN ('user:read', 'system:status', 'system:captcha');

-- 为访客分配查看权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, id FROM `sys_permission` 
WHERE `status` = 1 AND `permission_code` IN ('system:status');

-- 为超级管理员分配所有菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE `status` = 1;

-- 为管理员分配管理菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, id FROM `sys_menu` WHERE `status` = 1 AND `menu_code` IN ('dashboard', 'system_management', 'user_management', 'role_management', 'user_center');

-- 为普通用户分配基础菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, id FROM `sys_menu` WHERE `status` = 1 AND `menu_code` IN ('dashboard', 'user_center');

-- 为访客分配基础菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, id FROM `sys_menu` WHERE `status` = 1 AND `menu_code` IN ('dashboard');

-- 为现有用户分配角色（基于原有的role字段）
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, 
    CASE 
        WHEN u.role = 'admin' THEN 2
        WHEN u.role = 'user' THEN 3
        ELSE 3
    END
FROM `sys_user` u 
WHERE u.deleted_time IS NULL
ON DUPLICATE KEY UPDATE updated_time = CURRENT_TIMESTAMP; 