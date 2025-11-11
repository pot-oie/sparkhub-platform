<template>
  <div class="edit-view">
    <el-card class="edit-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><Edit /></el-icon>
          <span>编辑项目</span>
        </div>
      </template>

      <ProjectForm
        v-if="formData"
        :form-data="formData"
        :loading="submitLoading"
        submit-label="提交修改并重新审核"
        @submit="handleSubmit"
      />

      <el-empty v-if="!loading && !formData" description="项目加载失败或您无权编辑" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProjectDetailApi, updateProjectApi } from '@/api/project'
import type { ProjectUpdateDTO } from '@/api/types/project'
import { ElMessage } from 'element-plus'
import ProjectForm from '@/components/ProjectForm.vue'
import { Edit } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 页面加载 (loading) 和 提交加载 (submitLoading)
const loading = ref(true)
const submitLoading = ref(false)
const projectId = ref<number>(Number(route.params.id))

// formData (必须为 ref(null) 因为它需要被异步加载)
const formData = ref<ProjectUpdateDTO | null>(null)

/**
 * 1. 加载数据并回填表单
 */
onMounted(async () => {
  if (!projectId.value) {
    ElMessage.error('项目ID无效')
    loading.value = false
    return
  }
  try {
    const res = await getProjectDetailApi(projectId.value)
    // 检查项目状态，防止编辑已开始的项目
    if (res.status !== 0 && res.status !== 3) {
      ElMessage.warning('该项目已在众筹中或已成功, 无法编辑。')
      return
    }
    // 将 API 响应 映射为 ProjectUpdateDTO
    formData.value = {
      title: res.title,
      description: res.description,
      coverImage: res.coverImage,
      endTime: res.endTime,
      categoryId: res.categoryId || null,
      goalAmount: res.goalAmount,
      // 映射回报 (注意：新回报没有 id)
      rewards: res.rewards
        ? res.rewards.map((r) => ({
            title: r.title,
            description: r.description,
            amount: r.amount,
            stock: r.stock,
          }))
        : [],
    }
  } catch (err: any) {
    ElMessage.error(err.message || '项目数据加载失败')
  } finally {
    loading.value = false
  }
})
/**
 * 2. 提交更新
 * (ProjectForm 已经完成了验证)
 */
const handleSubmit = async () => {
  if (!formData.value) return

  submitLoading.value = true
  try {
    // 验证已在 ProjectForm 内部完成，直接调用 API
    await updateProjectApi(projectId.value, formData.value)
    ElMessage.success('项目更新成功！已重新提交审核。')
    // 跳转到 "我的项目" 列表页
    router.push('/my-projects')
  } catch (err: any) {
    ElMessage.error(err.message || '项目更新失败')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
/* 卡片标题样式 (同 CreateView) */
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
