# 🌟 SparkHub (星火计划) 众筹平台

> **项目：** JavaEE 课程设计大作业
>
> **目标：** 建设一个功能完善、安全可靠的全栈众筹平台，涵盖项目发起、审核、支持、支付、评论、通知等核心业务，并重点解决并发与权限问题。

---

## 🏗️ 整体架构与技术选型

本项目采用经典的前后端分离架构，通过 RESTful API 进行通信。

| 模块         | 技术栈                                                       | 亮点与优势                                                   |
| :----------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| **后端 API** | **Spring Boot 3.5.7<br/>Java 21<br/>MyBatis<br/>MySQL 5.7+<br/>Redis** | **并发安全：** 支付流程采用 MySQL 悲观锁 (`FOR UPDATE`)，保证库存和金额更新的原子性。<br/>**权限细致：** 基于 Spring Security + JWT 实现 RBAC，支持 `USER`/`CREATOR`/`ADMIN` 三种角色和细粒度的权限控制。<br/>**性能优化：** 广泛使用 Spring Cache 和 Redis 缓存热点数据（如项目列表、详情、分类）。 |
| **前端 Web** | **Vue 3 (Composition API)<br/>Vite<br/>Pinia (持久化)<br/>Element Plus<br/>TypeScript<br/>Node.js 20+** | **响应式 UI：** 采用 Element Plus 实现美观、专业的界面。<br/>**状态管理：** 使用 Pinia (`pinia-plugin-persistedstate`) 持久化管理用户认证、通知红点等状态。<br/>**路由守卫：** 严格的前后端权限校验机制，保障路由安全。 |
| **静态资源** | `sparkhub-uploads/` 目录                                     | 存储用户头像、项目封面、回报图片等。后端通过 `WebConfig` 配置静态资源映射（`/uploads/**`）。 |

---

## ✨ 核心功能展示

---

## 🖥️ 核心页面展示 (Web 前端)

以下按**用户使用流程**展示核心页面，直观呈现平台功能模块：

### 1. 用户入口 - 登录页

用户进入平台的首个交互页面，支持用户名密码登录，未注册用户可跳转至注册页。

![](PagesDisplay\login.png)

### 2. 用户入口 - 注册页

为新用户提供账号创建流程，填写用户名、邮箱、密码即可完成注册。

![](PagesDisplay\register.png)

### 3. 前台首页

平台核心展示页，聚合推荐项目、分类入口，用户可快速浏览众筹动态。

![](PagesDisplay\home.png)

### 4. 项目详情页

展示项目全维度信息（目标金额、进度、剩余时间），并包含**评论互动区**，支持用户提问、创作者回复。

![](PagesDisplay\detail.png)

![](PagesDisplay\comments.png)

### 5. 通知中心

用户查看系统通知（如项目审核结果）和互动消息（如评论点赞、项目支持提醒）的集中入口。

![](PagesDisplay\notification.png)

### 6. 个人资料页

用户管理个人信息（头像、邮箱、密码），并展示自身角色（普通用户 / 发起人）。

![](PagesDisplay\edit.png)

### 7. 发起项目页

创作者发起众筹的分步表单，包含项目基本信息、回报档位设置等核心流程。

![](PagesDisplay\create.png)

### 8. 管理后台 - 项目审核

平台管理员对众筹项目进行 “审核中 / 众筹中 / 成功 / 失败” 状态管理，确保项目合规性。

![](PagesDisplay\adminproject.png)

### 9. 管理后台 - 用户管理

管理员配置用户权限（普通用户 / 发起人 / 管理员），实现 RBAC 权限体系的可视化管理。

![](PagesDisplay\adminuser.png)

## 🗄️ 数据库设计与配置 (MySQL)

本项目数据库为 `sparkhub`，采用了 11 张表，分为“核心业务”和“互动通知”两大模块。

### 1. 核心业务表 (共 8 张)

| 表名                 | 描述         | 关键字段 / 关系                                              | 重点说明                                           |
| :------------------- | :----------- | :----------------------------------------------------------- | :------------------------------------------------- |
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

1.  **启动 MySQL 服务**：确保您的本地或远程 MySQL 服务已启动。
2.  **创建数据库**：使用以下 SQL 命令创建数据库（推荐使用 `utf8mb4` 编码）：

    ```sql
    CREATE DATABASE sparkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```

3.  **导入表结构和初始数据**：

    * 为了快速启动项目，我们已在仓库中提供了完整的数据库示例脚本。

    * 请在您的 `sparkhub` 数据库中**按顺序**执行以下两个文件：

        1.  **`create_tables.sql`**：用于创建所有 11 个表的结构。
        2.  **`populate_data.sql`**：用于填充一套完整的模拟数据（已包含 `ROLE_USER` 等必需角色、用户、项目、评论和通知）。

    * **[提示]** `populate_data.sql` 脚本会首先清空 (TRUNCATE) 所有相关表，因此可以安全地重复执行以重置数据库。

4.  **配置后端连接**：

    * 打开 `sparkhub/src/main/resources/application.properties`。
    * 修改数据库凭证（如果与默认值不同）：

    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/sparkhub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    spring.datasource.username=root
    spring.datasource.password=123456
    ```

5.  **（可选）体验账号**：
    `populate_data.sql` 脚本已内置以下账号（密码**均为 `123456`**）：
    * **管理员 (Admin)**: `admin`
    * **发起者 (Creator)**: `alice`, `bob`
    * **普通用户 (User)**: `charlie`, `dave`

---

## 💡 核心业务流程与功能

### 1. 项目生命周期
项目经历了 **创建 (Creator) → 审核 (Admin)** **→ 众筹中 (Public) → 成功/失败 (Scheduler)** 的完整流程。
* `ProjectScheduler` 定时任务（每小时执行）会自动检查到期的项目，并将其状态更新为 `成功(2)` 或 `失败(3)`。

### 2. 支付/支持流程 (并发核心)
1.  **创建订单：** 用户选择回报档位，创建 `status=0` (待支付) 订单。
2.  **执行支付：** 调用支付接口 (`BackingServiceImpl.executePayment`)，后端在 **@Transactional 事务**中，**按顺序**对 `backing`、`project_reward`、`project` 表的相应行执行 `FOR UPDATE` **悲观锁锁定**，检查库存，更新项目金额，并将订单状态设为 `status=1` (已支付)。

### 3. 用户互动功能
* **认证：** JWT 无状态登录，但通过 **Redis** 存储每个用户的有效 Token，实现了登出（删除 Redis 键）和多设备踢人（新登录覆盖旧 Token）。
* **收藏：** 允许用户收藏项目，并在项目详情页实时显示收藏状态。
* **评论/点赞：** 支持多级评论嵌套和点赞功能，并实时通知相关用户。
* **通知中心：** 统一管理系统通知（审核、成功、失败）和互动通知（评论、回复、点赞），并有未读计数红点提醒。

---

## ⚙️ 快速部署

请进入对应的子目录查看详细的配置和运行说明。

### 1. 后端 API (Java)
进入 `sparkhub/` 目录。
* **依赖：** Java 21, MySQL 5.7+, Redis
* **配置文件：**配置 `sparkhub/src/main/resources/application.properties` 中的数据库、Redis和**文件本地存储路径**。
* **运行：** 使用 IDE 启动 `SparkhubApplication.java`。

### 2. 前端 Web (Vue)
进入 `sparkhub-frontend/` 目录。
* **依赖：** Node.js `^20.19.0` 或 `>=22.12.0`
* **配置：** 检查 `src/utils/request.ts` 中的 `baseURL` 是否指向后端服务（默认为 `http://localhost:8080/api`）。
* **运行：** `npm install`，然后 `npm run dev`