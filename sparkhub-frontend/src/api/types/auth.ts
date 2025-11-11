import type { UserDTO } from './user'

/**
 * 登录 DTO
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 登录响应 (LoginResponse)
 */
export interface LoginResponse {
  token: string
  user: UserDTO
}

/**
 * 注册 DTO
 */
export interface RegisterDTO {
  username: string
  password: string
  email: string
}
