-- instant_order 菜品模型优化迁移脚本
-- 执行前请先在测试库完整演练，并做好备份。

-- 1) dish 增加显式范围字段
ALTER TABLE dish
  ADD COLUMN dish_scope TINYINT NOT NULL DEFAULT 1 COMMENT '1标准菜 2门店特色菜' AFTER headquarters_id,
  ADD COLUMN standard_dish_id INT NULL COMMENT '来源标准菜ID(可空)' AFTER dish_scope;

-- 2) 回填 dish_scope
UPDATE dish
SET dish_scope = CASE
  WHEN store_id IS NULL THEN 1
  ELSE 2
END;

-- 3) 索引优化（支持门店菜单与用户端查询）
CREATE INDEX idx_dish_scope_hq_store_status_cat
  ON dish (dish_scope, headquarters_id, store_id, status, category_id);

CREATE INDEX idx_dish_store_scope_status
  ON dish (store_id, dish_scope, status);

-- 4) 名称唯一约束改造（允许不同门店特色菜重名）
-- 4.1 执行前必须先人工确认没有重复导致建索引失败
-- 建议先运行：
-- SELECT headquarters_id, store_id, dish_scope, name, COUNT(*) c
-- FROM dish
-- GROUP BY headquarters_id, store_id, dish_scope, name
-- HAVING c > 1;

ALTER TABLE dish DROP INDEX name;

CREATE UNIQUE INDEX uk_dish_scope_name
  ON dish (headquarters_id, store_id, dish_scope, name);

-- 5) 分类名称唯一建议改为总部+类型维度（可选但强烈建议）
-- 执行前建议先检查重复：
-- SELECT headquarters_id, type, name, COUNT(*) c
-- FROM category
-- GROUP BY headquarters_id, type, name
-- HAVING c > 1;

ALTER TABLE category DROP INDEX uk_category_name;

CREATE UNIQUE INDEX uk_category_hq_type_name
  ON category (headquarters_id, type, name);

-- 6) 历史数据收口：清理 store_dish 中的特色菜关联（只保留标准菜上架关系）
DELETE sd
FROM store_dish sd
INNER JOIN dish d ON d.id = sd.dish_id
WHERE d.dish_scope = 2;

-- 7) 校验建议
-- 7.1 菜品范围与 store_id 一致性
-- SELECT id, name, dish_scope, store_id FROM dish
-- WHERE (dish_scope = 1 AND store_id IS NOT NULL)
--    OR (dish_scope = 2 AND store_id IS NULL);

-- 7.2 特色菜与门店总部一致性
-- SELECT d.id, d.name, d.store_id, d.headquarters_id, s.headquarters_id AS store_hq
-- FROM dish d
-- JOIN store s ON s.id = d.store_id
-- WHERE d.dish_scope = 2 AND d.headquarters_id <> s.headquarters_id;
