-- 企业级Spring Boot框架数据库初始化脚本
-- 数据库: Oracle 19c+

-- 创建用户表
CREATE TABLE t_user (
    id NUMBER(19) NOT NULL,
    username VARCHAR2(50) NOT NULL,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50),
    email VARCHAR2(100),
    phone VARCHAR2(20),
    status NUMBER(1) DEFAULT 1 NOT NULL,
    avatar VARCHAR2(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_user PRIMARY KEY (id),
    CONSTRAINT uk_t_user_username UNIQUE (username)
);

-- 创建角色表
CREATE TABLE t_role (
    id NUMBER(19) NOT NULL,
    role_name VARCHAR2(50) NOT NULL,
    role_code VARCHAR2(50) NOT NULL,
    description VARCHAR2(200),
    status NUMBER(1) DEFAULT 1 NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_role PRIMARY KEY (id),
    CONSTRAINT uk_t_role_code UNIQUE (role_code)
);

-- 创建用户角色关联表
CREATE TABLE t_user_role (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_user_role PRIMARY KEY (id),
    CONSTRAINT fk_t_user_role_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_t_user_role_role FOREIGN KEY (role_id) REFERENCES t_role(id)
);

-- 创建权限表
CREATE TABLE t_permission (
    id NUMBER(19) NOT NULL,
    permission_name VARCHAR2(50) NOT NULL,
    permission_code VARCHAR2(100) NOT NULL,
    resource_type VARCHAR2(20) NOT NULL,
    resource_url VARCHAR2(200),
    parent_id NUMBER(19),
    sort_order NUMBER(10) DEFAULT 0,
    icon VARCHAR2(100),
    description VARCHAR2(200),
    status NUMBER(1) DEFAULT 1 NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_permission PRIMARY KEY (id),
    CONSTRAINT uk_t_permission_code UNIQUE (permission_code)
);

-- 创建角色权限关联表
CREATE TABLE t_role_permission (
    id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    permission_id NUMBER(19) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_role_permission PRIMARY KEY (id),
    CONSTRAINT fk_t_role_permission_role FOREIGN KEY (role_id) REFERENCES t_role(id),
    CONSTRAINT fk_t_role_permission_perm FOREIGN KEY (permission_id) REFERENCES t_permission(id)
);

-- 创建系统配置表
CREATE TABLE t_sys_config (
    id NUMBER(19) NOT NULL,
    config_key VARCHAR2(100) NOT NULL,
    config_value CLOB,
    config_type VARCHAR2(20) DEFAULT 'string',
    description VARCHAR2(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_t_sys_config PRIMARY KEY (id),
    CONSTRAINT uk_t_sys_config_key UNIQUE (config_key)
);

-- 创建操作日志表
CREATE TABLE t_operation_log (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19),
    username VARCHAR2(50),
    operation VARCHAR2(50),
    method VARCHAR2(200),
    params CLOB,
    result CLOB,
    ip VARCHAR2(50),
    location VARCHAR2(100),
    user_agent VARCHAR2(500),
    status NUMBER(1),
    error_msg CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_t_operation_log PRIMARY KEY (id)
);

-- 创建登录日志表
CREATE TABLE t_login_log (
    id NUMBER(19) NOT NULL,
    username VARCHAR2(50),
    ip VARCHAR2(50),
    location VARCHAR2(100),
    browser VARCHAR2(100),
    os VARCHAR2(100),
    status NUMBER(1),
    msg VARCHAR2(200),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_t_login_log PRIMARY KEY (id)
);

-- 插入初始化数据

-- 插入管理员用户 (密码: admin)
INSERT INTO t_user (id, username, password, real_name, email, status) 
VALUES (1, 'admin', '$2a$10$7JB720yubVSOMV0H5nnZP.IhbU6B3SrEP1KcxqvjTM1YRKDC/T3bC', '系统管理员', 'admin@enterprise.com', 1);

-- 插入普通用户 (密码: user123)
INSERT INTO t_user (id, username, password, real_name, email, status) 
VALUES (2, 'user', '$2a$10$K6BPG5wnF8RGpVWD2gK6YeMYl9F1D1K1Y9P3P8I9W8Q3Q7V6U5T4S', '普通用户', 'user@enterprise.com', 1);

-- 插入角色数据
INSERT INTO t_role (id, role_name, role_code, description, status) 
VALUES (1, '超级管理员', 'ROLE_ADMIN', '系统超级管理员，拥有所有权限', 1);

INSERT INTO t_role (id, role_name, role_code, description, status) 
VALUES (2, '普通用户', 'ROLE_USER', '普通用户，拥有基本权限', 1);

-- 插入用户角色关联
INSERT INTO t_user_role (id, user_id, role_id) VALUES (1, 1, 1);
INSERT INTO t_user_role (id, user_id, role_id) VALUES (2, 2, 2);

-- 插入权限数据
INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (1, '系统管理', 'system', 'menu', '/system', NULL, 1);

INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (2, '用户管理', 'system:user', 'menu', '/system/user', 1, 1);

INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (3, '用户查询', 'system:user:query', 'button', '', 2, 1);

INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (4, '用户新增', 'system:user:add', 'button', '', 2, 2);

INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (5, '用户修改', 'system:user:edit', 'button', '', 2, 3);

INSERT INTO t_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort_order) 
VALUES (6, '用户删除', 'system:user:delete', 'button', '', 2, 4);

-- 插入角色权限关联
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (1, 1, 1);
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (2, 1, 2);
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (3, 1, 3);
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (4, 1, 4);
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (5, 1, 5);
INSERT INTO t_role_permission (id, role_id, permission_id) VALUES (6, 1, 6);

-- 插入系统配置
INSERT INTO t_sys_config (id, config_key, config_value, description) 
VALUES (1, 'sys.title', '企业级Spring Boot框架', '系统标题');

INSERT INTO t_sys_config (id, config_key, config_value, description) 
VALUES (2, 'sys.version', '1.0.0', '系统版本号');

INSERT INTO t_sys_config (id, config_key, config_value, description) 
VALUES (3, 'sys.upload.maxSize', '10485760', '文件上传最大大小(字节)');

-- 创建序列
CREATE SEQUENCE seq_t_user START WITH 3 INCREMENT BY 1;
CREATE SEQUENCE seq_t_role START WITH 3 INCREMENT BY 1;
CREATE SEQUENCE seq_t_user_role START WITH 3 INCREMENT BY 1;
CREATE SEQUENCE seq_t_permission START WITH 7 INCREMENT BY 1;
CREATE SEQUENCE seq_t_role_permission START WITH 7 INCREMENT BY 1;
CREATE SEQUENCE seq_t_sys_config START WITH 4 INCREMENT BY 1;
CREATE SEQUENCE seq_t_operation_log START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_t_login_log START WITH 1 INCREMENT BY 1;

-- 创建索引
CREATE INDEX idx_t_user_username ON t_user(username);
CREATE INDEX idx_t_user_email ON t_user(email);
CREATE INDEX idx_t_user_status ON t_user(status);
CREATE INDEX idx_t_user_create_time ON t_user(create_time);

CREATE INDEX idx_t_role_code ON t_role(role_code);
CREATE INDEX idx_t_role_status ON t_role(status);

CREATE INDEX idx_t_permission_code ON t_permission(permission_code);
CREATE INDEX idx_t_permission_parent ON t_permission(parent_id);
CREATE INDEX idx_t_permission_type ON t_permission(resource_type);

CREATE INDEX idx_t_operation_log_user ON t_operation_log(user_id);
CREATE INDEX idx_t_operation_log_time ON t_operation_log(operation_time);

CREATE INDEX idx_t_login_log_username ON t_login_log(username);
CREATE INDEX idx_t_login_log_time ON t_login_log(login_time);

-- 提交事务
COMMIT;