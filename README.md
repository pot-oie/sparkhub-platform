# 🌟 SparkHub (星火计划) 众筹平台

> **项目：** JavaEE 课程设计大作业
>
> **目标：** 建设一个功能完善、安全可靠的全栈众筹平台，涵盖项目发起、审核、支持、支付、评论、通知等核心业务，并重点解决并发与权限问题。

---

## 🏗️ 整体架构与技术选型

本项目采用经典的前后端分离架构，通过 RESTful API 进行通信。

| 模块         | 技术栈                                                       | 亮点与优势                                                   |
| :----------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| **后端 API** | **Spring Boot 3.5.7<br/>Java 21<br/>MyBatis<br/>MySQL<br/>Redis** | **并发安全：** 支付流程采用 MySQL 悲观锁 (`FOR UPDATE`)，保证库存和金额更新的原子性<br/>**权限细致：** 基于 Spring Security + JWT 实现 RBAC，支持 `USER`/`CREATOR`/`ADMIN` 三种角色和细粒度的权限控制<br/>**性能优化：** 广泛使用 Spring Cache 和 Redis 缓存热点数据 |
| **前端 Web** | **Vue 3<br/>Vite<br/>Pinia<br/>Element Plus**                | **响应式 UI：** 采用 Element Plus 实现美观、专业的界面<br/>**状态管理：** 使用 Pinia 进行用户认证、通知红点等状态管理，并使用持久化插件<br/>**路由守卫：** 严格的前后端权限校验机制，保障路由安全 |
| **部署**     | **Docker (推荐)**                                            | 容器化部署，简化环境配置                                     |

---

## 🗄️ 数据库设计与配置 (MySQL) 

### 1. 核心表设计 (共 8 张) 



---

## 🗄️ 数据库设计与配置 (MySQL) 

本项目数据库为 `sparkhub`，采用了 11 张表，分为“核心业务”和“互动通知”两大模块。

### 1. 核心业务表 (共 8 张) 

| 表名                 | 描述         | 关键字段 / 关系                                              | 重点说明                                           |
| -------------------- | ------------ | ------------------------------------------------------------ | -------------------------------------------------- |
| `user`               | 平台用户     | `username`, `password`, `email`                              | 存储 Spring Security 认证信息                      |
| `role`               | 用户角色     | `name` (e.g., `ROLE_ADMIN`)                                  | 定义 RBAC 权限体系                                 |
| `user_role`          | 用户角色关联 | `user_id` -> `user`, `role_id` -> `role`                     | 实现多对多关系                                     |
| `category`           | 项目分类     | `name`                                                       | 缓存的热点数据                                     |
| **`project`**        | 众筹项目主体 | `creator_id` -> `user`, `goal_amount`, `current_amount`      | 核心表，存储项目进度和状态                         |
| **`project_reward`** | 项目回报档位 | `project_id` -> `project`, `amount`, **`stock`**             | **`stock` 字段在支付时被悲观锁锁定，用于防止超卖** |
| **`backing`**        | 支持订单记录 | `backer_id` -> `user`, `project_id` -> `project`, `reward_id` -> `project_reward` | 支付流程中的核心事务表                             |
| `user_favorite`      | 用户收藏记录 | `user_id`, `project_id`                                      | 实现用户对项目的收藏功能                           |

### 2. 互动与通知表 (共 3 张)
为实现用户互动与消息推送，在核心表基础上额外添加 3 张表：

| 表名                       | 描述         | 关键字段 / 关系                          | 重点说明                                                     |
| :------------------------- | :----------- | :--------------------------------------- | :----------------------------------------------------------- |
| **`project_comment`**      | 项目评论     | `project_id`, `user_id`, **`parent_id`** | 实现**多级嵌套回复** (通过 `parent_id`) 和点赞计数 (`like_count`)。 |
| **`project_comment_like`** | 评论点赞记录 | `user_id`, `comment_id`                  | 确保用户对单条评论只能点赞一次 (`UNIQUE KEY`)。              |
| **`notification`**         | 用户通知中心 | `recipient_id`, `sender_id`              | 存储**系统通知** (如审核、成功) 和**互动通知** (如回复、点赞)。 |

### 3. 数据库部署指南

项目使用 **MySQL 5.7+**。

1. **启动 MySQL 服务**：确保您的本地或远程 MySQL 服务已启动。
2. **创建数据库**：使用以下 SQL 命令创建数据库（推荐使用 `utf8mb4` 编码）：

```sql
CREATE DATABASE sparkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **导入表结构和初始数据**：

* 执行您的 SQL 脚本，创建上述 11 张表。

* **重要：** 必须在 `role` 表中初始化至少三条数据：`ROLE_USER`、`ROLE_CREATOR` 和 `ROLE_ADMIN`，以确保用户注册和权限模块正常工作。

4. **配置后端连接**：

* 打开 `sparkhub/src/main/resources/application.properties`。

* 修改数据库凭证（如果与默认值不同）：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sparkhub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

---

## 💡 核心业务流程与功能

### 1. 项目生命周期
项目经历了 **创建 (Creator) → 审核 (Admin)** **→ 众筹中 (Public) → 成功/失败 (Scheduler)** 的完整流程。

### 2. 支付/支持流程 (并发核心)
1.  **创建订单：** 用户选择回报档位，创建 `status=0` (待支付) 订单。
2.  **执行支付：** 调用支付接口，后端同时对订单、回报和项目进行**悲观锁锁定**，检查库存，更新项目金额，并将订单状态设为 `status=1` (已支付)。

### 3. 用户互动功能
* **认证：** JWT 无状态登录，Redis 存储实现登出和多设备踢人。
* **收藏：** 允许用户收藏项目，并在项目详情页实时显示收藏状态。
* **评论/点赞：** 支持多级评论嵌套和点赞功能，并实时通知相关用户。
* **通知中心：** 统一管理系统通知和互动通知（评论、点赞），并有未读计数红点提醒。

---

## ⚙️ 快速部署

请进入对应的子目录查看详细的配置和运行说明。

### 1. 后端 API (Java)
进入 `sparkhub/` 目录。
* **依赖：** Java 21, MySQL, Redis
* **配置文件：** 配置 `sparkhub/src/main/resources/application.properties` 中的数据库和 Redis 连接信息。
* **运行：** `mvn spring-boot:run`

### 2. 前端 Web (Vue)
进入 `sparkhub-frontend/` 目录。
* **依赖：** Node.js 20+
* **配置：** 检查 `src/utils/request.ts` 中的 `baseURL` 是否指向后端服务（默认为 `http://localhost:8080/api`）。
* **运行：** `npm install`，然后 `npm run dev`