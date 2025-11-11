-- ----------------------------
-- SparkHub 数据库表结构
-- ----------------------------
-- 数据库: sparkhub
-- 包含 11 个表的完整创建脚本
-- 顺序: 1. role -> 2. user -> 3. user_role -> 4. category -> 5. project ->
--       6. project_reward -> 7. backing -> 8. user_favorite ->
--       9. project_comment -> 10. project_comment_like -> 11. notification
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- (可选) 按依赖反向顺序删除表
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `project_comment_like`;
DROP TABLE IF EXISTS `project_comment`;
DROP TABLE IF EXISTS `user_favorite`;
DROP TABLE IF EXISTS `backing`;
DROP TABLE IF EXISTS `project_reward`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `role`;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 1. 角色表 (role)
-- ----------------------------
CREATE TABLE `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID (PK)',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称 (e.g., ROLE_USER)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 2. 用户表 (user)
-- ----------------------------
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID (PK)',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码 (BCrypt 哈希值)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 3. 用户角色关联表 (user_role)
-- ----------------------------
CREATE TABLE `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID (FK)',
  `role_id` BIGINT NOT NULL COMMENT '角色ID (FK)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  CONSTRAINT `fk_userrole_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_userrole_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 4. 项目分类表 (category)
-- ----------------------------
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID (PK)',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类表';

-- ----------------------------
-- 5. 项目表 (project)
-- ----------------------------
CREATE TABLE `project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID (PK)',
  `creator_id` BIGINT NOT NULL COMMENT '发起者ID (FK)',
  `category_id` BIGINT NOT NULL COMMENT '分类ID (FK)',
  `title` VARCHAR(255) NOT NULL COMMENT '项目标题',
  `description` TEXT COMMENT '项目详情',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片 URL',
  `goal_amount` DECIMAL(10,2) NOT NULL COMMENT '目标金额',
  `current_amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前已筹集金额',
  `end_time` TIMESTAMP NOT NULL COMMENT '截止时间',
  `status` INT NOT NULL DEFAULT '0' COMMENT '项目状态 (0=审核中, 1=众筹中, 2=成功, 3=失败)',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_creator` (`creator_id`),
  KEY `idx_project_category` (`category_id`),
  CONSTRAINT `fk_project_user` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ----------------------------
-- 6. 项目回报档位表 (project_reward)
-- ----------------------------
CREATE TABLE `project_reward` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回报ID (PK)',
  `project_id` BIGINT NOT NULL COMMENT '项目ID (FK)',
  `title` VARCHAR(255) NOT NULL COMMENT '回报标题',
  `description` TEXT COMMENT '回报详情',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支持金额 (档位价格)',
  `stock` INT DEFAULT NULL COMMENT '名额库存 (NULL 为不限量)',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '回报配图 URL',
  PRIMARY KEY (`id`),
  KEY `idx_reward_project` (`project_id`),
  CONSTRAINT `fk_reward_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目回报档位表';

-- ----------------------------
-- 7. 支持订单表 (backing)
-- ----------------------------
CREATE TABLE `backing` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID (PK)',
  `backer_id` BIGINT NOT NULL COMMENT '支持者ID (FK)',
  `project_id` BIGINT NOT NULL COMMENT '项目ID (FK)',
  `reward_id` BIGINT NOT NULL COMMENT '回报ID (FK)',
  `backing_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支持金额',
  `status` INT NOT NULL DEFAULT '0' COMMENT '订单状态 (0=待支付, 1=已支付, 2=已取消)',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_backing_backer` (`backer_id`),
  KEY `idx_backing_project` (`project_id`),
  KEY `idx_backing_reward` (`reward_id`),
  CONSTRAINT `fk_backing_user` FOREIGN KEY (`backer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_backing_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_backing_reward` FOREIGN KEY (`reward_id`) REFERENCES `project_reward` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支持订单表';

-- ----------------------------
-- 8. 用户收藏表 (user_favorite)
-- ----------------------------
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID (FK)',
  `project_id` BIGINT NOT NULL COMMENT '项目ID (FK)',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_project` (`user_id`, `project_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ----------------------------
-- 9. 项目评论表 (project_comment)
-- ----------------------------
CREATE TABLE `project_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID (PK)',
  `project_id` BIGINT NOT NULL COMMENT '项目ID (FK)',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID (FK)',
  `parent_id` BIGINT DEFAULT NULL COMMENT '回复的父评论ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT '0' COMMENT '点赞总数',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_project` (`project_id`),
  KEY `idx_comment_user` (`user_id`),
  KEY `idx_comment_parent` (`parent_id`),
  CONSTRAINT `fk_comment_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `project_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目评论表';

-- ----------------------------
-- 10. 评论点赞表 (project_comment_like)
-- ----------------------------
CREATE TABLE `project_comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID (FK)',
  `comment_id` BIGINT NOT NULL COMMENT '被点赞评论ID (FK)',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `project_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ----------------------------
-- 11. 通知表 (notification)
-- ----------------------------
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `recipient_id` BIGINT NOT NULL COMMENT '接收通知的用户ID (FK)',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型 (e.g., PROJECT_APPROVED)',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '点击跳转的链接',
  `is_read` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否已读 (0=未读, 1=已读)',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sender_id` BIGINT DEFAULT NULL COMMENT '触发通知的用户ID (FK)',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_id_read_time` (`recipient_id`, `is_read`, `create_time`),
  KEY `fk_notification_recipient` (`recipient_id`),
  KEY `fk_notification_sender` (`sender_id`),
  CONSTRAINT `fk_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';