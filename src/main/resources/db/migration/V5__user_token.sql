-- pro 版新增：用户端登录 Token 表（手动执行，本项目无 Flyway 自动迁移）
-- 与管理端 tb_newbee_mall_admin_token 同套路，区别：user_id 为 bigint（用户表主键）

CREATE TABLE IF NOT EXISTS tb_newbee_mall_user_token (
    user_token_id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(64) NOT NULL COMMENT '登录Token（UUID去横线）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_token_id),
    UNIQUE KEY uk_user_token (token),
    KEY idx_user_token_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户端登录Token';
