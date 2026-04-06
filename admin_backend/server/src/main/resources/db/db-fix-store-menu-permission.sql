-- 门店菜单权限与数据修复脚本（手工执行）
-- 建议在 instant_order 库执行

-- 1) 确保门店菜品映射表存在
CREATE TABLE IF NOT EXISTS store_dish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '门店ID',
    dish_id INT NOT NULL COMMENT '菜品ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME NOT NULL DEFAULT NOW(),
    update_time DATETIME NOT NULL DEFAULT NOW(),
    create_user INT NOT NULL DEFAULT 0,
    update_user INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_store_dish UNIQUE (store_id, dish_id)
);

-- 2) 对齐 store_dish.store_id 与 store.id 类型
SET @store_id_column_type := (
    SELECT COLUMN_TYPE FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'id'
);
SET @sql_align_store_dish_store_id := CONCAT(
    "ALTER TABLE store_dish MODIFY COLUMN store_id ",
    @store_id_column_type,
    " NOT NULL COMMENT '门店ID'"
);
PREPARE stmt FROM @sql_align_store_dish_store_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 补齐 status 字段（兼容旧结构）
SET @exists_store_dish_status := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_dish' AND COLUMN_NAME = 'status'
);
SET @sql_store_dish_status := IF(
    @exists_store_dish_status = 0,
    "ALTER TABLE store_dish ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架'",
    "SELECT 'skip add store_dish.status'"
);
PREPARE stmt FROM @sql_store_dish_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 补齐索引
SET @exists_idx_store_dish_store_status := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_dish' AND INDEX_NAME = 'idx_store_dish_store_status'
);
SET @sql_idx_store_dish_store_status := IF(
    @exists_idx_store_dish_store_status = 0,
    "ALTER TABLE store_dish ADD INDEX idx_store_dish_store_status (store_id, status)",
    "SELECT 'skip add idx_store_dish_store_status'"
);
PREPARE stmt FROM @sql_idx_store_dish_store_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_idx_store_dish_dish := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_dish' AND INDEX_NAME = 'idx_store_dish_dish'
);
SET @sql_idx_store_dish_dish := IF(
    @exists_idx_store_dish_dish = 0,
    "ALTER TABLE store_dish ADD INDEX idx_store_dish_dish (dish_id)",
    "SELECT 'skip add idx_store_dish_dish'"
);
PREPARE stmt FROM @sql_idx_store_dish_dish;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 历史数据补齐：为已有门店特色菜补齐 store_dish 映射
INSERT INTO store_dish (store_id, dish_id, status, create_time, update_time, create_user, update_user)
SELECT d.store_id, d.id, IFNULL(d.status, 1), NOW(), NOW(), 0, 0
FROM dish d
         LEFT JOIN store_dish sd ON sd.store_id = d.store_id AND sd.dish_id = d.id
WHERE d.store_id IS NOT NULL
  AND sd.id IS NULL;
