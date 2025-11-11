import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { CommentDetailDTO, CommentCreateDTO } from './types/comment'

/**
 * @description [新] 获取指定项目的所有评论 (支持排序)
 * (GET /api/projects/{projectId}/comments)
 */
export const getCommentsApi = (projectId: number, sortBy: 'time' | 'hotness' = 'time') => {
  return service<BackendResult<CommentDetailDTO[]>>({
    url: `/projects/${projectId}/comments`,
    method: 'GET',
    params: {
      sortBy: sortBy,
    },
  }) as unknown as Promise<CommentDetailDTO[]>
}

/**
 * @description [新] 发表一条新评论 (或回复)
 * (POST /api/projects/{projectId}/comments)
 */
export const postCommentApi = (projectId: number, data: CommentCreateDTO) => {
  // 假设后端成功后会返回新创建的评论
  return service<BackendResult<CommentDetailDTO>>({
    url: `/projects/${projectId}/comments`,
    method: 'POST',
    data: data,
  }) as unknown as Promise<CommentDetailDTO>
}

/**
 * @description [新] 点赞一条评论
 * (POST /api/comments/{commentId}/like)
 */
export const likeCommentApi = (commentId: number) => {
  return service<BackendResult<void>>({
    url: `/comments/${commentId}/like`,
    method: 'POST',
  }) as unknown as Promise<void>
}

/**
 * @description [新] 取消点赞一条评论
 * (DELETE /api/comments/{commentId}/like)
 */
export const unlikeCommentApi = (commentId: number) => {
  return service<BackendResult<void>>({
    url: `/comments/${commentId}/like`,
    method: 'DELETE',
  }) as unknown as Promise<void>
}
