// 导入我们封装的 Axios 实例
import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { LoginDTO, LoginResponse, RegisterDTO } from './types/auth'

/**
 * 登录 API
 * @param data {LoginDTO}
 * @returns Promise<LoginResponse>
 */
export function loginApi(data: LoginDTO) {
  // 注意：service 响应拦截器已经帮我们剥离了 Result 对象，
  // 这里拿到的响应 res 直接就是 Result.data 的内容
  return service<BackendResult<LoginResponse>>({
    url: '/auth/login',
    method: 'POST',
    data,
  }) as unknown as Promise<LoginResponse>
}

/**
 * 注册 API
 * @param data {RegisterDTO}
 */
export function registerApi(data: RegisterDTO) {
  // 注册接口通常不返回 data，我们可以指定 void
  return service<BackendResult<void>>({
    url: '/auth/register',
    method: 'POST',
    data,
  }) as unknown as Promise<void>
}

/**
 * 登出 API
 */
export function logoutApi() {
  return service<BackendResult<void>>({
    url: '/auth/logout',
    method: 'POST',
  }) as unknown as Promise<void>
}
