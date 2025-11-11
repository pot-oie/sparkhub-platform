<template>
  <div class="comment-section">
    <el-card v-if="userStore.isAuthenticated()" class="post-comment-card">
      <el-form :model="postForm" @submit.prevent="handleSubmitComment">
        <el-form-item>
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="3"
            placeholder="发表一条友善的评论..."
            :disabled="postLoading"
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 0; display: flex; justify-content: flex-end">
          <el-button type="primary" native-type="submit" :loading="postLoading">
            发表评论
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert
      v-else
      title="登录后才能发表评论"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    >
      <router-link to="/login">
        <el-button type="primary" link>立即登录</el-button>
      </router-link>
    </el-alert>

    <div class="comment-list-header">
      <el-divider content-position="left">全部评论</el-divider>
      <el-radio-group v-model="sortBy" size="small" @change="fetchComments">
        <el-radio-button label="time">最新</el-radio-button>
        <el-radio-button label="hotness">最热</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading">
      <el-empty v-if="comments.length === 0" description="暂无评论, 快来抢沙发吧！" />

      <ul v-else class="comment-list" :key="listKey">
        <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :project-id="props.projectId"
          :creator-id="props.creatorId"
          :backer-ids="props.backerIds"
          @comment-posted="fetchComments"
        />
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCommentsApi, postCommentApi } from '@/api/comment'
// [!!! 关键 !!!] 确保导入了 DTO 类型
import type { CommentDetailDTO, CommentCreateDTO } from '@/api/types/comment'
import { ElMessage } from 'element-plus'
import CommentItem from './CommentItem.vue'

// 1. Props (保持不变)
const props = defineProps<{
  projectId: number
  creatorId: number
  backerIds: number[]
}>()

// 2. 核心状态 (保持不变)
const userStore = useUserStore()
const loading = ref(true)
const postLoading = ref(false)
const comments = ref<CommentDetailDTO[]>([]) // <--- 这里的类型是正确的 DTO
const sortBy = ref<'time' | 'hotness'>('time')
const postForm = reactive({
  content: '',
})
const listKey = ref(0)

// 3. 加载评论 (保持不变)
onMounted(() => {
  if (props.projectId) {
    fetchComments()
  }
})

// [!!! 修复 1: 新增一个辅助函数 !!!]
/**
 * 递归转换函数：将 API 返回的 'liked' 字段 映射到 DTO 的 'isLiked' 字段
 * @param comment - API 返回的原始评论对象 (类型为 any)
 * @returns 符合 CommentDetailDTO 规范的对象
 */
const mapCommentData = (comment: any): CommentDetailDTO => {
  // 1. 转换当前评论
  const mappedComment: CommentDetailDTO = {
    ...comment,
    isLiked: comment.liked, // [!!!] 这就是核心的转换
  } as CommentDetailDTO

  // (可选) 删除旧的 'liked' 键，保持数据清洁
  // delete (mappedComment as any).liked;

  // 2. 递归转换子评论 (replies)
  if (comment.replies && comment.replies.length > 0) {
    mappedComment.replies = comment.replies.map(mapCommentData)
  }

  return mappedComment
}

// [!!! 修复 2: 修改 fetchComments !!!]
const fetchComments = async () => {
  loading.value = true
  try {
    // 1. 从 API 获取原始数据 (这里面是 'liked')
    const rawComments = await getCommentsApi(props.projectId, sortBy.value)

    // 2. [!!! 新增 !!!] 使用辅助函数转换数据
    const mappedComments = rawComments.map(mapCommentData)

    // 3. 将转换后、符合 DTO 规范的数据交给 ref
    comments.value = mappedComments

    listKey.value += 1
  } catch (err: any) {
    ElMessage.error(err.message || '评论加载失败')
  } finally {
    loading.value = false
  }
}

// 4. 发表评论 (保持不变)
const handleSubmitComment = async () => {
  if (!postForm.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  postLoading.value = true
  try {
    const data: CommentCreateDTO = {
      content: postForm.content,
      parentId: null,
    }
    await postCommentApi(props.projectId, data)
    postForm.content = ''
    ElMessage.success('评论发表成功')
    await fetchComments() // 刷新列表 (此时 fetchComments 已经带了转换逻辑)
  } catch (err: any) {
    ElMessage.error(err.message || '评论发表失败')
  } finally {
    postLoading.value = false
  }
}
</script>

<style scoped>
/* (样式保持不变) */
.post-comment-card {
  margin-bottom: 20px;
  background-color: #fcfcfc;
}
.comment-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.comment-list-header .el-divider {
  flex-grow: 1;
  margin-right: 20px;
}
.comment-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
</style>
