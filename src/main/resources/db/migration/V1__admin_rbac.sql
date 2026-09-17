-- Admin RBAC foundation. Apply only after backup and schema pre-check.
-- This migration intentionally does not assign roles to existing administrators.

CREATE TABLE IF NOT EXISTS admin_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_admin_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_permission (
    permission_id BIGINT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    http_method VARCHAR(16) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_admin_permission_code (permission_code),
    UNIQUE KEY uk_admin_permission_route (http_method, request_path),
    KEY idx_admin_permission_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_user_role (
    admin_user_id INT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_user_id, role_id),
    CONSTRAINT fk_admin_user_role_user
        FOREIGN KEY (admin_user_id)
        REFERENCES tb_newbee_mall_admin_user (admin_user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_admin_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES admin_role (role_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_admin_role_permission_role
        FOREIGN KEY (role_id)
        REFERENCES admin_role (role_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_admin_role_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES admin_permission (permission_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO admin_permission
    (permission_code, permission_name, http_method, request_path)
VALUES
    ('admin.dashboard.read', '查看管理后台首页', 'GET', '/admin'),
    ('admin.dashboard.index', '查看管理后台首页', 'GET', '/admin/index*'),
    ('admin.profile.read', '查看管理员资料', 'GET', '/admin/profile'),
    ('admin.profile.write', '修改管理员资料', 'POST', '/admin/profile/name'),
    ('admin.profile.password', '修改管理员密码', 'POST', '/admin/profile/password'),
    ('admin.carousel.read', '查看轮播图', 'GET', '/admin/carousels/**'),
    ('admin.carousel.write', '维护轮播图', 'POST', '/admin/carousels/save'),
    ('admin.carousel.update', '更新轮播图', 'POST', '/admin/carousels/update'),
    ('admin.carousel.delete', '删除轮播图', 'POST', '/admin/carousels/delete'),
    ('admin.category.read', '查看商品分类', 'GET', '/admin/categories/**'),
    ('admin.category.write', '维护商品分类', 'POST', '/admin/categories/save'),
    ('admin.category.update', '更新商品分类', 'POST', '/admin/categories/update'),
    ('admin.category.delete', '删除商品分类', 'POST', '/admin/categories/delete'),
    ('admin.goods.read', '查看商品', 'GET', '/admin/goods/**'),
    ('admin.goods.write', '维护商品', 'POST', '/admin/goods/save'),
    ('admin.goods.update', '更新商品', 'POST', '/admin/goods/update'),
    ('admin.goods.status', '上下架商品', 'PUT', '/admin/goods/status/{sellStatus}'),
    ('admin.index-config.read', '查看首页配置', 'GET', '/admin/indexConfigs/**'),
    ('admin.index-config.write', '维护首页配置', 'POST', '/admin/indexConfigs/save'),
    ('admin.index-config.update', '更新首页配置', 'POST', '/admin/indexConfigs/update'),
    ('admin.index-config.delete', '删除首页配置', 'POST', '/admin/indexConfigs/delete'),
    ('admin.order.read', '查看订单', 'GET', '/admin/orders/**'),
    ('admin.order.item-read', '查看订单商品', 'GET', '/admin/order-items/**'),
    ('admin.order.update', '更新订单', 'POST', '/admin/orders/update'),
    ('admin.order.complete', '完成订单', 'POST', '/admin/orders/checkDone'),
    ('admin.order.ship', '订单出库', 'POST', '/admin/orders/checkOut'),
    ('admin.order.close', '关闭订单', 'POST', '/admin/orders/close'),
    ('admin.user.read', '查看用户', 'GET', '/admin/users/**'),
    ('admin.user.lock', '锁定或解锁用户', 'POST', '/admin/users/lock/{lockStatus}'),
    ('admin.upload.write', '上传图片', 'POST', '/admin/upload/file'),
    ('admin.upload.batch', '批量上传图片', 'POST', '/admin/upload/files');

-- Roles and admin_user_role assignments are deliberately left for a reviewed follow-up migration.
