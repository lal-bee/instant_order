-- 单总部模型迁移脚本（由你手动执行）
-- 目标：删除总部概念，系统仅按门店(store_id)隔离
-- 执行前请完整备份，并先在测试库演练。

-- =========================
-- 0) 预检查
-- =========================
-- SHOW CREATE TABLE store;
-- SHOW CREATE TABLE dish;
-- SHOW CREATE TABLE category;

-- =========================
-- 1) 去掉 store.headquarters_id 外键、索引、字段（幂等）
-- =========================
SET @fk_store_hq := (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'headquarters_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql_drop_fk_store_hq := IF(@fk_store_hq IS NOT NULL,
    CONCAT('ALTER TABLE store DROP FOREIGN KEY ', @fk_store_hq),
    "SELECT 'skip drop store headquarters fk'"
);
PREPARE stmt FROM @sql_drop_fk_store_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_store_hq := (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'headquarters_id'
      AND INDEX_NAME <> 'PRIMARY'
    LIMIT 1
);
SET @sql_drop_idx_store_hq := IF(@idx_store_hq IS NOT NULL,
    CONCAT('ALTER TABLE store DROP INDEX ', @idx_store_hq),
    "SELECT 'skip drop store headquarters index'"
);
PREPARE stmt FROM @sql_drop_idx_store_hq;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_store_hq_col := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'headquarters_id'
);
SET @sql_drop_store_hq_col := IF(@exists_store_hq_col > 0,
    "ALTER TABLE store DROP COLUMN headquarters_id",
    "SELECT 'skip drop store.headquarters_id'"
);
PREPARE stmt FROM @sql_drop_store_hq_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- 2) 去掉 dish/category 的 headquarters_id 字段（幂等）
-- =========================
SET @exists_dish_hq_col := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dish'
      AND COLUMN_NAME = 'headquarters_id'
);
SET @sql_drop_dish_hq_col := IF(@exists_dish_hq_col > 0,
    "ALTER TABLE dish DROP COLUMN headquarters_id",
    "SELECT 'skip drop dish.headquarters_id'"
);
PREPARE stmt FROM @sql_drop_dish_hq_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_category_hq_col := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'category'
      AND COLUMN_NAME = 'headquarters_id'
);
SET @sql_drop_category_hq_col := IF(@exists_category_hq_col > 0,
    "ALTER TABLE category DROP COLUMN headquarters_id",
    "SELECT 'skip drop category.headquarters_id'"
);
PREPARE stmt FROM @sql_drop_category_hq_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- 3) 重建菜品唯一规则（允许不同门店特色菜重名）
-- 标准菜: (dish_scope=1, store_id=NULL) 名称全局唯一
-- 特色菜: (dish_scope=2, store_id=门店ID) 名称在同门店唯一
-- =========================

SET @exists_scope_store_key := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dish'
      AND COLUMN_NAME = 'scope_store_key'
);
SET @sql_add_scope_store_key := IF(@exists_scope_store_key = 0,
    "ALTER TABLE dish ADD COLUMN scope_store_key BIGINT GENERATED ALWAYS AS (IFNULL(store_id, 0)) STORED",
    "SELECT 'skip add dish.scope_store_key'"
);
PREPARE stmt FROM @sql_add_scope_store_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_uk_dish_scope_store_name := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dish'
      AND INDEX_NAME = 'uk_dish_scope_store_name'
);
SET @sql_add_uk_dish_scope_store_name := IF(@exists_uk_dish_scope_store_name = 0,
    "CREATE UNIQUE INDEX uk_dish_scope_store_name ON dish (dish_scope, scope_store_key, name)",
    "SELECT 'skip add uk_dish_scope_store_name'"
);
PREPARE stmt FROM @sql_add_uk_dish_scope_store_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- 4) 分类唯一规则（单总部模型）
-- =========================
SET @exists_uk_category_type_name := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'category'
      AND INDEX_NAME = 'uk_category_type_name'
);
SET @sql_add_uk_category_type_name := IF(@exists_uk_category_type_name = 0,
    "CREATE UNIQUE INDEX uk_category_type_name ON category (type, name)",
    "SELECT 'skip add uk_category_type_name'"
);
PREPARE stmt FROM @sql_add_uk_category_type_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- 5) store_dish 职责收敛（只保留标准菜上架关系）
-- =========================
DELETE sd
FROM store_dish sd
INNER JOIN dish d ON d.id = sd.dish_id
WHERE d.dish_scope = 2;

-- =========================
-- 6) 删除总部表（无依赖时）
-- =========================
DROP TABLE IF EXISTS headquarters;

-- =========================
-- 7) 校验建议
-- =========================
-- 7.1 dish_scope 与 store_id 一致性
-- SELECT id, name, dish_scope, store_id
-- FROM dish
-- WHERE (dish_scope = 1 AND store_id IS NOT NULL)
--    OR (dish_scope = 2 AND store_id IS NULL);

-- 7.2 门店特色菜同门店重名检查（应为0行）
-- SELECT store_id, name, COUNT(*) c
-- FROM dish
-- WHERE dish_scope = 2
-- GROUP BY store_id, name
-- HAVING c > 1;
