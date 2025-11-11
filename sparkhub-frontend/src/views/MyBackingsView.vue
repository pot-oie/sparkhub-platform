<template>
  <div class="my-backings-view">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <el-icon><Collection /></el-icon>
          <span>我支持的项目</span>
        </div>
      </template>

      <el-table :data="backingList" v-loading="loading" style="width: 100%">
        <el-table-column prop="projectTitle" label="项目标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" :href="`/project/${row.projectId}`" target="_blank">
              {{ row.projectTitle }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="rewardTitle" label="回报档位" min-width="180" />

        <el-table-column prop="backingAmount" label="支持金额 (元)" width="150" align="right">
          <template #default="{ row }">
            <span class="amount-raised">{{ row.backingAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTypeFormatter(row.status)">
              {{ statusFormatter(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="backingTime" label="支持时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.backingTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && backingList.length === 0" description="您还没有支持任何项目" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyBackingsApi } from '@/api/backing'
import type { Backing } from '@/api/types/backing'
import { Collection } from '@element-plus/icons-vue'

const loading = ref(true)
const backingList = ref<Backing[]>([])

onMounted(() => {
  fetchMyBackings()
})

const fetchMyBackings = async () => {
  loading.value = true
  try {
    const res = await getMyBackingsApi()
    backingList.value = res
  } catch (err: any) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const statusFormatter = (status: number) => {
  switch (status) {
    case 0:
      return '待支付'
    case 1:
      return '已支付'
    case 2:
      return '已取消'
    default:
      return '未知状态'
  }
}

const statusTypeFormatter = (status: number) => {
  switch (status) {
    case 0:
      return 'warning' // 待支付 (活力黄)
    case 1:
      return 'success' // 已支付 (绿色)
    case 2:
      return 'info' // 已取消 (灰色)
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
