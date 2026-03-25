-- 店长-分店链路系统性纠偏 SQL（MySQL 8+）
-- 执行库：hanye_take_out
-- 目标模型：
-- employee.role: 2=董事长 1=店长 0=员工
-- employee.store_id: 店长允许为空
-- store.manager_employee_id: 关联店长

USE `hanye_take_out`;

START TRANSACTION;

-- 1) 解除 store.brand_id 的强制非空（后端分店新增流程未传 brand_id）
SET @has_fk_store_brand := (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND CONSTRAINT_NAME = 'fk_store_brand'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql_drop_fk_store_brand := IF(
    @has_fk_store_brand > 0,
    'ALTER TABLE `store` DROP FOREIGN KEY `fk_store_brand`',
    'SELECT ''skip drop fk_store_brand'''
);
PREPARE stmt FROM @sql_drop_fk_store_brand;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `store`
    MODIFY COLUMN `brand_id` int NULL COMMENT '历史字段（兼容保留）';

-- 2) 修复 employee 约束：董事长可空店铺、店长可空店铺、普通员工必须有店铺
SET @has_chk_employee_store_required := (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'employee'
      AND CONSTRAINT_NAME = 'chk_employee_store_required'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @sql_drop_chk_employee_store_required := IF(
    @has_chk_employee_store_required > 0,
    'ALTER TABLE `employee` DROP CHECK `chk_employee_store_required`',
    'SELECT ''skip drop chk_employee_store_required'''
);
PREPARE stmt FROM @sql_drop_chk_employee_store_required;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `employee`
    ADD CONSTRAINT `chk_employee_store_required`
        CHECK (
            ((`role` = 2) AND (`store_id` IS NULL))
            OR (`role` = 1)
            OR ((`role` = 0) AND (`store_id` IS NOT NULL))
        );

COMMIT;
