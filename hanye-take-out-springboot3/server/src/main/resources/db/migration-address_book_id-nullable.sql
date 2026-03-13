-- 堂食订单无配送地址，address_book_id 需允许 NULL
-- 在已有数据库上执行此脚本即可修复 Column 'address_book_id' cannot be null 错误
USE hanye_take_out;
ALTER TABLE orders MODIFY COLUMN address_book_id bigint DEFAULT NULL COMMENT '地址id（堂食为null）';
