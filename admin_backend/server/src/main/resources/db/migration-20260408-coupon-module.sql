-- 优惠券发布与管理模块（仅生成脚本，需人工执行）
-- 文件：migration-20260408-coupon-module.sql

-- 1) 用户会员字段
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `is_member` tinyint NOT NULL DEFAULT 0 COMMENT '是否会员 1是 0否',
    ADD COLUMN IF NOT EXISTS `member_level` int NULL COMMENT '会员等级',
    ADD COLUMN IF NOT EXISTS `member_expire_time` datetime NULL COMMENT '会员到期时间';

-- 2) 优惠券模板表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(64) NOT NULL COMMENT '优惠券名称',
    `coupon_type` tinyint NOT NULL COMMENT '优惠券类型 1满减券 2折扣券',
    `publish_type` tinyint NOT NULL COMMENT '发布类型 1全局券 2门店券',
    `store_id` bigint NULL COMMENT '所属门店id，门店券必填',
    `receive_type` tinyint NOT NULL COMMENT '领取类型 1全员可领 2会员可领',
    `threshold_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
    `discount_amount` decimal(10,2) NULL COMMENT '优惠金额（满减券）',
    `discount_rate` decimal(5,2) NULL COMMENT '折扣率（折扣券，0-10）',
    `total_count` int NOT NULL COMMENT '总发行量',
    `receive_count` int NOT NULL DEFAULT 0 COMMENT '已领取数量',
    `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
    `per_user_limit` int NOT NULL DEFAULT 1 COMMENT '每人限领',
    `start_time` datetime NOT NULL COMMENT '生效开始时间',
    `end_time` datetime NOT NULL COMMENT '生效结束时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    `remark` varchar(255) NULL COMMENT '备注',
    `create_user` int NOT NULL,
    `update_user` int NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_coupon_publish_type` (`publish_type`),
    KEY `idx_coupon_store_id` (`store_id`),
    KEY `idx_coupon_status` (`status`),
    KEY `idx_coupon_start_end` (`start_time`, `end_time`),
    CONSTRAINT `fk_coupon_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 3) 用户券实例表
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户id',
    `coupon_id` bigint NOT NULL COMMENT '优惠券模板id',
    `status` tinyint NOT NULL COMMENT '状态 1未使用 2已锁定 3已使用 4已过期',
    `threshold_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '快照-门槛金额',
    `discount_amount` decimal(10,2) NULL COMMENT '快照-优惠金额',
    `discount_rate` decimal(5,2) NULL COMMENT '快照-折扣率',
    `start_time` datetime NOT NULL COMMENT '快照-生效开始',
    `end_time` datetime NOT NULL COMMENT '快照-生效结束',
    `receive_time` datetime NOT NULL COMMENT '领取时间',
    `lock_time` datetime NULL COMMENT '锁定时间',
    `use_time` datetime NULL COMMENT '使用时间',
    `order_id` bigint NULL COMMENT '使用订单id',
    `create_user` int NOT NULL,
    `update_user` int NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_coupon_user_status` (`user_id`, `status`),
    KEY `idx_user_coupon_coupon_id` (`coupon_id`),
    KEY `idx_user_coupon_order_id` (`order_id`),
    KEY `idx_user_coupon_end_time` (`end_time`),
    CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户领取优惠券实例表';

-- 4) 优惠券操作日志
CREATE TABLE IF NOT EXISTS `coupon_operate_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `coupon_id` bigint NOT NULL COMMENT '优惠券id',
    `operate_employee_id` bigint NOT NULL COMMENT '操作员工id',
    `operate_role` varchar(16) NOT NULL COMMENT '操作角色编码',
    `operate_type` varchar(32) NOT NULL COMMENT '操作类型 create/update/status',
    `operate_desc` varchar(255) NULL COMMENT '操作描述',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_coupon_operate_coupon` (`coupon_id`),
    KEY `idx_coupon_operate_employee` (`operate_employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券操作日志表';

-- 5) 订单表增加优惠券字段
ALTER TABLE `orders`
    ADD COLUMN IF NOT EXISTS `coupon_id` bigint NULL COMMENT '优惠券模板id',
    ADD COLUMN IF NOT EXISTS `user_coupon_id` bigint NULL COMMENT '用户券id',
    ADD COLUMN IF NOT EXISTS `coupon_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    ADD COLUMN IF NOT EXISTS `origin_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '原始金额';

-- 6) 索引
SET @idx_orders_user_coupon := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND index_name = 'idx_orders_user_coupon_id'
);
SET @idx_orders_user_coupon_sql := IF(@idx_orders_user_coupon = 0,
                                      'ALTER TABLE `orders` ADD INDEX `idx_orders_user_coupon_id` (`user_coupon_id`)',
                                      'SELECT 1');
PREPARE stmt FROM @idx_orders_user_coupon_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
