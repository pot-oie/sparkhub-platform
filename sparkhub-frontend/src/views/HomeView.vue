<template>
  <div class="home-view" v-loading="loading">
    <div class="hero-section">
      <h1 class="hero-title">
        <el-icon :size="32" style="margin-right: 12px"><Opportunity /></el-icon>
        点亮你的创意
      </h1>
      <p class="hero-subtitle">在 SparkHub 找到支持，将你的想法变为现实。</p>
      <el-button
        v-if="isCreator"
        type="primary"
        size="large"
        class="hero-cta-button"
        @click="router.push('/create')"
      >
        发起你的项目
      </el-button>
    </div>

    <div class="section-header">
      <h2 class="section-title">探索项目</h2>
    </div>

    <el-row :gutter="20">
      <el-col
        :span="8"
        v-for="project in projectList"
        :key="project.id"
        style="margin-bottom: 20px"
      >
        <el-card shadow="hover" class="project-card" :body-style="{ padding: '0px' }">
          <router-link :to="`/project/${project.id}`">
            <el-image :src="formatImageUrl(project.coverImage)" fit="cover" class="project-image">
              <template #placeholder>
                <div class="image-placeholder">加载中...</div>
              </template>
              <template #error>
                <div class="image-placeholder">图片加载失败</div>
              </template>
            </el-image>
          </router-link>

          <div class="project-info">
            <router-link :to="`/project/${project.id}`" class="project-link">
              <h3 class="project-title">{{ project.title }}</h3>
            </router-link>

            <p class="project-description">{{ project.description }}</p>

            <div class="project-progress">
              <el-progress
                :percentage="calculatePercentage(project.currentAmount, project.goalAmount)"
                :color="percentageColor(project.currentAmount, project.goalAmount)"
                :stroke-width="10"
                :show-text="false"
              />
              <span class="progress-text">
                {{ Math.round(calculatePercentage(project.currentAmount, project.goalAmount)) }}%
              </span>
            </div>

            <div class="project-stats">
              <div class="stat-item">
                <span class="label">已筹集</span>
                <span class="value raised">¥{{ project.currentAmount }}</span>
              </div>
              <div class="stat-item">
                <span class="label">目标</span>
                <span class="value">¥{{ project.goalAmount }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination
      v-if="!loading && totalProjects > 0"
      layout="prev, pager, next"
      :total="totalProjects"
      :current-page="listParams.pageNum"
      :page-size="listParams.pageSize"
      @current-change="handlePageChange"
      class="pagination-center"
      :background="true"
    />

    <el-empty v-if="!loading && projectList.length === 0" description="暂无众筹项目" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProjectListApi } from '@/api/project'
import type { Project } from '@/api/types/project'
import { formatImageUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Opportunity } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

// --- 1. 状态定义 ---
const loading = ref(true)
const router = useRouter()
const projectList = ref<Project[]>([])
const totalProjects = ref(0)
const listParams = ref({
  pageNum: 1,
  pageSize: 9, // 3x3 布局
})
const userStore = useUserStore()
const isCreator = computed(() => userStore.isCreator)

// --- 2. 加载数据 ---
onMounted(() => {
  fetchProjectList()
})

const fetchProjectList = async () => {
  loading.value = true
  try {
    const res = await getProjectListApi(listParams.value)
    projectList.value = res.list
    totalProjects.value = res.total
  } catch (err: any) {
    ElMessage.error(err.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 处理分页
const handlePageChange = (newPage: number) => {
  listParams.value.pageNum = newPage
  fetchProjectList()
}

// --- 3. 辅助函数 ---

const calculatePercentage = (current: number, goal: number) => {
  if (isNaN(current) || isNaN(goal) || goal === 0) {
    return 0
  }
  const percentage = (current / goal) * 100
  return Math.min(percentage, 100)
}

/**
 * 进度条颜色
 * 使用我们在 global.css 定义的 CSS 变量
 */
const percentageColor = (current: number, goal: number) => {
  if (calculatePercentage(current, goal) >= 100) {
    return 'var(--el-color-success)' // Element 的成功绿
  }
  // 返回主色调 (生机青)
  return 'var(--el-color-primary)'
}
</script>

<style scoped>
/* --- 英雄区域 --- */
.hero-section {
  text-align: center;
  padding: 50px 20px;
  background-color: var(--spark-panel-color); /* 白色背景 */
  border-radius: 12px; /* 匹配卡片圆角 */
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); /* 柔和阴影 */
}
.hero-title {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 10px 0;
  /* 使用主色调 */
  color: var(--spark-primary-color);
}
.hero-subtitle {
  font-size: 18px;
  color: var(--spark-text-regular);
  margin-bottom: 25px;
}
.hero-cta-button {
  font-size: 16px;
  padding: 20px 30px;
  /* 按钮会自动使用主色调 */
}

/* --- 列表标题 --- */
.section-header {
  margin-bottom: 20px;
}
.section-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--spark-text-primary);
  margin: 0;
}

/* --- 项目卡片 --- */
.project-card {
  /* 确保卡片在 flex 布局中等高 */
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}
.project-card:hover {
  /* 鼠标悬停时轻微上浮 */
  transform: translateY(-5px);
}

.project-image {
  width: 100%;
  height: 200px;
  display: block;
  /* 图片顶部圆角与卡片匹配 */
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  background-color: var(--spark-bg-color); /* 浅灰背景 */
}
/* 图片占位符 */
.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: var(--spark-bg-color);
  color: var(--spark-text-secondary);
}

/* 卡片信息区 */
.project-info {
  padding: 16px;
  flex-grow: 1; /* 占据剩余空间 */
  display: flex; /* 设为 flex 容器 */
  flex-direction: column; /* 垂直布局 */
}

/* 标题链接 */
.project-link {
  text-decoration: none;
  color: var(--spark-text-primary);
}
.project-link:hover {
  color: var(--spark-primary-color);
}
.project-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: inherit; /* 继承 .project-link 的颜色 */
  transition: color 0.2s ease;
  /* 2 行省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5; /* 1.5em * 2 lines = 3em */
  height: 3em; /* 必须设置高度 */
}

.project-description {
  font-size: 14px;
  color: var(--spark-text-regular);
  flex-grow: 1; /* 把下面的内容推到底部 */
  margin: 0 0 15px 0;
  /* 2 行省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 3em;
}
/* 进度条组件占满剩余空间 */
.project-progress .el-progress {
  flex-grow: 1;
}
/* 进度条区域 */
.project-progress {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}
/* 进度条百分比文字 */
.progress-text {
  font-size: 14px;
  font-weight: 600;
  /* 使用主色调 (生机青) */
  color: var(--spark-primary-color);
  margin-left: 10px;
  width: 40px; /* 固定宽度，防止布局跳动 */
  text-align: right;
  flex-shrink: 0; /* 防止被压缩 */
}
/* 统计数据 */
.project-stats {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid var(--spark-border-color);
  padding-top: 15px;
}
.stat-item {
  display: flex;
  flex-direction: column;
}
.stat-item .label {
  font-size: 12px;
  /* 使用次要文字颜色 */
  color: var(--spark-text-secondary);
  margin-bottom: 4px;
}
.stat-item .value {
  font-size: 16px;
  font-weight: 600;
  /* 默认使用常规文字颜色 */
  color: var(--spark-text-regular);
}
/* 使用“活力黄”辅助色 */
.stat-item .value.raised {
  color: var(--spark-accent-color);
}

/* 分页 (移除行内样式) */
.pagination-center {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
