import type { Reward } from './reward'

/**
 * 项目摘要 DTO (用于列表页)
 * 对应后端的 ProjectSummaryDTO.java
 */
export interface ProjectSummaryDTO {
  id: number
  title: string
  description: string
  coverImage: string
  goalAmount: number
  currentAmount: number
  status: number
  endTime: string
  // (注意: 摘要 DTO 通常不包含 'rewards' 列表)
}

/**
 * 项目 (Project) 实体
 * 根据数据库设计和业务需求
 */
export interface Project {
  id: number
  title: string
  description: string
  coverImage: string
  goalAmount: number
  currentAmount: number
  status: number // 0:审核中, 1:众筹中, 2:成功, 3:失败
  endTime: string // 假设后端返回字符串日期
  rewards?: Reward[] // 可选的回报档位列表
  isFavorite?: boolean // 是否已收藏
  categoryId: number | null
  creatorId: number
  backerIds: number[] // 支持者用户 ID 列表
}

/**
 * 获取项目列表的查询参数
 */
export interface ProjectListParams {
  pageNum?: number
  pageSize?: number
  categoryId?: number // (可选) 按分类筛选
  status?: number // (可选) 按状态筛选
  // ... 其他可能的搜索字段
}

/**
 * 创建回报档位的 DTO (用于嵌套在 ProjectCreateDTO 中)
 */
export interface RewardCreateDTO {
  title: string
  description: string
  amount: number
  stock: number
  imageUrl?: string | null
}

/**
 * 创建项目的 DTO
 * (对应 ProjectCreateDTO.java)
 */
export interface ProjectCreateDTO {
  title: string
  description: string
  coverImage: string // 暂时先用 URL，后续可改为文件上传
  goalAmount: number
  endTime: string // 格式: "YYYY-MM-DDTHH:mm:ss" (ISO 字符串)
  categoryId: number | null // 假设需要一个分类 ID

  // 嵌套的回报档位列表
  rewards: RewardCreateDTO[]
}

/**
 * 更新项目的 DTO
 * (对应 ProjectUpdateDTO.java)
 * (注意: 根据后端 service, 暂不支持更新金额和回报)
 */
export interface ProjectUpdateDTO {
  title: string
  description: string
  coverImage: string
  endTime: string // 格式: "YYYY-MM-DDTHH:mm:ss"
  categoryId: number | null
  goalAmount: number
  rewards: RewardCreateDTO[]
}
