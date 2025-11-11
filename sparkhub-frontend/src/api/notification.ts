import request from '@/utils/request'
import type {
  NotificationDTO,
  PageInfo,
  NotificationUnreadCountDTO,
  GetNotificationsParams,
} from './types/notification'

/**
 * A. 获取未读通知总数 (用于“红点”)
 * GET /api/notifications/unread-count
 */
export const getUnreadCountApi = (): Promise<NotificationUnreadCountDTO> => {
  return request.get('/notifications/unread-count')
}

/**
 * B. 获取我的通知列表 (分页 + 过滤)
 * GET /api/notifications
 * @param params - 包含 pageNum, pageSize, filter 的查询参数
 */
export const getNotificationsApi = (
  params: GetNotificationsParams,
): Promise<PageInfo<NotificationDTO>> => {
  return request.get('/notifications', { params })
}

/**
 * C. 将单条通知标记为已读
 * POST /api/notifications/{id}/read
 * @param id - 要标记为已读的通知 ID
 */
export const markNotificationAsReadApi = (id: number): Promise<null> => {
  return request.post(`/notifications/${id}/read`)
}

/**
 * D. 将所有通知标记为已读
 * POST /api/notifications/read-all
 */
export const markAllNotificationsAsReadApi = (): Promise<null> => {
  return request.post('/notifications/read-all')
}
