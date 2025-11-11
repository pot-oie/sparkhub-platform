<template>
  <li class="comment-item">
    <el-avatar :size="40" :src="formatImageUrl(comment.avatar)" class="comment-avatar" />

    <div class="comment-body">
      <div class="comment-header">
        <div class="comment-user-info">
          <span class="comment-username">{{ comment.username }}</span>
          <el-tag v-if="isCreator" type="danger" size="small" class="comment-tag"> 发起者 </el-tag>
          <el-tag v-if="isBacker" type="warning" size="small" class="comment-tag"> 已支持 </el-tag>
        </div>
        <span class="comment-time">{{ formatTimeAgo(comment.createTime) }}</span>
      </div>

      <p class="comment-content">{{ comment.content }}</p>

      <div class="comment-actions">
        <el-button
          :type="localIsLiked ? 'primary' : 'default'"
          text
          size="small"
          :loading="likeLoading"
          @click="handleLike"
        >
          <el-icon>
            <Select v-if="localIsLiked" />
            <Pointer v-else />
          </el-icon>
          <span style="margin-left: 4px">
            {{ localLikeCount > 0 ? localLikeCount : '点赞' }}
          </span>
        </el-button>
        <el-button
          text
          size="small"
          type="primary"
          class="reply-btn"
          @click="showReplyBox = !showReplyBox"
        >
          回复
        </el-button>
      </div>

      <div v-if="showReplyBox" class="reply-box">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="2"
          :placeholder="`回复 @${comment.username}`"
          :disabled="replyLoading"
        />
        <div class="reply-box-actions">
          <el-button @click="showReplyBox = false" size="small">取消</el-button>
          <el-button type="primary" size="small" :loading="replyLoading" @click="handleSubmitReply">
            发表回复
          </el-button>
        </div>
      </div>

      <ul v-if="comment.replies && comment.replies.length > 0" class="reply-list">
        <CommentItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :comment="reply"
          :project-id="projectId"
          :creator-id="props.creatorId"
          :backer-ids="props.backerIds"
          @reply-posted="emit('comment-posted')"
        />
      </ul>
    </div>
  </li>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { useUserStore } from '@/stores/user'
import { postCommentApi, likeCommentApi, unlikeCommentApi } from '@/api/comment'
import type { CommentDetailDTO, CommentCreateDTO } from '@/api/types/comment'
import { formatImageUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Pointer, Select } from '@element-plus/icons-vue'

// 1. Props (保持不变)
const props = defineProps<{
  comment: CommentDetailDTO
  projectId: number
  creatorId: number
  backerIds: number[]
}>()

const emit = defineEmits(['reply-posted', 'comment-posted'])

// 2. 核心状态
const userStore = useUserStore()
const showReplyBox = ref(false)
const replyContent = ref('')
const replyLoading = ref(false)
const likeLoading = ref(false)

// [!!! 修复 2: 确保使用 'isLiked' (驼峰命名) !!!]
// 因为父组件已经转换了数据, 这里的 props.comment.isLiked 是有值的
const localIsLiked = ref(props.comment.isLiked)
const localLikeCount = ref(props.comment.likeCount)

// 3. 使用 watchEffect
// [!!! 修复 3: 确保使用 'isLiked' (驼峰命名) !!!]
watchEffect(() => {
  localIsLiked.value = props.comment.isLiked
  localLikeCount.value = props.comment.likeCount
})

// 4. 徽章计算属性 (保持不变)
const isCreator = computed(() => {
  return props.comment.userId === props.creatorId
})
const isBacker = computed(() => {
  if (isCreator.value) return false
  return props.backerIds && props.backerIds.includes(props.comment.userId)
})

// 5. 点赞/取消点赞 (保持不变)
// (这个逻辑现在是正确的)
const handleLike = async () => {
  if (!userStore.isAuthenticated()) {
    ElMessage.warning('请先登录再点赞')
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    if (localIsLiked.value) {
      await unlikeCommentApi(props.comment.id)
      localLikeCount.value--
      localIsLiked.value = false
    } else {
      await likeCommentApi(props.comment.id)
      localLikeCount.value++
      localIsLiked.value = true
    }
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 6. 提交回复 (保持不变)
const handleSubmitReply = async () => {
  if (!userStore.isAuthenticated()) {
    ElMessage.warning('请先登录再回复')
    return
  }
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  replyLoading.value = true
  try {
    const data: CommentCreateDTO = {
      content: replyContent.value,
      parentId: props.comment.id,
    }
    await postCommentApi(props.projectId, data)
    emit('reply-posted')
    replyContent.value = ''
    showReplyBox.value = false
    ElMessage.success('回复成功')
  } catch (err: any) {
    ElMessage.error(err.message || '回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 8. 辅助函数 (保持不变)
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
/* (样式保持不变) */
.comment-item {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}
.comment-body {
  flex-grow: 1;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 5px;
}
.comment-user-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: -5px;
}
.comment-username {
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
  margin-bottom: 5px;
}
.comment-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}
.comment-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 10px;
}
.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}
.comment-actions {
  display: flex;
  align-items: center;
  margin-top: 10px;
  color: #909399;
}
.reply-btn {
  margin-left: 10px;
}
.reply-box {
  margin-top: 15px;
  background-color: #fcfcfc;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #f0f2f5;
}
.reply-box-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.reply-list {
  list-style: none;
  padding: 0 0 0 55px;
  margin: 15px 0 0 0;
}
.reply-list .comment-item {
  border-bottom: none;
  padding-bottom: 10px;
  padding-top: 10px;
}
.reply-list .comment-item:first-child {
  padding-top: 0;
}
</style>
