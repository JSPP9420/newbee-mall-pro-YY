-- V3: 用户表密码列改名，去掉 MD5 历史包袱
-- password_md5 -> login_password（与管理端 tb_newbee_mall_admin_user.login_password 命名统一）
-- 已在运行库执行：
--   ALTER TABLE tb_newbee_mall_user
--     CHANGE COLUMN password_md5 login_password varchar(100) NOT NULL DEFAULT '' COMMENT '登录密码(BCrypt 60位)';

ALTER TABLE tb_newbee_mall_user
    CHANGE COLUMN password_md5 login_password varchar(100) NOT NULL DEFAULT '' COMMENT '登录密码(BCrypt 60位)';
