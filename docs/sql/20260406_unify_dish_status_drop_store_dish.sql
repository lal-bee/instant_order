-- =====================================================
-- 20260406_unify_dish_status_drop_store_dish.sql
-- 目标：
-- 1) 标准菜/特色菜统一使用 dish.status
-- 2) dish.store_id 为空=标准菜，非空=门店特色菜
-- 3) 不保留 store_dish，直接删除
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) 回填 dish_scope（若保留 dish_scope 字段）
UPDATE dish
SET dish_scope = CASE WHEN store_id IS NULL THEN 1 ELSE 2 END
WHERE dish_scope IS NULL OR dish_scope NOT IN (1, 2);

-- 2) dish.status 兜底
UPDATE dish
SET status = 1
WHERE status IS NULL;

-- 3) 生成列：scope_store_key（NULL store_id 归一为0）
SET @has_scope_store_key := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dish'
    AND COLUMN_NAME = 'scope_store_key'
);
SET @sql_add_scope_store_key := IF(
  @has_scope_store_key = 0,
  'ALTER TABLE dish ADD COLUMN scope_store_key BIGINT GENERATED ALWAYS AS (IFNULL(store_id, 0)) STORED',
  'SELECT "skip add dish.scope_store_key"'
);
PREPARE stmt FROM @sql_add_scope_store_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 唯一索引：标准菜全局唯一；特色菜同门店唯一
SET @has_uk_dish_scope_store_name := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dish'
    AND INDEX_NAME = 'uk_dish_scope_store_name'
);
SET @sql_add_uk_dish_scope_store_name := IF(
  @has_uk_dish_scope_store_name = 0,
  'CREATE UNIQUE INDEX uk_dish_scope_store_name ON dish(dish_scope, scope_store_key, name)',
  'SELECT "skip add uk_dish_scope_store_name"'
);
PREPARE stmt FROM @sql_add_uk_dish_scope_store_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 直接删除 store_dish（不保留备份）
DROP TABLE IF EXISTS store_dish;

-- 6) 校验
-- 6.1 dish_scope 与 store_id 一致性（应返回0行）
SELECT id, name, dish_scope, store_id
FROM dish
WHERE (dish_scope = 1 AND store_id IS NOT NULL)
   OR (dish_scope = 2 AND store_id IS NULL);

-- 6.2 同门店特色菜重名检查（应返回0行）
SELECT store_id, name, COUNT(*) c
FROM dish
WHERE dish_scope = 2
GROUP BY store_id, name
HAVING c > 1;
