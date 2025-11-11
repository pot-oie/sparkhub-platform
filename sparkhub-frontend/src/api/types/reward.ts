/**
 * 项目回报档位 (ProjectReward) 实体
 */
export interface Reward {
  id: number
  projectId: number
  title: string
  description: string
  amount: number // 支持金额
  stock: number // 名额库存
  imageUrl?: string | null
}
