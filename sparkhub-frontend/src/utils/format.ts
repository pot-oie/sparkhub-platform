// 从你的规格中获取服务器的基础 URL
const SERVER_BASE_URL = 'http://localhost:8080'

/**
 * @description 将后端返回的相对路径 (/uploads/xxxx.jpg) 转换为完整 URL
 * @param relativePath (e.g., /uploads/xxxx.jpg)
 * @returns 完整的可访问 URL
 */
export const formatImageUrl = (relativePath: string | undefined | null) => {
  if (!relativePath) {
    return '' // 返回空字符串, el-image 会显示占位符
  }
  // 避免重复拼接
  if (relativePath.startsWith('http')) {
    return relativePath
  }
  return `${SERVER_BASE_URL}${relativePath}`
}
