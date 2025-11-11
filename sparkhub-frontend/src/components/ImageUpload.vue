<template>
  <el-upload
    v-model:file-list="fileList"
    :action="uploadUrl"
    :headers="uploadHeaders"
    name="file"
    list-type="picture-card"
    :limit="1"
    :on-exceed="handleExceed"
    :on-success="handleSuccess"
    :on-remove="handleRemove"
    :before-upload="beforeUpload"
  >
    <el-icon v-if="fileList.length < 1"><Plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatImageUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps, UploadUserFile } from 'element-plus'

// 1. v-model
// defineModel 会自动创建 model (它是一个 ref)
// 它将持有父组件传入的 *相对 URL* (e.g., /uploads/xxxx.jpg)
const model = defineModel<string | null>()

// 2. 配置 Upload
const userStore = useUserStore()
// 从你的规格 中获取 action URL
const uploadUrl = 'http://localhost:8080/api/files/upload'
// 规定需要 Token
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`,
}))

// 3. File List
// el-upload 的 UI 状态
const fileList = ref<UploadUserFile[]>([])

// 4. 同步 v-model (父 -> 子)
// 当父组件 (e.g., 编辑页) 传入一个 URL 时, 我们需要更新 fileList 来显示缩略图
watch(
  model,
  (newUrl) => {
    // 检查 (newUrl) 是否存在, 且 (fileList) 是否为空
    if (newUrl && fileList.value.length === 0) {
      fileList.value = [
        {
          name: 'cover.jpg', // 占位符文件名
          url: formatImageUrl(newUrl), // 必须是完整 URL 才能显示
          status: 'success',
        },
      ]
    }
  },
  { immediate: true }, // 立即执行一次
)

// 5. Handlers (子 -> 父)

// 上传成功
const handleSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  // 你的规格: { "code": 200, "data": { "url": "/uploads/xxxx.jpg" } }
  if (response.code === 200) {
    const relativeUrl = response.data.url
    model.value = relativeUrl // 更新 v-model, 将 *相对 URL* 传回父组件
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    fileList.value = [] // 上传失败, 清空列表
  }
}

// 移除图片
const handleRemove: UploadProps['onRemove'] = () => {
  model.value = null // 清空 v-model
}

// 数量超限
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('只能上传一张封面图片，请先移除当前图片。')
}

// 上传前检查
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件 (jpg, png, gif)!')
    return false
  }
  const isLt6M = rawFile.size / 1024 / 1024 < 6
  if (!isLt6M) {
    ElMessage.error('图片大小不能超过 6MB!')
    return false
  }
  return true
}
</script>
