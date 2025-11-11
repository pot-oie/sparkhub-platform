/**
 * 分页信息泛型 (来自后端 PageHelper)
 * 注意: 你可能已经在 'src/api/types/common.ts' 中定义了此类型。
 * 如果已定义, 请删除此处的 PageInfo, 并在 'src/api/notification.ts' 中
 * 从 'common.ts' 导入它。
 */
export interface PageInfo<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  size: number
  startRow: number
  endRow: number
  pages: number
  prePage: number
  nextPage: number
  isFirstPage: boolean
  isLastPage: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
}

/**
 * 核心通知对象 (对应 NotificationDTO)
 */
export interface NotificationDTO {
  id: number // 通知的唯一ID
  content: string // 通知内容
  type: string // 通知的机器可读类型
  linkUrl: string // 点击通知后应跳转的前端 URL
  isRead: boolean // 是否已读
  createTime: string // (ISO 8601 格式)

  // --- 互动通知 专属字段 ---
  senderId: number | null // 触发此通知的用户ID
  senderUsername: string | null // 触发者的用户名
  senderAvatar: string | null // 触发者的头像 URL
}

/**
 * API: GET /api/notifications/unread-count 的响应数据类型
 */
export interface NotificationUnreadCountDTO {
  count: number
}

/**
 * API: GET /api/notifications 的查询参数类型
 */
export type NotificationFilter = 'all' | 'unread' | 'system' | 'interaction'

export interface GetNotificationsParams {
  pageNum?: number
  pageSize?: number
  filter?: NotificationFilter
}
