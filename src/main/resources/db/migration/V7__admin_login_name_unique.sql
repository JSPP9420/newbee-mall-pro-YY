-- ============================================================
-- V7: 管理员登录名唯一索引（安全加固 #5）
-- 背景：tb_newbee_mall_admin_user.login_user_name 原表只有主键，
--      改登录名接口无唯一性校验，可能改出重名；
--      重名后 selectByLoginUserName 命中哪条不确定，可能锁死账号。
-- 方案：数据库唯一索引兜底 + Service 预检查（友好提示）+
--      捕获 DuplicateKeyException（覆盖预检查盲区：被锁定账号占用的
--      登录名因 selectByLoginUserName 带 locked=0 过滤而查不到）。
-- ============================================================

-- 1. 存量去重：同 login_user_name 只保留 admin_user_id 最小（最早创建）的一行
--    （默认数据只有一个 admin 账号，此步为保险，正常应无删除）
DELETE a1 FROM tb_newbee_mall_admin_user a1
JOIN tb_newbee_mall_admin_user a2
  ON a1.login_user_name = a2.login_user_name
 AND a1.admin_user_id > a2.admin_user_id;

-- 2. 加唯一索引：数据库层保证登录名全局唯一
ALTER TABLE tb_newbee_mall_admin_user
    ADD UNIQUE KEY uk_login_user_name (login_user_name);

-- 验证：
-- SHOW INDEX FROM tb_newbee_mall_admin_user WHERE Key_name = 'uk_login_user_name';
