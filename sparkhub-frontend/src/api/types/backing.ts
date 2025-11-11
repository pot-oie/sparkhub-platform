/**
 * 创建订单 (Backing) 的请求 DTO
 */
export interface CreateBackingDTO {
  rewardId: number
}

/**
 * 订单 (Backing) 实体
 * (后端创建订单后返回的对象)
 */
export interface Backing {
  id: number
  backerId: number
  projectId: number
  rewardId: number
  backingAmount: number
  status: number // 0:待支付, 1:已支付, 2:已取消
  createTime: string
}
