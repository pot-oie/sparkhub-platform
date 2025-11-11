/**
 * 角色 (Role) 实体
 */
export interface Role {
  id: number
  name: string // e.g., "ROLE_USER"
}

/**
 * 用户 (User) DTO 实体
 * (对应后端登录时返回的用户信息)
 */
export interface UserDTO {
  id: number
  username: string
  email: string
  avatar: string | null // 头像 URL (可能是 /uploads/xxx.jpg)
  roles: Role[]
}

/**
 * DTO 用于更新头像 (通过 URL)
 * (对应 PUT /api/users/avatar)
 */
export interface UpdateAvatarDTO {
  avatarUrl: string
}
