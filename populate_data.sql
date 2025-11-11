-- ----------------------------
-- SparkHub 数据库模拟数据
-- ----------------------------
-- 描述: 包含 11 个表的全套模拟数据。
-- [重要] 脚本已配置为可重复执行 (先清空再插入)。
-- [重要] 所有数据严格遵循外键约束和业务逻辑 (e.g., 金额、点赞数)。
-- ----------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- 0. (安全) 清空所有表数据
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `notification`;
TRUNCATE TABLE `project_comment_like`;
TRUNCATE TABLE `project_comment`;
TRUNCATE TABLE `user_favorite`;
TRUNCATE TABLE `backing`;
TRUNCATE TABLE `project_reward`;
TRUNCATE TABLE `project`;
TRUNCATE TABLE `user_role`;
TRUNCATE TABLE `category`;
TRUNCATE TABLE `role`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 1. 插入角色表 (role)
-- ----------------------------
INSERT INTO `role` (`id`, `name`) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_CREATOR'),
(3, 'ROLE_ADMIN');

-- ----------------------------
-- 2. 插入项目分类表 (category)
-- ----------------------------
INSERT INTO `category` (`id`, `name`) VALUES
(1, '科技'),
(2, '设计'),
(3, '游戏'),
(4, '影视'),
(5, '音乐');

-- ----------------------------
-- 3. 插入用户表 (user)
-- ----------------------------
-- [重要] 所有用户的密码均为 '123456'
-- ----------------------------
INSERT INTO `user` (`id`, `username`, `password`, `email`, `avatar`, `create_time`) VALUES
(101, 'admin', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'admin@sparkhub.com', '/uploads/avatar6.jpg', NOW()),
(102, 'alice', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'alice@example.com', '/uploads/avatar1.jpg', NOW()),
(103, 'bob', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'bob@example.com', '/uploads/avatar2.jpg', NOW()),
(104, 'charlie', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'charlie@example.com', '/uploads/avatar3.jpg', NOW()),
(105, 'dave', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'dave@example.com', '/uploads/avatar4.jpg', NOW()),
(106, 'eve', '$2a$10$vBiupb2crCt4vibEpaAm9OQ8NuE9INaof9S/tUkL7zTlVCfKzJghe', 'eve@example.com', '/uploads/avatar5.jpg', NOW());

-- ----------------------------
-- 4. 插入用户角色关联表 (user_role)
-- ----------------------------
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(101, 1), (101, 2), (101, 3),
(102, 1), (102, 2),
(103, 1), (103, 2),
(104, 1),
(105, 1),
(106, 1), (106, 2);

-- ----------------------------
-- 5. 插入项目表 (project)
-- ----------------------------
-- [重要] current_amount 是根据下方 backing 表预先计算的总和。
-- P-10 (众筹中): 398.00
-- P-11 (成功): 102,650.00
-- P-12 (失败): 600.00
-- P-13 (审核中): 0.00
-- P-14 (众筹中): 161,920.00
-- ----------------------------
INSERT INTO `project` (`id`, `creator_id`, `category_id`, `title`, `description`, `cover_image`, `goal_amount`, `current_amount`, `end_time`, `status`, `create_time`) VALUES
(10, 102, 1, 'E-Ink 电子墨水屏智能日历', '一款可以自动同步日程的极简桌面日历，采用 E-Ink 屏幕，超长待机。', '/uploads/project_10_cover.jpg', 50000.00, 398.00, DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW()),
(11, 103, 3, '《像素神域》- 复古风Roguelike RPG', '重拾童年记忆！一款融合了经典 RPG 元素与 Roguelike 随机性的独立游戏。', '/uploads/project_11_cover.jpg', 100000.00, 102650.00, DATE_SUB(NOW(), INTERVAL 10 DAY), 2, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(12, 102, 2, '模块化磁吸桌面收纳系统', '自由组合，无限扩展。让你的桌面告别凌乱，所有配件均采用磁吸设计。', '/uploads/project_12_cover.jpg', 80000.00, 600.00, DATE_SUB(NOW(), INTERVAL 5 DAY), 3, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(13, 106, 5, '《城市边缘》- 我的第一张独立民谣专辑', '记录我在城市中行走的故事，10首原创歌曲，希望能通过众筹完成录音和制作。', '/uploads/project_13_cover.jpg', 30000.00, 0.00, DATE_ADD(NOW(), INTERVAL 45 DAY), 0, NOW()),
(14, 103, 4, '《深空回响》- 科幻短片电影', '一部关于孤独宇航员在木卫二发现生命迹象的科幻短片。需要资金完成后期特效制作。', '/uploads/project_14_cover.jpg', 200000.00, 161920.00, DATE_ADD(NOW(), INTERVAL 15 DAY), 1, DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ----------------------------
-- 6. 插入项目回报档位表 (project_reward)
-- ----------------------------
INSERT INTO `project_reward` (`id`, `project_id`, `title`, `description`, `amount`, `stock`, `image_url`) VALUES
-- P-10 (E-Ink 日历) 的回报
(1001, 10, '【早鸟价】电子日历 x1', '比零售价优惠 30%！', 99.00, 200, '/uploads/reward_10_1.jpg'),
(1002, 10, '【标准价】电子日历 x1', '标准零售套餐。', 129.00, NULL, '/uploads/reward_10_1.jpg'),
(1003, 10, '【豪华版】电子日历 x1 + 定制皮套', '包含一台日历和专属定制真皮保护套。', 299.00, 100, '/uploads/reward_10_2.jpg'),
-- P-11 (《像素神域》游戏) 的回报
(1101, 11, '【数字版】游戏激活码', '获取 Steam 平台游戏激活码一份。', 50.00, NULL, '/uploads/reward_11_1.jpg'),
(1102, 11, '【数字画册版】激活码 + 电子画册', '包含游戏激活码和高清数字设定集。', 80.00, NULL, '/uploads/reward_11_2.jpg'),
(1103, 11, '【实体收藏版】', '包含实体游戏光盘、设定集、原声带CD和主题T恤。', 500.00, 500, '/uploads/reward_11_3.jpg'),
(1104, 11, '【写入感谢名单】', '你的名字将出现在游戏片尾的“特别感谢”名单中！', 2000.00, 100, NULL),
(1105, 11, '【执行制作人】', '包含实体收藏版，且你的名字将以“执行制作人”身份出现在片尾！', 50000.00, 5, NULL),
-- P-12 (桌面收纳) 的回报
(1201, 12, '【基础套装】', '包含一个底座和三个标准模块。', 100.00, 300, '/uploads/reward_12_1.jpg'),
(1202, 12, '【完整套装】', '包含两个底座和所有8种功能模块。', 500.00, 100, '/uploads/reward_12_2.jpg'),
-- P-13 (音乐专辑) 的回报
(1301, 13, '【数字专辑】', '专辑上线后第一时间获取高品质数字版。', 80.00, NULL, '/uploads/reward_13_1.jpg'),
(1302, 13, '【签名实体CD】', '包含签名版实体CD，以及感谢卡。', 250.00, 200, '/uploads/reward_13_2.jpg'),
-- P-14 (科幻短片) 的回报
(1401, 14, '【高清数字版】', '影片完成后发送 4K 高清数字版观看链接。', 120.00, NULL, NULL),
(1402, 14, '【蓝光实体版 + 海报】', '包含蓝光光盘和主创签名海报。', 300.00, 500, '/uploads/reward_14_1.jpg'),
(1403, 14, '【片尾感谢】', '你的名字将出现在片尾“众筹支持者”名单中。', 1500.00, 100, NULL),
(1404, 14, '【副制片人】', '以“副制片人”身份出现在片尾，并受邀参加线下首映礼。', 10000.00, 10, NULL),
(1405, 14, '【联合制片人】', '以“联合制片人”身份出现在片头和片尾，深度参与项目。', 50000.00, 3, NULL);

-- ----------------------------
-- 7. 插入支持订单表 (backing)
-- ----------------------------
INSERT INTO `backing` (`id`, `backer_id`, `project_id`, `reward_id`, `backing_amount`, `status`, `create_time`) VALUES
-- P-10 (E-Ink 日历) 订单 (合计: 398.00)
(3001, 104, 10, 1001, 99.00, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),  -- Charlie (已支付)
(3002, 105, 10, 1003, 299.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),  -- Dave (已支付)
(3003, 106, 10, 1001, 99.00, 0, NOW()),                         -- Eve (待支付)
(3004, 104, 10, 1002, 129.00, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)), -- Charlie (已取消)

-- P-11 (《像素神域》游戏) 订单 (合计: 102,650.00)
(3005, 104, 11, 1102, 80.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),  -- Charlie (已支付)
(3006, 105, 11, 1103, 500.00, 1, DATE_SUB(NOW(), INTERVAL 25 DAY)),  -- Dave (已支付)
(3007, 106, 11, 1104, 2000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)), -- Eve (已支付)
(3008, 101, 11, 1105, 50000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)), -- Admin (已支付)
(3009, 102, 11, 1105, 50000.00, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)), -- Alice (已支付)

-- P-12 (桌面收纳) 订单 (合计: 600.00)
(3010, 105, 12, 1201, 100.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)), -- Dave (已支付)
(3011, 106, 12, 1202, 500.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)), -- Eve (已支付)

-- P-14 (科幻短片) 订单 (合计: 161,920.00)
(3012, 104, 14, 1402, 300.00, 1, DATE_SUB(NOW(), INTERVAL 14 DAY)),  -- Charlie (已支付)
(3013, 106, 14, 1403, 1500.00, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),  -- Eve (已支付)
(3014, 102, 14, 1401, 120.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),  -- Alice (已支付)
(3015, 101, 14, 1404, 10000.00, 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),   -- Admin (已支付)
(3016, 103, 14, 1405, 50000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),   -- Bob (已支付)
(3017, 105, 14, 1405, 50000.00, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),   -- Dave (已支付)
(3018, 104, 14, 1405, 50000.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));   -- Charlie (已支付)

-- ----------------------------
-- 8. 插入用户收藏表 (user_favorite)
-- ----------------------------
INSERT INTO `user_favorite` (`user_id`, `project_id`, `create_time`) VALUES
(104, 10, NOW()), -- Charlie 收藏了 E-Ink 日历
(104, 14, NOW()), -- Charlie 收藏了 科幻短片
(106, 11, NOW()), -- Eve 收藏了 像素神域
(102, 14, NOW()), -- Alice 收藏了 科幻短片
(101, 10, NOW()); -- Admin 收藏了 E-Ink 日历

-- ----------------------------
-- 9. 插入项目评论表 (project_comment)
-- ----------------------------
-- [重要] like_count 是根据下方的 like 表预先计算好的。
-- ----------------------------
INSERT INTO `project_comment` (`id`, `project_id`, `user_id`, `parent_id`, `content`, `like_count`, `create_time`) VALUES
-- P-10 (E-Ink 日历) 的评论
(2001, 10, 104, NULL, '这个日历支持中文显示吗？看起来很棒！', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2002, 10, 102, 2001, '支持的！我们内置了多国语言包，包括简体中文。感谢您的支持！', 2, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2003, 10, 105, NULL, '电池能用多久？详情里好像没写。', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2004, 10, 102, 2003, '日常使用（每天刷新2-3次）的情况下，一次充电大约可以使用 3-4 周。', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- P-14 (科幻短片) 的评论
(2005, 14, 106, NULL, '太酷了！预告片的特效看起来非常震撼！', 3, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2006, 14, 104, NULL, '已经支持了 1500 的档位，希望能早日看到成片。', 1, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2007, 14, 103, 2006, '万分感谢您的支持！我们正在加紧后期制作，一定不会辜负大家的期待！', 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- P-11 (《像素神域》游戏 - 已成功) 的评论
(2008, 11, 105, NULL, '游戏在三天前已经通关了，非常棒！强烈推荐！期待 DLC！', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2009, 11, 103, 2008, '哈哈，感谢支持！没想到这么快就通关了！DLC 已经在规划中了，敬请期待！', 2, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ----------------------------
-- 10. 插入评论点赞表 (project_comment_like)
-- ----------------------------
INSERT INTO `project_comment_like` (`user_id`, `comment_id`, `create_time`) VALUES
(106, 2001, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(104, 2002, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(105, 2002, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(105, 2004, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(104, 2005, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(105, 2005, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(102, 2005, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(106, 2006, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(104, 2007, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(106, 2008, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(102, 2008, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(105, 2009, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(106, 2009, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ----------------------------
-- 11. 插入通知表 (notification)
-- ----------------------------
INSERT INTO `notification` (`id`, `recipient_id`, `type`, `content`, `link_url`, `is_read`, `create_time`, `sender_id`) VALUES
-- 场景1: 项目状态变更 (系统通知)
(4001, 103, 'PROJECT_SUCCESS', "恭喜！您的项目 '《像素神域》- 复古风Roguelike RPG' 已成功达成众筹目标！", '/project/11', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),
(4002, 102, 'PROJECT_FAILED', "很遗憾，您的项目 '模块化磁吸桌面收纳系统' 众筹失败。", '/project/12', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),
(4003, 102, 'PROJECT_APPROVED', "您的项目 'E-Ink 电子墨水屏智能日历' 已通过审核，并已开始众筹！", '/project/10', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),
(4004, 106, 'PROJECT_PENDING', "您的项目 '《城市边缘》- 我的第一张独立民谣专辑' 已提交，正在等待管理员审核。", '/project/13', 0, NOW(), NULL),

-- 场景2: 新增评论 (有人评论了你的项目)
(4005, 102, 'NEW_PROJECT_COMMENT', "用户 'charlie' 在您的项目 'E-Ink 电子墨水屏智能日历' 下发表了评论。", '/project/10#comment-2001', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 104),
(4006, 103, 'NEW_PROJECT_COMMENT', "用户 'charlie' 在您的项目 '《深空回响》- 科幻短片电影' 下发表了评论。", '/project/14#comment-2006', 1, DATE_SUB(NOW(), INTERVAL 9 DAY), 104),
(4007, 105, 'NEW_PROJECT_COMMENT', "用户 'dave' 在您的项目 'E-Ink 电子墨水屏智能日历' 下发表了评论。", '/project/10#comment-2003', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 105),


-- 场景3: 新增回复 (有人回复了你的评论)
(4008, 104, 'NEW_COMMENT_REPLY', "用户 'alice' 回复了您的评论：'支持的！我们内置了多国语言包...'", '/project/10#comment-2002', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 102),
(4009, 104, 'NEW_COMMENT_REPLY', "用户 'bob' 回复了您的评论：'万分感谢您的支持！我们正在加紧...'", '/project/14#comment-2007', 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 103),
(4010, 105, 'NEW_COMMENT_REPLY', "用户 'bob' 回复了您的评论：'哈哈，感谢支持！没想到这么快就...'", '/project/11#comment-2009', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 103),

-- 场景4: 新增点赞 (有人赞了你的评论)
(4011, 104, 'COMMENT_LIKED', "用户 'eve' 赞了您的评论。", '/project/10#comment-2001', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 106),
(4012, 102, 'COMMENT_LIKED', "用户 'dave' 赞了您的评论。", '/project/10#comment-2002', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 105),
(4013, 103, 'COMMENT_LIKED', "用户 'eve' 赞了您的评论。", '/project/11#comment-2009', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 106),
(4014, 106, 'COMMENT_LIKED', "用户 'admin' 赞了您的评论。", '/project/14#comment-2005', 0, NOW(), 101), -- 假设 admin 刚刚点赞

-- 场景5: 新增支持 (有人支持了你的项目)
(4015, 102, 'NEW_BACKING', "用户 'charlie' 支持了您的项目 'E-Ink 电子墨水屏智能日历' 99.00 元。", '/project/10/backers', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 104),
(4016, 102, 'NEW_BACKING', "用户 'dave' 支持了您的项目 'E-Ink 电子墨水屏智能日历' 299.00 元。", '/project/10/backers', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 105),
(4017, 103, 'NEW_BACKING', "用户 'admin' 支持了您的项目 '《深空回响》- 科幻短片电影' 10000.00 元。", '/project/14/backers', 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 101);