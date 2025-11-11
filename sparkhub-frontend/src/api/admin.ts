import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { Project } from './types/project'
import type {
  AdminProjectListParams,
  AdminProjectPage,
  ProjectAuditDTO,
  AdminUserPage,
  UserAdminDTO,
  UserRoleUpdateDTO,
} from './types/admin'

/**
 * @description [Admin] 获取项目列表 (分页)
 * (对应 GET /api/admin/projects)
 */
export const getAdminProjectListApi = (params: AdminProjectListParams) => {
  return service<BackendResult<AdminProjectPage>>({
    url: '/admin/projects',
    method: 'GET',
    params: params,
  }) as unknown as Promise<AdminProjectPage>
}

/**
 * @description [Admin] 审核项目 (通过/拒绝)
 * (对应 PUT /api/admin/projects/{id}/status)
 */
export const auditProjectApi = (id: number, data: ProjectAuditDTO) => {
  return service<BackendResult<Project>>({
    // (假设返回更新后的项目)
    url: `/admin/projects/${id}/status`,
    method: 'PUT',
    data: data,
  }) as unknown as Promise<Project>
}

/**
 * @description [Admin] 获取用户列表 (分页)
 * (对应 GET /api/admin/users)
 */
export const getAdminUserListApi = (params: { pageNum?: number; pageSize?: number }) => {
  return service<BackendResult<AdminUserPage>>({
    url: '/admin/users',
    method: 'GET',
    params: params,
  }) as unknown as Promise<AdminUserPage>
}

/**
 * @description [Admin] 更新用户角色
 * (对应 PUT /api/admin/users/{id}/role)
 * @param userId 用户ID
 * @param data DTO
 * @returns 更新后的 UserAdminDTO
 */
export const updateUserRoleApi = (userId: number, data: UserRoleUpdateDTO) => {
  // 假设后端成功后会返回更新后的 User DTO
  return service<BackendResult<UserAdminDTO>>({
    url: `/admin/users/${userId}/role`,
    method: 'PUT',
    data: data,
  }) as unknown as Promise<UserAdminDTO>
}

// --- TODO: (Category APIs) ---
// export const createCategoryApi = ...
// export const updateCategoryApi = ...
// export const deleteCategoryApi = ...
