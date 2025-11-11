<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <div v-if="userStore.userInfo" class="user-info-list">
            <div class="info-item avatar-row">
              <span class="info-label">头像</span>
              <div class="info-value">
                <div v-loading="uploadLoading">
                  <el-avatar
                    :size="80"
                    :src="formatImageUrl(userStore.userInfo.avatar)"
                    class="profile-avatar-clickable"
                    @click="openAvatarDialog"
                  />
                  <span class="avatar-tip">点击头像可更换</span>
                </div>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userStore.userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userStore.userInfo.email || '未提供' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">角色</span>
              <span class="info-value">
                <el-tag
                  v-for="role in userStore.userInfo.roles"
                  :key="typeof role === 'object' ? role.id : role"
                  :type="getRoleTagType(typeof role === 'object' ? role.name : role)"
                  class="role-tag"
                >
                  {{ translateRole(role) }}
                </el-tag>
                <span v-if="!userStore.userInfo.roles?.length">无角色</span>
              </span>
            </div>
          </div>
          <el-empty v-else description="用户信息加载失败" />
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <el-icon><Edit /></el-icon>
              <span>更新信息</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="修改邮箱" name="email">
              <el-form
                :model="formData"
                :rules="formRules"
                ref="emailFormRef"
                label-position="top"
                class="update-form"
              >
                <el-form-item label="新邮箱" prop="email">
                  <el-input
                    v-model="formData.email"
                    placeholder="请输入新邮箱地址"
                    :prefix-icon="Message"
                    size="large"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="loading"
                    @click="handleUpdateEmail"
                    size="large"
                  >
                    更新邮箱
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form
                :model="formData"
                :rules="formRules"
                ref="passwordFormRef"
                label-position="top"
                class="update-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="formData.oldPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                    :prefix-icon="Lock"
                    size="large"
                  ></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="formData.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                    :prefix-icon="Lock"
                    size="large"
                  ></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="formData.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                    :prefix-icon="Lock"
                    size="large"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="loading"
                    @click="handleUpdatePassword"
                    size="large"
                  >
                    更新密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      title="选择新头像"
      width="500px"
      :close-on-click-modal="false"
    >
      <template #title>
        <div class="dialog-header">
          <el-icon><Picture /></el-icon>
          <span>选择新头像</span>
        </div>
      </template>
      <div class="avatar-grid" v-loading="uploadLoading">
        <el-avatar
          v-for="avatarUrl in avatarList"
          :key="avatarUrl"
          :size="80"
          :src="formatImageUrl(avatarUrl)"
          class="avatar-selectable"
          :class="{
            'is-selected': userStore.userInfo?.avatar === avatarUrl,
          }"
          @click="handleSelectAvatar(avatarUrl)"
        />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateEmailApi, updatePasswordApi, updateAvatarApi } from '@/api/user'
import type { FormInstance, FormRules } from 'element-plus'
import { formatImageUrl } from '@/utils/format'
import { User, Edit, Message, Lock, Picture } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const emailFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const activeTab = ref('email')
const formData = reactive({
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// --- 头像上传逻辑 ---
const uploadLoading = ref(false)
const dialogVisible = ref(false)
const avatarList = ref([
  '/uploads/avatar1.jpg',
  '/uploads/avatar2.jpg',
  '/uploads/avatar3.jpg',
  '/uploads/avatar4.jpg',
  '/uploads/avatar5.jpg',
  '/uploads/avatar6.jpg',
  '/uploads/avatar7.jpg',
  '/uploads/avatar8.jpg',
])

const openAvatarDialog = () => {
  if (uploadLoading.value) return
  dialogVisible.value = true
}

const handleSelectAvatar = async (avatarUrl: string) => {
  if (uploadLoading.value) return
  if (userStore.userInfo?.avatar === avatarUrl) {
    dialogVisible.value = false
    return
  }
  uploadLoading.value = true
  try {
    const updatedUser = await updateAvatarApi({ avatarUrl: avatarUrl })
    userStore.setUserAvatar(updatedUser.avatar as string)
    ElMessage.success('头像更新成功！')
    dialogVisible.value = false
  } catch (err: any) {
    ElMessage.error(err.message || '头像更新失败')
  } finally {
    uploadLoading.value = false
  }
}

// --- 角色翻译 ---
const translateRole = (role: any) => {
  const roleName = role.name || role
  switch (roleName) {
    case 'ROLE_ADMIN':
      return '管理员'
    case 'ROLE_CREATOR':
      return '发起者'
    case 'ROLE_USER':
      return '用户'
    default:
      return roleName
  }
}
// 角色 Tag 颜色
const getRoleTagType = (roleName: string) => {
  switch (roleName) {
    case 'ROLE_ADMIN':
      return 'danger'
    case 'ROLE_CREATOR':
      return 'primary' // 自动变为“生机青”
    case 'ROLE_USER':
      return 'success'
    default:
      return 'info'
  }
}

// --- 验证规则 ---
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const validateNewPasswordNotSame = (rule: any, value: any, callback: any) => {
  if (value === formData.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}
const formRules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'change' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'change' },
  ],
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'change' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'change' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'change' },
    { validator: validateNewPasswordNotSame, trigger: 'change' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'change' },
    { validator: validateConfirmPassword, trigger: 'change' },
  ],
})

// --- 生命周期 & 提交 ---
onMounted(() => {
  if (userStore.userInfo?.email) {
    formData.email = userStore.userInfo.email
  }
})

const handleUpdateEmail = async () => {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
  } catch (err) {
    return
  }
  loading.value = true
  try {
    await updateEmailApi({ email: formData.email })
    ElMessage.success('邮箱更新成功！请重新登录。')
    userStore.clearLoginSession()
    router.push('/login')
  } catch (err: any) {
    ElMessage.error(err.message || '更新失败')
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch (err) {
    return
  }
  loading.value = true
  try {
    await updatePasswordApi({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
    })
    ElMessage.success('密码更新成功！请重新登录。')
    userStore.clearLoginSession()
    router.push('/login')
  } catch (err: any) {
    ElMessage.error(err.message || '更新失败')
  } finally {
    loading.value = false
    formData.oldPassword = ''
    formData.newPassword = ''
    formData.confirmPassword = ''
  }
}
</script>

<style scoped>
/* 卡片标题 */
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--spark-text-primary);
}
.card-header .el-icon {
  margin-right: 8px;
}

/* 信息展示列表 */
.user-info-list {
  font-size: 14px;
}
.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--spark-border-color);
}
.info-item:last-child {
  border-bottom: none;
}
.info-item.avatar-row {
  align-items: flex-start;
}
.info-label {
  width: 80px;
  color: var(--spark-text-secondary);
  flex-shrink: 0;
}
.info-value {
  color: var(--spark-text-regular);
  display: flex;
  align-items: center;
  flex-wrap: wrap; /* 允许换行 */
}
.avatar-tip {
  font-size: 12px;
  color: var(--spark-text-secondary);
  margin-left: 12px;
}
.role-tag {
  margin-right: 8px;
  margin-bottom: 4px; /* 允许换行 */
}

/* 更新表单 */
.update-form {
  padding: 0 10px;
}
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
}

/* 弹窗标题 */
.dialog-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}
.dialog-header .el-icon {
  margin-right: 8px;
  color: var(--el-color-primary);
}

/* 头像选择器 (样式不变, 自动应用新主题色) */
.avatar-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: center;
  padding: 10px;
}
.avatar-selectable {
  cursor: pointer;
  border: 3px solid transparent;
  border-radius: 50%;
  transition: all 0.2s ease;
}
.avatar-selectable:hover {
  border-color: var(--el-color-primary-light-3);
  transform: scale(1.05);
}
.avatar-selectable.is-selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 10px var(--el-color-primary-light-5);
}
.profile-avatar-clickable {
  cursor: pointer;
  border: 3px solid transparent;
  border-radius: 50%;
  transition: all 0.2s ease;
}
.profile-avatar-clickable:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 0 10px var(--el-color-primary-light-7);
}
</style>
