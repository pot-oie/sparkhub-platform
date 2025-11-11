<template>
  <li class="notification-item" :class="{ 'is-unread': !notification.isRead }">
    <div class="item-avatar">
      <el-avatar v-if="isInteraction" :size="40" :src="formatImageUrl(notification.senderAvatar)" />
      <el-avatar v-else :size="40" :icon="InfoFilled" class="system-avatar" />
    </div>

    <div class="item-content">
      <div v-if="isInteraction" class="item-header">
        <span class="sender-name">{{ notification.senderUsername }}</span>
      </div>

      <p class="item-text" :class="{ 'text-bold': !notification.isRead }">
        {{ notification.content }}
      </p>

      <span class="item-time">{{ formatTimeAgo(notification.createTime) }}</span>
    </div>

    <div v-if="!localIsRead" class="unread-dot"></div>
  </li>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'
import { computed, ref, watchEffect } from 'vue'
import type { NotificationDTO } from '@/api/types/notification'
import { formatImageUrl } from '@/utils/format'
import { InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  notification: {
    type: Object as PropType<NotificationDTO>,
    required: true,
  },
})

// 声明本地响应式状态
const localIsRead = ref(props.notification.isRead)

// 监听 Prop 变化并更新本地状态
// 当父组件 (NotificationView.vue) 中的 list 数组被更新，
// 且 Prop 对象中的 isRead 属性发生变化时，这里会同步更新 localIsRead
watchEffect(() => {
  localIsRead.value = props.notification.isRead
})

// 计算是否为互动通知 (有发送者)
const isInteraction = computed(() => {
  return props.notification.senderId != null
})

// 辅助函数
const formatTimeAgo = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  try {
    const now = new Date()
    const past = new Date(dateTimeStr)
    const diffMs = now.getTime() - past.getTime()
    const diffSec = Math.floor(diffMs / 1000)
    const diffMin = Math.floor(diffSec / 60)
    const diffHour = Math.floor(diffMin / 60)
    const diffDay = Math.floor(diffHour / 24)
    if (diffDay > 0) return `${diffDay}天前`
    if (diffHour > 0) return `${diffHour}小时前`
    if (diffMin > 0) return `${diffMin}分钟前`
    return '刚刚'
  } catch (e) {
    return dateTimeStr
  }
}
</script>

<style scoped>
.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px; /* 增加内边距 */
  border-bottom: 1px solid var(--spark-border-color);
  transition: background-color 0.2s;
  list-style: none;
  position: relative; /* 为未读点定位 */
}
.notification-item:hover {
  background-color: #fcfcfc;
}
.notification-item:last-child {
  border-bottom: none;
}

/* 未读状态 */
.notification-item.is-unread {
  background-color: var(--el-color-primary-light-9); /* 使用主色调最浅的背景 */
}

/* 1. 头像 */
.item-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}
/* 系统通知头像的背景色 */
.system-avatar {
  background-color: #eef2fb; /* Element UI info-light-9 */
  color: #909399; /* --spark-text-secondary */
}

/* 2. 内容 */
.item-content {
  flex-grow: 1;
  min-width: 0; /* 防止 flex 溢出 */
}

.item-header {
  margin-bottom: 5px;
}
.sender-name {
  font-weight: 600;
  color: var(--spark-text-primary);
  font-size: 15px;
}

.item-text {
  font-size: 15px;
  color: var(--spark-text-regular);
  line-height: 1.6;
  margin: 0 0 8px 0;
}

/* 未读消息内容加粗 */
.item-text.text-bold {
  font-weight: 600;
  color: var(--spark-text-primary); /* 使用主文字色 */
}

.item-time {
  font-size: 12px;
  color: var(--spark-text-secondary);
}

/* 未读指示器样式 */
.unread-dot {
  position: absolute;
  top: 18px; /* 顶部定位 */
  right: 18px; /* 右侧定位 */
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--spark-primary-color); /* 使用主色调 */
}
</style>
