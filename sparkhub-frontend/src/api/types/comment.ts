/**
 * [新] DTO (Data Transfer Object) 用于显示一条评论 (支持嵌套)
 * (对应 GET /api/projects/{projectId}/comments)
 */
export interface CommentDetailDTO {
  id: number
  content: string
  createTime: string // (ISO 8601 格式)
  likeCount: number // 点赞总数
  isLiked: boolean // 当前用户是否已点赞

  // 评论者信息
  userId: number
  username: string
  avatar: string // (URL 路径, e.g., "/uploads/avatar1.jpg")

  // --- 嵌套评论 ---
  parentId: number | null // (如果是顶级评论, 此值为 null)
  replies: CommentDetailDTO[] // (此评论下的所有回复)
}

/**
 * [新] DTO 用于发表一条新评论 (支持嵌套)
 * (对应 POST /api/projects/{projectId}/comments)
 */
export interface CommentCreateDTO {
  content: string
  parentId: number | null // (null = 顶级评论, number = 回复)
}
