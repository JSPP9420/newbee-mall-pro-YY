-- M1a: 用户表 + Token 表
-- 对比原项目改进点：
-- 1. login_name 加了 UNIQUE 索引（原项目没有，并发注册会重复）
-- 2. 字段名用 password 而非 password_md5（存的是 BCrypt，不是 MD5）
-- 3. 用 status 代替 locked_flag（语义更通用）
-- 4. 用 utf8mb4 而非 utf8（支持 emoji）

CREATE TABLE IF NOT EXISTS `mall_user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `login_name`     VARCHAR(20)  NOT NULL COMMENT '登录名',
    `password`       VARCHAR(100) NOT NULL COMMENT '密码哈希（BCrypt）',
    `nick_name`      VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `introduce_sign` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '个性签名',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0正常 1锁定',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `mall_token` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `token`       VARCHAR(36) NOT NULL COMMENT 'Token（UUID）',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `expires_at`  DATETIME    NOT NULL COMMENT '过期时间',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录Token表';
