-- 标准菜/门店特色菜统一模型迁移（单总部模型）

-- 1) dish 增加作用域字段（幂等）
SET @exists_dish_scope := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'dish_scope'
);
SET @sql_dish_scope := IF(@exists_dish_scope = 0,
    "ALTER TABLE dish ADD COLUMN dish_scope TINYINT NOT NULL DEFAULT 1 COMMENT '1标准菜 2门店特色菜'",
    "SELECT 'skip add dish.dish_scope'"
);
PREPARE stmt FROM @sql_dish_scope;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_standard_dish_id := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'standard_dish_id'
);
SET @sql_standard_dish_id := IF(@exists_standard_dish_id = 0,
    "ALTER TABLE dish ADD COLUMN standard_dish_id INT NULL COMMENT '来源标准菜ID，可空'",
    "SELECT 'skip add dish.standard_dish_id'"
);
PREPARE stmt FROM @sql_standard_dish_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 回填 dish_scope（兼容旧数据：store_id 为空视为标准菜）
UPDATE dish
SET dish_scope = CASE
    WHEN store_id IS NULL THEN 1
    ELSE 2
END
WHERE dish_scope IS NULL OR dish_scope NOT IN (1, 2);

-- 3) 门店上架关联表（只承载标准菜上架状态）
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

-- 4) 按 store.id 的真实类型对齐 store_dish.store_id
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
SET @exists_idx_dish_scope := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND INDEX_NAME = 'idx_dish_scope'
);
SET @sql_idx_dish_scope := IF(@exists_idx_dish_scope = 0,
    "ALTER TABLE dish ADD INDEX idx_dish_scope (dish_scope)",
    "SELECT 'skip add idx_dish_scope'"
);
PREPARE stmt FROM @sql_idx_dish_scope;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
