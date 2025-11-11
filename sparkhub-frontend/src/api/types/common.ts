/**
 * 后端统一响应数据结构
 * T 是 "data" 字段的类型
 */
export interface BackendResult<T> {
  code: number
  message: string
  data: T
}
/**
 * 后端统一分页数据结构
 * 对应 PageInfo 对象
 */
export interface PageInfo<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  list: T[] // <-- 列表数据
  hasPreviousPage: boolean
  hasNextPage: boolean
}
