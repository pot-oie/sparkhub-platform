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
| **语言**      | Java            | 21                                 | (基于你的 `pom.xml` 选择)           |
| **安全框架**  | Spring Security | RBAC + `@PreAuthorize`             | 负责认证与授权                      |
| **认证协议**  | JWT             | jjwt 0.12.5                        | 用于前后端分离的无状态认证          |
| **数据访问**  | MyBatis         | 3.x                                | ORM 框架，配合 XML 实现复杂联表查询 |
| **数据库**    | MySQL           | 5.7+                               | 关系型数据库                        |
| **缓存**      | Redis           | Spring Cache + StringRedisTemplate | JWT 状态存储和业务数据缓存          |
| **异步/定时** | Spring Task     | `@EnableScheduling`                | 项目到期自动检查和状态更新          |

## 🗄️ 数据库设计 (8 张核心表)

`role` (角色), `user` (用户), `user_role` (关联), `category` (分类), `project` (项目), `project_reward` (回报), `user_favorite` (收藏), `backing` (支持订单)。

## 📡 核心 API 端点示例

| 模块              | 方法   | 路径                         | 权限要求                    |
| :---------------- | :----- | :--------------------------- | :-------------------------- |
| **用户/认证**     | `POST` | `/api/auth/login`            | 任何人                      |
|                   | `POST` | `/api/auth/logout`           | 已认证                      |
| **项目 (公开)**   | `GET`  | `/api/projects?pageNum=1`    | 任何人                      |
|                   | `GET`  | `/api/projects/{id}`         | 任何人（逻辑校验）          |
| **项目 (发起者)** | `POST` | `/api/projects`              | `ROLE_CREATOR`              |
|                   | `PUT`  | `/api/projects/{id}`         | `ROLE_CREATOR` & 所有者检查 |
| **支持/支付**     | `POST` | `/api/backings`              | `ROLE_USER`                 |
|                   | `POST` | `/api/backings/{id}/pay`     | `ROLE_USER` & 事务锁        |
| **管理 (Admin)**  | `GET`  | `/api/admin/projects`        | `ROLE_ADMIN`                |
|                   | `PUT`  | `/api/admin/users/{id}/role` | `ROLE_ADMIN`                |