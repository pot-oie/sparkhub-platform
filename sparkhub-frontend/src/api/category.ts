import service from '@/utils/request'
import type { BackendResult } from './types/common'
import type { CategoryDTO } from './types/category'

export const getCategoryListApi = () => {
  return service<BackendResult<CategoryDTO[]>>({
    url: '/categories',
    method: 'GET',
  }) as unknown as Promise<CategoryDTO[]>
}
