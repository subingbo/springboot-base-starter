USE campus;

-- 部门
INSERT INTO sys_dept (id, parent_id, ancestors, name, leader, sort, status) VALUES
(1, 0, '0', '总部', '管理员', 1, 1),
(2, 1, '0,1', '研发部', '研发负责人', 1, 1),
(3, 1, '0,1', '市场部', '市场负责人', 2, 1);

-- 岗位
INSERT INTO sys_post (id, code, name, sort, status) VALUES
(1, 'ceo', '首席执行官', 1, 1),
(2, 'dev', '开发工程师', 2, 1),
(3, 'test', '测试工程师', 3, 1);

-- 角色
INSERT INTO sys_role (id, name, code, sort, data_scope, status) VALUES
(1, '普通用户', 'USER', 1, 5, 1),
(2, '管理员', 'ADMIN', 2, 1, 1);

-- 菜单与按钮
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

-- 管理员拥有全部菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu;

-- admin / admin123 (BCrypt)
INSERT INTO sys_user (id, dept_id, username, password, nickname, email, sex, status, token_version, create_time, update_time, deleted, version)
VALUES (1, 1, 'admin', '$2a$10$kszLIbA5uh3DlempUJQSYOvES1gXsxortyR2HfC4.r3UZJiH0Le.S', '管理员', 'admin@campus.local', 1, 1, 0, NOW(), NOW(), 0, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO sys_user_post (user_id, post_id) VALUES (1, 1);

-- 字典
INSERT INTO sys_dict_type (id, dict_name, dict_type, status) VALUES
(1, '用户性别', 'sys_user_sex', 1),
(2, '通用状态', 'sys_common_status', 1);

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, css_class, is_default, status) VALUES
(1, 'sys_user_sex', '未知', '0', 1, NULL, 1, 1),
(2, 'sys_user_sex', '男', '1', 2, NULL, 0, 1),
(3, 'sys_user_sex', '女', '2', 3, NULL, 0, 1),
(4, 'sys_common_status', '停用', '0', 1, 'danger', 0, 1),
(5, 'sys_common_status', '正常', '1', 2, 'primary', 1, 1);

-- 系统参数
INSERT INTO sys_config (id, config_name, config_key, config_value, remark) VALUES
(1, '最大登录失败次数', 'sys.account.maxLoginFail', '5', '达到次数后锁定账号'),
(2, '登录锁定时长（分钟）', 'sys.account.lockMinutes', '15', '账号锁定时长'),
(3, '文件大小上限（MB）', 'sys.file.maxSizeMb', '10', '单文件上传大小上限');
