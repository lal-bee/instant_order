-- 堂食系统清理：移除外卖遗留表与字段（可重复执行）

SET @db_name = DATABASE();

-- 1) 删除订单表外卖字段（若存在）
SET @exists_orders_address_book_id := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'address_book_id'
);
SET @sql_orders_address_book_id := IF(
    @exists_orders_address_book_id > 0,
    'ALTER TABLE orders DROP COLUMN address_book_id',
    'SELECT ''skip drop orders.address_book_id'''
);
PREPARE stmt FROM @sql_orders_address_book_id; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_orders_estimated_delivery_time := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'estimated_delivery_time'
);
SET @sql_orders_estimated_delivery_time := IF(
    @exists_orders_estimated_delivery_time > 0,
    'ALTER TABLE orders DROP COLUMN estimated_delivery_time',
    'SELECT ''skip drop orders.estimated_delivery_time'''
);
PREPARE stmt FROM @sql_orders_estimated_delivery_time; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_orders_delivery_status := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivery_status'
);
SET @sql_orders_delivery_status := IF(
    @exists_orders_delivery_status > 0,
    'ALTER TABLE orders DROP COLUMN delivery_status',
    'SELECT ''skip drop orders.delivery_status'''
);
PREPARE stmt FROM @sql_orders_delivery_status; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_orders_delivery_time := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'delivery_time'
);
SET @sql_orders_delivery_time := IF(
    @exists_orders_delivery_time > 0,
    'ALTER TABLE orders DROP COLUMN delivery_time',
    'SELECT ''skip drop orders.delivery_time'''
);
PREPARE stmt FROM @sql_orders_delivery_time; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists_orders_pack_amount := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'pack_amount'
);
SET @sql_orders_pack_amount := IF(
    @exists_orders_pack_amount > 0,
    'ALTER TABLE orders DROP COLUMN pack_amount',
    'SELECT ''skip drop orders.pack_amount'''
);
PREPARE stmt FROM @sql_orders_pack_amount; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 删除地址簿表（若存在）
SET @exists_address_book := (
    SELECT COUNT(1) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'address_book'
);
SET @sql_drop_address_book := IF(
    @exists_address_book > 0,
    'DROP TABLE address_book',
    'SELECT ''skip drop table address_book'''
);
PREPARE stmt FROM @sql_drop_address_book; EXECUTE stmt; DEALLOCATE PREPARE stmt;
