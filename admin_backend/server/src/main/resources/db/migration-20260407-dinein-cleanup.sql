-- 堂食点餐语义清理
-- 说明：
-- 1) 对已有堂食单补齐就餐信息文案

UPDATE orders
SET address = CONCAT('门店：', IFNULL(store_id, ''), ' / 桌号：', IFNULL(table_no, ''))
WHERE (address IS NULL OR address = '')
  AND table_no IS NOT NULL;
