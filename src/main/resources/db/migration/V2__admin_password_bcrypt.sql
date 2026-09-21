-- V2: 管理员密码 MD5 -> BCrypt 平滑迁移
-- 背景：BCrypt 哈希固定 60 字符，原列 varchar(50) 放不下，需扩列
-- 迁移策略：懒升级（登录时 BCrypt 优先、MD5 兜底，MD5 命中后自动重写为 BCrypt），
--          无需停机、无需用户改密码，登录几次后库内密码全部升级为 BCrypt
-- 已在运行库执行：
--   ALTER TABLE tb_newbee_mall_admin_user
--     MODIFY COLUMN login_password varchar(100) NOT NULL COMMENT '管理员登陆密码(BCrypt 60位/兼容MD5 32位)';

ALTER TABLE tb_newbee_mall_admin_user
    MODIFY COLUMN login_password varchar(100) NOT NULL COMMENT '管理员登陆密码(BCrypt 60位/兼容MD5 32位)';
