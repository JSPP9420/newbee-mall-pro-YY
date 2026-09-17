-- pro 版新增：管理端登录 Token 表（手动执行，本项目无 Flyway 自动迁移）
-- 与原项目迁移脚本风格保持一致：IF NOT EXISTS，可重复执行

CREATE TABLE IF NOT EXISTS tb_newbee_mall_admin_token (
    admin_token_id INT NOT NULL AUTO_INCREMENT,
    token VARCHAR(64) NOT NULL COMMENT '登录Token（UUID去横线）',
    admin_user_id INT NOT NULL COMMENT '管理员ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (admin_token_id),
    UNIQUE KEY uk_admin_token (token),
    KEY idx_admin_token_user (admin_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端登录Token';
