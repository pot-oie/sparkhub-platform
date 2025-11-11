<template>
  <div class="my-projects-view">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <el-icon><Briefcase /></el-icon>
          <span>我发起的项目</span>
        </div>
      </template>

      <el-table :data="projectList" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="项目标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" :href="`/project/${row.id}`" target="_blank">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="项目状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTypeFormatter(row.status)">
              {{ statusFormatter(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="goalAmount" label="目标金额 (元)" width="150" align="right">
          <template #default="{ row }">
            {{ row.goalAmount.toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column prop="currentAmount" label="已筹集 (元)" width="150" align="right">
          <template #default="{ row }">
            <span class="amount-raised">{{ row.currentAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="endTime" label="截止日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <router-link
              v-if="row.status === 0 || row.status === 3"
              :to="`/project/edit/${row.id}`"
            >
              <el-button type="primary" link size="small"> 编辑 </el-button>
            </router-link>
            <span v-else>--</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && projectList.length === 0" description="您还没有发起任何项目" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyProjectsApi } from '@/api/project'
import type { Project } from '@/api/types/project'
import { Briefcase } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const projectList = ref<Project[]>([])

onMounted(() => {
  fetchMyProjects()
})

const fetchMyProjects = async () => {
  loading.value = true
  try {
    const res = await getMyProjectsApi()
    projectList.value = res as Project[]
  } catch (err: any) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const statusFormatter = (status: number) => {
  switch (status) {
    case 0:
      return '审核中'
    case 1:
      return '众筹中'
    case 2:
      return '众筹成功'
    case 3:
      return '众筹失败'
    default:
      return '未知状态'
  }
}

/**
 * 格式化项目状态 (type)
 */
const statusTypeFormatter = (status: number) => {
  switch (status) {
    case 0:
      return 'info' // 审核中 (灰色)
    case 1:
      return 'primary' // 众筹中 (生机青)
    case 2:
      return 'success' // 成功 (绿色)
    case 3:
      return 'danger' // 失败 (红色)
    default:
      return 'info'
  }
}

const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '--'
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN', { hour12: false })
  } catch (e) {
    return dateTimeStr
  }
}
</script>

<style scoped>
/* 卡片标题样式 (同 ProfileView) */
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--spark-text-primary);
}
.card-header .el-icon {
  margin-right: 8px;
  color: var(--spark-primary-color); /* 生机青 */
}

/* 高亮“已筹集”或“支持”金额 */
.amount-raised {
  color: var(--spark-accent-color); /* 活力黄 */
  font-weight: 600;
}
</style>
