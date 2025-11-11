# SparkHub (星火计划) 众筹平台前端

> 🔗 **相关项目：** [SparkHub 后端仓库链接](../sparkhub)

本项目是 SparkHub 众筹平台的客户端，采用现代化前端技术栈 Vue 3 + Vite + Element Plus 构建，旨在提供高性能、流畅的用户体验。

## 🛠️ 技术栈概览

- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia (包含持久化插件)
- **语言**: TypeScript
- **请求库**: Axios

## ✨ 核心亮点与安全设计

- **统一主题与风格**: 使用自定义 CSS 变量在 `global.css` 中定义了品牌色调（生机青），并全面覆盖 Element Plus 主题色，确保了界面的专业度和视觉一致性。
- **集中式状态管理**: 使用 Pinia 管理用户认证状态 (`token`, `userInfo`) 和未读通知计数 (`unreadCount`)，并使用持久化插件避免页面刷新时状态丢失。
- **安全路由守卫**: 通过 Vue Router 的 `beforeEach` 钩子，实现了基于 Pinia 状态的权限校验，严格阻止非授权用户访问管理员 (`/admin`) 或个人中心 (`/profile`) 等受保护路由，并引导用户登录。
- **统一 API 处理**: 封装的 Axios 实例自动附加 JWT Token 到请求头，并统一拦截处理后端返回的错误码，例如遇到 401 错误时自动清除本地 Token 并跳转至登录页。

## ⚙️ 核心目录结构

本项目遵循标准 Vue CLI / Vite 结构，核心业务逻辑位于 `src/` 目录下：

```plaintext
sparkhub-frontend/
├── src/
│   ├── api/          # 所有后端接口封装和类型定义
│   ├── components/   # 通用和业务组件 (如 ImageUpload, ProjectForm)
│   ├── router/       # 路由配置及全局权限守卫 (index.ts)
│   ├── stores/       # Pinia 状态管理模块
│   ├── styles/       # 全局样式和主题色定义 (global.css)
│   ├── utils/        # 工具函数 (请求拦截, URL格式化)
│   └── views/        # 页面视图 (前台Layout, AdminLayout, HomeView等)
└── ...
```

## 🚀 项目运行指南

在运行前端项目前，请确保 Node.js 环境已就绪，并且**后端 API 服务已经启动**在 `http://localhost:8080`。

### 1. 安装依赖

```bash
npm install
```

### 2. 开发模式运行 (热重载)

服务将启动在 http://localhost:5173/。

```bash
npm run dev
```

### 3. 构建生产包

```bash
npm run build
```

### 4. 代码质量检查

```bash
npm run lint
```
