SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_file;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_oper_log;
DROP TABLE IF EXISTS sys_config;
DROP TABLE IF EXISTS sys_dict_data;
DROP TABLE IF EXISTS sys_dict_type;
DROP TABLE IF EXISTS sys_role_dept;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_post;
DROP TABLE IF EXISTS sys_post;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_dept;

CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    ancestors VARCHAR(512) NOT NULL DEFAULT '0',
    name VARCHAR(64) NOT NULL,
    leader VARCHAR(64), phone VARCHAR(20), email VARCHAR(128),
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    KEY idx_dept_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_id BIGINT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(64), email VARCHAR(128), phone VARCHAR(20),
    sex TINYINT NOT NULL DEFAULT 0,
    avatar VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 1,
    token_version INT NOT NULL DEFAULT 0,
    login_ip VARCHAR(64), login_time DATETIME,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    data_scope TINYINT NOT NULL DEFAULT 5,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    path VARCHAR(255), component VARCHAR(255), perms VARCHAR(128),
    type CHAR(1) NOT NULL, icon VARCHAR(64),
    sort INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    KEY idx_menu_parent (parent_id), KEY idx_menu_perms (perms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_post_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL, role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id), KEY idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_post (
    user_id BIGINT NOT NULL, post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id), KEY idx_user_post_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL, menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id), KEY idx_role_menu_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_dept (
    role_id BIGINT NOT NULL, dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id), KEY idx_role_dept_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(64) NOT NULL,
    dict_type VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(64) NOT NULL,
    dict_label VARCHAR(64) NOT NULL,
    dict_value VARCHAR(64) NOT NULL,
    dict_sort INT NOT NULL DEFAULT 0,
    css_class VARCHAR(64),
    is_default TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_data (dict_type, dict_value), KEY idx_dict_data_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(64) NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_oper_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(64) NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    method VARCHAR(255), request_method VARCHAR(16),
    oper_user_id BIGINT, oper_name VARCHAR(64), oper_url VARCHAR(512), oper_ip VARCHAR(64),
    oper_location VARCHAR(128), oper_param TEXT, json_result TEXT,
    success TINYINT NOT NULL DEFAULT 1, error_msg TEXT,
    cost_time BIGINT NOT NULL DEFAULT 0, oper_time DATETIME NOT NULL,
    KEY idx_oper_time (oper_time), KEY idx_oper_user (oper_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL, user_id BIGINT, ip VARCHAR(64), location VARCHAR(128),
    browser VARCHAR(64), os VARCHAR(64), success TINYINT NOT NULL DEFAULT 1,
    message VARCHAR(255), login_time DATETIME NOT NULL,
    KEY idx_login_time (login_time), KEY idx_login_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(128) NOT NULL,
    stored_path VARCHAR(512) NOT NULL,
    url VARCHAR(512), size BIGINT NOT NULL, content_type VARCHAR(128), sha256 CHAR(64),
    scene VARCHAR(32) NOT NULL, uploader_id BIGINT,
    create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME,
    remark VARCHAR(500), deleted TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
    KEY idx_file_uploader (uploader_id), KEY idx_file_sha256 (sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_dept (id, parent_id, ancestors, name, leader, sort, status) VALUES
(1, 0, '0', '总部', '管理员', 1, 1),
(2, 1, '0,1', '研发部', '研发负责人', 1, 1),
(3, 1, '0,1', '市场部', '市场负责人', 2, 1);

INSERT INTO sys_post (id, code, name, sort, status) VALUES
(1, 'ceo', '首席执行官', 1, 1),
(2, 'dev', '开发工程师', 2, 1),
(3, 'test', '测试工程师', 3, 1);

INSERT INTO sys_role (id, name, code, sort, data_scope, status) VALUES
(1, '普通用户', 'USER', 1, 5, 1),
(2, '管理员', 'ADMIN', 2, 1, 1);

INSERT INTO sys_menu (id, parent_id, name, path, component, perms, type, icon, sort, visible, status) VALUES
(1, 0, '系统管理', '/system', 'Layout', NULL, 'M', 'setting', 1, 1, 1),
(100, 1, '用户管理', 'user', 'system/user/index', 'system:user:list', 'C', 'user', 1, 1, 1),
(101, 1, '角色管理', 'role', 'system/role/index', 'system:role:list', 'C', 'peoples', 2, 1, 1),
(102, 1, '菜单管理', 'menu', 'system/menu/index', 'system:menu:list', 'C', 'tree-table', 3, 1, 1),
(103, 1, '部门管理', 'dept', 'system/dept/index', 'system:dept:list', 'C', 'tree', 4, 1, 1),
(104, 1, '岗位管理', 'post', 'system/post/index', 'system:post:list', 'C', 'post', 5, 1, 1),
(105, 1, '字典管理', 'dict', 'system/dict/index', 'system:dict:list', 'C', 'dict', 6, 1, 1),
(106, 1, '参数设置', 'config', 'system/config/index', 'system:config:list', 'C', 'edit', 7, 1, 1),
(107, 1, '操作日志', 'operlog', 'system/operlog/index', 'system:operlog:list', 'C', 'form', 8, 1, 1),
(108, 1, '登录日志', 'loginlog', 'system/loginlog/index', 'system:loginlog:list', 'C', 'logininfor', 9, 1, 1),
(109, 1, '在线用户', 'online', 'system/online/index', 'system:online:list', 'C', 'online', 10, 1, 1),
(110, 1, 'IP 黑名单', 'ip', 'system/ip/index', 'system:ip:list', 'C', 'lock', 11, 1, 1),
(1001, 100, '用户查询', NULL, NULL, 'system:user:query', 'F', NULL, 1, 1, 1),
(1002, 100, '用户新增', NULL, NULL, 'system:user:add', 'F', NULL, 2, 1, 1),
(1003, 100, '用户修改', NULL, NULL, 'system:user:edit', 'F', NULL, 3, 1, 1),
(1004, 100, '用户删除', NULL, NULL, 'system:user:remove', 'F', NULL, 4, 1, 1),
(1005, 100, '重置密码', NULL, NULL, 'system:user:resetPwd', 'F', NULL, 5, 1, 1),
(1006, 100, '分配角色', NULL, NULL, 'system:user:assignRole', 'F', NULL, 6, 1, 1),
(1007, 100, '用户导出', NULL, NULL, 'system:user:export', 'F', NULL, 7, 1, 1),
(1008, 100, '用户导入', NULL, NULL, 'system:user:import', 'F', NULL, 8, 1, 1),
(1011, 101, '角色查询', NULL, NULL, 'system:role:query', 'F', NULL, 1, 1, 1),
(1012, 101, '角色新增', NULL, NULL, 'system:role:add', 'F', NULL, 2, 1, 1),
(1013, 101, '角色修改', NULL, NULL, 'system:role:edit', 'F', NULL, 3, 1, 1),
(1014, 101, '角色删除', NULL, NULL, 'system:role:remove', 'F', NULL, 4, 1, 1),
(1015, 101, '分配菜单', NULL, NULL, 'system:role:assignMenu', 'F', NULL, 5, 1, 1),
(1016, 101, '分配部门', NULL, NULL, 'system:role:assignDept', 'F', NULL, 6, 1, 1),
(1021, 102, '菜单查询', NULL, NULL, 'system:menu:query', 'F', NULL, 1, 1, 1),
(1022, 102, '菜单新增', NULL, NULL, 'system:menu:add', 'F', NULL, 2, 1, 1),
(1023, 102, '菜单修改', NULL, NULL, 'system:menu:edit', 'F', NULL, 3, 1, 1),
(1024, 102, '菜单删除', NULL, NULL, 'system:menu:remove', 'F', NULL, 4, 1, 1),
(1031, 103, '部门查询', NULL, NULL, 'system:dept:query', 'F', NULL, 1, 1, 1),
(1032, 103, '部门新增', NULL, NULL, 'system:dept:add', 'F', NULL, 2, 1, 1),
(1033, 103, '部门修改', NULL, NULL, 'system:dept:edit', 'F', NULL, 3, 1, 1),
(1034, 103, '部门删除', NULL, NULL, 'system:dept:remove', 'F', NULL, 4, 1, 1),
(1041, 104, '岗位查询', NULL, NULL, 'system:post:query', 'F', NULL, 1, 1, 1),
(1042, 104, '岗位新增', NULL, NULL, 'system:post:add', 'F', NULL, 2, 1, 1),
(1043, 104, '岗位修改', NULL, NULL, 'system:post:edit', 'F', NULL, 3, 1, 1),
(1044, 104, '岗位删除', NULL, NULL, 'system:post:remove', 'F', NULL, 4, 1, 1),
(1051, 105, '字典查询', NULL, NULL, 'system:dict:query', 'F', NULL, 1, 1, 1),
(1052, 105, '字典新增', NULL, NULL, 'system:dict:add', 'F', NULL, 2, 1, 1),
(1053, 105, '字典修改', NULL, NULL, 'system:dict:edit', 'F', NULL, 3, 1, 1),
(1054, 105, '字典删除', NULL, NULL, 'system:dict:remove', 'F', NULL, 4, 1, 1),
(1061, 106, '参数查询', NULL, NULL, 'system:config:query', 'F', NULL, 1, 1, 1),
(1062, 106, '参数新增', NULL, NULL, 'system:config:add', 'F', NULL, 2, 1, 1),
(1063, 106, '参数修改', NULL, NULL, 'system:config:edit', 'F', NULL, 3, 1, 1),
(1064, 106, '参数删除', NULL, NULL, 'system:config:remove', 'F', NULL, 4, 1, 1),
(1071, 107, '日志查询', NULL, NULL, 'system:operlog:query', 'F', NULL, 1, 1, 1),
(1072, 107, '日志删除', NULL, NULL, 'system:operlog:remove', 'F', NULL, 2, 1, 1),
(1073, 107, '日志导出', NULL, NULL, 'system:operlog:export', 'F', NULL, 3, 1, 1),
(1081, 108, '日志查询', NULL, NULL, 'system:loginlog:query', 'F', NULL, 1, 1, 1),
(1082, 108, '日志删除', NULL, NULL, 'system:loginlog:remove', 'F', NULL, 2, 1, 1),
(1083, 108, '日志导出', NULL, NULL, 'system:loginlog:export', 'F', NULL, 3, 1, 1),
(1091, 109, '在线查询', NULL, NULL, 'system:online:query', 'F', NULL, 1, 1, 1),
(1092, 109, '强制下线', NULL, NULL, 'system:online:forceLogout', 'F', NULL, 2, 1, 1),
(1101, 110, '黑名单查询', NULL, NULL, 'system:ip:query', 'F', NULL, 1, 1, 1),
(1102, 110, '加入黑名单', NULL, NULL, 'system:ip:add', 'F', NULL, 2, 1, 1),
(1103, 110, '移出黑名单', NULL, NULL, 'system:ip:remove', 'F', NULL, 3, 1, 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu;

INSERT INTO sys_user (id, dept_id, username, password, nickname, email, sex, status, token_version, create_time, update_time, deleted, version)
VALUES (1, 1, 'admin', '$2a$10$kszLIbA5uh3DlempUJQSYOvES1gXsxortyR2HfC4.r3UZJiH0Le.S', '管理员', 'admin@campus.local', 1, 1, 0, NOW(), NOW(), 0, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO sys_user_post (user_id, post_id) VALUES (1, 1);

INSERT INTO sys_dict_type (id, dict_name, dict_type, status) VALUES
(1, '用户性别', 'sys_user_sex', 1),
(2, '通用状态', 'sys_common_status', 1);

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, css_class, is_default, status) VALUES
(1, 'sys_user_sex', '未知', '0', 1, NULL, 1, 1),
(2, 'sys_user_sex', '男', '1', 2, NULL, 0, 1),
(3, 'sys_user_sex', '女', '2', 3, NULL, 0, 1),
(4, 'sys_common_status', '停用', '0', 1, 'danger', 0, 1),
(5, 'sys_common_status', '正常', '1', 2, 'primary', 1, 1);

INSERT INTO sys_config (id, config_name, config_key, config_value, remark) VALUES
(1, '最大登录失败次数', 'sys.account.maxLoginFail', '5', '达到次数后锁定账号'),
(2, '登录锁定时长（分钟）', 'sys.account.lockMinutes', '15', '账号锁定时长'),
(3, '文件大小上限（MB）', 'sys.file.maxSizeMb', '10', '单文件上传大小上限');

SET FOREIGN_KEY_CHECKS = 1;
