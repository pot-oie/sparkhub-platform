<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </div>
    </template>

    <el-table :data="userList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />

      <el-table-column label="基础角色" width="100">
        <template #default="{ row }">
          <el-tag v-if="hasRole(row, 'ROLE_USER')" type="info">用户</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="发起者 (Creator)" width="150" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="hasRole(row, 'ROLE_CREATOR')"
            :loading="row.loading"
            :before-change="() => handleRoleChange(row, 'ROLE_CREATOR')"
          />
        </template>
      </el-table-column>

      <el-table-column label="管理员 (Admin)" width="150" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="hasRole(row, 'ROLE_ADMIN')"
            :loading="row.loading"
            :before-change="() => handleRoleChange(row, 'ROLE_ADMIN')"
            :disabled="isSelf(row)"
          />
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && userList.length === 0" description="暂无用户" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminUserListApi, updateUserRoleApi } from '@/api/admin'
import type { UserAdminDTO } from '@/api/types/admin'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const loading = ref(true)
const userStore = useUserStore()
type UserAdminDTOWithLoading = UserAdminDTO & { loading?: boolean }
const userList = ref<UserAdminDTOWithLoading[]>([])

const listParams = ref({
  pageNum: 1,
  pageSize: 10,
})

onMounted(() => {
  fetchUsers()
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUserListApi(listParams.value)
    userList.value = res.list
  } catch (err: any) {
    ElMessage.error(err.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const hasRole = (user: UserAdminDTO, roleName: string) => {
  if (!user.roles || !Array.isArray(user.roles)) return false
  return user.roles.some((r: any) => (r.name || r) === roleName)
}
const isSelf = (user: UserAdminDTO) => {
  return user.username === userStore.userInfo?.username
}
const handleRoleChange = async (user: UserAdminDTOWithLoading, roleName: string) => {
  const isAdd = !hasRole(user, roleName)
  const actionText = isAdd ? '授予' : '移除'
  if (isSelf(user) && roleName === 'ROLE_ADMIN' && !isAdd) {
    ElMessage.error('不能移除自己的管理员权限')
    return false
  }
  try {
    await ElMessageBox.confirm(
      `确定要 ${actionText} [${user.username}] 的 ${translateRole(roleName)} 权限吗?`,
      '权限变更确认',
      { type: 'warning' },
    )
    user.loading = true
    const res = await updateUserRoleApi(user.id, {
      roleName: roleName,
      isAdd: isAdd,
    })
    user.roles = res.roles
    ElMessage.success('权限更新成功')
    return true
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '操作失败')
    }
    return false
  } finally {
    user.loading = false
  }
}
const translateRole = (role: string) => {
  switch (role) {
    case 'ROLE_ADMIN':
      return '管理员'
    case 'ROLE_CREATOR':
      return '发起者'
    case 'ROLE_USER':
      return '用户'
    default:
      return role
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary); /* 标题使用“专业蓝” */
}
.card-header .el-icon {
  margin-right: 8px;
}
</style>
