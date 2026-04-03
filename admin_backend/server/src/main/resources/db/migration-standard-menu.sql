-- 标准固定菜单（总店维护 + 门店上架）增量迁移

-- 1) 分类增加总部字段
SET @exists_category_hq := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND COLUMN_NAME = 'headquarters_id'
);
SET @sql_category_hq := IF(@exists_category_hq = 0,
    "ALTER TABLE category ADD COLUMN headquarters_id BIGINT NULL COMMENT '所属总部ID'",
    "SELECT 'skip add category.headquarters_id'"
);
PREPARE stmt FROM @sql_category_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 菜品增加总部字段
SET @exists_dish_hq := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'headquarters_id'
);
SET @sql_dish_hq := IF(@exists_dish_hq = 0,
    "ALTER TABLE dish ADD COLUMN headquarters_id BIGINT NULL COMMENT '所属总部ID'",
    "SELECT 'skip add dish.headquarters_id'"
);
PREPARE stmt FROM @sql_dish_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 门店上架菜品关联表
CREATE TABLE IF NOT EXISTS store_dish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '门店ID',
    dish_id INT NOT NULL COMMENT '标准菜品ID(复用dish.id)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME NOT NULL DEFAULT NOW(),
    update_time DATETIME NOT NULL DEFAULT NOW(),
    create_user INT NOT NULL,
    update_user INT NOT NULL,
    CONSTRAINT uk_store_dish UNIQUE (store_id, dish_id)
);

-- 4) 按 store.id 的真实类型对齐 store_dish.store_id（避免 3780 外键类型不兼容）
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

-- 5) 外键（幂等）
SET @exists_fk_store_dish_store := (
    SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_dish'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME = 'fk_store_dish_store'
);
SET @sql_fk_store_dish_store := IF(@exists_fk_store_dish_store = 0,
    "ALTER TABLE store_dish ADD CONSTRAINT fk_store_dish_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE ON UPDATE CASCADE",
    "SELECT 'skip add fk_store_dish_store'"
);
PREPARE stmt FROM @sql_fk_store_dish_store;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_fk_store_dish_dish := (
    SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_dish'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME = 'fk_store_dish_dish'
);
SET @sql_fk_store_dish_dish := IF(@exists_fk_store_dish_dish = 0,
    "ALTER TABLE store_dish ADD CONSTRAINT fk_store_dish_dish FOREIGN KEY (dish_id) REFERENCES dish(id) ON DELETE CASCADE ON UPDATE CASCADE",
    "SELECT 'skip add fk_store_dish_dish'"
);
PREPARE stmt FROM @sql_fk_store_dish_dish;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) 索引（幂等）
SET @exists_idx_category_hq := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND INDEX_NAME = 'idx_category_hq'
);
SET @sql_idx_category_hq := IF(@exists_idx_category_hq = 0,
    "ALTER TABLE category ADD INDEX idx_category_hq (headquarters_id)",
    "SELECT 'skip add idx_category_hq'"
);
PREPARE stmt FROM @sql_idx_category_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_idx_dish_hq := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND INDEX_NAME = 'idx_dish_hq'
);
SET @sql_idx_dish_hq := IF(@exists_idx_dish_hq = 0,
    "ALTER TABLE dish ADD INDEX idx_dish_hq (headquarters_id)",
    "SELECT 'skip add idx_dish_hq'"
);
PREPARE stmt FROM @sql_idx_dish_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
