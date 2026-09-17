ALTER TABLE tb_newbee_mall_order
    ADD COLUMN request_token varchar(64) NULL COMMENT '订单创建幂等请求令牌' AFTER order_no,
    ADD UNIQUE KEY uk_order_no (order_no),
    ADD UNIQUE KEY uk_order_user_request_token (user_id, request_token);
