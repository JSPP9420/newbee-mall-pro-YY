-- Expand the legacy password column before storing BCrypt hashes.
-- Apply after backup; existing 32-character MD5 hashes remain valid for lazy migration.
ALTER TABLE tb_newbee_mall_user
    MODIFY COLUMN password_md5 VARCHAR(100) NOT NULL DEFAULT ''
    COMMENT '密码哈希（兼容旧MD5，新增密码使用BCrypt）';
