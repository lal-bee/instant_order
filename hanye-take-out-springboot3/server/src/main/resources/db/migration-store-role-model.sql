-- 总店-分店-员工权限模型增量迁移

-- 1) 新增总店表
CREATE TABLE IF NOT EXISTS headquarters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT NOW(),
    update_time DATETIME NOT NULL DEFAULT NOW(),
    create_user INT NOT NULL,
    update_user INT NOT NULL
);

-- 2) 新增分店表
CREATE TABLE IF NOT EXISTS store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    headquarters_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    manager_employee_id INT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT NOW(),
    update_time DATETIME NOT NULL DEFAULT NOW(),
    create_user INT NOT NULL,
    update_user INT NOT NULL,
    CONSTRAINT fk_store_headquarters FOREIGN KEY (headquarters_id) REFERENCES headquarters(id) ON UPDATE CASCADE
);

-- 3) employee 增加分店和角色字段（幂等）
SET @exists_store_id := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND COLUMN_NAME = 'store_id'
);
SET @sql_store_id := IF(@exists_store_id = 0,
    "ALTER TABLE employee ADD COLUMN store_id BIGINT NULL COMMENT '所属分店ID'",
    "SELECT 'skip add employee.store_id'"
);
PREPARE stmt FROM @sql_store_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_role := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND COLUMN_NAME = 'role'
);
SET @sql_role := IF(@exists_role = 0,
    "ALTER TABLE employee ADD COLUMN role VARCHAR(32) NULL COMMENT '角色：CHAIRMAN/MANAGER/EMPLOYEE'",
    "SELECT 'skip add employee.role'"
);
PREPARE stmt FROM @sql_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果历史数据存在，先按默认值回填（请按实际业务调整默认 store_id）
UPDATE employee SET role = 'EMPLOYEE' WHERE role IS NULL;
UPDATE employee SET store_id = 1 WHERE store_id IS NULL;

ALTER TABLE employee MODIFY COLUMN store_id BIGINT NOT NULL COMMENT '所属分店ID';
ALTER TABLE employee MODIFY COLUMN role VARCHAR(32) NOT NULL COMMENT '角色：CHAIRMAN/MANAGER/EMPLOYEE';

-- 4) 索引和约束（幂等）
SET @exists_idx_store := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND INDEX_NAME = 'idx_employee_store_id'
);
SET @sql_idx_store := IF(@exists_idx_store = 0,
    "ALTER TABLE employee ADD INDEX idx_employee_store_id (store_id)",
    "SELECT 'skip add idx_employee_store_id'"
);
PREPARE stmt FROM @sql_idx_store;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_idx_role := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND INDEX_NAME = 'idx_employee_role'
);
SET @sql_idx_role := IF(@exists_idx_role = 0,
    "ALTER TABLE employee ADD INDEX idx_employee_role (role)",
    "SELECT 'skip add idx_employee_role'"
);
PREPARE stmt FROM @sql_idx_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_fk_employee_store := (
    SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME = 'fk_employee_store'
);
SET @sql_fk_employee_store := IF(@exists_fk_employee_store = 0,
    "ALTER TABLE employee ADD CONSTRAINT fk_employee_store FOREIGN KEY (store_id) REFERENCES store(id) ON UPDATE CASCADE",
    "SELECT 'skip add fk_employee_store'"
);
PREPARE stmt FROM @sql_fk_employee_store;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) 同一分店仅一个店长（MySQL 8+，幂等）
SET @exists_manager_flag := (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND COLUMN_NAME = 'manager_flag'
);
SET @sql_manager_flag := IF(@exists_manager_flag = 0,
    "ALTER TABLE employee ADD COLUMN manager_flag TINYINT GENERATED ALWAYS AS (CASE WHEN role = 'MANAGER' THEN 1 ELSE NULL END) VIRTUAL",
    "SELECT 'skip add manager_flag'"
);
PREPARE stmt FROM @sql_manager_flag;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_uk_store_manager := (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employee' AND INDEX_NAME = 'uk_employee_store_manager'
);
SET @sql_uk_store_manager := IF(@exists_uk_store_manager = 0,
    "ALTER TABLE employee ADD UNIQUE INDEX uk_employee_store_manager (store_id, manager_flag)",
    "SELECT 'skip add uk_employee_store_manager'"
);
PREPARE stmt FROM @sql_uk_store_manager;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) 分店负责人回填外键（可空，后续由业务回填，幂等）
SET @exists_fk_store_manager := (
    SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME = 'fk_store_manager_employee'
);
SET @sql_fk_store_manager := IF(@exists_fk_store_manager = 0,
    "ALTER TABLE store ADD CONSTRAINT fk_store_manager_employee FOREIGN KEY (manager_employee_id) REFERENCES employee(id) ON UPDATE CASCADE ON DELETE SET NULL",
    "SELECT 'skip add fk_store_manager_employee'"
);
PREPARE stmt FROM @sql_fk_store_manager;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
