<template>
  <div class="create-view">
    <el-card class="create-card">
      <template #header>
        <div class="card-header">
          <el-icon><Opportunity /></el-icon>
          <span>发起新项目</span>
        </div>
      </template>

      <ProjectForm
        :form-data="formData"
        :loading="loading"
        submit-label="提交项目审核"
        @submit="handleSubmit"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createProjectApi } from '@/api/project'
import type { ProjectCreateDTO } from '@/api/types/project'
import { ElMessage } from 'element-plus'
import ProjectForm from '@/components/ProjectForm.vue'
import { Opportunity } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

// 1. 表单数据 (父组件持有状态)
const formData = reactive<ProjectCreateDTO>({
  title: '',
  description: '',
  coverImage: '',
  goalAmount: 1000,
  endTime: '',
  categoryId: null,
  rewards: [],
})

// 2. 提交逻辑 (API 调用)
const handleSubmit = async () => {
  loading.value = true
  try {
    // 验证已在 ProjectForm.vue 内部完成
    const newProject = await createProjectApi(formData)
    ElMessage.success('项目创建成功！已提交审核。')
    router.push(`/project/${newProject.id}`)
  } catch (err: any) {
    ElMessage.error(err.message || '项目创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 卡片标题样式 */
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--spark-text-primary);
}
.card-header .el-icon {
  margin-right: 8px;
  color: var(--spark-primary-color);
}
</style>
