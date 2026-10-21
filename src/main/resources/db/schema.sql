-- Campus 脚手架数据库结构（仅用于初始化，重建会丢失数据）
CREATE DATABASE IF NOT EXISTS campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus;

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
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    ancestors   VARCHAR(512) NOT NULL DEFAULT '0',
    name        VARCHAR(64)  NOT NULL,
    leader      VARCHAR(64)           DEFAULT NULL,
    phone       VARCHAR(20)           DEFAULT NULL,
    email       VARCHAR(128)          DEFAULT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    create_by   BIGINT                DEFAULT NULL,
    create_time DATETIME              DEFAULT NULL,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME              DEFAULT NULL,
    remark      VARCHAR(500)          DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    KEY idx_dept_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_id       BIGINT               DEFAULT NULL,
    username      VARCHAR(64)  NOT NULL,
    password      VARCHAR(128) NOT NULL,
    nickname      VARCHAR(64)           DEFAULT NULL,
    email         VARCHAR(128)          DEFAULT NULL,
    phone         VARCHAR(20)           DEFAULT NULL,
    sex           TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未知 1=男 2=女',
    avatar        VARCHAR(512)          DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    token_version INT          NOT NULL DEFAULT 0,
    login_ip      VARCHAR(64)           DEFAULT NULL,
    login_time    DATETIME              DEFAULT NULL,
    create_by     BIGINT                DEFAULT NULL,
    create_time   DATETIME              DEFAULT NULL,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME              DEFAULT NULL,
    remark        VARCHAR(500)          DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_dept (dept_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    code        VARCHAR(64) NOT NULL,
    sort        INT         NOT NULL DEFAULT 0,
    data_scope  TINYINT     NOT NULL DEFAULT 5 COMMENT '1=全部 2=自定义 3=本部门 4=本部门及以下 5=仅本人',
    status      TINYINT     NOT NULL DEFAULT 1,
    create_by   BIGINT               DEFAULT NULL,
    create_time DATETIME             DEFAULT NULL,
    update_by   BIGINT               DEFAULT NULL,
    update_time DATETIME             DEFAULT NULL,
    remark      VARCHAR(500)         DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(64)  NOT NULL,
    path        VARCHAR(255)          DEFAULT NULL,
    component   VARCHAR(255)          DEFAULT NULL,
    perms       VARCHAR(128)          DEFAULT NULL,
    type        CHAR(1)      NOT NULL COMMENT 'M=目录 C=菜单 F=按钮',
    icon        VARCHAR(64)           DEFAULT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    visible     TINYINT      NOT NULL DEFAULT 1,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_by   BIGINT                DEFAULT NULL,
    create_time DATETIME              DEFAULT NULL,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME              DEFAULT NULL,
    remark      VARCHAR(500)          DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    KEY idx_menu_parent (parent_id),
    KEY idx_menu_perms (perms)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_post (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64) NOT NULL,
    name        VARCHAR(64) NOT NULL,
    sort        INT         NOT NULL DEFAULT 0,
    status      TINYINT     NOT NULL DEFAULT 1,
    create_by   BIGINT               DEFAULT NULL,
    create_time DATETIME             DEFAULT NULL,
    update_by   BIGINT               DEFAULT NULL,
    update_time DATETIME             DEFAULT NULL,
    remark      VARCHAR(500)         DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    UNIQUE KEY uk_post_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_role_role (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id),
    KEY idx_user_post_post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_role_menu_menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id),
    KEY idx_role_dept_dept (dept_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_dict_type (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name   VARCHAR(64) NOT NULL,
    dict_type   VARCHAR(64) NOT NULL,
    status      TINYINT     NOT NULL DEFAULT 1,
    create_by   BIGINT               DEFAULT NULL,
    create_time DATETIME             DEFAULT NULL,
    update_by   BIGINT               DEFAULT NULL,
    update_time DATETIME             DEFAULT NULL,
    remark      VARCHAR(500)         DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_dict_data (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type   VARCHAR(64) NOT NULL,
    dict_label  VARCHAR(64) NOT NULL,
    dict_value  VARCHAR(64) NOT NULL,
    dict_sort   INT         NOT NULL DEFAULT 0,
    css_class   VARCHAR(64)          DEFAULT NULL,
    is_default  TINYINT     NOT NULL DEFAULT 0,
    status      TINYINT     NOT NULL DEFAULT 1,
    create_by   BIGINT               DEFAULT NULL,
    create_time DATETIME             DEFAULT NULL,
    update_by   BIGINT               DEFAULT NULL,
    update_time DATETIME             DEFAULT NULL,
    remark      VARCHAR(500)         DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_data (dict_type, dict_value),
    KEY idx_dict_data_type (dict_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name  VARCHAR(64)   NOT NULL,
    config_key   VARCHAR(128)  NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    create_by    BIGINT                 DEFAULT NULL,
    create_time  DATETIME               DEFAULT NULL,
    update_by    BIGINT                 DEFAULT NULL,
    update_time  DATETIME               DEFAULT NULL,
    remark       VARCHAR(500)           DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    version      INT           NOT NULL DEFAULT 0,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_oper_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(64)  NOT NULL,
    business_type  VARCHAR(32)  NOT NULL,
    method         VARCHAR(255)          DEFAULT NULL,
    request_method VARCHAR(16)           DEFAULT NULL,
    oper_user_id   BIGINT                DEFAULT NULL,
    oper_name      VARCHAR(64)           DEFAULT NULL,
    oper_url       VARCHAR(512)          DEFAULT NULL,
    oper_ip        VARCHAR(64)           DEFAULT NULL,
    oper_location  VARCHAR(128)          DEFAULT NULL,
    oper_param     TEXT,
    json_result    TEXT,
    success        TINYINT      NOT NULL DEFAULT 1,
    error_msg      TEXT,
    cost_time      BIGINT       NOT NULL DEFAULT 0,
    oper_time      DATETIME     NOT NULL,
    KEY idx_oper_time (oper_time),
    KEY idx_oper_user (oper_user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_login_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(64)  NOT NULL,
    user_id    BIGINT                DEFAULT NULL,
    ip         VARCHAR(64)           DEFAULT NULL,
    location   VARCHAR(128)          DEFAULT NULL,
    browser    VARCHAR(64)           DEFAULT NULL,
    os         VARCHAR(64)           DEFAULT NULL,
    success    TINYINT      NOT NULL DEFAULT 1,
    message    VARCHAR(255)          DEFAULT NULL,
    login_time DATETIME     NOT NULL,
    KEY idx_login_time (login_time),
    KEY idx_login_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_file (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(128) NOT NULL,
    stored_path   VARCHAR(512) NOT NULL,
    url           VARCHAR(512)          DEFAULT NULL,
    size          BIGINT       NOT NULL,
    content_type  VARCHAR(128)          DEFAULT NULL,
    sha256        CHAR(64)              DEFAULT NULL,
    scene         VARCHAR(32)  NOT NULL,
    uploader_id   BIGINT                DEFAULT NULL,
    create_by     BIGINT                DEFAULT NULL,
    create_time   DATETIME              DEFAULT NULL,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME              DEFAULT NULL,
    remark        VARCHAR(500)          DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    KEY idx_file_uploader (uploader_id),
    KEY idx_file_sha256 (sha256)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
