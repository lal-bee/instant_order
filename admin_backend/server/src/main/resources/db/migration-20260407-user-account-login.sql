-- 用户端简易账号注册登录补丁（H5）
-- 目标：移除对 mock/openid 登录的依赖，新增 username/password 登录最小字段

ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `username` varchar(50) NULL COMMENT '登录用户名',
    ADD COLUMN IF NOT EXISTS `password` varchar(100) NULL COMMENT '登录密码(MD5)',
    ADD COLUMN IF NOT EXISTS `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    ADD COLUMN IF NOT EXISTS `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'uk_user_username'
);
SET @idx_sql := IF(@idx_exists = 0,
                   'ALTER TABLE `user` ADD UNIQUE KEY `uk_user_username` (`username`)',
                   'SELECT 1');
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
