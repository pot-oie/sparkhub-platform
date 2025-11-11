<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <el-icon :size="28" class="logo-icon"><Opportunity /></el-icon>
          <span class="logo-text">SparkHub</span>
        </div>
        <h3 class="login-title">欢迎回来</h3>
      </template>

      <el-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        label-position="top"
        @submit.native.prevent="handleLogin"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            clearable
            :prefix-icon="User"
            size="large"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
            size="large"
          ></el-input>
        </el-form-item>

        <el-form-item style="margin-top: 30px">
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
            size="large"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        <el-link type="primary" @click="goToRegister">还没有账户？立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { loginApi } from '@/api/auth'
import type { LoginDTO } from '@/api/types/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Opportunity } from '@element-plus/icons-vue'

// 1. 核心变量
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive<LoginDTO>({
  username: '',
  password: '',
})

// 规则触发改为 'change'，体验更好
const formRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'change' }],
})

// 2. 核心函数

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (err) {
    return // 验证失败
  }
  loading.value = true
  try {
    const res = await loginApi(formData)
    ElMessage.success('登录成功！')
    userStore.setLoginSession(res)
    // 登录成功后跳转到首页或重定向地址
    const redirect = router.currentRoute.value.query.redirect as string | undefined
    router.push(redirect || '/')
  } catch (err: any) {
    ElMessage.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  /* 100vh 满屏，并使用全局背景色 */
  min-height: 100vh;
  background-color: var(--spark-bg-color);
  padding: 20px;
}

/* 登录卡片 */
.login-card {
  width: 420px; /* 宽度调整 */
  max-width: 100%;
}

/* 登录头部 */
.login-header {
  display: flex;
  align-items: center;
  justify-content: center; /* 居中 */
}
.logo-icon {
  color: var(--spark-primary-color);
}
.logo-text {
  font-size: 24px;
  font-weight: 600;
  color: var(--spark-primary-color);
  margin-left: 8px;
}
.login-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--spark-text-regular);
  text-align: center;
  margin: 10px 0 -5px 0; /* 调整间距 */
}

/* 表单样式 */
.login-form {
  margin-top: 20px;
}
:deep(.el-form-item__label) {
  /* 增大 label 字体 */
  font-size: 14px;
  font-weight: 500;
}
:deep(.el-input__inner) {
  /* 增大输入框字体 */
  font-size: 14px;
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 10px;
}
</style>
