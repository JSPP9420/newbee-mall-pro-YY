-- ============================================================
-- V6: 购物车表并发安全改造（物理删 + 唯一索引）
-- 背景：加购是 check-then-act（先查后插），并发下会重复插入；
--      软删行会占用唯一索引位导致"删除后再加购"撞索引。
-- 方案：购物车改为物理删，腾出索引位；(user_id, goods_id) 唯一索引
--      作为数据库层最后防线；代码捕获 DuplicateKeyException 转修改。
-- 注意：本脚本只动购物车表，其他表的 is_deleted 机制不受影响。
-- ============================================================

-- 1. 物理清理软删死行（原软删 = is_deleted=1 的行，购物车数据低价值，无需保留）
DELETE FROM tb_newbee_mall_shopping_cart_item WHERE is_deleted = 1;

-- 2. 存量去重：同 (user_id, goods_id) 只保留 cart_item_id 最大（最新）的一行，
--    否则第 3 步加唯一索引会失败
DELETE c1 FROM tb_newbee_mall_shopping_cart_item c1
JOIN tb_newbee_mall_shopping_cart_item c2
  ON c1.user_id = c2.user_id
 AND c1.goods_id = c2.goods_id
 AND c1.cart_item_id < c2.cart_item_id;

-- 3. 加唯一索引：数据库层保证一个用户的一种商品最多一行
ALTER TABLE tb_newbee_mall_shopping_cart_item
    ADD UNIQUE KEY uk_user_goods (user_id, goods_id);

-- 4. 删除 is_deleted 列（物理删方案下该列不再有语义）
ALTER TABLE tb_newbee_mall_shopping_cart_item
    DROP COLUMN is_deleted;

-- 验证：
-- SHOW INDEX FROM tb_newbee_mall_shopping_cart_item WHERE Key_name = 'uk_user_goods';
-- SHOW COLUMNS FROM tb_newbee_mall_shopping_cart_item LIKE 'is_deleted'; -- 应为空
