import axios, { type AxiosResponse, type AxiosError } from 'axios'
import { useUserStore } from '@/stores/user'
import type { BackendResult } from '@/api/types/common'
import { ElMessage } from 'element-plus'
import router from '@/router' // 1. 引入 router

// 1. 创建 Axios 实例
const service = axios.create({
  // 保持你设置的 baseURL
  baseURL: 'http://localhost:8080/api',
  timeout: 10000, // 请求超时
})

// 2. 请求拦截器 (Request Interceptor)
service.interceptors.request.use(
  (config) => {
    // 在发送请求前，获取 Pinia
    const userStore = useUserStore()

    // 如果 token 存在，则为每个请求添加 Authorization Header
    // 这是解决你问题的核心：所有请求都会尝试携带 token
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 3. 响应拦截器 (Response Interceptor)
service.interceptors.response.use(
  /**
   * 业务成功处理 (HTTP 状态码为 2xx)
   */
  (response: AxiosResponse<BackendResult<any>>) => {
    const res: BackendResult<any> = response.data

    // code 为 200，代表业务成功
    if (res.code === 200) {
      // 直接返回 data 字段
      return res.data
    }

    // --- 业务错误处理 (code !== 200) ---

    // 401: 未授权 (Token 无效或过期)
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.clearLoginSession() // 清除本地 Token

      // 2. 跳转到登录页，并携带当前页面路径作为 redirect 参数
      router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
    } else {
      // 其他业务错误 (例如 400, 500 等)
      ElMessage.error(res.message || '业务处理失败')
    }

    // 将业务错误 reject 出去，让 .catch() 捕获
    return Promise.reject(new Error(res.message || 'Error'))
  },
  /**
   * HTTP 网络错误处理 (例如 404, 503, CORS 错误等)
   */
  (error: AxiosError) => {
    // 3. 处理 HTTP 网络层面的错误
    let message = ''
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 401:
          message = '未授权，请检查登录状态'
          // 也可在此处执行 401 跳转逻辑
          break
        case 403:
          message = '禁止访问'
          break
        case 404:
          message = '请求的资源未找到'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `HTTP 错误: ${error.response.status}`
      }
    } else if (error.request) {
      // 请求已发出，但未收到响应 (例如网络超时或后端服务器崩溃)
      message = '网络错误，请检查连接或稍后重试'
    } else {
      // 请求在设置时出错 (例如代码BUG)
      message = `请求失败: ${error.message}`
    }

    ElMessage.error(message)
    return Promise.reject(error)
  },
)

// 4. 导出实例
export default service
