// 文件: src/api/user.ts

import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { UpdateAvatarDTO } from './types/user'
import type { UserDTO } from './types/user'

// --- DTO 类型定义 ---

export interface UpdateEmailDTO {
  email: string
}

export interface UpdatePasswordDTO {
  oldPassword: string
  newPassword: string
}

// --- API 函数 ---

/**
 * @description 更新当前用户的邮箱
 * @param data {UpdateEmailDTO}
 */
export const updateEmailApi = (data: UpdateEmailDTO) => {
  // 假设 API 是: PUT /api/users/profile/email
  return service<BackendResult<void>>({
    url: '/users/profile/email',
    method: 'PUT',
    data,
  }) as unknown as Promise<void>
}

/**
 * @description 更新当前用户的密码
 * @param data {UpdatePasswordDTO}
 */
export const updatePasswordApi = (data: UpdatePasswordDTO) => {
  // 假设 API 是: PUT /api/users/profile/password
  return service<BackendResult<void>>({
    url: '/users/profile/password',
    method: 'PUT',
    data,
  }) as unknown as Promise<void>
}

/**
 * @description [用户] 更新当前用户的头像 (通过 URL)
 * (对应 PUT /api/users/avatar)
 */
export const updateAvatarApi = (data: UpdateAvatarDTO) => {
  // 假设后端成功后会返回更新后的 User DTO
  return service<BackendResult<UserDTO>>({
    url: '/users/avatar',
    method: 'POST',
    data: data,
  }) as unknown as Promise<UserDTO>
}
