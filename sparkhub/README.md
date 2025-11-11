# SparkHub (星火计划) 众筹平台后端 API

> 🔗 **相关项目：** [SparkHub 前端仓库链接](../sparkhub-frontend)

本项目是基于 Spring Boot 3 和 Vue 的全栈众筹平台的**后端 API** 实现。项目采用经典分层架构，并结合 Spring Security、MyBatis 和 Redis 构建了一个稳定、安全且高效的 Web 服务。

## ✨ 核心亮点与技术深度

* **并发安全事务（核心难点）：** 支付模块（`BackingServiceImpl`）采用 `@Transactional` 事务，并结合 MyBatis 在数据库层面（MySQL）使用**悲观锁 (`FOR UPDATE`)** 实现了订单、回报库存和项目金额更新的原子性，彻底避免了超卖等并发问题。
* **无状态认证与登出：** 采用 JWT (jjwt) 作为认证方案，并结合 Redis 存储 Token 状态，实现了对 JWT 的集中式管理，支持**高效登出**和**强制踢人**的安全需求。
* **精细化权限控制 (RBAC)：** 基于 Spring Security，定义了 `ROLE_USER`, `ROLE_CREATOR`, `ROLE_ADMIN` 三大角色，并利用 `@PreAuthorize` 表达式实现了对项目操作（如编辑）的**所有权检查** (`@projectSecurity.isOwner()`)。
* **性能优化与缓存：** 集成 Spring Cache + Redis，对项目列表、项目详情和分类等高频读取接口使用了缓存策略 (`@Cacheable`/`@CacheEvict`)，显著提升了响应速度。

## 🛠️ 技术栈 (Technology Stack)

| 类别          | 技术            | 版本/组件                          | 描述                                |
| :------------ | :-------------- | :--------------------------------- | :---------------------------------- |
| **核心框架**  | Spring Boot     | 3.5.7                              | 项目基础框架                        |
| **语言**      | Java            | 21                                 | (根据 `pom.xml` 设定)               |
| **安全框架**  | Spring Security | 6.x                                | 负责认证与授权                      |
| **认证协议**  | JWT             | jjwt 0.12.5                        | 用于前后端分离的无状态认证          |
| **数据访问**  | MyBatis         | 3.0.5                              | ORM 框架，配合 XML 实现复杂联表查询 |
| **数据库**    | MySQL           | 5.7+                               | 关系型数据库                        |
| **缓存**      | Redis           | Spring Cache + StringRedisTemplate | JWT 状态存储和业务数据缓存          |
| **异步/定时** | Spring Task     | `@EnableScheduling`                | 项目到期自动检查和状态更新          |

## 🗄️ 数据库设计 (11 张表)

本项目数据库为 `sparkhub`，采用了 11 张表，分为“核心业务”和“互动通知”两大模块。

### 1. 核心业务表 (共 8 张)
| 表名             | 描述         |
| :--------------- | :----------- |
| `user`           | 平台用户     |
| `role`           | 用户角色     |
| `user_role`      | 用户角色关联 |
| `category`       | 项目分类     |
| `project`        | 众筹项目主体 |
| `project_reward` | 项目回报档位 |
| `backing`        | 支持订单记录 |
| `user_favorite`  | 用户收藏记录 |

### 2. 互动与通知表 (共 3 张)
| 表名                   | 描述                              |
| :--------------------- | :-------------------------------- |
| `project_comment`      | 项目评论（支持 `parent_id` 嵌套） |
| `project_comment_like` | 评论点赞记录                      |
| `notification`         | 用户通知中心                      |

## 📡 核心 API 端点示例

| 模块              | 方法     | 路径                              | 权限要求                |
| :---------------- | :------- | :-------------------------------- | :---------------------- |
| **用户/认证**     | `POST`   | `/api/auth/login`                 | 任何人                  |
|                   | `POST`   | `/api/auth/register`              | 任何人                  |
|                   | `POST`   | `/api/auth/logout`                | 已认证                  |
|                   | `GET`    | `/api/users/me`                   | 已认证                  |
| **项目 (公开)**   | `GET`    | `/api/projects`                   | 任何人                  |
|                   | `GET`    | `/api/projects/{id}`              | 任何人（逻辑校验）      |
| **项目 (发起者)** | `POST`   | `/api/projects`                   | `ROLE_CREATOR`          |
|                   | `PUT`    | `/api/projects/{id}`              | `ROLE_CREATOR` & 所有者 |
|                   | `GET`    | `/api/projects/my`                | `ROLE_CREATOR`          |
| **支持/支付**     | `POST`   | `/api/backings`                   | `ROLE_USER`             |
|                   | `POST`   | `/api/backings/{id}/pay`          | `ROLE_USER` & 事务锁    |
|                   | `GET`    | `/api/backings/my`                | `ROLE_USER`             |
| **收藏**          | `POST`   | `/api/favorites/{projectId}`      | `ROLE_USER`             |
|                   | `DELETE` | `/api/favorites/{projectId}`      | `ROLE_USER`             |
| **评论/点赞**     | `GET`    | `/api/projects/{pId}/comments`    | 任何人                  |
|                   | `POST`   | `/api/projects/{pId}/comments`    | `ROLE_USER`             |
|                   | `POST`   | `/api/comments/{cId}/like`        | `ROLE_USER`             |
| **通知**          | `GET`    | `/api/notifications`              | 已认证                  |
|                   | `GET`    | `/api/notifications/unread-count` | 已认证                  |
|                   | `POST`   | `/api/notifications/{id}/read`    | 已认证                  |
| **管理 (Admin)**  | `GET`    | `/api/admin/projects`             | `ROLE_ADMIN`            |
|                   | `PUT`    | `/api/admin/projects/{id}/status` | `ROLE_ADMIN`            |
|                   | `GET`    | `/api/admin/users`                | `ROLE_ADMIN`            |
|                   | `PUT`    | `/api/admin/users/{id}/role`      | `ROLE_ADMIN`            |