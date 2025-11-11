import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { CreateBackingDTO, Backing } from './types/backing'

/**
 * @description 1. 创建支持订单 (选择回报)
 * @param data {CreateBackingDTO}
 * @returns Promise<Backing> (返回新创建的订单)
 */
export function createBackingApi(data: CreateBackingDTO) {
  return service<BackendResult<Backing>>({
    url: '/backings', // POST /api/backings
    method: 'POST',
    data,
  }) as unknown as Promise<Backing>
}

/**
 * @description 2. 模拟支付订单
 * @param id {number} 订单ID
 */
export function payBackingApi(id: number) {
  return service<BackendResult<void>>({
    url: `/backings/${id}/pay`, // POST /api/backings/{id}/pay
    method: 'POST',
  }) as unknown as Promise<void>
}

/**
 * @description (预留) 获取我支持的订单列表
 */
export function getMyBackingsApi() {
  return service<BackendResult<Backing[]>>({
    // 假设返回列表
    url: '/backings/my', // GET /api/backings/my
    method: 'GET',
  }) as unknown as Promise<Backing[]>
}
