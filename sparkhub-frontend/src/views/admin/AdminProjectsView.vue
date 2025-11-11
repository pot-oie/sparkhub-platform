<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <el-icon><DocumentChecked /></el-icon>
        <span>项目审核</span>
      </div>
    </template>

    <div class="filter-bar">
      <span class="filter-label">项目状态:</span>
      <el-radio-group v-model="listParams.status" @change="handleFilterChange">
        <el-radio-button :label="-1">全部</el-radio-button>
        <el-radio-button :label="0">审核中</el-radio-button>
        <el-radio-button :label="1">众筹中</el-radio-button>
        <el-radio-button :label="2">众筹成功</el-radio-button>
        <el-radio-button :label="3">众筹失败</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="projectList" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="项目标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" :href="`/project/${row.id}`" target="_blank">
            {{ row.title }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column prop="creatorName" label="发起者" width="120" />

      <el-table-column prop="status" label="当前状态" width="120">
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

      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div v-if="row.status === 0">
            <el-button type="success" link size="small" @click="handleAudit(row.id, 1)">
              通过
            </el-button>
            <el-button type="danger" link size="small" @click="handleAudit(row.id, 3)">
              拒绝
            </el-button>
          </div>
          <span v-else>--</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="!loading && total > 0"
      layout="prev, pager, next"
      :total="total"
      :current-page="listParams.pageNum"
      :page-size="listParams.pageSize"
      @current-change="handlePageChange"
      class="pagination-center"
      :background="true"
    />

    <el-empty v-if="!loading && projectList.length === 0" description="暂无项目" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminProjectListApi, auditProjectApi } from '@/api/admin'
import type { ProjectSummaryDTO } from '@/api/types/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentChecked } from '@element-plus/icons-vue'

const loading = ref(true)
const projectList = ref<ProjectSummaryDTO[]>([])
const total = ref(0)

// 筛选状态
const listParams = ref({
  pageNum: 1,
  pageSize: 10,
  status: 0, // 默认只看 "审核中"
})

onMounted(() => {
  fetchProjects()
})

const fetchProjects = async () => {
  loading.value = true
  try {
    // 1. 获取 listParams.value 的当前值
    const currentParams = listParams.value

    // 2. 构建要发送给 API 的参数对象
    const params: any = {
      pageNum: currentParams.pageNum,
      pageSize: currentParams.pageSize,
    }

    // 3. 仅在状态不是 "全部" (-1) 时，才添加 status 参数
    if (currentParams.status !== -1) {
      params.status = currentParams.status
    }

    // 4. 发送请求
    const res = await getAdminProjectListApi(params)

    projectList.value = res.list
    total.value = res.total
  } catch (err: any) {
    ElMessage.error(err.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 筛选变更时重新加载
const handleFilterChange = () => {
  listParams.value.pageNum = 1 // 回到第一页
  fetchProjects()
}

// 分页处理函数
const handlePageChange = (newPage: number) => {
  listParams.value.pageNum = newPage
  fetchProjects()
}

const handleAudit = async (id: number, newStatus: number) => {
  const actionText = newStatus === 1 ? '通过' : '拒绝并删除'
  try {
    await ElMessageBox.confirm(`您确定要 "${actionText}" 这个项目吗？`, '审核确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await auditProjectApi(id, { status: newStatus })
    ElMessage.success(`操作成功：项目已${actionText}`)
    // 刷新列表 (如果只看 "审核中", 移除是正确行为)
    if (listParams.value.status === 0) {
      projectList.value = projectList.value.filter((p) => p.id !== id)
    } else {
      await fetchProjects() // 如果在看其他列表，则完整刷新
    }
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '操作失败')
    }
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
      return '未知'
  }
}

// 状态格式化 (众筹中 变为 'primary')
const statusTypeFormatter = (status: number) => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'primary' // 自动应用“专业蓝”
    case 2:
      return 'success'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary); /* 标题使用“专业蓝” */
}
.card-header .el-icon {
  margin-right: 8px;
}
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.filter-label {
  font-size: 14px;
  color: var(--spark-text-regular);
  margin-right: 10px;
}
.pagination-center {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 10px;
}
</style>
