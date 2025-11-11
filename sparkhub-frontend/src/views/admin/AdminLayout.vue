<template>
  <el-container class="admin-layout-container">
    <el-header class="admin-header">
      <div class="logo">
        <el-icon :size="24" class="logo-icon"><Opportunity /></el-icon>
        <span class="logo-text">SparkHub - Admin</span>
      </div>
      <div class="flex-grow"></div>
      <div class="user-info">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-icon style="margin-right: 6px"><UserFilled /></el-icon>
            {{ userStore.userInfo?.username || '管理员' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">返回主站</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside width="200px" class="admin-aside">
        <el-menu :default-active="route.path" :router="true" class="admin-menu">
          <el-menu-item index="/admin/projects">
            <el-icon><Document /></el-icon>
            <span>项目审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logoutApi } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Document, User, Opportunity, UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const handleCommand = (command: string) => {
  if (command === 'home') {
    router.push('/')
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
    // 使用 clearLoginSession
    userStore.clearLoginSession()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (err) {
    // 用户点击了 "取消"
  }
}
</script>

<style>
/*
  这个 style 块 *没有* scoped.
  它为后台管理重置了 Element Plus 的主色调。
*/
.admin-layout-container {
  /* 重置为 Element Plus 默认蓝 */
  --el-color-primary: #409eff;
  --el-color-primary-light-1: #53a8ff;
  --el-color-primary-light-2: #66b1ff;
  --el-color-primary-light-3: #79bbff;
  --el-color-primary-light-4: #8cc5ff;
  --el-color-primary-light-5: #9fceff;
  --el-color-primary-light-6: #b2d7ff;
  --el-color-primary-light-7: #c6e0ff;
  --el-color-primary-light-8: #d9e8ff;
  --el-color-primary-light-9: #ecf5ff;
  --el-color-primary-dark-2: #337ecc;
}
</style>

<style scoped>
.admin-layout-container {
  height: 100vh;
}
.admin-header {
  display: flex;
  align-items: center;
  background-color: #2b303b;
  color: #ffffff;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
}
.logo-icon {
  margin-right: 10px;
  color: #ffffff;
}
.logo-text {
  color: #ffffff;
}
.flex-grow {
  flex-grow: 1;
}
.el-dropdown-link {
  cursor: pointer;
  color: #ffffff;
  display: flex;
  align-items: center;
}
.admin-aside {
  background-color: var(--spark-panel-color);
  border-right: 1px solid var(--spark-border-color);
}
.admin-menu {
  height: 100%;
  border-right: none;
}
.admin-main {
  background-color: var(--spark-bg-color);
  padding: 20px;
}
</style>
