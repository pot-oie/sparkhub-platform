<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <el-icon :size="28" class="logo-icon"><Opportunity /></el-icon>
          <span class="logo-text">SparkHub</span>
        </div>
        <h3 class="register-title">创建新账户</h3>
      </template>

      <el-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        label-position="top"
        @submit.native.prevent="handleRegister"
        class="register-form"
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

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            clearable
            :prefix-icon="Message"
            size="large"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入 6 位以上密码"
            show-password
            :prefix-icon="Lock"
            size="large"
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
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
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        <el-link type="primary" @click="goToLogin">已有账户？立即登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
import type { RegisterDTO } from '@/api/types/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Message, Opportunity } from '@element-plus/icons-vue'

// --- 1. 核心变量 ---
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

// --- 2. 验证规则 ---

// 自定义“确认密码”验证器
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致！'))
  } else {
    callback()
  }
}

// 表单验证规则 (trigger 改为 'change')
const formRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'change' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'change' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'change' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'change' }],
})

// --- 3. 核心函数 ---

const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (err) {
    return // 验证失败
  }
  loading.value = true
  const apiData: RegisterDTO = {
    username: formData.username,
    email: formData.email,
    password: formData.password,
  }
  try {
    await registerApi(apiData)
    ElMessage.success('注册成功！即将跳转到登录页...')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err: any) {
    ElMessage.error(err.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 复用 LoginView 的样式 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  /* 100vh 满屏，并使用全局背景色 */
  min-height: 100vh;
  background-color: var(--spark-bg-color);
  padding: 20px;
}

.register-card {
  width: 420px;
  max-width: 100%;
}

/* 注册头部 */
.register-header {
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
.register-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--spark-text-regular);
  text-align: center;
  margin: 10px 0 -5px 0;
}

/* 表单样式 */
.register-form {
  margin-top: 20px;
}
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
}
:deep(.el-input__inner) {
  font-size: 14px;
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 10px;
}
</style>
