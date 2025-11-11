import service from '@/utils/request'
import type { PageInfo } from './types/common'
import type {
  Project,
  ProjectListParams,
  ProjectCreateDTO,
  ProjectUpdateDTO,
} from './types/project'

/**
 * @description 获取项目列表 (分页)
 * @param params {ProjectListParams}
 */
export function getProjectListApi(params: ProjectListParams) {
  // GET 请求的参数使用 'params' 字段
  return service<PageInfo<Project>>({
    url: '/projects', // 对应 GET /api/projects
    method: 'GET',
    params: params,
  }) as unknown as Promise<PageInfo<Project>>
}

/**
 * @description 获取项目详情 (公开)
 * @param id 项目 ID
 * @returns Project
 */
export function getProjectDetailApi(id: number) {
  return service<Project>({
    url: `/projects/${id}`, // 对应 GET /api/projects/{id}
    method: 'GET',
  }) as unknown as Promise<Project>
}

/**
 * @description 发起新项目 (ROLE_CREATOR)
 * @param data ProjectCreateDTO
 * @returns Project (后端返回创建成功的项目对象)
 */
export const createProjectApi = (data: ProjectCreateDTO) => {
  return service<Project>({
    url: '/projects', // 对应 POST /api/projects
    method: 'POST',
    data: data, // POST 请求用 data
  }) as unknown as Promise<Project>
}

/**
 * @description 获取我发起的项目列表 (ROLE_CREATOR)
 * @returns Project[] (返回项目数组, 非分页)
 */
export const getMyProjectsApi = () => {
  // 假设接口返回一个数组, 类似于 getMyBackingsApi
  return service<Project[]>({
    url: '/projects/my', // 对应 GET /api/projects/my
    method: 'GET',
  }) as unknown as Promise<Project[]>
}

/**
 * @description 更新一个项目 (ROLE_CREATOR)
 * (对应 PUT /api/projects/{id})
 * @param id 项目ID
 * @param data ProjectUpdateDTO
 * @returns Project (后端返回更新后的项目对象)
 */
export const updateProjectApi = (id: number, data: ProjectUpdateDTO) => {
  return service<Project>({
    url: `/projects/${id}`,
    method: 'PUT',
    data: data,
  }) as unknown as Promise<Project>
}
