import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

import LayoutView from '@/views/layout/LayoutView.vue'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ProjectDetailView from '@/views/ProjectDetailView.vue'
import ProjectCreateView from '@/views/ProjectCreateView.vue'
import MyProjectsView from '@/views/MyProjectsView.vue'
import MyBackingsView from '@/views/MyBackingsView.vue'
import MyFavoritesView from '@/views/MyFavoritesView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ProjectEditView from '@/views/ProjectEditView.vue'
import AdminLayout from '@/views/admin/AdminLayout.vue'
import AdminProjectsView from '@/views/admin/AdminProjectsView.vue'
import AdminUsersView from '@/views/admin/AdminUsersView.vue'
import NotificationView from '@/views/NotificationView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // --- 主布局路由 ---
      // 访问 / 时，会先加载 LayoutView 组件
      path: '/',
      name: 'layout',
      component: LayoutView,
      // 在 / 路径下，默认重定向到 /home
      redirect: '/home',
      // "children" 数组中的所有路由都会在 LayoutView 的 <RouterView /> 中显示
      children: [
        { path: 'home', name: 'home', component: HomeView, meta: { title: '首页' } },
        { path: 'profile', name: 'profile', component: ProfileView, meta: { title: '个人中心' } },
        {
          path: 'create',
          name: 'create',
          component: ProjectCreateView,
          meta: { title: '发起项目' },
        },
        {
          path: 'my-projects',
          name: 'myProjects',
          component: MyProjectsView,
          meta: { title: '我的项目' },
        },
        {
          path: 'my-backings',
          name: 'myBackings',
          component: MyBackingsView,
          meta: { title: '我支持的' },
        },
        {
          path: 'my-favorites',
          name: 'myFavorites',
          component: MyFavoritesView,
          meta: { title: '我的收藏' },
        },
        {
          path: 'project/:id',
          name: 'projectDetail',
          component: ProjectDetailView,
          meta: { title: '项目详情' },
        },
        {
          path: 'project/edit/:id',
          name: 'projectEdit',
          component: ProjectEditView,
          meta: { title: '编辑项目' },
        },
        {
          path: 'notifications',
          name: 'notifications',
          component: NotificationView,
          meta: { title: '通知中心' },
        },
      ],
    },
    {
      // --- 后台管理布局 ---
      path: '/admin',
      name: 'admin',
      component: AdminLayout,
      redirect: '/admin/projects',
      meta: { requiresAdmin: true }, // <-- [关键] 标记此路由需要 Admin 权限
      children: [
        {
          path: 'projects', // /admin/projects
          name: 'adminProjects',
          component: AdminProjectsView,
          meta: { title: '项目审核' },
        },
        {
          path: 'users', // /admin/users
          name: 'adminUsers',
          component: AdminUsersView,
          meta: { title: '用户管理' },
        },
        // ... (未来可添加 /admin/categories)
      ],
    },
    // 注册页路由
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { title: '注册' },
    },
    // 登录页路由
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '登录' },
    },
  ],
})

// --- 全局前置路由守卫 ---
// to:     即将要进入的目标路由
// from:   当前正要离开的路由
// next:   一个函数，必须调用它来 RESOLVE 这个钩子
router.beforeEach((to, from, next) => {
  // 1. 获取 Pinia Store
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated()

  // 2. 定义白名单 (不需要登录就能访问的页面)
  const whiteList = ['/login', '/register']

  if (isAuthenticated) {
    // 3. 如果用户已登录
    if (whiteList.includes(to.path)) {
      // 3.1 如果已登录，还想去 登录页/注册页，则重定向到首页
      next({ path: '/' })
      return
    }
    // 3.2 检查目标路由是否需要 Admin 权限
    if (to.meta.requiresAdmin) {
      if (userStore.isAdmin) {
        // 是管理员, 放行
        next()
      } else {
        // 不是管理员, 弹窗提示并踢回主站首页
        ElMessage.error('您没有管理员权限')
        next({ path: '/' }) // 或者 next(from.path) 留在原地
      }
    } else {
      // 3.3 访问的是普通页面 (例如 /home, /profile), 正常放行
      next()
    }
  } else {
    // 4. 如果用户未登录
    if (whiteList.includes(to.path)) {
      // 4.1 如果访问的是白名单页面，则直接放行
      next()
    } else {
      // 4.2 如果访问的是其他受保护页面，则重定向到登录页
      ElMessage.warning('请先登录') // 友好提示
      next({ path: '/login' })
    }
  }
})

export default router
