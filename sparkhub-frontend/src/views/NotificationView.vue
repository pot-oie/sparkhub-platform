<template>
  <div class="notification-view">
    <el-card>
      <div class="view-header">
        <h2 class="view-title">通知中心</h2>
        <el-button
          type="primary"
          link
          :disabled="loading || list.length === 0"
          :loading="markAllLoading"
          @click="handleMarkAllRead"
        >
          全部标记为已读
        </el-button>
      </div>

      <el-tabs v-model="activeFilter" @tab-change="handleFilterChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="未读" name="unread" />
        <el-tab-pane label="系统通知" name="system" />
        <el-tab-pane label="互动消息" name="interaction" />
      </el-tabs>

      <div v-loading="loading" class="list-container">
        <el-empty
          v-if="!loading && list.length === 0"
          description="暂无通知"
          style="padding: 40px 0"
        />

        <ul v-else class="notification-list">
          <NotificationItem
            v-for="notification in list"
            :key="notification.id"
            :notification="notification"
            class="notification-list-item"
            @click="handleClickNotification(notification)"
          />
        </ul>

        <el-pagination
          v-if="!loading && total > pageParams.pageSize"
          layout="prev, pager, next"
          :total="total"
          :current-page="pageParams.pageNum"
          :page-size="pageParams.pageSize"
          @current-change="handlePageChange"
          class="pagination-center"
          :background="true"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getNotificationsApi,
  markAllNotificationsAsReadApi,
  markNotificationAsReadApi,
} from '@/api/notification'
import type { NotificationDTO, NotificationFilter } from '@/api/types/notification'
import { ElMessage } from 'element-plus'
import NotificationItem from '@/components/NotificationItem.vue'

// 1. 状态
const loading = ref(true)
const markAllLoading = ref(false)
const list = ref<NotificationDTO[]>([])
const total = ref(0)
const activeFilter = ref<NotificationFilter>('all')
const pageParams = reactive({
  pageNum: 1,
  pageSize: 10,
})

const router = useRouter()
const userStore = useUserStore()

/**
 * 转换函数：将 API 返回的 'read' 字段 映射到 DTO 的 'isRead' 字段
 * @param notification - API 返回的原始通知对象 (类型为 any)
 * @returns 符合 NotificationDTO 规范的对象
 */
const mapNotificationData = (notification: any): NotificationDTO => {
  const mappedNotification: NotificationDTO = {
    ...notification,
    isRead: notification.read, // 将 read 赋值给 isRead
  } as NotificationDTO

  // 删除旧的 'read' 键，保持数据清洁
  delete (mappedNotification as any).read

  return mappedNotification
}

// 2. 加载数据
onMounted(() => {
  fetchNotifications()
})

const fetchNotifications = async () => {
  loading.value = true
  try {
    const params = {
      ...pageParams,
      filter: activeFilter.value === 'all' ? undefined : activeFilter.value,
    }
    const res = await getNotificationsApi(params)
    // 1. 映射列表中的每一项
    const mappedList = res.list.map(mapNotificationData)
    // 2. 将转换后的列表赋值给响应式状态
    list.value = mappedList
    total.value = res.total
  } catch (err: any) {
    ElMessage.error(err.message || '加载通知失败')
  } finally {
    loading.value = false
  }
}

// 3. 交互
// 筛选 (Tabs 切换)
const handleFilterChange = () => {
  pageParams.pageNum = 1 // 重置到第一页
  fetchNotifications()
}

// 分页
const handlePageChange = (newPage: number) => {
  pageParams.pageNum = newPage
  fetchNotifications()
}

// 全部已读
const handleMarkAllRead = async () => {
  markAllLoading.value = true
  try {
    await markAllNotificationsAsReadApi()
    ElMessage.success('已将所有通知标记为已读')
    // 重新获取列表 (会显示为已读)
    await fetchNotifications()
    // 更新 Store 中的红点
    userStore.setUnreadCount(0)
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    markAllLoading.value = false
  }
}

// 点击单条通知
const handleClickNotification = async (notification: NotificationDTO) => {
  // 1. 如果未读, 则调用 API 标记为已读
  if (!notification.isRead) {
    try {
      await markNotificationAsReadApi(notification.id)
      // 在本地更新状态, 避免重新 fetch
      notification.isRead = true
      // 更新 Store 红点
      userStore.decrementUnreadCount()
    } catch (err) {
      console.error('标记单条已读失败:', err)
      // 即使标记失败, 也继续跳转
    }
  }
  // 2. 跳转
  if (notification.linkUrl) {
    router.push(notification.linkUrl)
  }
}
</script>

<style scoped>
/* 借鉴 HomeView 和 global.css 的风格 */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px; /* (为 tabs 留出空间) */
}

.view-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--spark-text-primary);
  margin: 0;
}

.list-container {
  /* 最小高度, 防止加载时抖动 */
  min-height: 300px;
}

.notification-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 为列表项添加点击手势 */
.notification-list-item {
  cursor: pointer;
}

.pagination-center {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
