-- 扫码点餐（tableId）兼容补丁
-- 目标：
-- 1) table_info 不存在则创建
-- 2) orders 缺失 store_id/table_id/table_no 时补齐
-- 3) 按现有门店补充测试桌号 A01/A02/A03（幂等）

CREATE TABLE IF NOT EXISTS `table_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '餐桌id',
  `store_id` bigint NOT NULL COMMENT '所属门店id',
  `table_no` varchar(50) NOT NULL COMMENT '桌号',
  `status` tinyint DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_table` (`store_id`, `table_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐桌表';

ALTER TABLE `orders`
  ADD COLUMN IF NOT EXISTS `store_id` bigint NULL COMMENT '门店id',
  ADD COLUMN IF NOT EXISTS `table_id` bigint NULL COMMENT '餐桌id',
  ADD COLUMN IF NOT EXISTS `table_no` varchar(50) NULL COMMENT '桌号';

INSERT INTO `table_info` (`store_id`, `table_no`, `status`)
SELECT s.id, t.table_no, 1
FROM `store` s
         CROSS JOIN (
    SELECT 'A01' AS table_no
    UNION ALL SELECT 'A02'
    UNION ALL SELECT 'A03'
) t
         LEFT JOIN `table_info` ti
                   ON ti.store_id = s.id AND ti.table_no = t.table_no
WHERE ti.id IS NULL;
