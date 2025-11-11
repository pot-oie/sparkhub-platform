<template>
  <div class="project-detail-view" v-loading="loading">
    <el-card v-if="project" class="detail-card">
      <el-row :gutter="40">
        <el-col :span="14">
          <el-image
            :src="formatImageUrl(project.coverImage)"
            fit="cover"
            class="detail-image"
            :preview-src-list="[formatImageUrl(project.coverImage)]"
            :initial-index="0"
          >
            <template #placeholder>
              <div class="image-placeholder-large">加载中...</div>
            </template>
            <template #error>
              <div class="image-placeholder-large">图片加载失败</div>
            </template>
          </el-image>
        </el-col>

        <el-col :span="10">
          <div class="detail-info">
            <h1 class="detail-title">{{ project.title }}</h1>

            <div class="project-progress">
              <el-progress
                :percentage="calculatePercentage(project.currentAmount, project.goalAmount)"
                :color="percentageColor(project.currentAmount, project.goalAmount)"
                :stroke-width="12"
                :show-text="false"
              />
              <span class="progress-text">
                {{ Math.round(calculatePercentage(project.currentAmount, project.goalAmount)) }}%
              </span>
            </div>

            <div class="project-stats">
              <div class="stat-item">
                <span class="label">已筹集</span>
                <span class="value raised">¥{{ project?.currentAmount }}</span>
              </div>
              <div class="stat-item">
                <span class="label">目标金额</span>
                <span class="value goal">¥{{ project?.goalAmount }}</span>
              </div>
              <div class="stat-item">
                <span class="label">剩余时间</span>
                <span class="value remaining">
                  {{
                    calculateRemainingDays(project?.endTime) === '已结束'
                      ? '已结束'
                      : calculateRemainingDays(project?.endTime) + ' 天'
                  }}
                </span>
              </div>
            </div>

            <h3 class="reward-title">选择回报</h3>
            <div class="reward-list">
              <div
                v-if="project.rewards && project.rewards.length > 0"
                v-for="reward in project.rewards"
                :key="reward.id"
                class="reward-card"
                :class="{
                  'is-selected': selectedRewardId === reward.id,
                  'is-disabled': reward.stock === 0,
                }"
                @click="selectReward(reward)"
              >
                <el-image
                  :src="formatImageUrl(reward.imageUrl)"
                  fit="cover"
                  class="reward-image"
                  :preview-src-list="[formatImageUrl(reward.imageUrl)]"
                  :initial-index="0"
                >
                  <template #error>
                    <div class="image-slot-placeholder">无配图</div>
                  </template>
                </el-image>

                <div class="reward-content">
                  <div class="reward-header">
                    <span class="reward-subtitle">{{ reward.title }}</span>
                    <span class="reward-amount">¥{{ reward.amount }}</span>
                  </div>

                  <p class="reward-desc">{{ reward.description }}</p>

                  <span class="reward-stock">
                    {{ reward.stock === 0 ? '已售罄' : `剩余 ${reward.stock || '∞'} 份` }}
                  </span>
                </div>
              </div>

              <el-empty v-else description="暂无回报档位" />
            </div>

            <div class="action-buttons">
              <el-button
                :type="isFavorite ? 'warning' : 'default'"
                :icon="isFavorite ? StarFilled : Star"
                :loading="favoriteLoading"
                @click="handleToggleFavorite"
                size="large"
                :plain="!isFavorite"
                class="favorite-btn"
              >
                {{ isFavorite ? '已收藏' : '收藏项目' }}
              </el-button>

              <el-button
                type="primary"
                size="large"
                class="support-btn"
                @click="handleSupport"
                :loading="supportLoading"
                :disabled="!selectedRewardId"
              >
                {{ selectedRewardId ? '立即支持' : '请先选择回报' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-tabs style="margin-top: 30px" class="detail-tabs">
        <el-tab-pane label="项目详情">
          <div class="long-description">
            {{ project.description }}
          </div>
        </el-tab-pane>
        <el-tab-pane label="评论">
          <CommentSection
            v-if="project"
            :project-id="projectId"
            :creator-id="project.creatorId"
            :backer-ids="project.backerIds"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-empty v-if="!loading && !project" description="项目不存在或加载失败" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getProjectDetailApi } from '@/api/project'
import { createBackingApi, payBackingApi } from '@/api/backing'
import { addFavoriteApi, removeFavoriteApi } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import type { Project } from '@/api/types/project'
import type { Reward } from '@/api/types/reward'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatImageUrl } from '@/utils/format'
import CommentSection from '@/components/CommentSection.vue'

// 1. 核心变量
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const supportLoading = ref(false)
const project = ref<Project | null>(null)
const projectId = ref<number>(Number(route.params.id))
const selectedRewardId = ref<number | null>(null)
const isFavorite = ref(false)
const favoriteLoading = ref(false)

// 2. 加载数据
onMounted(() => {
  if (projectId.value) {
    fetchProjectDetail()
  } else {
    loading.value = false
  }
})
const fetchProjectDetail = async () => {
  loading.value = true
  try {
    const res = await getProjectDetailApi(projectId.value)
    project.value = res
    isFavorite.value = res.isFavorite || false
  } catch (err) {
    console.error(err)
    project.value = null
  } finally {
    loading.value = false
  }
}

// 3. 辅助函数
const calculatePercentage = (current: number, goal: number) => {
  if (goal === 0) return 0
  const percentage = (current / goal) * 100
  return Math.min(percentage, 100)
}
/**
 * 进度条颜色
 * 使用 global.css 定义的 CSS 变量
 */
const percentageColor = (current: number, goal: number) => {
  if (calculatePercentage(current, goal) >= 100) {
    return 'var(--el-color-success)' // 成功 (绿色)
  }
  // 返回主色调 (生机青)
  return 'var(--el-color-primary)'
}
const calculateRemainingDays = (endTimeStr: string | undefined | null) => {
  if (!endTimeStr) {
    return '--'
  }
  try {
    const end = new Date(endTimeStr)
    const now = new Date()
    const diffMs = end.getTime() - now.getTime()
    if (diffMs <= 0) {
      return '已结束'
    }
    const days = Math.ceil(diffMs / (1000 * 60 * 60 * 24))
    return String(days)
  } catch (e) {
    console.error('日期解析失败:', e)
    return '--'
  }
}

// 4. 交互函数
const selectReward = (reward: Reward) => {
  if (reward.stock === 0) {
    ElMessage.warning('该回报档位已无库存')
    return
  }
  if (selectedRewardId.value === reward.id) {
    selectedRewardId.value = null
  } else {
    selectedRewardId.value = reward.id
  }
}
const handleToggleFavorite = async () => {
  if (!userStore.isAuthenticated()) {
    ElMessage.warning('请先登录后再操作')
    await router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  if (favoriteLoading.value) return
  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavoriteApi(projectId.value)
      ElMessage.success('已取消收藏')
      isFavorite.value = false
    } else {
      await addFavoriteApi(projectId.value)
      ElMessage.success('收藏成功')
      isFavorite.value = true
    }
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}
const handleSupport = async () => {
  if (!userStore.isAuthenticated()) {
    ElMessage.warning('请先登录后再支持项目')
    await router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  if (!selectedRewardId.value) {
    ElMessage.warning('请先选择一个回报档V位')
    return
  }
  const selectedReward = project.value?.rewards?.find((r) => r.id === selectedRewardId.value)
  if (!selectedReward) return
  try {
    await ElMessageBox.confirm(
      `您将支持 ${selectedReward.amount} 元以获取 "${selectedReward.title}" 回报，是否确认？`,
      '支持确认',
      {
        confirmButtonText: '确认支持',
        cancelButtonText: '取消',
        type: 'info',
      },
    )
  } catch (err) {
    return // 用户点击了 "取消"
  }
  supportLoading.value = true
  try {
    const backing = await createBackingApi({ rewardId: selectedRewardId.value })
    await payBackingApi(backing.id)
    ElMessage.success('支持成功！感谢您的贡献！')
    await fetchProjectDetail()
    selectedRewardId.value = null
  } catch (err: any) {
    ElMessage.error(err.message || '支持失败')
  } finally {
    supportLoading.value = false
  }
}
</script>

<style scoped>
.detail-card {
  /* 使用 global.css 的卡片样式 (圆角, 阴影) */
  padding: 10px 20px 20px 20px; /* 增加卡片内部 padding */
}

.detail-image {
  width: 100%;
  /* 调整高度，使其不那么夸张 */
  height: 480px;
  border-radius: 8px; /* 圆角 */
  background-color: var(--spark-bg-color); /* 占位背景色 */
}

/* 大图占位符 */
.image-placeholder-large {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: var(--spark-bg-color);
  color: var(--spark-text-secondary);
  font-size: 18px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.detail-title {
  /* 增大字号 */
  font-size: 28px;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 20px;
  line-height: 1.4;
}

/* 进度条区域 (同 HomeView) */
.project-progress {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.project-progress .el-progress {
  flex-grow: 1;
}
.progress-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--spark-primary-color);
  margin-left: 15px;
  width: 50px; /* 固定宽度 */
  text-align: right;
  flex-shrink: 0;
}

/* 统计数据 */
.project-stats {
  display: flex;
  justify-content: space-around;
  background-color: transparent;
  padding: 15px 0; /* 移除左右 padding, 增加上下 */
  border-radius: 8px; /* 圆角 */
  margin-bottom: 20px;
  /* 上下边框 */
  border-top: 1px solid var(--spark-border-color);
  border-bottom: 1px solid var(--spark-border-color);
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 10px;
}
.stat-item .label {
  font-size: 14px;
  color: var(--spark-text-secondary); /* 使用次要文字色 */
  margin-bottom: 8px;
}
.stat-item .value {
  font-size: 22px; /* 增大字号 */
  font-weight: 600;
}
/* 应用我们的品牌色 */
.stat-item .value.raised {
  color: var(--spark-accent-color); /* 活力黄 */
}
.stat-item .value.goal {
  color: var(--spark-text-regular); /* 常规文字 */
}
.stat-item .value.remaining {
  color: var(--spark-primary-color); /* 生机青 */
}

/* 回报标题 */
.reward-title {
  font-size: 18px;
  border-bottom: 1px solid var(--spark-border-color);
  padding-bottom: 10px;
  margin-bottom: 15px;
  color: var(--spark-text-primary);
}

/* 回报列表 */
.reward-list {
  max-height: 250px;
  overflow-y: auto;
  flex-grow: 1;
  padding-right: 5px; /* 增加一点内边距，防止滚动条太贴 */
}
.reward-image {
  width: 100px; /* 稍微调小一点 */
  min-width: 100px;
  background-color: var(--spark-bg-color);
  display: block;
}
.image-slot-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--spark-bg-color);
  color: var(--spark-text-secondary);
  font-size: 12px;
}

/* 回报卡片 */
.reward-card {
  display: flex;
  border: 2px solid var(--spark-border-color); /* 使用标准边框色 */
  border-radius: 8px; /* 统一圆角 */
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #ffffff;
  overflow: hidden;
}
.reward-card:hover {
  /* 悬停时使用主色调（浅色） */
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
/* 选中状态使用“生机青” */
.reward-card.is-selected {
  border-color: var(--spark-primary-color);
  /* 阴影也使用“生机青” */
  box-shadow: 0 4px 12px rgba(0, 191, 165, 0.2);
}
.reward-card.is-disabled {
  cursor: not-allowed;
  background-color: var(--spark-bg-color);
  color: var(--spark-text-secondary);
}
.reward-card.is-disabled:hover {
  border-color: var(--spark-border-color);
  box-shadow: none;
}
.reward-card.is-disabled .reward-amount,
.reward-card.is-disabled .reward-content {
  color: var(--spark-text-secondary);
}

.reward-amount {
  font-size: 18px; /* 调小一点 */
  font-weight: 600;
  color: var(--el-color-primary); /* 自动变为“生机青” */
  margin-left: 10px;
  white-space: nowrap;
}
.reward-content {
  flex-grow: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.reward-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.reward-subtitle {
  font-size: 16px;
  font-weight: 600;
  flex-grow: 1;
}
.reward-desc {
  font-size: 14px;
  color: var(--spark-text-regular);
  margin-top: 5px;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}
.reward-stock {
  font-size: 12px;
  color: var(--spark-text-secondary);
  margin-top: 10px;
  display: block;
  flex-shrink: 0;
}

/* 详情 Tab 样式 */
.detail-tabs {
  margin-top: 30px;
}
.long-description {
  padding: 10px 5px; /* 增加内边距 */
  line-height: 1.8;
  color: var(--spark-text-primary);
  white-space: pre-wrap; /* 保留换行和空格 */
  font-size: 15px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  margin-top: 20px;
  width: 100%;
}
.favorite-btn {
  flex-grow: 1;
}
.support-btn {
  flex-grow: 2;
  margin-left: 10px;
}
</style>
