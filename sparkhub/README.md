# SparkHub (星火中心) 众筹平台后端 API

本项目是一个基于 Spring Boot 3 和 Vue（需另行开发）的轻量级创意项目众筹平台。此仓库为**后端 API** 的完整实现。

## ✨ 核心功能

* **RESTful API:** 提供清晰、规范的 API 接口。
* **权限系统 (RBAC):** 基于 Spring Security + JWT，实现了三种角色的权限分离 (ROLE_USER, ROLE_CREATOR, ROLE_ADMIN)。
* **项目流程:** 完整的项目生命周期管理 (创建 -> 审核 -> 众筹中 -> 成功/失败)。
* **支付事务:** 使用**悲观锁 (FOR UPDATE)** 和事务，实现了并发安全的虚拟支付流程 (订单创建、库存扣减、金额增加)。
* **核心功能:** 用户注册、登录、登出、收藏、支持项目。
* **发起者功能:** 创建、修改项目 (含回报档位)。
* **管理员功能:** 审核项目、管理用户、管理分类。
* **性能优化:** 使用 Redis 对高频读取的 API (如项目列表、详情、分类) 进行了缓存。
* **高效查询:** 使用 MyBatis + PageHelper 实现了简单高效的 SQL 查询和分页。

## 🛠️ 技术栈 (Technology Stack)

| 类别 | 技术 | 描述 |
| :--- | :--- | :--- |
| **核心框架** | Spring Boot 3.x | 项目基础框架 (使用 Spring Boot 3.x) |
| **语言** | Java 21 | (基于你的 `pom.xml` 选择) |
| **安全框架** | Spring Security | 负责认证与授权 (RBAC) |
| **认证协议** | JWT (jjwt 0.12.5) | 用于前后端分离的无状态认证 |
| **数据访问** | MyBatis 3.x | ORM 框架，处理对象与数据库映射 |
| **分页插件** | PageHelper 2.1.0 | MyBatis 的物理分页插件 |
| **数据库** | MySQL 5.7+ | 关系型数据库 |
| **缓存** | Redis | 1. 缓存 JWT (实现登出) <br> 2. 缓存业务数据 (Spring Cache) |
| **构建管理** | Maven | 项目依赖和构建管理 |

## 🚀 启动项目 (本地)

### 1. 先决条件

在运行本项目前，请确保你的开发环境中已安装并启动：
1.  **Java 21 (JDK)**
2.  **Maven 3.x**
3.  **MySQL 5.7** (或 8.x)
4.  **Redis**

### 2. 配置

1.  **Clone 本仓库**
    ```bash
    git clone ...
    cd sparkhub
    ```
2.  **创建数据库**
    * 连接到你的 MySQL，手动创建一个数据库：
    ```sql
    CREATE DATABASE sparkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
3.  **运行 SQL 脚本**
    * 在 `sparkhub` 数据库中，按顺序执行我们之前创建的 8 张表的 `CREATE TABLE` 脚本 (包括 `role` 表的 `INSERT` 初始数据)。
4.  **配置 `application.properties`**
    * 打开 `src/main/resources/application.properties`
    * 修改 `spring.datasource.username` 和 `spring.datasource.password` 为你自己的 MySQL 账号密码。
    * (如果你的 Redis 有密码，也请配置)
    * 确保 `sparkhub.jwt.secret` 是一个复杂的字符串。

### 3. 运行

* **使用 IDE (推荐):**
    * 在 IntelliJ IDEA 中打开项目，等待 Maven 依赖下载完毕。
    * 运行 `SparkHubApplication.java` (它在 `com.pot.sparkhub` 包下)。
* **使用 Maven:**
    ```bash
    mvn spring-boot:run
    ```

### 4. 访问

* 后端服务将启动在 `http://localhost:8080`
* 你可以使用 Postman 或 Apifox 等工具测试 "API 端点" 表中的接口。

## 🗄️ 数据库设计

共 8 张核心表：

1.  `role` (角色表)
2.  `user` (用户表)
3.  `user_role` (用户角色关联表)
4.  `category` (项目分类表)
5.  `project` (项目表)
6.  `project_reward` (项目回报档位表)
7.  `user_favorite` (用户收藏表)
8.  `backing` (支持订单表)

## 📡 核心 API 端点

| 角色 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| 任何人 | `POST`| `/api/auth/register` | 注册 |
| 任何人 | `POST`| `/api/auth/login` | 登录 |
| 任何人 | `GET` | `/api/projects` | 分页获取项目列表 |
| 任何人 | `GET` | `/api/projects/{id}` | 获取项目详情 |
| **USER**| `POST`| `/api/favorites/{projectId}` | 添加收藏 |
| **USER**| `POST`| `/api/backings` | 创建支持订单 |
| **USER**| `POST`| `/api/backings/{id}/pay` | 模拟支付订单 |
| **CREATOR**|`POST`| `/api/projects` | 发起新项目 |
| **ADMIN**| `PUT` | `/api/admin/projects/{id}/status`| 审核项目 |
| **ADMIN**| `GET` | `/api/admin/users` | 获取用户列表 |