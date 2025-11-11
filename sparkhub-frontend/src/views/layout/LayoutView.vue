<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <router-link to="/" class="logo-link">
        <el-icon :size="24" class="logo-icon"><Opportunity /></el-icon>
        <span class="logo-text">SparkHub</span>
      </router-link>

      <el-menu
        mode="horizontal"
        :router="true"
        :default-active="route.path"
        class="nav-menu"
        :ellipsis="false"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <template v-if="isUser">
          <el-menu-item index="/my-backings">我支持的</el-menu-item>
          <el-menu-item index="/my-favorites">我的收藏</el-menu-item>
        </template>
        <template v-if="isCreator">
          <el-menu-item index="/create">发起项目</el-menu-item>
          <el-menu-item index="/my-projects">我的项目</el-menu-item>
        </template>
        <template v-if="isAdmin">
          <el-menu-item index="/admin" class="admin-menu-item">后台管理</el-menu-item>
        </template>
      </el-menu>

      <div class="flex-grow"></div>

      <div class="user-info">
        <div v-if="!userStore.isAuthenticated()" class="auth-buttons">
          <el-button link @click="router.push('/login')">登录</el-button>
          <el-button type="primary" plain @click="router.push('/register')">注册</el-button>
        </div>

        <template v-else>
          <el-badge
            :value="userStore.unreadCount"
            :max="99"
            :hidden="userStore.unreadCount === 0"
            class="notification-badge"
            @click="router.push('/notifications')"
          >
            <el-icon :size="22" class="notification-icon"><Bell /></el-icon>
          </el-badge>

          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar
                :size="32"
                :src="formatImageUrl(userStore.userInfo?.avatar)"
                style="margin-right: 10px"
              />
              <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="notifications">
                  通知中心
                  <el-badge :is-dot="userStore.unreadCount > 0" style="margin-left: 8px" />
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </el-header>

    <el-main class="layout-main">
      <div class="main-content-wrapper">
        <RouterView />
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed, watchEffect } from 'vue'
import { useUserStore } from '@/stores/user'
import { logoutApi } from '@/api/auth'
import { getUnreadCountApi } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Opportunity, Bell } from '@element-plus/icons-vue'
import { formatImageUrl } from '@/utils/format'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const isUser = computed(() => userStore.isUser)
const isCreator = computed(() => userStore.isCreator)
const isAdmin = computed(() => userStore.isAdmin)

// 轮询或在登录时获取未读计数
watchEffect(async () => {
  // 当用户登录后 (isAuthenticated 变为 true)
  if (userStore.isAuthenticated()) {
    try {
      // 调用 API
      const res = await getUnreadCountApi()
      // 更新 Store
      userStore.setUnreadCount(res.count)
    } catch (err: any) {
      console.error('获取未读通知失败:', err.message)
      // (即使用户未登录或 API 失败, 也不打扰用户, 仅在控制台报错)
    }
  }
})

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notifications') {
    // 处理通知导航
    router.push('/notifications')
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('您确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    try {
      await logoutApi()
    } catch (err: any) {
      console.error('登出 API 失败:', err.message)
    }

    userStore.clearLoginSession()
    ElMessage.success('退出成功')

    // 登出后跳转到首页，体验更好
    await router.push('/')
  } catch (err) {
    // 用户点击了 "取消"
  }
}
</script>

<style scoped>
/*
  只定义布局 (layout) 和特定组件的样式。
*/

.layout-container {
  min-height: 100vh;
  /* 使用在 global.css 定义的背景色 */
  background-color: var(--spark-bg-color);
}

.layout-header {
  display: flex;
  align-items: center;
  /* 使用面板色 (白色)，并添加阴影 */
  background-color: var(--spark-panel-color);
  border-bottom: 1px solid var(--spark-border-color);
  height: 64px; /* 增加一点高度 */
  padding: 0 24px; /* 增加左右内边距 */

  /* 粘性头部 */
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Logo 链接样式 */
.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  margin-right: 30px;
}
.logo-icon {
  /* 使用主色调 */
  color: var(--spark-primary-color);
}
.logo-text {
  font-size: 22px;
  font-weight: 600;
  /* 使用主色调 */
  color: var(--spark-primary-color);
  margin-left: 8px;
  font-family:
    'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑',
    Arial, sans-serif;
}

.nav-menu {
  /* 移除菜单的底边框，让 header 统一处理 */
  border-bottom: none;
  /* 确保菜单和 header 高度一致 */
  height: 63px; /* 略小于 64px 避免边框问题 */
  background-color: transparent; /* 背景透明，融入 header */
}
.nav-menu .el-menu-item {
  height: 100%;
  font-size: 15px;
}

.flex-grow {
  flex-grow: 1;
}

/* 未登录时的按钮 */
.auth-buttons .el-button {
  margin-left: 10px;
}

.user-info {
  display: flex;
  align-items: center;
}

/* 通知徽章样式 */
.notification-badge {
  /* 调整徽章本身的位置 */
  margin-right: 20px;
}
.notification-icon {
  cursor: pointer;
  color: var(--spark-text-regular);
  transition: color 0.2s;
}
.notification-icon:hover {
  color: var(--spark-primary-color);
}

.el-dropdown-link {
  cursor: pointer;
  /* 下拉菜单的文字使用标准文字颜色 */
  color: var(--spark-text-regular);
  display: flex;
  align-items: center;
}
.el-dropdown-link:hover {
  /* 鼠标悬停时使用主色调 */
  color: var(--spark-primary-color);
}
.username {
  /* 限制用户名最大宽度，避免太长 */
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主内容区域 */
.layout-main {
  /* 调整内边距，顶部空间大一些 */
  padding: 30px 20px;
  background-color: var(--spark-bg-color);
}

/* 简约风格的内容包装器 */
.main-content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

/*
  为所有菜单项（包括“首页”等）
  在悬浮 (hover) 和激活 (is-active) 时加粗
*/
.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  font-weight: 600;
}

/* 后台管理菜单项的特殊悬停样式 */
.admin-menu-item.el-menu-item:hover {
  background-color: #f5f3f9;
  color: #7e57c2;
  font-weight: 600;
}
</style>
