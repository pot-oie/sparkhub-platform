// 文件: src/api/favorite.ts

import service from '@/utils/request'
import type { BackendResult } from './types/common'
// 假设“我的收藏”返回的是项目列表
import type { ProjectSummaryDTO } from './types/project'

/**
 * @description 获取我收藏的项目列表
 * @returns Promise<Project[]>
 */
export const getMyFavoritesApi = () => {
  return service<BackendResult<ProjectSummaryDTO[]>>({
    url: '/favorites/my', // 对应 GET /api/favorites/my
    method: 'GET',
  }) as unknown as Promise<ProjectSummaryDTO[]>
}

/**
 * @description 添加收藏
 * @param projectId 项目 ID
 * @returns Promise<void>
 */
export const addFavoriteApi = (projectId: number) => {
  return service<BackendResult<void>>({
    url: `/favorites/${projectId}`, // 对应 POST /api/favorites/{projectId}
    method: 'POST',
  }) as unknown as Promise<void>
}

/**
 * @description 取消收藏
 * @param projectId 项目 ID
 * @returns Promise<void>
 */
export const removeFavoriteApi = (projectId: number) => {
  return service<BackendResult<void>>({
    url: `/favorites/${projectId}`, // 对应 DELETE /api/favorites/{projectId}
    method: 'DELETE',
  }) as unknown as Promise<void>
}
