import type { ProjectSummaryDTO } from './project' // 导入项目摘要
import type { PageInfo } from './common'

/**
 * [Admin] 获取项目列表 (用于审核)
 * (假设与 'GET /api/projects' 使用相同的参数和响应)
 */
export interface AdminProjectListParams {
  pageNum?: number
  pageSize?: number
  status?: number // (例如, status=0 只看"审核中")
}

// (假设返回的是 ProjectSummaryDTO 的分页)
export type AdminProjectPage = PageInfo<ProjectSummaryDTO>

/**
 * [Admin] 审核项目 DTO
 * (对应 PUT /api/admin/projects/{id}/status)
 */
export interface ProjectAuditDTO {
  status: number // (例如 1=通过, 3=拒绝)
}

/**
 * [Admin] 用户 DTO (用于列表)
 * (对应 GET /api/admin/users)
 */
export interface UserAdminDTO {
  id: number
  username: string
  email: string
  roles: any[] // (假设 'roles' 是一个数组)
  // ... 其他你后端返回的字段
}

// (假设返回的是 UserAdminDTO 的分页)
export type AdminUserPage = PageInfo<UserAdminDTO>

/**
 * [Admin] 更新用户角色 DTO
 * (对应 PUT /api/admin/users/{id}/role)
 */
export interface UserRoleUpdateDTO {
  roleName: string // e.g., "ROLE_CREATOR" or "ROLE_ADMIN"
  isAdd: boolean
}
